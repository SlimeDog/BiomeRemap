package me.ford.biomeremap.volotile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import me.ford.biomeremap.BiomeRemap;

/**
 * VolotileChunkUpdated
 */
public class VolotileChunkUpdater implements ChunkUpdater {
    private final boolean after116;
    private final BiomeRemap br;
    private final Class<?> craftChunkClass;
    private final Class<?> nmsChunkClass;
    private final Class<?> packetClass;
    private final Class<?> packetPlayOutUnloadChunkClass;
    private final Class<?> packetPlayOutMapChunkClass;
    private final Class<?> craftPlayerClass;
    private final Class<?> playerConnectionClass;
    private final Class<?> entityPlayerClass;
    private final Constructor<?> packetPlayOutUnloadChunkConstructor;
    private final Constructor<?> packetPlayOutMapChunkConstructor;
    private final Method getHandleMethod;
    private final Method getHandleMethod2;
    private final Method sendPacketMethod;
    private final Field playerConnectionField;

    public VolotileChunkUpdater(BiomeRemap br) throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException {
        this.br = br;
        String version = this.br.getServer().getClass().getPackage().getName().split("\\.")[3];
        after116 = version.contains("v1_16") || version.contains("v1_17") || version.contains("v1_18")  || version.contains("v1_19");

        // classes
        craftChunkClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftChunk");
        nmsChunkClass = Class.forName("net.minecraft.server." + version + ".Chunk");
        packetClass = Class.forName("net.minecraft.server." + version + ".Packet");
        packetPlayOutUnloadChunkClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutUnloadChunk");
        packetPlayOutMapChunkClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutMapChunk");
        craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
        playerConnectionClass = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
        entityPlayerClass = Class.forName("net.minecraft.server." + version + ".EntityPlayer");

        // methods
        getHandleMethod = craftChunkClass.getMethod("getHandle");
        getHandleMethod2 = craftPlayerClass.getMethod("getHandle");
        sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);

        // constructors
        packetPlayOutUnloadChunkConstructor = packetPlayOutUnloadChunkClass.getConstructor(int.class, int.class);
        if (after116) {
            packetPlayOutMapChunkConstructor = packetPlayOutMapChunkClass.getConstructor(nmsChunkClass, int.class, boolean.class);
        } else {
            packetPlayOutMapChunkConstructor = packetPlayOutMapChunkClass.getConstructor(nmsChunkClass, int.class);
        }

        // field
        playerConnectionField = entityPlayerClass.getDeclaredField("playerConnection");
    }

    @Override
    public void updateChunk(Player player, Chunk chunk)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
        Object nmsChunk = getHandleMethod.invoke(chunk);
        Object unload = packetPlayOutUnloadChunkConstructor.newInstance(chunk.getX(), chunk.getZ());
        Object load;
        if (after116) {
            load = packetPlayOutMapChunkConstructor.newInstance(nmsChunk, 65535, true);
        } else {
            load = packetPlayOutMapChunkConstructor.newInstance(nmsChunk, 65535);
        }
        Object entityPlayer = getHandleMethod2.invoke(player);
        Object playerConnection = playerConnectionField.get(entityPlayer);
        sendPacketMethod.invoke(playerConnection, unload);
        sendPacketMethod.invoke(playerConnection, load);
    }

    
}