package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.util.StringUtil;

import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class InfoSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap info <biomemap-id>";
	private final Settings settings;
	private final Messages messages;

	public InfoSub(Settings settings, Messages messages) {
		super("info");
		this.settings = settings;
		this.messages = messages;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], settings.getBiomeMapNames(), list);
		} else {
			return list;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		if (args.length < 1) {
			return false;
		}
		BiomeMap map = settings.getBiomeMap(args[0]);
		if (map == null) {
			sender.sendMessage(messages.errorBiomeMapNotFound(args[0]));
			return true;
		}
		sender.sendMessage(messages.getBiomeRemapInfo(map.getDescription(), map.getApplicableWorldNames()));
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
