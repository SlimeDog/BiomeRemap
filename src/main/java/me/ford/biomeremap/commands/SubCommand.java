package me.ford.biomeremap.commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand implements CommandWithOptions {
	private final String name;
	
	public SubCommand(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract boolean hasPermission(CommandSender sender);
	
	public abstract String getUsage(CommandSender sender);

}
