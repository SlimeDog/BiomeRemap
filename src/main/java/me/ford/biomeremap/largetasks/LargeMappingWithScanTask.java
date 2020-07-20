package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.PopulatorQueue;

public class LargeMappingWithScanTask extends LargeMappingTask {
	private final Map<Biome, Integer> biomeMap = new HashMap<>();
	private final Consumer<BiomeReport> biomeReport;
	private final OnMappingDone onMappingDone;
	private final PopulatorQueue queue;
	private final boolean[][] checked = new boolean[32][32];

	public LargeMappingWithScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ,
			boolean debug, int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, BiomeMap map,
			Consumer<BiomeReport> biomeReport) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender, map);
		this.biomeReport = biomeReport;
		this.onMappingDone = new OnMappingDone((x, z) -> addBiomes(world, x, z), world, minX, minZ, maxX, maxZ);
		plugin.getRemapper().addDoneChecker(onMappingDone);
		queue = new PopulatorQueue(biomeMap, world, getPlugin().getScanner());
		getPlugin().getScanner().setPopulatorQueue(queue);
		setPopulatorQueue(queue);
	}

	@Override
	protected void remapChunks() {
		super.remapChunks();
		queue.tick();
	}

	private void addBiomes(World world, int chunkX, int chunkZ) {
		if (checked[chunkX - getMinX()][chunkZ - getMinZ()])
			return;
		if (getPlugin().getScanner().addBiomesFor(biomeMap, world, chunkX, chunkZ)) {
			checked[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		} // else will be done later, after the remap
	}

	@Override
	protected void whenDone() {
		getPlugin().getScanner().finalizePopulatorQueue();
		removeQueue();
		super.whenDone();
		// make sure the to show after scan is done
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			getPlugin().getRemapper().removeDoneCheker(onMappingDone);
			biomeReport.accept(new BiomeReport(biomeMap));
		}, 10L);
	}

}
