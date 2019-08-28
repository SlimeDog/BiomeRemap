package me.ford.biomeremap.settings;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages extends CustomConfigHandler {
	private static final String FILE_NAME = "messages.yml";

	public Messages(JavaPlugin plugin) {
		super(plugin, FILE_NAME);
	}

	public String getPrefix() {
		return getMessage("BIOMEREMAP_PREFIX", "[BiomeRemap] ");
	}
	
	public String getBiomeRemapInfo(String description, List<String> worldNames) {
		String wn = worldNames.isEmpty() ? "none" : String.join(", ", worldNames);
		return getMessage("BIOMEREMAP_INFO", "Description: {BIOMEREMAP_DESC} \nWorlds: {BIOMEREMAP_WORLDS}")
						.replace("{BIOMEREMAP_DESC}", description)
						.replace("{BIOMEREMAP_WORLDS}", wn);
	}
	
	public String getBiomeRemapListHeaders() {
		return getMessage("BIOMEREMAP_LIST_HEADER", "List of biomemaps:");
	}
	
	public String getBiomeRemapNoMap(String world) {
		return getMessage("BIOMEREMAP_LIST_NO_MAP", "No biomemap found for world {WORLD_NAME}")
						.replace("{WORLD_NAME}", world);
	}
	
	public String getBiomeRemapListItem(String biome) {
		return getMessage("BIOMEREMAP_LIST_ITEM" ," - {BIOME_ID}").replace("{BIOME_ID}", biome);
	}
	
	public String getBiomeRemapReload() {
		return getMessage("BIOMEREMAP_RELOAD", "BiomeRemap was reloaded successfully.");
	}
	
	public String getChunkRemapStarted(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_REMAP_CHUNK_START", "Remapping chunk world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getBiomeRemapInPrgoress() {
		return getMessage("BIOMEREMAP_REMAP_IN_PROGRESS", "A biome remap is already in progress; please try again in a few minutes");
	}
	
	public String getRegionRemapStarted(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_REMAP_REGION_START", "Remapping region world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getBiomeRemapComplete() {
		return getMessage("BIOMEREMAP_REMAP_COMPLETE", "Remap complete.");
	}
	
	public String getBiomeRemapProgress(String progress) {
		return getMessage("BIOMEREMAP_REMAP_PROGRESS", "{PERCENTAGE}")
						.replace("{PERCENTAGE}", progress);
	}
	
	public String getBiomeRemapSummary(int chunks, long ms, int ticks) {
		return getMessage("BIOMEREMAP_REMAP_SUMMARY", "Remapped {CHUNKS} chunks in {MILLISECONDS} ms in a total of {TICKS} ticks")
						.replace("{CHUNKS}", String.valueOf(chunks))
						.replace("{MILLISECONDS", String.valueOf(ms))
						.replace("{TICKS}", String.valueOf(ticks));
	}

	public String getInfoConfigLoaded() {
		return getMessage("INFO_CONFIG_FILES_LOADED_SUCCESSFULLY", "Configuration files loaded successfully");
	}
	
	public String getInfoWorldMapped(String world, String biomemap) {
		return getMessage("INFO_WORLD_BIOME_MAPPED", "Biomemap {BIOMEMAP} was successfully assigned to world {WORLD_NAME}")
						.replace("{WORLD_NAME}", world).replace("{BIOMEMAP}", biomemap);
	}
	
	public String getScanInProgress() {
		return getMessage("BIOMEREMAP_SCAN_IN_PROGRESS", "A biome scan is already in progress; please try again in a few minutes");
	}
	
	public String getScanChunkStart(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_SCAN_CHUNK_START", "Scanning chunk world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getScanRegionStart(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_SCAN_REGION_START", "Scanning region world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getScanProgress(String progress) {
		return getMessage("BIOMEREMAP_SCAN_PROGRESS", "{PERCENTAGE}").replace("{PERCENTAGE}", progress);
	}
	
	public String getScanComplete() {
		return getMessage("BIOMEREMAP_SCAN_COMPLETE", "Scan complete");
	}
	
	public String getScanChunkHeader(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_SCAN_CHUNK_HEADER", "Biomes in chunk world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getScanRegionHeader(String worldName, int x, int z) {
		return getMessage("BIOMEREMAP_SCAN_REGION_HEADER", "Biomes in region world:{WORLD_NAME} x:{X} z:{Z}")
						.replace("{WORLD_NAME}", worldName).replace("{X}", String.valueOf(x)).replace("{Z}", String.valueOf(z));
	}
	
	public String getScanListItem(String percentage, String biome) {
		return getMessage("BIOMEREMAP_SCAN_LIST_ITEM", "{PERCENTAGE} {BIOME_ID}")
						.replace("{PERCENTAGE}", percentage).replace("{BIOME_ID}", biome);
	}
	
	public String warnConfigRecreated() {
		return getMessage("WARN_CONFIG_FILES_RECREATED", "Configuration files do not exist; default files were created");
	}

	public String errorBiomeMapIncomplete(String map) {
		return getMessage("ERROR_BIOMEMAP_INCOMPLETE", "Biomemap {BIOMEMAP} definition is incomplete")
						.replace("{BIOMEMAP}",map);
	}
	
	public String errorBiomeNotFound(String biome) {
		return getMessage("ERROR_BIOME_NOT_FOUND", "Biome {BIOME_ID} does not exist").replace("{BIOME_ID}", biome);
	}
	
	public String errorBiomeMapNotFound(String mapName) {
		return getMessage("ERROR_BIOMEMAP_NOT_FOUND", "Biomemap {BIOMEMAP} does not exist").replace("{BIOMEMAP}", mapName);
	}
	
	public String errorNoPermissions() {
		return getMessage("ERROR_NO_PERMISSION", "You do not have permission to execute that command.");
	}
	
	public String errorWorldNotFound(String worldName) {
		return getMessage("ERROR_WORLD_NOT_FOUND", "World name {WORLD_NAME} was not found.")
						.replace("{WORLD_NAME}", worldName);
	}
	
	public String errorNotInteger(String value) {
		return getMessage("ERROR_PARAMETER_INVALID_INTEGER", "{VALUE} is not a value integer")
						.replace("{VALUE}", value);
	}
	
	public String errorDuplicateBiomeMapsForWorld(String worldName) {
		return getMessage("ERROR_WORLD_DUPLICATE_ASSIGNMENT", "Multiple biomemaps are assigned to world {WORLD_NAME}; fix configuration and reload")
						.replace("{WORLD_NAME}", worldName);
	}
	
	public String errorConfigUnreadable() {
		return getMessage("ERROR_CONFIG_FILE_UNREADABLE", "Cannot read config.yml; no biomemaps were assigned to worlds");
	}

	public String errorConfigMapincomplete(String map, String biome) {
		return getMessage("ERROR_CONFIG_MAP_INCOMPLETE", "Biomemap {BIOMEMAP} has incomplete map for biome {BIOME_ID}; fix configuration and reload")
						.replace("{BIOMEMAP}", map).replace("{BIOME_ID}", biome);
	}

	public String errorNoBiomeMapAssigned(String map) {
		return getMessage("ERROR_NO_BIOMEMAP_ASSIGNMENT", "Errors were found in biomemap {BIOMEMAP}; biomemap was not assigned to any worlds")
						.replace("{BIOMEMAP}", map);
	}
	
	public String getMessage(String path, String def) {
		return ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString(path, def));
	}

}
