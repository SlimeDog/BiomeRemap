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
import org.bukkit.plugin.java.JavaPluginLoader;

import dev.ratas.slimedogcore.impl.SlimeDogCore;
import dev.ratas.slimedogcore.impl.utils.UpdateChecker;
import me.ford.biomeremap.commands.BiomeRemapCommand;
import me.ford.biomeremap.hooks.PlaceholderAPIHook;
import me.ford.biomeremap.mapping.BiomeRemapper;
import me.ford.biomeremap.mapping.BiomeScanner;
import me.ford.biomeremap.mapping.TeleportListener;
import me.ford.biomeremap.populator.MappingPopulator;
import me.ford.biomeremap.settings.Messages;
import me.ford.biomeremap.settings.Settings;
import me.ford.biomeremap.settings.Settings.ReloadIssues;
import me.ford.biomeremap.volotile.APIBiomeManager;
import me.ford.biomeremap.volotile.BiomeManager;

public class BiomeRemap extends SlimeDogCore {
	private static final int SPIGOT_RESOURCE_ID = 70973;
	private static BiomeRemap staticInstance;
	private final Logger logger;
	private Messages messages;
	private Settings settings;
	private boolean testing = false;
	private BiomeRemapper remapper;
	private BiomeScanner scanner;
	private TeleportListener teleListener;
	private MappingPopulator populator;
	private BiomeManager biomeManager;

	// helpers
	private boolean existsDataFolder;
	private boolean existsConfig;
	private boolean existsMessages;
	private boolean canReadDataFolder;
	private boolean canReadConfig;
	private boolean canReadMessages;

	public BiomeRemap() {
		super();
		this.logger = null;
	}

	protected BiomeRemap(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		this(loader, description, dataFolder, file, null);
	}

	protected BiomeRemap(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file,
			Logger logger) {
		super(loader, description, dataFolder, file);
		this.logger = logger;
		testing = true;
	}

	@Override
	public void pluginEnabled() {
		staticInstance = this;

		messages = new Messages(this);
		attempConfigReloads(true);
		settings = new Settings(this, messages);

		if (settings.enableMetrics() && !testing) {
			new Metrics(this, 5513);
		}

		// saving debug message periodically
		this.getScheduler().runTaskTimer(() -> saveDebug(), 120 * 20L, 120 * 20L);

		// remapper, scanner
		remapper = new BiomeRemapper(this, settings, messages, () -> scanner, () -> teleListener);
		scanner = new BiomeScanner(settings);
		teleListener = new TeleportListener(this, settings);

		// commands
		getCommand("biomeremap").setExecutor(new BiomeRemapCommand(this, settings, messages, remapper, scanner));

		// NMS biome manager
		if (!testing) {
			biomeManager = new APIBiomeManager();
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
			}, SPIGOT_RESOURCE_ID).check();
		}
	}

	public MappingPopulator getPopulator() {
		return populator;
	}

	public BiomeManager getBiomeManager() {
		return biomeManager;
	}

	@Override
	public void pluginDisabled() {
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
			messages.reloadConfig();
			success = !messages.isEmpty();
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
			staticInstance.getScheduler().runTaskLater(() -> debugMessage(debugMsg), 1L);
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

	@Override
	public Logger getLogger() {
		return logger != null ? logger : super.getLogger();
	}

}
