package me.ford.biomeremap.mapping;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.settings.Settings;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_15_R1.PlayerConnection;

/**
 * TeleportListener
 */
public class TeleportListener implements Listener {
    private final BiomeRemap br;
    private final Settings settings;
    private final Set<TeleportChunkInfo> infos = new HashSet<>();

    public TeleportListener(BiomeRemap br) {
        this.br = br;
        this.settings = br.getSettings();
        this.br.getServer().getPluginManager().registerEvents(this, br);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Chunk chunkTo = to.getChunk();
        if (from.getChunk() == chunkTo || chunkTo.getInhabitedTime() > 0) return;
        TeleportChunkInfo info = new TeleportChunkInfo(event.getPlayer().getUniqueId(), chunkTo.getWorld(), chunkTo.getX(), chunkTo.getZ(), System.currentTimeMillis());
        infos.add(info);
        br.getServer().getScheduler().runTaskLater(br, () -> remove(info), settings.getTeleportCacheTime());
    }

    private void remove(TeleportChunkInfo info) {
        infos.remove(info);
    }

    public void sendUpdatesIfNeeded(Chunk chunk) {
        int range = br.getServer().getViewDistance();
        for (TeleportChunkInfo info : infos) {
            Player player = getPlayerInRange(info, chunk, range);
            if (player != null) {
                sendUpdate(player, chunk);
            }
        }
    }

    private Player getPlayerInRange(TeleportChunkInfo info, Chunk chunk, int range) {
        Server server = br.getServer();
        if (info.chunkInRange(chunk, range)) {
            return server.getPlayer(info.getId());
        }
        return null;
    }

    private void sendUpdate(Player player, Chunk chunk) {
        // TODO - this is version specific!
        net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        PacketPlayOutUnloadChunk unload = new PacketPlayOutUnloadChunk(chunk.getX(), chunk.getZ());
        PacketPlayOutMapChunk load = new PacketPlayOutMapChunk(nmsChunk, 65535);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(unload);
        connection.sendPacket(load);
    }
    
}