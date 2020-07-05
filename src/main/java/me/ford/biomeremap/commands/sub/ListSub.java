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
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class ListSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap list [world]";
	private final Settings settings;
	private final Messages messages;
	private final List<String> worldNames = new ArrayList<>();

	public ListSub(BiomeRemap plugin) {
		super("list");
		this.settings = plugin.getSettings();
		this.messages = plugin.getMessages();
		for (World world : plugin.getServer().getWorlds()) {
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
			for (String name : settings.getBiomeMapNames()) {
				BiomeMap map = settings.getBiomeMap(name);
				if (map == null) {
					BiomeRemap.logger().warning(messages.errorBiomeMapNotFound(name));
					continue;
				}
				list.add(map);
			}
		} else {
			worldName = args[0];
			if (!worldNames.contains(worldName)) {
				sender.sendMessage(messages.errorWorldNotFound(worldName));
				return true;
			}
			BiomeMap map = settings.getApplicableBiomeMap(worldName);
			if (map != null)
				list.add(map);
		}
		if (list.isEmpty() && worldName != null) {
			sender.sendMessage(messages.getBiomeRemapNoMap(worldName));
		} else {
			sender.sendMessage(messages.getBiomeRemapListHeaders());
			for (BiomeMap map : list) {
				sender.sendMessage(messages.getBiomeRemapListItem(map.getName()));
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
