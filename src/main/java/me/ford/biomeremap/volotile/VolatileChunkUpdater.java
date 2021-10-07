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
public class VolatileChunkUpdater implements ChunkUpdater {
	private final boolean one16point1;
	private final boolean one17;
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

	public VolatileChunkUpdater(BiomeRemap br)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		this.br = br;
		String version = this.br.getServer().getClass().getPackage().getName().split("\\.")[3];
		one16point1 = version.contains("v1_16_R1");
		one17 = version.contains("v1_17");
		// assume post 1.17

		// classes
		craftChunkClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftChunk");
		craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
		nmsChunkClass = Class.forName("net.minecraft.world.level.chunk.Chunk");
		packetClass = Class.forName("net.minecraft.network.protocol.Packet");
		packetPlayOutUnloadChunkClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk");
		packetPlayOutMapChunkClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutMapChunk");
		playerConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");
		entityPlayerClass = Class.forName("net.minecraft.server.level.EntityPlayer");

		// methods
		getHandleMethod = craftChunkClass.getMethod("getHandle");
		getHandleMethod2 = craftPlayerClass.getMethod("getHandle");
		sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);

		// constructors
		packetPlayOutUnloadChunkConstructor = packetPlayOutUnloadChunkClass.getConstructor(int.class, int.class);
		if (one16point1) {
			packetPlayOutMapChunkConstructor = packetPlayOutMapChunkClass.getConstructor(nmsChunkClass, int.class,
					boolean.class);
		} else if (one17) {
			packetPlayOutMapChunkConstructor = packetPlayOutMapChunkClass.getConstructor(nmsChunkClass);
		} else {
			packetPlayOutMapChunkConstructor = packetPlayOutMapChunkClass.getConstructor(nmsChunkClass, int.class);
		}

		// field
		playerConnectionField = entityPlayerClass.getDeclaredField(one17 ? "b" : "playerConnection");
	}

	@Override
	public void updateChunk(Player player, Chunk chunk)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		Object nmsChunk = getHandleMethod.invoke(chunk);
		Object unload = packetPlayOutUnloadChunkConstructor.newInstance(chunk.getX(), chunk.getZ());
		Object load;
		if (one16point1) {
			load = packetPlayOutMapChunkConstructor.newInstance(nmsChunk, 65535, true);
		} else if (one17) {
			load = packetPlayOutMapChunkConstructor.newInstance(nmsChunk);
		} else {
			load = packetPlayOutMapChunkConstructor.newInstance(nmsChunk, 65535);
		}
		Object entityPlayer = getHandleMethod2.invoke(player);
		Object playerConnection = playerConnectionField.get(entityPlayer);
		sendPacketMethod.invoke(playerConnection, unload);
		sendPacketMethod.invoke(playerConnection, load);
	}

}