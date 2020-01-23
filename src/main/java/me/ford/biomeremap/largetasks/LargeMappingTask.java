package me.ford.biomeremap.largetasks;

import java.util.function.Consumer;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeMap;

public class LargeMappingTask extends LargeTask {
	private BiomeMap map;

	public LargeMappingTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, BiomeMap map) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
		this.map = map;
		if (this.map == null) {
			this.map = plugin.getSettings().getApplicableBiomeMap(world.getName());
		}
	}

	@Override
	protected void doTaskForChunk(int x, int z, boolean debug) {
		World world = getWorld();
		if (!world.isChunkGenerated(x, z)) {
			world.getChunkAt(x, z); // populator takes care
		} else {
			getPlugin().getRemapper().remapChunk(world.getChunkAt(x, z), debug, map);
		}
	}

	@Override
	protected void whenDone() {
	}

}
