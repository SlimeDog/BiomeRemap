package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;

public class LargeScanTaskStarter extends LargeTaskStarter {
	private final Runnable runnable;
	private final Consumer<BiomeReport> mapReturner;
	private final int yLayer;
	private final boolean useNMS;

	public LargeScanTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int yLayer, int z, boolean region,
			boolean debug, Runnable runnable, boolean useNMS) {
		this(plugin, world, owner, x, yLayer, z, region, debug, runnable, useNMS, null);
	}
	
	public LargeScanTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int yLayer,int z, boolean region,
			boolean debug, Runnable runnable, boolean useNMS, Consumer<BiomeReport> mapReturner) {
		super(plugin, world, owner, x, z, region, debug);
		this.runnable = runnable;
		this.mapReturner = mapReturner;
		this.yLayer = yLayer;
		this.useNMS = useNMS;
	}

	protected void startTask() {
		new LargeScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
				br().getSettings().getScanProgressStep(),
				(progress) -> onProgress(owner(), progress), 
				(task) -> onEnd(owner(), task, debug()), 
				(report) -> showMap(owner(), report, region(), debug(), world().getName(), x(), z()), yLayer, useNMS);
	}

	private void onProgress(CommandSender sender, String progress) {
		String msg = br().getMessages().getScanProgress(progress);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender)) br().logMessage(progress);
	}

	private void onEnd(CommandSender sender, TaskReport report, boolean debug) {
		String completeMsg = br().getMessages().getScanComplete();
		sender.sendMessage(completeMsg);
		if (!(sender instanceof ConsoleCommandSender)) br().logMessage(completeMsg);
		if (debug) sender.sendMessage(br().getMessages().getBiomeRemapSummary(report.getChunksDone(), report.getCompTime(), report.getTicksUsed()));
		if (runnable != null) runnable.run();
	}

	private void showMap(CommandSender sender, BiomeReport report, boolean region, boolean debug,
			String worldName, int x, int z) {
		String header;
		if (region) {
			header = br().getMessages().getScanRegionHeader(worldName, x, z);
		} else {
			header = br().getMessages().getScanChunkHeader(worldName, x, z);
		}
		sender.sendMessage(header);
		if (!(sender instanceof ConsoleCommandSender)) br().logMessage(header);
		Map<Biome, Integer> sortedMap = report.getBiomes().entrySet().stream()
				.sorted((e1,e2) -> e1.getKey().name().compareTo(e2.getKey().name()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Biome, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100*((double) entry.getValue())/total);
			String msg = br().getMessages().getScanListItem(percentage, entry.getKey().name(), entry.getValue());
			sender.sendMessage(msg);
			if (!(sender instanceof ConsoleCommandSender)) br().logMessage(msg);
		}
		String msg = br().getMessages().getScanListItem("100%", "TOTAL", (int) total);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender)) br().logMessage(msg);
		if (mapReturner != null) mapReturner.accept(report);
	}

}
