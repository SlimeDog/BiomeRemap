package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeScanTaskStarter;
import me.ford.biomeremap.largetasks.LargeTempScanTaskStarter;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;

public class ScanSub extends SubCommand {
	private static final String PERMS = "biomeremap.scan";
	private static final String USAGE = "/biomeremap scan <chunk | region> [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	private boolean scanning = false;
	private final Pattern layerPattern = Pattern.compile("--layer=(\\d+)");
	private final Pattern multiLayerPattern = Pattern.compile("--layers=(\\d+)-(\\d+)");

	public ScanSub(BiomeRemap plugin) {
		super("scan");
		br = plugin;
		for (World world : br.getServer().getWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList(new String[] { "chunk", "region" }), list);
		} else if (args.length == 2) {
			return StringUtil.copyPartialMatches(args[1], worldNames, list);
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		if (scanning) {
			sender.sendMessage(br.getMessages().getScanInProgress());
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		String regionOrChunk = args[0];
		if (!regionOrChunk.equalsIgnoreCase("region") && !regionOrChunk.equalsIgnoreCase("chunk")) {
			return false;
		}
		boolean region = regionOrChunk.equalsIgnoreCase("region");
		boolean ingame = sender instanceof Player;
		boolean debug = opts.contains("--debug");
		boolean temp = opts.contains("--temp");
		int maxLayer = Integer.MIN_VALUE;
		for (String opt : opts) {
			Matcher matcher = layerPattern.matcher(opt);
			if (matcher.matches()) {
				try {
					maxLayer = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					sender.sendMessage("Could not parse number for layer from option: " + opt); // shouldn't happen
																								// because of the regex
					return true;
				}
				sender.sendMessage("Scanning at layer " + maxLayer + " instead of 0"); // TODO - message?
				break;
			}
			matcher = multiLayerPattern.matcher(opt);
			if (matcher.matches()) {
				int start;
				try {
					start = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					sender.sendMessage("Could not parse number for layers from option(1): " + opt); // shouldn't happen
																									// because of the
																									// regex
					return true;
				}
				int stop;
				try {
					stop = Integer.parseInt(matcher.group(2));
				} catch (NumberFormatException e) {
					sender.sendMessage("Could not parse number for layers from option(2): " + opt); // shouldn't happen
																									// because of the
																									// regex
					return true;
				}
				sender.sendMessage("Running for multiple layers"); // TODO - message?
				runLayers(sender, args, opts, opt, start, stop);
				return true;
			}
		}
		World world;
		int x, z;

		if (!ingame && args.length < 4) {
			return false;
		}
		if (args.length < 4) { // has to be in game
			if (args.length > 1) { // either no arguments for current chunk/region or specify chunk/region
				return false;
			}
			Location loc = ((Player) sender).getLocation();
			world = loc.getWorld();
			x = loc.getChunk().getX();
			z = loc.getChunk().getZ();
			if (region) {
				x = x >> 5;
				z = z >> 5;
			}
		} else {
			world = br.getServer().getWorld(args[1]);
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[2]));
				return true;
			}
			try {
				z = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[3]));
				return true;
			}
		}
		if (world == null) {
			sender.sendMessage(br.getMessages().errorWorldNotFound(args[1]));
			return true;
		}
		if (maxLayer == Integer.MIN_VALUE) {
			maxLayer = world.getMaxHeight();
		}
		if (maxLayer > world.getMaxHeight()) {
			maxLayer = world.getMaxHeight();
		}
		int minLayer = world.getMinHeight();
		if (br.getSettings().getApplicableBiomeMap(world.getName()) == null) {
			sender.sendMessage(br.getMessages().getBiomeRemapNoMap(world.getName()));
			return true;
		}
		if (region) {
			sender.sendMessage(br.getMessages().getScanRegionStart(world.getName(), x, z));
		} else {
			sender.sendMessage(br.getMessages().getScanChunkStart(world.getName(), x, z));
		}
		ReportTarget target;
		if (!ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getServer().getConsoleSender());
		}
		if (!temp) {
			new LargeScanTaskStarter(br, world, target, x, minLayer, maxLayer, z, region, debug, () -> taskDone());
		} else {
			new LargeTempScanTaskStarter(br, world, target, x, minLayer, maxLayer, z, region, debug, () -> taskDone());
		}
		scanning = true;
		return true;
	}

	private void runLayers(CommandSender sender, String[] args, List<String> opts, String curOpt, int start, int stop) {
		List<String> newOpts = new ArrayList<>(opts);
		newOpts.remove(curOpt);
		new BukkitRunnable() {
			private int nr = start;

			@Override
			public void run() {
				if (!scanning) {
					newOpts.add(String.format("--layer=%d", nr));
					onCommand(sender, args, newOpts);
					newOpts.remove(newOpts.size() - 1);
					nr++;
					if (nr > stop) {
						cancel();
					}
				}
			}
		}.runTaskTimer(br, 4L, 4L);
	}

	public void taskDone() {
		scanning = false;
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
