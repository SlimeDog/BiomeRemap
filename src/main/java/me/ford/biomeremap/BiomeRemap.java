package me.ford.biomeremap;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.populator.MappingPopulator;
//import me.ford.biomeremap.listeners.ChunkListener;
import me.ford.biomeremap.settings.CustomConfigHandler;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemap extends JavaPlugin {
	private CustomConfigHandler messageConfig;
	private Settings settings;
	
	@Override
	public void onEnable() {
//		getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
		settings = new Settings(this);
		messageConfig = new CustomConfigHandler(this, "messages.yml");
		MappingPopulator populator = new MappingPopulator();
		for (World world : getServer().getWorlds()) {
			world.getPopulators().add(populator);
		}
	}
	
	public void reload() {
		reloadConfig();
		settings.reload();
		messageConfig.reloadCustomConfig();
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public static Logger logger() {
		return JavaPlugin.getPlugin(BiomeRemap.class).getLogger();
	}
	
	public static void debug(String msg) {
		logger().info("[DEBUG]" + msg);
	}

}
