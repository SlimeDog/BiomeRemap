package me.ford.biomeremap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

//import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.commands.BiomeRemapCommand;
//import me.ford.biomeremap.populator.MappingPopulator;
//import me.ford.biomeremap.listeners.ChunkListener;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemap extends JavaPlugin {
	private Messages messages;
	private Settings settings;
	
	@Override
	public void onEnable() {
//		getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
		saveDefaultConfig();
		settings = new Settings(this);
		messages = new Messages(this);
		messages.saveDefaultConfig();
//		MappingPopulator populator = new MappingPopulator();
//		for (World world : getServer().getWorlds()) {
//			world.getPopulators().add(populator);
//		}
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
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(
			                            new FileWriter(JavaPlugin.getPlugin(BiomeRemap.class).getDataFolder().getAbsolutePath() + File.separatorChar + "debug.log", true)  //Set true for append mode
			                        );
		    writer.newLine();   //Add new line
		    writer.write(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ": " + msg);
		    writer.close();
		} catch (IOException e) {
			logger().warning("Unable to save debug logging data!");
		} 
//		logger().info("[DEBUG]" + msg);
	}

}
