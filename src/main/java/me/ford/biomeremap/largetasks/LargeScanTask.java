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
	private int nulls = 0;
	
	public LargeScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			Consumer<String> progress, Consumer<TaskReport> ender, Consumer<BiomeReport> biomes) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progress, ender);
		this.biomes = biomes;
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		findBiomes(world, x, z, debug);
	}
	
	private void findBiomes(World world, int chunkX, int chunkZ, boolean debug) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				addBiome(world.getBiome(x, z));
			}
		}
	}
	
	private void addBiome(Biome biome) {
		if (biome == null) {
			nulls++;
			return;
		}
		Integer cur = biomeMap.get(biome);
		if (cur == null) {
			cur = 0;
		}
		cur++;
		biomeMap.put(biome, cur);
	}

	@Override
	protected void whenDone() {
		biomes.accept(new BiomeReport(biomeMap, nulls));
	}
	
	public static class BiomeReport {
		private final Map<Biome, Integer> biomes;
		private final int nrOfNulls;
		
		public BiomeReport(Map<Biome, Integer> biomes, int nrOfNulls) {
			this.biomes = biomes;
			this.nrOfNulls = nrOfNulls;
		}
		
		public Map<Biome, Integer> getBiomes() {
			return biomes;
		}
		
		public int nrOfNulls() {
			return nrOfNulls;
		}
	}

}
