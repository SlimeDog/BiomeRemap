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

	public LargeScanTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int z, boolean region,
			boolean debug, Runnable runnable) {
		this(plugin, world, owner, x, z, region, debug, runnable, null);
	}
	
	public LargeScanTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int z, boolean region,
			boolean debug, Runnable runnable, Consumer<BiomeReport> mapReturner) {
		super(plugin, world, owner, x, z, region, debug);
		this.runnable = runnable;
		this.mapReturner = mapReturner;
	}

	protected void startTask() {
		new LargeScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
				br().getSettings().getScanProgressStep(),
				(progress) -> onProgress(owner(), progress), 
				(task) -> onEnd(owner(), task, debug()), 
				(report) -> showMap(owner(), report, region(), debug(), world().getName(), x(), z()));
	}

	private void onProgress(CommandSender sender, String progress) {
		String msg = br().getMessages().getScanProgress(progress);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender)) br().getLogger().info(progress);
	}

	private void onEnd(CommandSender sender, TaskReport report, boolean debug) {
		String completeMsg = br().getMessages().getScanComplete();
		sender.sendMessage(completeMsg);
		if (!(sender instanceof ConsoleCommandSender)) br().getLogger().info(completeMsg);
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
		if (!(sender instanceof ConsoleCommandSender)) br().getLogger().info(header);
		Map<Biome, Integer> sortedMap = report.getBiomes().entrySet().stream()
				.sorted((e1,e2) -> e1.getKey().name().compareTo(e2.getKey().name()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Biome, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100*((double) entry.getValue())/total);
			String msg = br().getMessages().getScanListItem(percentage, entry.getKey().name());
			sender.sendMessage(msg);
			if (!(sender instanceof ConsoleCommandSender)) br().getLogger().info(msg);
		}
		if (mapReturner != null) mapReturner.accept(report);
	}

}
