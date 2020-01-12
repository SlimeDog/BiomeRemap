package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;

public class LargeScanTask extends LargeTask {
	private final Map<Biome, Integer> biomeMap = new HashMap<>();
	private final Consumer<BiomeReport> biomes;
	private final int yLayer;
	
	public LargeScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, Consumer<BiomeReport> biomes,
			int yLayer) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
		this.biomes = biomes;
		this.yLayer = yLayer;
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		findBiomes(world, x, z, debug);
	}
	
	private void findBiomes(World world, int chunkX, int chunkZ, boolean debug) {
		getPlugin().getScanner().addBiomesFor(biomeMap, world, chunkX, chunkZ, yLayer);
	}

	@Override
	protected void whenDone() {
		biomes.accept(new BiomeReport(biomeMap));
	}
	
	public static class BiomeReport {
		private final Map<Biome, Integer> biomes;
		
		public BiomeReport(Map<Biome, Integer> biomes) {
			this.biomes = biomes;
		}
		
		public Map<Biome, Integer> getBiomes() {
			return biomes;
		}
	}

}
