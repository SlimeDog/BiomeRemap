package me.ford.biomeremap.largetasks;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;

public class LargeMappingTaskStarter extends LargeTaskStarter {
	private final Runnable runnable;
	private final boolean scan;

	public LargeMappingTaskStarter(BiomeRemap plugin, World world, CommandSender owner, int x, int z, boolean region,
			boolean debug, Runnable runnable, boolean scan) {
		super(plugin, world, owner, x, z, region, debug);
		this.runnable = runnable;
		this.scan = scan;
	}

	@Override
	protected void startTask() {
		new LargeMappingTask(br(), world(), chunkX(), stopX(), chunkZ(), stopZ(), debug(),
				br().getSettings().getRegionRemapProgressStep(),
				(progress) -> reportProgress(owner(), progress),
				(report) -> remappingEnded(owner(), report, debug(), world(), x(), z(), scan));
	}
	
	private void reportProgress(CommandSender sender, String progress) {
		String msg = br().getMessages().getBiomeRemapProgress(progress);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender)) br().getLogger().info(msg);
	}
	
	private void remappingEnded(CommandSender sender, TaskReport report, boolean debug, World world, int x, int z, boolean scanAfter) {
		sender.sendMessage(br().getMessages().getBiomeRemapComplete());
		if (debug) sender.sendMessage(br().getMessages().getBiomeRemapSummary(report.getChunksDone(), report.getCompTime(), report.getTicksUsed()));
		if (scan) new LargeScanTaskStarter(br(), world(), owner(), x(), z(), region(), debug(), null);
		if (runnable != null) runnable.run();
	}

}
