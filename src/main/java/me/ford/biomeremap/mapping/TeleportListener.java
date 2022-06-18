package me.ford.biomeremap.mapping;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.wrappers.SDCOnlinePlayerProvider;
import me.ford.biomeremap.settings.Settings;

/**
 * TeleportListener
 */
public class TeleportListener implements Listener {
	private final SlimeDogPlugin br;
	private final Settings settings;
	private final Set<TeleportChunkInfo> infos = new HashSet<>();

	public TeleportListener(SlimeDogPlugin br, Settings settings) {
		this.br = br;
		this.settings = settings;
		this.br.getPluginManager().registerEvents(this);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		Chunk chunkTo = to.getChunk();
		if (from.getChunk() == chunkTo || chunkTo.getInhabitedTime() > 0)
			return;
		TeleportChunkInfo info = new TeleportChunkInfo(event.getPlayer().getUniqueId(), chunkTo.getWorld(),
				chunkTo.getX(), chunkTo.getZ(), System.currentTimeMillis());
		infos.add(info);
		br.getScheduler().runTaskLater(() -> remove(info), settings.getTeleportCacheTime());
	}

	private void remove(TeleportChunkInfo info) {
		infos.remove(info);
	}

	public void sendUpdatesIfNeeded(Chunk chunk) {
		int range = chunk.getWorld().getViewDistance();
		for (TeleportChunkInfo info : infos) {
			SDCPlayerRecipient player = getPlayerInRange(info, chunk, range);
			if (player != null) {
				sendUpdate(player, chunk);
			}
		}
	}

	private SDCPlayerRecipient getPlayerInRange(TeleportChunkInfo info, Chunk chunk, int range) {
		SDCOnlinePlayerProvider server = br.getOnlinePlayerProvider();
		if (info.chunkInRange(chunk, range)) {
			return server.getPlayerByID(info.getId());
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private void sendUpdate(SDCPlayerRecipient player, Chunk chunk) {
		chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
	}

}