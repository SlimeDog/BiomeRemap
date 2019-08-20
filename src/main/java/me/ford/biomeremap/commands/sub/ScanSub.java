package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeScanTask;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeTask.TaskReport;

public class ScanSub extends SubCommand {
	private static final String PERMS = "TBD"; // TODO - perms
	private static final String USAGE = "/biomeremap scan <chunk | region> [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();

	public ScanSub(BiomeRemap plugin) {
		br = plugin;
		for (World world : br.getServer().getWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList(new String[] {"chunk", "region"}), list);
		} else if (args.length == 2) {
			return StringUtil.copyPartialMatches(args[1], worldNames, list);
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		if (args.length < 1) {
			return false;
		}
		String regionOrChunk = args[0];
		if (!regionOrChunk.equalsIgnoreCase("region") && !regionOrChunk.equalsIgnoreCase("chunk")) {
			return false;
		}
		boolean region = regionOrChunk.equalsIgnoreCase("region");
		boolean ingame = sender instanceof Player;
		boolean debug = Arrays.asList(opts).contains("--debug");
		World world;
		int x, z;

		if (!ingame && args.length < 4) {
			return false;
		}
		if (args.length < 4) { // has to be in game
			Location loc = ((Player) sender).getLocation();
			world = loc.getWorld();
			x = loc.getChunk().getX();
			z = loc.getChunk().getZ();
		} else {
			world = br.getServer().getWorld(args[1]);
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return false; // TODO - some error?
			}
			try {
				z = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return false; // TODO - some error?
			}
		}
		if (world == null) {
			sender.sendMessage(br.getMessages().errorWorldNotFound());
			return true;
		}
		
		int minX, maxX, minZ, maxZ;
		if (!region) {
			minX = x;
			maxX = x + 1;
			minZ = z;
			maxZ = z + 1;
		} else {
			minX = x * 32;
			maxX = minX + 32;
			minZ = z * 32;
			maxZ = minZ + 32;
		}
		sender.sendMessage("Staring... (TODO: message)"); // TODO - messaging
		new LargeScanTask(br, world, minX, maxX, minZ, maxZ, debug,
				(progress) -> onProgress(sender, progress), 
				(task) -> onEnd(sender, task, debug), 
				(report) -> showMap(sender, report));
		return true;
	}
	
	private void onProgress(CommandSender sender, String progress) {
		sender.sendMessage(progress);
		if (!(sender instanceof ConsoleCommandSender)) br.getLogger().info(progress);
	}
	
	private void onEnd(CommandSender sender, TaskReport report, boolean debug) {
		sender.sendMessage("Finished"); // TODO - do I need this? This might be quicker than the other ones
		if (debug) sender.sendMessage(String.format("Did %d chunks in %d ms in a total of %d ticks", report.getChunksDone(), report.getCompTime(), report.getTicksUsed())); // TODO - messaging
	}
	
	private void showMap(CommandSender sender, BiomeReport report) {
		sender.sendMessage("HEADER"); // TODO - messaging
		String format = "%d %s";
		for (Entry<Biome, Integer> entry : report.getBiomes().entrySet()) {
			sender.sendMessage(String.format(format, entry.getValue(), entry.getKey().name())); // TODO - messaging
		}
		int nr = report.nrOfNulls();
		if (nr > 0) sender.sendMessage(String.format(format, nr, "null"));
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
