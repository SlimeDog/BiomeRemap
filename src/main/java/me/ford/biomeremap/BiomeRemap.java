package me.ford.biomeremap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
import me.ford.biomeremap.mapping.TeleportListener;
import me.ford.biomeremap.populator.MappingPopulator;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;
import me.ford.biomeremap.settings.Settings.ReloadIssues;
import me.ford.biomeremap.updates.UpdateChecker;
import me.ford.biomeremap.volotile.BiomeManager;
import me.ford.biomeremap.volotile.ChunkUpdater;
import me.ford.biomeremap.volotile.VolotileBiomeManager;
import me.ford.biomeremap.volotile.VolotileChunkUpdater;

public class BiomeRemap extends JavaPlugin {
	private static BiomeRemap staticInstance;
	private Messages messages;
	private Settings settings;
	private boolean testing = false;
	private BiomeRemapper remapper;
	private BiomeScanner scanner;
	private TeleportListener teleListener;
	private MappingPopulator populator;
	private BiomeManager biomeManager;
	private ChunkUpdater chunkUpdater;

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
		staticInstance = this;

		messages = new Messages(this);
		attempConfigReloads(true);
		settings = new Settings(this);

		if (settings.enableMetrics() && !testing) {
			new Metrics(this);
		}

		// commands
		getCommand("biomeremap").setExecutor(new BiomeRemapCommand(this));

		// saving debug message periodically
		this.getServer().getScheduler().runTaskTimer(this, () -> saveDebug(), 120 * 20L, 120 * 20L);

		// remapper, scanner
		remapper = new BiomeRemapper(this);
		scanner = new BiomeScanner(this);
		teleListener = new TeleportListener(this);

		// NMS biome manager
		if (!testing) {
			try {
				biomeManager = new VolotileBiomeManager(this);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
				getLogger().log(Level.SEVERE, "Could not start volotile biome manager! Disabling plugin! ", e);
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			// NMS chunk updater
			try {
				chunkUpdater = new VolotileChunkUpdater(this);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
				getLogger().log(Level.SEVERE, "Could not start volotile chunk updater! Disabling plugin! ", e);
				e.printStackTrace();
			}
		}

		// setup up populator
		populator = new MappingPopulator(remapper);
		for (World world : getServer().getWorlds()) {
			world.getPopulators().add(populator);
		}

		// hooks
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPIHook(this);
		} else {
			getLogger().warning("PlaceholderAPI not found!");
		}

		// update
		if (settings.checkForUpdates()) {
			new UpdateChecker(this, (response, version) -> {
				switch (response) {
					case LATEST:
						logMessage(messages.updateCurrentVersion());
						break;
					case FOUND_NEW:
						logMessage(messages.updateNewVersionAvailable(version));
						break;
					case UNAVAILABLE:
						logMessage(messages.updateInfoUnavailable());
						break;
				}
			}).check();
		}
	}

	public MappingPopulator getPopulator() {
		return populator;
	}

	public BiomeManager getBiomeManager() {
		return biomeManager;
	}

	public ChunkUpdater getChunkUpdater() {
		return chunkUpdater;
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
		if ((!canReadDataFolder && existsDataFolder) || (!canReadConfig && existsConfig)
				|| (!canReadMessages && existsMessages)) {
			getLogger().severe(getMessages().errorConfigUnreadable());
			if (!canReadConfig && !canReadMessages)
				return;
		}
		if (!existsDataFolder || !existsConfig || !existsMessages) {
			if (!first)
				getLogger().warning(getMessages().warnConfigRecreated());
			if (!existsConfig) {
				saveDefaultConfig();
				canReadConfig = config.canRead();
			}
			if (!existsMessages) {
				messages.saveDefaultConfig();
				canReadMessages = msgs.canRead();
			}
		}
	}

	public ReloadIssues reload() {
		ReloadIssues issues = null;
		boolean success = false;
		attemptConfigReloads();
		if (canReadConfig) {
			reloadConfig();
			success = !getConfig().getKeys(true).isEmpty();
			if (success)
				issues = settings.reload();
		}
		if (canReadMessages) {
			messages.reloadCustomConfig();
			success = !messages.getCustomConfig().getKeys(true).isEmpty();
		}
		if (success && issues != null && !issues.hasIssues()) {
			getLogger().info(getMessages().getInfoConfigLoaded());
		}
		return success ? issues : null;
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

	public TeleportListener getTeleportListener() {
		return teleListener;
	}

	public void logMessage(String msg) {
		getServer().getConsoleSender().sendMessage(getMessages().getPrefix() + msg);
	}

	public static Logger logger() {
		return staticInstance.getLogger();
	}

	private static List<String> debugBuffer = new ArrayList<>();
	private static boolean debugIsSaving = false;

	public static void debug(String msg) {
		String debugMsg = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ": " + msg;
		if (debugIsSaving) {
			staticInstance.getServer().getScheduler().runTaskLater(staticInstance, () -> debugMessage(debugMsg), 1L);
			return;
		}
		debugMessage(debugMsg);
	}

	private static void debugMessage(String msg) {
		debugBuffer.add(msg);
		if (debugBuffer.size() > 20) {
			saveDebug();
		}
	}

	private static void saveDebug() {
		if (debugBuffer.isEmpty())
			return;
		debugIsSaving = true;
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(
					staticInstance.getDataFolder().getAbsolutePath() + File.separatorChar + "debug.log", true) // Set
																												// true
																												// for
																												// append
																												// mode
			);
			writer.newLine(); // Add new line
			writer.write(String.join("\n", debugBuffer));
			writer.close();
		} catch (IOException e) {
			logger().warning("Unable to save debug logging data!");
		}
		debugBuffer.clear();
		debugIsSaving = true;
	}

}
