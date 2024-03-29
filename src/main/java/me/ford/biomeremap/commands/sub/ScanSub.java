package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.commands.SDCCommandOption;
import dev.ratas.slimedogcore.api.commands.SDCCommandOptionSet;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import dev.ratas.slimedogcore.impl.commands.CommandOptionSet;
import me.ford.biomeremap.largetasks.LargeScanTaskStarter;
import me.ford.biomeremap.largetasks.LargeTempScanTaskStarter;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class ScanSub extends BRSubCommand {
	private static final String NAME = "scan";
	private static final String PERMS = "biomeremap.scan";
	private static final String USAGE = "/biomeremap scan <chunk | region> [<world> <x> <z>]";
	private final SlimeDogPlugin br;
	private final Settings settings;
	private final Messages messages;
	private final BiomeRemapper remapper;
	private final BiomeScanner scanner;
	private final List<String> worldNames = new ArrayList<>();
	private boolean scanning = false;
	private final Pattern layerPattern = Pattern.compile("--layer=(\\d+)");
	private final Pattern multiLayerPattern = Pattern.compile("--layers=(\\d+)-(\\d+)");

	public ScanSub(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper,
			BiomeScanner scanner) {
		super(NAME, PERMS, USAGE);
		br = plugin;
		this.settings = settings;
		this.messages = messages;
		this.remapper = remapper;
		this.scanner = scanner;
		for (World world : br.getWorldProvider().getAllWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList(new String[] { "chunk", "region" }), list);
		} else if (args.length == 2) {
			return StringUtil.copyPartialMatches(args[1], worldNames, list);
		}
		return list;
	}

	@Override
	public boolean onOptionedCommand(SDCRecipient sender, String[] args, SDCCommandOptionSet opts) {
		if (scanning) {
			sender.sendMessage(messages.getScanInProgress().getMessage());
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
		boolean ingame = sender.isPlayer();
		boolean debug = opts.hasRawOption("--debug");
		boolean temp = opts.hasRawOption("--temp");
		int maxLayer = Integer.MIN_VALUE;
		for (SDCCommandOption cmdOpt : opts.getOptions()) {
			String opt = cmdOpt.getValue();
			Matcher matcher = layerPattern.matcher(opt);
			if (matcher.matches()) {
				try {
					maxLayer = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					// shouldn't happen because of the regex
					sender.sendRawMessage("Could not parse number for layer from option: " + opt);
					return true;
				}
				sender.sendRawMessage("Scanning at layer " + maxLayer + " instead of 0"); // TODO - message?
				break;
			}
			matcher = multiLayerPattern.matcher(opt);
			if (matcher.matches()) {
				int start;
				try {
					start = Integer.parseInt(matcher.group(1));
				} catch (NumberFormatException e) {
					// shouldn't happen because of the regex
					sender.sendRawMessage("Could not parse number for layers from option(1): " + opt);
					return true;
				}
				int stop;
				try {
					stop = Integer.parseInt(matcher.group(2));
				} catch (NumberFormatException e) {
					// shouldn't happen because of the regex
					sender.sendRawMessage("Could not parse number for layers from option(2): " + opt);
					return true;
				}
				sender.sendRawMessage("Running for multiple layers"); // TODO - message?
				runLayers(sender, args, opts, cmdOpt, start, stop);
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
			Location loc = ((SDCPlayerRecipient) sender).getLocation();
			world = loc.getWorld();
			x = loc.getChunk().getX();
			z = loc.getChunk().getZ();
			if (region) {
				x = x >> 5;
				z = z >> 5;
			}
		} else {
			world = br.getWorldProvider().getWorldByName(args[1]);
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorNotInteger();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[2])));
				return true;
			}
			try {
				z = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorNotInteger();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[3])));
				return true;
			}
		}
		if (world == null) {
			SDCSingleContextMessageFactory<String> msg = messages.errorWorldNotFound();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[1])));
			return true;
		}
		if (maxLayer == Integer.MIN_VALUE) {
			maxLayer = world.getMaxHeight();
		}
		if (maxLayer > world.getMaxHeight()) {
			maxLayer = world.getMaxHeight();
		}
		int minLayer = world.getMinHeight();
		if (settings.getApplicableBiomeMap(world.getName()) == null) {
			SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapNoMap();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(world.getName())));
			return true;
		}
		if (region) {
			SDCTripleContextMessageFactory<String, Integer, Integer> msg = messages.getScanRegionStart();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(world.getName(), x, z)));
		} else {
			SDCTripleContextMessageFactory<String, Integer, Integer> msg = messages.getScanChunkStart();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(world.getName(), x, z)));
		}
		ReportTarget target;
		if (!ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getConsoleRecipient());
		}
		if (!temp) {
			new LargeScanTaskStarter(br, settings, messages, remapper, scanner, world, target, x, minLayer, maxLayer, z,
					region, debug, () -> taskDone());
		} else {
			new LargeTempScanTaskStarter(br, settings, messages, world, target, x, minLayer, maxLayer, z, region, debug,
					() -> taskDone());
		}
		scanning = true;
		return true;
	}

	private static SDCCommandOptionSet getCopyWithout(SDCCommandOptionSet opts, SDCCommandOption curOpt) {
		SDCCommandOptionSet newOpts = new CommandOptionSet();
		for (SDCCommandOption opt : opts.getOptions()) {
			if (curOpt != opt) {
				newOpts.addOption(curOpt.getRaw(), curOpt.getValue());
			}
		}
		return newOpts;
	}

	private void runLayers(SDCRecipient sender, String[] args, SDCCommandOptionSet opts, SDCCommandOption curOpt,
			int start, int stop) {
		final AtomicInteger nr = new AtomicInteger(start);
		SDCCommandOptionSet newOpts = getCopyWithout(opts, curOpt);
		br.getScheduler().runTaskTimer((t) -> {
			if (!scanning) {
				String opt = String.format("--layer=%d", nr);
				newOpts.addOption(opt, opt);
				onOptionedCommand(sender, args, newOpts);
				newOpts.removeOptionWithName(opt);
				nr.incrementAndGet();
				if (nr.get() > stop) {
					t.cancel();
				}
			}
		}, 4L, 4L);
	}

	public void taskDone() {
		scanning = false;
	}

}
