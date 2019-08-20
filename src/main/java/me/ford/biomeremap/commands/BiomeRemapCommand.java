package me.ford.biomeremap.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.sub.ChunkSub;
import me.ford.biomeremap.commands.sub.HelpSub;
import me.ford.biomeremap.commands.sub.InfoSub;
import me.ford.biomeremap.commands.sub.ListSub;
import me.ford.biomeremap.commands.sub.RegionSub;

public class BiomeRemapCommand extends ArgSplittingCommand {
	private final Map<String, SubCommand> subCommands = new HashMap<>();
	private final BiomeRemap br;
	
	public BiomeRemapCommand(BiomeRemap plugin) {
		br = plugin;
		subCommands.put("chunk", new ChunkSub(br));
		subCommands.put("region", new RegionSub(br));
		subCommands.put("help", new HelpSub());
		subCommands.put("info", new InfoSub(br));
		subCommands.put("list", new ListSub(br));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], subCommands.keySet(), list);
		} else if (args.length > 1) {
			SubCommand cmd = subCommands.get(args[0]);
			if (cmd == null) {
				return list;
			} else {
				if (cmd.hasPermission(sender)) {
					return cmd.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length), opts);
				} else {
					return list;
				}
			}
		} else {
			return null; // shouldn't really happen
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		if (args.length == 0) {
			sender.sendMessage(getUsage(sender));
			return true;
		} else {
			SubCommand cmd = subCommands.get(args[0]);
			if (cmd == null || !cmd.hasPermission(sender)) {
				if (cmd != null) {
					sender.sendMessage(br.getMessages().errorNoPermissions());
					return true;
				}
				sender.sendMessage(getUsage(sender));
				return true;
			}
			if (!cmd.onCommand(sender, Arrays.copyOfRange(args, 1, args.length), opts)) {
				sender.sendMessage(cmd.getUsage(sender));
			}
			return true;
		}
	}
	
	private String getUsage(CommandSender sender) {
		StringBuilder msg = new StringBuilder();
		for (SubCommand cmd : subCommands.values()) {
			if (cmd.hasPermission(sender)) {
				if (msg.length() > 0) {
					msg.append("\n");
				}
				msg.append(cmd.getUsage(sender));
			}
		}
		return msg.toString();
	}

}
