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
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.settings.ChunkArea;
import me.ford.biomeremap.mapping.settings.MultiReportTarget;
import me.ford.biomeremap.mapping.settings.RemapOptions;
import me.ford.biomeremap.mapping.settings.ReportTarget;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;

public class ChunkSub extends SubCommand {
	private static final String PERMS = "biomeremap.remap";
	private static final String USAGE = "/biomeremap chunk [<world> <x> <z>]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();

	public ChunkSub(BiomeRemap plugin) {
		super("chunk");
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
		boolean debug = opts.contains("--debug");
		boolean scanAfter = opts.contains("--scan")
				&& (sender.hasPermission("biomeremap.scan") || sender instanceof ConsoleCommandSender);
		boolean ingame = sender instanceof Player;
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
			chunk = ((Player) sender).getLocation().getChunk();
		} else {
			// check world
			String worldName = args[0];
			World world = br.getServer().getWorld(worldName);
			if (world == null) {
				sender.sendMessage(br.getMessages().errorWorldNotFound(worldName));
				return true;
			}

			// check x and z
			int x;
			try {
				x = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[1]));
				return true;
			}
			int z;
			try {
				z = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(br.getMessages().errorNotInteger(args[2]));
				return true;
			}
			chunk = world.getChunkAt(x, z);
		}
		BiomeMap map = br.getSettings().getApplicableBiomeMap(chunk.getWorld().getName());
		if (map == null) {
			sender.sendMessage(br.getMessages().getBiomeRemapNoMap(chunk.getWorld().getName()));
			return true;
		}
		String startMsg = br.getMessages().getChunkRemapStarted(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
		ReportTarget target;
		if (!ingame) {
			target = new SingleReportTarget(sender);
		} else {
			target = new MultiReportTarget(sender, br.getServer().getConsoleSender());
		}
		target.sendMessage(startMsg);
		ChunkArea area = new ChunkArea(chunk.getWorld(), chunk.getX(), chunk.getZ());
		RemapOptions options = new RemapOptions.Builder().isDebug(debug).scanAfter(scanAfter).withArea(area)
				.withTarget(target).withMap(map).endRunnable(() -> {
					String completeMsg = br.getMessages().getBiomeRemapComplete();
					target.sendMessage(completeMsg);
				}).maxY(maxY).build();
		br.getRemapper().remapArea(options);
		return true;
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
