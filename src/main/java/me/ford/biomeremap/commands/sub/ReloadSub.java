package me.ford.biomeremap.commands.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;

public class ReloadSub extends SubCommand {
	private static final String PERMS = "biomeremap.reload";
	private static final String USAGE = "/biomeremap reload";
	private final BiomeRemap br;
	
	public ReloadSub(BiomeRemap plugin) {
		super("reload");
		br = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, List<String> opts) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args, List<String> opts) {
		br.reload();
		sender.sendMessage(br.getMessages().getBiomeRemapReload());
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
