package me.ford.biomeremap.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface CommandWithOptions {

	public List<String> onTabComplete(CommandSender sender, String[] args, String[] opts);
	
	public boolean onCommand(CommandSender sender, String[] args, String[] opts);

}
