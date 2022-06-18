package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Biome;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.mapping.PopulatorQueue;
import me.ford.biomeremap.settings.Settings;

public class LargeMappingWithScanTask extends LargeMappingTask {
	private final Map<Biome, Integer> biomeMap = new HashMap<>();
	private final Consumer<BiomeReport> biomeReport;
	private final OnMappingDone onMappingDone;
	private final PopulatorQueue queue;
	private final boolean[][] checked = new boolean[32][32];
	private final BiomeRemapper remapper;
	private final BiomeScanner scanner;

	public LargeMappingWithScanTask(SlimeDogPlugin plugin, Settings settings, BiomeRemapper remapper,
			BiomeScanner scanner, World world, int minX, int maxX, int minZ, int maxZ, boolean debug, int progressStep,
			Consumer<String> progress, Consumer<TaskReport> ender, BiomeMap map, Consumer<BiomeReport> biomeReport,
			int maxY) {
		super(plugin, settings, remapper, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender, map,
				maxY);
		this.biomeReport = biomeReport;
		this.onMappingDone = new OnMappingDone((x, z) -> addBiomes(world, x, z, map.getFloor(), maxY), world, minX,
				minZ, maxX, maxZ);
		this.remapper = remapper;
		this.scanner = scanner;
		this.remapper.addDoneChecker(onMappingDone);
		queue = new PopulatorQueue(biomeMap, world, scanner);
		scanner.setPopulatorQueue(queue);
		setPopulatorQueue(queue);
	}

	@Override
	protected void remapChunks() {
		super.remapChunks();
		queue.tick();
	}

	private void addBiomes(World world, int chunkX, int chunkZ, int minY, int maxY) {
		if (checked[chunkX - getMinX()][chunkZ - getMinZ()])
			return;
		if (scanner.addBiomesFor(biomeMap, world, chunkX, chunkZ, minY, maxY)) {
			checked[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		} // else will be done later, after the remap
	}

	@Override
	protected void whenDone() {
		scanner.finalizePopulatorQueue();
		removeQueue();
		super.whenDone();
		// make sure the to show after scan is done
		getPlugin().getScheduler().runTaskLater(() -> {
			remapper.removeDoneCheker(onMappingDone);
			biomeReport.accept(new BiomeReport(biomeMap));
		}, 10L);
	}

}
