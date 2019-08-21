package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.commands.BiomeRemapCommand;
import me.ford.biomeremap.commands.SubCommand;

public class HelpSub extends SubCommand {
	private static final String PERMS = "biomeremap.use";
	private static final String USAGE = "/biomeremap help";
	private final BiomeRemapCommand base;
	
	public HelpSub(BiomeRemapCommand base) {
		this.base = base;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, String[] opts) {
		sender.sendMessage(String.format("/biomeremap <%s>", String.join("|", getAllowedSubCommands(sender))));
		return true;
	}
	
	private List<String> getAllowedSubCommands(CommandSender sender) {
		List<String> cmds = new ArrayList<>();
		for (Entry<String, SubCommand> cmd : base.getSubCommands().entrySet()) {
			if (cmd.getValue() != this && cmd.getValue().hasPermission(sender)) {
				cmds.add(cmd.getKey());
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
