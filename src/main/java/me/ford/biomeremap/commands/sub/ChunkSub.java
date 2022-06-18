package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.util.StringUtil;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.settings.ChunkArea;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.RemapOptions;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class ChunkSub extends BRSubCommand {
	private static final String NAME = "chunk";
	private static final String PERMS = "biomeremap.remap";
	private static final String USAGE = "/biomeremap chunk [<world> <x> <z>]";
	private final SlimeDogPlugin br;
	private final Settings settings;
	private final Messages messages;
	private final BiomeRemapper remapper;
	private final List<String> worldNames = new ArrayList<>();

	public ChunkSub(SlimeDogPlugin plugin, Settings settings, Messages messages, BiomeRemapper remapper) {
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
		boolean debug = opts.contains("--debug");
		boolean scanAfter = opts.contains("--scan")
				&& (sender.hasPermission("biomeremap.scan") || !sender.isPlayer());
		boolean ingame = sender.isPlayer();
		if (!ingame && args.length < 3) {
			return false;
		}
		int maxY = getMaxY(opts);
		boolean myLocation = args.length < 3;
		Chunk chunk;
		if (myLocation) {
			if (args.length > 0) {// either no arguments for current chunk or specify chunk
				return false;
			}
			chunk = ((SDCPlayerRecipient) sender).getLocation().getChunk();
		} else {
			// check world
			String worldName = args[0];
			World world = br.getWorldProvider().getWorldByName(worldName);
			if (world == null) {
				sender.sendRawMessage(messages.errorWorldNotFound(worldName));
				return true;
			}

			// check x and z
			int x;
			try {
				x = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendRawMessage(messages.errorNotInteger(args[1]));
				return true;
			}
			int z;
			try {
				z = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendRawMessage(messages.errorNotInteger(args[2]));
				return true;
			}
			chunk = world.getChunkAt(x, z);
		}
		BiomeMap map = settings.getApplicableBiomeMap(chunk.getWorld().getName());
		if (map == null) {
			sender.sendRawMessage(messages.getBiomeRemapNoMap(chunk.getWorld().getName()));
			return true;
		}
		String startMsg = messages.getChunkRemapStarted(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
		ReportTarget target;
		if (!ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getConsoleRecipient());
		}
		target.sendMessage(startMsg);
		ChunkArea area = new ChunkArea(chunk.getWorld(), chunk.getX(), chunk.getZ());
		RemapOptions options = new RemapOptions.Builder().isDebug(debug).scanAfter(scanAfter).withArea(area)
				.withTarget(target).withMap(map).endRunnable(() -> {
					String completeMsg = messages.getBiomeRemapComplete();
					target.sendMessage(completeMsg);
				}).maxY(maxY).build();
		remapper.remapArea(options);
		return true;
	}

}
