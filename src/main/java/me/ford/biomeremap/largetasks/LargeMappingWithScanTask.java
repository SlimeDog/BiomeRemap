package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.mapping.BiomeMap;

public class LargeMappingWithScanTask extends LargeMappingTask {
	private final Map<Biome, Integer> biomeMap = new HashMap<>();
	private final Consumer<BiomeReport> biomeReport;
	private final OnMappingDone onMappingDone;
	private final boolean[][] checked = new boolean[32][32];

	public LargeMappingWithScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ,
			boolean debug, int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, BiomeMap map, 
			Consumer<BiomeReport> biomeReport) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender, map);
		this.biomeReport = biomeReport;
		this.onMappingDone = new OnMappingDone((x, z) -> addBiomes(world, x, z), minX, minZ, maxX, maxZ);
		plugin.getRemapper().addDoneChecker(onMappingDone);
	}

	private void addBiomes(World world, int chunkX, int chunkZ) {
		if (checked[chunkX - getMinX()][chunkZ - getMinZ()]) return;
		checked[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		getPlugin().getScanner().addBiomesFor(biomeMap, world, chunkX, chunkZ);
	}

	@Override
	protected void whenDone() {
		super.whenDone();
		// make sure the to show after scan is done
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			getPlugin().getRemapper().removeDoneCheker(onMappingDone);
			biomeReport.accept(new BiomeReport(biomeMap));
			int res = 0;
			for (int value : biomeMap.values()) res += value;
			getPlugin().getLogger().info("COUNT:" + onMappingDone.getCount() + "->" + res);
		}, 10L);
	}

}
