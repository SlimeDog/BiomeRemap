package me.ford.biomeremap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
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
import me.ford.biomeremap.hooks.PlaceholderAPIHook;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.populator.MappingPopulator;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;

public class BiomeRemap extends JavaPlugin {
	private static BiomeRemap staticInstance;
	private Messages messages;
	private Settings settings;
	private boolean testing = false;
	private BiomeRemapper remapper;
	private BiomeScanner scanner;

	// helpers 
	private boolean existsDataFolder;
	private boolean existsConfig;
	private boolean existsMessages;
	private boolean canReadDataFolder;
	private boolean canReadConfig;
	private boolean canReadMessages;
	
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
		staticInstance = this;
		
		messages = new Messages(this);
		attempConfigReloads(true);
		settings = new Settings(this);
		// commands
		getCommand("biomeremap").setExecutor(new BiomeRemapCommand(this));
		
		// saving debug message periodically
		this.getServer().getScheduler().runTaskTimer(this, () -> saveDebug(), 120 * 20L, 120 * 20L);
		
		// remapper, scanner
		remapper = new BiomeRemapper(this);
		scanner = new BiomeScanner();
		
		// setup up populator
		MappingPopulator populator = new MappingPopulator(remapper);
		for (World world : getServer().getWorlds()) {
			world.getPopulators().add(populator);
		}
		
		// hooks
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPIHook(this);
		} else {
			getLogger().warning("PlaceholderAPI not found!");
		}
	}
	
	@Override
	public void onDisable() {
		saveDebug();
	}

	private void attemptConfigReloads() {
		attempConfigReloads(false);
	}

	private void attempConfigReloads(boolean first) {
		File config = new File(getDataFolder(), "config.yml");
		File msgs = new File(getDataFolder(), "messages.yml");
		existsDataFolder = getDataFolder().exists();
		existsConfig = config.exists();
		existsMessages = msgs.exists();
		canReadDataFolder = getDataFolder().canRead();
		canReadConfig = config.canRead();
		canReadMessages = msgs.canRead();
		if (!canReadDataFolder || !canReadConfig || !canReadMessages) {
			getLogger().severe(getMessages().errorConfigUnreadable());
			if (!canReadConfig && !canReadMessages)	return;
		}
		if (!existsDataFolder || !existsConfig || !existsMessages) {
			if (!first) getLogger().warning(getMessages().warnConfigRecreated());
			if (!existsConfig) saveDefaultConfig();
			if (!existsMessages) messages.saveDefaultConfig();
		}
	}
	
	public boolean reload() {
		boolean success = true;
		attemptConfigReloads();
		if (canReadConfig) {
			reloadConfig();
			if (getConfig().getKeys(true).isEmpty()) success = false;
			if (success) settings.reload();
		}
		if (canReadMessages) {
			messages.reloadCustomConfig();
			if (messages.getCustomConfig().getKeys(true).isEmpty()) success = false;
		}
		if (success) {
			getLogger().info(getMessages().getInfoConfigLoaded());
		}
		return success;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public Messages getMessages() {
		return messages;
	}
	
	public BiomeRemapper getRemapper() {
		return remapper;
	}
	
	public BiomeScanner getScanner() {
		return scanner;
	}
	
	public void logMessage(String msg) {
		getServer().getConsoleSender().sendMessage(getMessages().getPrefix() + msg);
	}
	
	public static Logger logger() {
		return staticInstance.getLogger();
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
