package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.mapping.BiomeMap;

public class LargeMappingTaskStarter extends LargeTaskStarter {
	private final Runnable runnable;
	private final boolean scan;
	private BiomeMap map;

	public LargeMappingTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int z, boolean region,
			boolean debug, Runnable runnable, boolean scan, BiomeMap map) {
		super(plugin, world, owner, x, z, region, debug);
		this.runnable = runnable;
		this.scan = scan;
		this.map = map;
	}

	@Override
	protected void startTask() {
		if (!scan) {
			new LargeMappingTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
					br().getSettings().getRegionRemapProgressStep(), (progress) -> reportProgress(owner(), progress),
					(report) -> remappingEnded(owner(), report, debug(), world(), x(), z(), scan), map);
		} else {
			new LargeMappingWithScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
					br().getSettings().getRegionRemapProgressStep(), (progress) -> reportProgress(owner(), progress),
					(report) -> remappingEnded(owner(), report, debug(), world(), x(), z(), scan), map,
					(report) -> showMap(owner(), report, true, debug(), world().getName(), x(), z()));
		}
	}

	private void showMap(CommandSender sender, BiomeReport report, boolean region, boolean debug, String worldName,
			int x, int z) {
		String header;
		if (region) {
			header = br().getMessages().getScanRegionHeader(worldName, x, z);
		} else {
			header = br().getMessages().getScanChunkHeader(worldName, x, z);
		}
		sender.sendMessage(header);
		if (!(sender instanceof ConsoleCommandSender))
			br().logMessage(header);
		Map<Biome, Integer> sortedMap = report.getBiomes().entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().name().compareTo(e2.getKey().name()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Biome, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100 * ((double) entry.getValue()) / total);
			String msg = br().getMessages().getScanListItem(percentage, entry.getKey().name(), entry.getValue());
			sender.sendMessage(msg);
			if (!(sender instanceof ConsoleCommandSender))
				br().logMessage(msg);
		}
		String msg = br().getMessages().getScanListItem("100%", "TOTAL", (int) total);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender))
			br().logMessage(msg);
	}

	private void reportProgress(CommandSender sender, String progress) {
		String msg = br().getMessages().getBiomeRemapProgress(progress);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender))
			br().logMessage(msg);
	}

	private void remappingEnded(CommandSender sender, TaskReport report, boolean debug, World world, int x, int z,
			boolean scanAfter) {
		String completeMsg = br().getMessages().getBiomeRemapComplete();
		sender.sendMessage(completeMsg);
		if (!(sender instanceof ConsoleCommandSender))
			br().logMessage(completeMsg);
		if (debug)
			sender.sendMessage(br().getMessages().getBiomeRemapSummary(report.getChunksDone(), report.getCompTime(),
					report.getTicksUsed()));
		if (runnable != null)
			runnable.run();
	}

}
