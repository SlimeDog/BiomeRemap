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
import me.ford.biomeremap.mapping.LargeMappingTask;
import me.ford.biomeremap.mapping.LargeMappingTask.TaskReport;

public class RegionSub extends SubCommand {
	private static final String PERMS = "biomeremap.remap";
	private static final String USAGE = "/biomeremap region [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	private boolean remapping = false;

	public RegionSub(BiomeRemap plugin) {
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
			sender.sendMessage("Currently in the middle of a remap!");
			return true;
		}
		List<String> options = Arrays.asList(opts);
		boolean debug = options.contains("--debug");
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
				sender.sendMessage(br.getMessages().errorWorldNotFound()); // TODO - messaging
				return true;
			}

			// check x and z
			try {
				regionX = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				return false; // USAGE - maybe somethinge else?
			}
			try {
				regionZ = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return false; // USAGE - maybe somethinge else?
			}
		}
		if (br.getSettings().getApplicableBiomeMap(world.getName()) == null) {
			sender.sendMessage("world not configured - which message?"); // TODO - messaging
			return true;
		}
		String startedMsg = br.getMessages().getBiomeRemapStarted();
		sender.sendMessage(startedMsg);
		br.getLogger().info(startedMsg);
		int chunkXStart = regionX * 32;
		int chunkZStart = regionZ * 32;
		remapping = true;
		new LargeMappingTask(br, world, chunkXStart, chunkXStart + 32, chunkZStart, chunkZStart + 32, debug,
				(progress) -> reportProgress(sender, progress),
				(report) -> remappingEnded(sender, report, debug));
		return true;
	}
	
	private void reportProgress(CommandSender sender, String progress) {
		String msg = br.getMessages().getBiomeRemapProgress(progress);
		sender.sendMessage(msg);
		br.getLogger().info(msg);
	}
	
	private void remappingEnded(CommandSender sender, TaskReport report, boolean debug) {
		remapping = false;
		sender.sendMessage(br.getMessages().getBiomeRemapComplete());
		if (debug) sender.sendMessage(String.format("Did %d chunks in %d ms in a total of %d ticks", report.getChunksDone(), report.getCompTime(), report.getTicksUsed())); // TODO - messaging
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
