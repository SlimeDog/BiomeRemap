package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.mapping.BiomeMap;

public class ListSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap list [world]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	
	public ListSub(BiomeRemap plugin) {
		super("list");
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
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		List<BiomeMap> list = new ArrayList<>();
		String worldName = null;
		if (args.length == 0) {
			for (String name : br.getSettings().getBiomeMapNames()) {
				BiomeMap map = br.getSettings().getBiomeMap(name);
				if (map == null) {
					br.getLogger().warning("BiomeMap by name '" + name + "' listed, but returns null!");
					continue;
				}
				list.add(map);
			}
		} else {
			worldName = args[0];
			if (!worldNames.contains(worldName)) {
				sender.sendMessage(br.getMessages().errorWorldNotFound(worldName));
				return true;
			}
			BiomeMap map = br.getSettings().getApplicableBiomeMap(worldName);
			if (map != null) list.add(map);
		}
		if (list.isEmpty() && worldName != null) {
			sender.sendMessage(br.getMessages().getBiomeRemapNoMap(worldName));
		} else {
			sender.sendMessage(br.getMessages().getBiomeRemapListHeaders());
			for (BiomeMap map : list) {
				sender.sendMessage(br.getMessages().getBiomeRemapListItem(map.getName()));
			}
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
