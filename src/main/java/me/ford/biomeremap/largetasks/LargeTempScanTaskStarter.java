package me.ford.biomeremap.largetasks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;
import me.ford.biomeremap.largetasks.LargeTempScanTask.TemperatureReport;

/**
 * LargeTempScanTaskStarter
 */
public class LargeTempScanTaskStarter extends LargeTaskStarter {
    private final int yLayer;
    private final Runnable endRunnable;

    public LargeTempScanTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int yLayer, int z, boolean region,
            boolean debug, Runnable endRunnable) {
        super(plugin, world, owner, x, z, region, debug);
        this.yLayer = yLayer;
        this.endRunnable = endRunnable;
    }

    @Override
    protected void startTask() {
        new LargeTempScanTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(), 
                    br().getSettings().getScanProgressStep(),
                    (progress) -> onProgress(owner(), progress), 
                    (task) -> onEnd(owner(), task, debug()), 
                    (map) -> showMap(owner(), map, region(), debug(), world().getName(), x(), z()), yLayer);
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
        if (endRunnable != null) endRunnable.run();
	}

	private void showMap(CommandSender sender, TemperatureReport report, boolean region, boolean debug,
			String worldName, int x, int z) {
		String header;
		if (region) {
			header = br().getMessages().getScanRegionHeader(worldName, x, z);
		} else {
			header = br().getMessages().getScanChunkHeader(worldName, x, z);
		}
		sender.sendMessage(header);
		if (!(sender instanceof ConsoleCommandSender)) br().logMessage(header);
		Map<Double, Integer> sortedMap = report.getTemps().entrySet().stream()
				.sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
		double total = 0;
		for (int val : sortedMap.values()) {
			total += val;
		}
		for (Entry<Double, Integer> entry : sortedMap.entrySet()) {
			String percentage = String.format("%3.0f%%", 100*((double) entry.getValue())/total);
			String msg = br().getMessages().getScanListItem(percentage, String.format("%4.2f", entry.getKey()));
			sender.sendMessage(msg);
			if (!(sender instanceof ConsoleCommandSender)) br().logMessage(msg);
		}
	}
    
}