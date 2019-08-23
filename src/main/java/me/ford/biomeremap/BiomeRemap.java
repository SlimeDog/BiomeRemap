package me.ford.biomeremap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import me.ford.biomeremap.commands.BiomeRemapCommand;
import me.ford.biomeremap.populator.MappingPopulator;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemap extends JavaPlugin {
	private static BiomeRemap staticInstance;
	private Messages messages;
	private Settings settings;
	private boolean testing = false;
	
	public BiomeRemap() {
		super();
	}
	
	protected BiomeRemap(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        testing = true;
    }

	
	@Override
	public void onEnable() {
		if (!testing) new Metrics(this);
		getLogger().info("Testing:" + testing);
		staticInstance = this;
		saveDefaultConfig();
		messages = new Messages(this);
		settings = new Settings(this);
		messages.saveDefaultConfig();
		MappingPopulator populator = new MappingPopulator();
		for (World world : getServer().getWorlds()) {
			world.getPopulators().add(populator);
		}
		// commands
		getCommand("biomeremap").setExecutor(new BiomeRemapCommand(this));
		
		// saving debug message periodically
		this.getServer().getScheduler().runTaskTimer(this, () -> saveDebug(), 120 * 20L, 120 * 20L);
	}
	
	@Override
	public void onDisable() {
		saveDebug();
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
	
	private static List<String> debugBuffer = new ArrayList<>();
	
	public static void debug(String msg) {
		debugBuffer.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ": " + msg);
		if (debugBuffer.size() > 20) {
			saveDebug();
		}
	}
	
	private static void saveDebug() {
		if (debugBuffer.isEmpty()) return;
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(
			                            new FileWriter(staticInstance.getDataFolder().getAbsolutePath() + File.separatorChar + "debug.log", true)  //Set true for append mode
			                        );
			writer.newLine();   //Add new line
		    writer.write(String.join("\n", debugBuffer));
		    writer.close();
		} catch (IOException e) {
			logger().warning("Unable to save debug logging data!");
		}
		debugBuffer.clear();
	}

}
