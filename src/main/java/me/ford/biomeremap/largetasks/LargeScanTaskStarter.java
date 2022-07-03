package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.block.Biome;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class LargeScanTaskStarter extends LargeTaskStarter {
	private final Runnable runnable;
	private final Consumer<BiomeReport> mapReturner;
	private final int minLayer;
	private final int maxLayer;
	private final Settings settings;
	private final Messages messages;
	private final BiomeRemapper remapper;
	private final BiomeScanner scanner;

	public LargeScanTaskStarter(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper,
			BiomeScanner scanner, World world, ReportTarget owner, int x, int minLayer, int maxLayer, int z,
			boolean region, boolean debug, Runnable runnable) {
		this(plugin, settings, messages, remapper, scanner, world, owner, x, minLayer, maxLayer, z, region, debug,
				runnable, null);
	}

	public LargeScanTaskStarter(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper,
			BiomeScanner scanner, World world, ReportTarget owner, int x, int minLayer, int maxLayer, int z,
			boolean region, boolean debug, Runnable runnable, Consumer<BiomeReport> mapReturner) {
		super(plugin, world, owner, x, z, region, debug);
		this.runnable = runnable;
		this.mapReturner = mapReturner;
		this.minLayer = minLayer;
		this.maxLayer = maxLayer;
		this.settings = settings;
		this.messages = messages;
		this.remapper = remapper;
		this.scanner = scanner;
	}

	protected void startTask() {
		new LargeScanTask(br(), remapper, scanner, world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
				settings.getScanProgressStep(), (progress) -> onProgress(owner(), progress),
				(task) -> onEnd(owner(), task, debug()),
				(report) -> showMap(owner(), report, region(), debug(), world().getName(), x(), z()), minLayer,
				maxLayer);
	}

	private void onProgress(ReportTarget sender, String progress) {
		SDCSingleContextMessageFactory<String> msg = messages.getScanProgress();
		sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(progress)));
	}

	private void onEnd(ReportTarget sender, TaskReport report, boolean debug) {
		sender.sendMessage(messages.getScanComplete().getMessage());
		if (debug) {
			SDCTripleContextMessageFactory<Integer, Long, Integer> msg = messages.getBiomeRemapSummary();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(report.getChunksDone(),
					report.getCompTime(), report.getTicksUsed())));
		}
		if (runnable != null)
			runnable.run();
	}

	private void showMap(ReportTarget sender, BiomeReport report, boolean region, boolean debug, String worldName,
			int x, int z) {
		SDCTripleContextMessageFactory<String, Integer, Integer> header;
		if (region) {

			header = messages.getScanRegionHeader();
		} else {
			header = messages.getScanChunkHeader();
		}
		sender.sendMessage(header.getMessage(header.getContextFactory().getContext(worldName, x, z)));
		Map<Biome, Integer> sortedMap = report.getBiomes().entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().name().compareTo(e2.getKey().name()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Biome, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100 * ((double) entry.getValue()) / total);
			SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
			sender.sendMessage(msg.getMessage(
					msg.getContextFactory().getContext(percentage, entry.getKey().name(), entry.getValue())));
		}
		SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
		sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext("100%", "TOTAL", (int) total)));
		if (mapReturner != null)
			mapReturner.accept(report);
	}

}
