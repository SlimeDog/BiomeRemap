package me.ford.biomeremap.largetasks;

import java.util.function.Consumer;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeRemapper;

public class LargeMappingTask extends LargeTask {

	public LargeMappingTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		if (!world.isChunkGenerated(x, z)) {
			world.getChunkAt(x, z); // populator takes care
		} else {
			BiomeRemapper.getInstance().remapChunk(world.getChunkAt(x, z), debug);
		}
	}

	@Override
	protected void whenDone() {
	}

}
