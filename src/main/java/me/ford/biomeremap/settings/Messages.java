package me.ford.biomeremap.settings;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages extends CustomConfigHandler {
	private static final String FILE_NAME = "messages.yml";

	public Messages(JavaPlugin plugin) {
		super(plugin, FILE_NAME);
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
	
	public String getInfoWorldMapped(String world, String biome) {
		return getMessage("INFO_WORLD_BIOME_MAPPED", "World {WORLD_NAME} was successfully mapped to biomemap {BIOME_ID}")
						.replace("{WORLD_NAME}", world).replace("{BIOME_ID}", biome);
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
	
	public String errorBiomeNotFound(String biome) {
		return getMessage("ERROR_BIOME_NOT_FOUND", "Biome id {BIOME_ID} was not found.").replace("{BIOME_ID}", biome);
	}
	
	public String errorNoPermissions() {
		return getMessage("ERROR_NO_PERMISSION", "You do not have permission to execute that command.");
	}
	
	public String errorWorldNotFound(String worldName) {
		return getMessage("ERROR_WORLD_NOT_FOUND", "World name {WORLD_NAME} was not found.")
						.replace("{WORLD_NAME}", worldName);
	}
	
	public String getMessage(String path, String def) {
		return ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString(path, def));
	}

}
