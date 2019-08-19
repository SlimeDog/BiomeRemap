package me.ford.biomeremap.commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand implements CommandWithOptions {
	
	public abstract boolean hasPermission(CommandSender sender);
	
	public abstract String getUsage(CommandSender sender);

}
