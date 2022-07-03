package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.SDCMessage;
import dev.ratas.slimedogcore.api.messaging.context.SDCTripleContext;
import dev.ratas.slimedogcore.api.messaging.context.SDCVoidContext;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.RegionArea;
import me.ford.biomeremap.mapping.settings.RemapOptions;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class RegionSub extends BRSubCommand {
	private static final String NAME = "region";
	private static final String PERMS = "biomeremap.remap";
	private static final String USAGE = "/biomeremap region [<world> <x> <z>]";
	private final SlimeDogPlugin br;
	private final Settings settings;
	private final Messages messages;
	private final BiomeRemapper remapper;
	private final List<String> worldNames = new ArrayList<>();
	private boolean remapping = false;

	public RegionSub(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper) {
		super(NAME, PERMS, USAGE);
		br = plugin;
		this.settings = settings;
		this.messages = messages;
		this.remapper = remapper;
		for (World world : br.getWorldProvider().getAllWorlds()) {
			worldNames.add(world.getName());
		}
	}

	@Override
	public List<String> onTabComplete(SDCRecipient sender, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], worldNames, list);
		}
		return list; // not going to suggest antrhing
	}

	@Override
	public boolean onCommand(SDCRecipient sender, String[] args, List<String> opts) {
		if (remapping) {
			sender.sendMessage(messages.getBiomeRemapInPrgoress().getMessage());
			return true;
		}
		boolean debug = opts.contains("--debug");
		boolean scanAfter = opts.contains("--scan")
				&& (sender.hasPermission("biomeremap.scan") || !sender.isPlayer());
		boolean ingame = sender.isPlayer();
		if (!ingame && args.length < 3) {
			return false;
		}
		int maxY = getMaxY(opts);
		boolean myLocation = args.length < 3;
		World world;
		int regionX;
		int regionZ;
		if (myLocation) {
			if (args.length > 0) { // either no arguments for current region or specify region
				return false;
			}
			Chunk chunk = ((SDCPlayerRecipient) sender).getLocation().getChunk();
			regionX = chunk.getX() >> 5;
			regionZ = chunk.getZ() >> 5;
			world = chunk.getWorld();
		} else {
			// check world
			String worldName = args[0];
			world = br.getWorldProvider().getWorldByName(worldName);
			if (world == null) {
				SDCSingleContextMessageFactory<String> msg = messages.errorWorldNotFound();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(worldName)));
				return true;
			}

			// check x and z
			try {
				regionX = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorNotInteger();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[1])));
				return true;
			}
			try {
				regionZ = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorNotInteger();
				sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(args[2])));
				return true;
			}
		}
		BiomeMap map = settings.getApplicableBiomeMap(world.getName());
		if (map == null) {
			SDCSingleContextMessageFactory<String> msg = messages.getBiomeRemapNoMap();
			sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(world.getName())));
			return true;
		}
		ReportTarget target;
		if (!ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getConsoleRecipient());
		}
		SDCTripleContextMessageFactory<String, Integer, Integer> msg = messages.getRegionRemapStarted();
		sender.sendMessage(msg.getMessage(msg.getContextFactory().getContext(world.getName(), regionX, regionZ)));
		SDCTripleContextMessageFactory<String, Integer, Integer> regionStarted = messages.getRegionRemapStarted();
		SDCMessage<SDCTripleContext<String, Integer, Integer>> startedMsg = regionStarted
				.getMessage(regionStarted.getContextFactory().getContext(world.getName(), regionX, regionZ));
		target.sendMessage(startedMsg);
		remapping = true;
		RegionArea area = new RegionArea(world, regionX, regionZ);
		RemapOptions options = new RemapOptions.Builder().isDebug(debug).scanAfter(scanAfter).withArea(area)
				.withTarget(target).withMap(map).endRunnable(() -> {
					SDCMessage<SDCVoidContext> completeMsg = messages.getBiomeRemapComplete().getMessage();
					target.sendMessage(completeMsg);
					remapEnded();
				}).maxY(maxY).build();
		remapper.remapArea(options);
		return true;
	}

	private void remapEnded() {
		remapping = false;
	}

}
