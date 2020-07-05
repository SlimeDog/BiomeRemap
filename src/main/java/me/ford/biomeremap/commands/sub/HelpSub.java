package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.commands.BiomeRemapCommand;
import me.ford.biomeremap.commands.SubCommand;

public class HelpSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap help";
	private final BiomeRemapCommand base;

	public HelpSub(BiomeRemapCommand base) {
		super("help");
		this.base = base;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		sender.sendMessage(String.format("/biomeremap <%s>", String.join("|", getAllowedSubCommands(sender))));
		return true;
	}

	private List<String> getAllowedSubCommands(CommandSender sender) {
		List<String> cmds = new ArrayList<>();
		for (SubCommand cmd : base.getSubCommands()) {
			if (cmd != this && cmd.hasPermission(sender)) {
				cmds.add(cmd.getName());
			}
		}
		return cmds;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(PERMS) || sender instanceof ConsoleCommandSender;
	}

	@Override
	public String getUsage(CommandSender sender) {
		return USAGE;
	}

}
