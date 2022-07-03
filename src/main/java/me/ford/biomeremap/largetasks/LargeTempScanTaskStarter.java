package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.World;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.largetasks.LargeTempScanTask.TemperatureReport;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

/**
 * LargeTempScanTaskStarter
 */
public class LargeTempScanTaskStarter extends LargeTaskStarter {
	private final int minLayer;
	private final int maxLayer;
	private final Runnable endRunnable;
	private final Settings settings;
	private final Messages messages;

	public LargeTempScanTaskStarter(SlimeDogPlugin plugin, Settings settings, Messages messages, World world,
			ReportTarget owner, int x, int minLayer,
			int maxLayer, int z, boolean region, boolean debug, Runnable endRunnable) {
		super(plugin, world, owner, x, z, region, debug);
		this.minLayer = minLayer;
		this.maxLayer = maxLayer;
		this.endRunnable = endRunnable;
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	protected void startTask() {
		new LargeTempScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
				settings.getScanProgressStep(), (progress) -> onProgress(owner(), progress),
				(task) -> onEnd(owner(), task, debug()),
				(map) -> showMap(owner(), map, region(), debug(), world().getName(), x(), z()), minLayer, maxLayer);
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
		if (endRunnable != null)
			endRunnable.run();
	}

	private void showMap(ReportTarget sender, TemperatureReport report, boolean region, boolean debug, String worldName,
			int x, int z) {
		SDCTripleContextMessageFactory<String, Integer, Integer> header;
		if (region) {
			header = messages.getScanRegionHeader();
		} else {
			header = messages.getScanChunkHeader();
		}
		sender.sendMessage(header.getMessage(header.getContextFactory().getContext(worldName, x, z)));
		Map<Double, Integer> sortedMap = report.getTemps().entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Double, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100 * ((double) entry.getValue()) / total);
			SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
			sender.sendMessage(msg
					.getMessage(msg.getContextFactory().getContext(percentage, String.format("%4.2f", entry.getKey()),
							entry.getValue())));
		}
		SDCTripleContextMessageFactory<String, String, Integer> msg = messages.getScanListItem();
		sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext("100%", "TOTAL", (int) total)));
	}

}