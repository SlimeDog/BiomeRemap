package me.ford.biomeremap;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.commands.BiomeRemapCommand;
import me.ford.biomeremap.populator.MappingPopulator;
//import me.ford.biomeremap.listeners.ChunkListener;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemap extends JavaPlugin {
	private Messages messages;
	private Settings settings;
	
	@Override
	public void onEnable() {
//		getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
		settings = new Settings(this);
		messages = new Messages(this);
		MappingPopulator populator = new MappingPopulator();
		for (World world : getServer().getWorlds()) {
			world.getPopulators().add(populator);
		}
		// commands
		getCommand("biomeremap").setExecutor(new BiomeRemapCommand(this));
	}
	
	public void reload() {
		reloadConfig();
		settings.reload();
		messages.reloadCustomConfig();
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public Messages getMessages() {
		return messages;
	}
	
	public static Logger logger() {
		return JavaPlugin.getPlugin(BiomeRemap.class).getLogger();
	}
	
	public static void debug(String msg) {
		logger().info("[DEBUG]" + msg);
	}

}
