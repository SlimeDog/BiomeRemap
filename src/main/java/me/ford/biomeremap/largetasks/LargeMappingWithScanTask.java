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

	public LargeMappingWithScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ,
			boolean debug, int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, BiomeMap map, 
			Consumer<BiomeReport> biomeReport) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender, map);
		this.biomeReport = biomeReport;
	}

	@Override
	protected void doTaskForChunk(World world, int x, int z, boolean debug) {
		super.doTaskForChunk(world, x, z, debug, () -> getPlugin().getScanner().addBiomesFor(biomeMap, world, x, z));
	}

	@Override
	protected void whenDone() {
		super.whenDone();
		// make sure the to show after scan is done
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> biomeReport.accept(new BiomeReport(biomeMap)), 10L);
	}

}
