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
import me.ford.biomeremap.mapping.BiomeRemapper;

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
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], worldNames, list);
		}
		return list; // not going to suggest antrhing
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		List<String> options = Arrays.asList(opts);
		boolean debug = options.contains("--debug");
		boolean scanAfter = options.contains("--scan") && (sender.hasPermission("biomeremap.scan") || sender instanceof ConsoleCommandSender);
		boolean ingame = sender instanceof Player;
		if (!ingame && args.length < 3) {
			return false;
		}
		boolean myLocation = args.length < 3;
		Chunk chunk;
		if (myLocation) {
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
		if (br.getSettings().getApplicableBiomeMap(chunk.getWorld().getName()) == null) {
			sender.sendMessage(br.getMessages().getBiomeRemapNoMap(chunk.getWorld().getName()));
			return true;
		}
		String startMsg = br.getMessages().getChunkRemapStarted(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
		sender.sendMessage(startMsg);
		if (ingame) br.getLogger().info(startMsg);
		BiomeRemapper.getInstance().remapChunk(chunk, debug);
		sender.sendMessage(br.getMessages().getBiomeRemapComplete());
		if (scanAfter) { // TODO - this can be done better if I redesign some things
			String cmd = String.format("biomeremap scan chunk %s %d %d", chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
			if (debug) cmd += "--debug";
			sender.getServer().dispatchCommand(sender, cmd);
		}
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
