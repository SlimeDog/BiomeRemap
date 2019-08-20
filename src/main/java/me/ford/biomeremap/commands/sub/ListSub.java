package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.mapping.BiomeMap;

public class ListSub extends SubCommand {
	private static final String PERMS = "biomeremap.use"; // TODO - different perms?
	private static final String USAGE = "/biomeremap list [world]";
	private final BiomeRemap br;
	private final List<String> worldNames = new ArrayList<>();
	
	public ListSub(BiomeRemap plugin) {
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
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		List<BiomeMap> list = new ArrayList<>();
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
			String worldName = args[0];
			if (!worldNames.contains(worldName)) {
				sender.sendMessage(br.getMessages().errorWorldNotFound());
				return true;
			}
			BiomeMap map = br.getSettings().getApplicableBiomeMap(worldName);
			if (map != null) list.add(map);
		}
		sender.sendMessage(br.getMessages().getBiomeRemapListHeaders());
		for (BiomeMap map : list) {
			sender.sendMessage(br.getMessages().getBiomeRemapListItem(map.getName()));
		}
		if (list.isEmpty()) {
			sender.sendMessage("No mapping found " + (args.length > 0 ? "for world " + args[0] : "")); // TODO - messaging
			sender.sendMessage("Some message into messages.yml for this?");
		}
		return true;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(PERMS);
	}

	@Override
	public String getUsage(CommandSender sender) {
		return USAGE;
	}

}
