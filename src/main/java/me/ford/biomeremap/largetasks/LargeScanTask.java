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
	private final boolean useNMS;
	private final OnMappingDone onMappingDone;
	private final boolean[][] checked = new boolean[32][32];
	
	public LargeScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, Consumer<BiomeReport> biomes,
			int yLayer, boolean useNMS) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
		this.biomes = biomes;
		this.yLayer = yLayer;
		this.useNMS = useNMS;
		this.onMappingDone = new OnMappingDone((x, z) -> findBiomes(world, x, z, debug), minX, minZ, maxX, maxZ);
		getPlugin().getRemapper().addDoneChecker(onMappingDone); // checks the newly generated ones
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		findBiomes(world, x, z, debug);
	}
	
	private void findBiomes(World world, int chunkX, int chunkZ, boolean debug) {
		if (checked[chunkX - getMinX()][chunkZ - getMinZ()]) return; // already done
		getPlugin().getScanner().addBiomesFor(biomeMap, world, chunkX, chunkZ, yLayer, useNMS);
		checked[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		return;
	}

	@Override
	protected void whenDone() {
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			biomes.accept(new BiomeReport(biomeMap));
			getPlugin().getRemapper().removeDoneCheker(onMappingDone);
			int res = 0;
			for (int value : biomeMap.values()) res += value;
			getPlugin().getLogger().info("COUNT:" + onMappingDone.getCount() + "->" + res);
		}, 10L); // make sure they all get remapped and scanned
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
