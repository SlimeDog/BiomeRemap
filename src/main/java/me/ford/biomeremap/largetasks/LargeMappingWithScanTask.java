package me.ford.biomeremap.largetasks;

import java.util.function.Consumer;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;

public class LargeMappingWithScanTask extends LargeMappingTask {

	public LargeMappingWithScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ,
			boolean debug, int progressStep, Consumer<String> progress, Consumer<TaskReport> ender) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		super.doTaskForChunk(world, x, z, debug);
	}

}
