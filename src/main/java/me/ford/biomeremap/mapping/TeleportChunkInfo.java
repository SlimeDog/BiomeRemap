package me.ford.biomeremap.mapping;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.World;

/**
 * TeleportChunkInfo
 */
public class TeleportChunkInfo {
	private final UUID uuid;
	private final World world;
	private final int chunkX;
	private final int chunkZ;
	private final long time;

	public TeleportChunkInfo(UUID uuid, World world, int chunkX, int chunkZ, long time) {
		this.uuid = uuid;
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.time = time;
	}

	public UUID getId() {
		return uuid;
	}

	public World getWorld() {
		return world;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkZ() {
		return chunkZ;
	}

	public long getTeleportTime() {
		return time;
	}

	public boolean chunkInRange(Chunk chunk, int range) {
		if (chunk.getWorld() != world)
			return false;
		int x = chunk.getX();
		int z = chunk.getZ();
		return Math.abs(x - chunkX) <= range || Math.abs(z - chunkZ) <= range;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, world, chunkX, chunkZ, time);
	}

}