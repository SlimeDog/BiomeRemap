package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeMappingTask;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;

public class RegionSub extends SubCommand {
	private static final String PERMS = "biomeremap.remap";
	private static final String USAGE = "/biomeremap region [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	private boolean remapping = false;

	public RegionSub(BiomeRemap plugin) {
		super("region");
		br = plugin;
		for (World world : br.getServer().getWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], worldNames, list);
		}
		return list; // not going to suggest antrhing
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		if (remapping) {
			sender.sendMessage(br.getMessages().getBiomeRemapInPrgoress());
			return true;
		}
		List<String> options = Arrays.asList(opts);
		boolean debug = options.contains("--debug");
		boolean scanAfter = options.contains("--scan") && (sender.hasPermission("biomeremap.scan") || sender instanceof ConsoleCommandSender);
		boolean ingame = sender instanceof Player;
		if (!ingame && args.length < 3) {
			return false;
		}
		boolean myLocation = args.length < 3;
		World world;
		int regionX;
		int regionZ;
		if (myLocation) {
			Chunk chunk = ((Player) sender).getLocation().getChunk();
			regionX = chunk.getX() >> 5;
			regionZ = chunk.getZ() >> 5;
			world = chunk.getWorld();
		} else {
			// check world
			String worldName = args[0];
			world = br.getServer().getWorld(worldName);
			if (world == null) {
				sender.sendMessage(br.getMessages().errorWorldNotFound(worldName));
				return true;
			}

			// check x and z
			try {
				regionX = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[1]));
				return true;
			}
			try {
				regionZ = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[2]));
				return true;
			}
		}
		if (br.getSettings().getApplicableBiomeMap(world.getName()) == null) {
			sender.sendMessage(br.getMessages().getBiomeRemapNoMap(world.getName()));
			return true;
		}
		String startedMsg = br.getMessages().getRegionRemapStarted(world.getName(), regionX, regionZ);
		sender.sendMessage(startedMsg);
		if (!ingame) br.getLogger().info(startedMsg);
		int chunkXStart = regionX * 32;
		int chunkZStart = regionZ * 32;
		remapping = true;
		new LargeMappingTask(br, world, chunkXStart, chunkXStart + 32, chunkZStart, chunkZStart + 32, debug,
				br.getSettings().getRegionRemapProgressStep(),
				(progress) -> reportProgress(sender, progress),
				(report) -> remappingEnded(sender, report, debug, world, regionX, regionZ, scanAfter));
		return true;
	}
	
	private void reportProgress(CommandSender sender, String progress) {
		String msg = br.getMessages().getBiomeRemapProgress(progress);
		sender.sendMessage(msg);
		if (!(sender instanceof ConsoleCommandSender)) br.getLogger().info(msg);
	}
	
	private void remappingEnded(CommandSender sender, TaskReport report, boolean debug, World world, int x, int z, boolean scanAfter) {
		remapping = false;
		sender.sendMessage(br.getMessages().getBiomeRemapComplete());
		if (debug) sender.sendMessage(br.getMessages().getBiomeRemapSummary(report.getChunksDone(), report.getCompTime(), report.getTicksUsed()));
		if (scanAfter) { // TODO - this can be done better if I redesign some things
			String cmd = String.format("biomeremap scan region %s %d %d", world.getName(), x, z);
			if (debug) cmd += "--debug";
			sender.getServer().dispatchCommand(sender, cmd);
		}
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(PERMS) || (sender instanceof ConsoleCommandSender);
	}

	@Override
	public String getUsage(CommandSender sender) {
		return USAGE;
	}

}
