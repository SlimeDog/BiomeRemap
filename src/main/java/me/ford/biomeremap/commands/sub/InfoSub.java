package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.mapping.BiomeMap;

public class InfoSub extends SubCommand {
	private static final String PERMS = "biomeremap.use"; // TODO - different perms?
	private static final String USAGE = "/biomeremap info <biomemap-id>";
	private final BiomeRemap br;
	
	public InfoSub(BiomeRemap plugin) {
		super("info");
		br = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], br.getSettings().getBiomeMapNames(), list);
		} else {
			return list;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		if (args.length < 1) {
			return false;
		}
		BiomeMap map = br.getSettings().getBiomeMap(args[0]);
		if (map == null) {
			sender.sendMessage(br.getMessages().errorBiomeNotFound(args[0]));
			return true;
		}
		sender.sendMessage(br.getMessages().getBiomeRemapInfo(map.getDescription(), map.getApplicableWorldNames()));
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
