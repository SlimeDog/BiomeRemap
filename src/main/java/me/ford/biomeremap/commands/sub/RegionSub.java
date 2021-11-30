package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeMappingTaskStarter;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;

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
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], worldNames, list);
		}
		return list; // not going to suggest antrhing
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		if (remapping) {
			sender.sendMessage(br.getMessages().getBiomeRemapInPrgoress());
			return true;
		}
		boolean debug = opts.contains("--debug");
		boolean scanAfter = opts.contains("--scan")
				&& (sender.hasPermission("biomeremap.scan") || sender instanceof ConsoleCommandSender);
		boolean ingame = sender instanceof Player;
		if (!ingame && args.length < 3) {
			return false;
		}
		boolean myLocation = args.length < 3;
		World world;
		int regionX;
		int regionZ;
		if (myLocation) {
			if (args.length > 0) { // either no arguments for current region or specify region
				return false;
			}
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
		ReportTarget target;
		if (ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getServer().getConsoleSender());
		}
		String startedMsg = br.getMessages().getRegionRemapStarted(world.getName(), regionX, regionZ);
		target.sendMessage(startedMsg);
		remapping = true;
		new LargeMappingTaskStarter(br, world, target, regionX, regionZ, true, debug, () -> remapEnded(), scanAfter,
				null);
		return true;
	}

	private void remapEnded() {
		remapping = false;
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
