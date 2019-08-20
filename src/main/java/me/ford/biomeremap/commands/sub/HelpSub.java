package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ford.biomeremap.commands.SubCommand;

public class HelpSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap help";

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		sender.sendMessage("What else should I put here other than the usage information?"); // TODO - additions?
		return false;
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
