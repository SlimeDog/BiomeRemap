package me.ford.biomeremap.largetasks;

import java.util.function.BiConsumer;

import org.bukkit.Chunk;
import org.bukkit.World;

/**
 * OnMappingDone
 */
public class OnMappingDone {
	private final int minX, minZ, maxX, maxZ;
	private final BiConsumer<Integer, Integer> onPopulate;
	private final World world;
	private int count = 0;

	public OnMappingDone(BiConsumer<Integer, Integer> onPopulate, World world, int minX, int minZ, int maxX, int maxZ) {
		this.onPopulate = onPopulate;
		this.world = world;
		this.minX = minX;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxZ = maxZ;
	}

	public void afterRemap(Chunk chunk) {
		if (chunk.getWorld() != world)
			return;
		int curX = chunk.getX();
		int curZ = chunk.getZ();
		if (curX < minX || curX >= maxX || curZ < minZ || curZ >= maxZ)
			return; // only those in range!
		onPopulate.accept(chunk.getX(), chunk.getZ());
		count++;
	}

	public int getCount() {
		return count;
	}

}