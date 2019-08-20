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
		return getMessage("BIOMEREMAP_INFO", "Description: {BIOMEREMAP_DESC} \nWorlds: {BIOMEREMAP_WORLDS}")
						.replace("{BIOMEREMAP_DESC}", description)
						.replace("{BIOMEREMAP_WORLDS}", String.join(", ", worldNames));
	}
	
	public String getBiomeRemapListHeaders() {
		return getMessage("BIOMEREMAP_LIST_HEADER", "List of biomemaps:");
	}
	
	public String getBiomeRemapListItem(String biome) {
		return getMessage("BIOMEREMAP_LIST_ITEM" ," - {BIOME_ID}").replace("{BIOME_ID}", biome);
	}
	
	public String getBiomeRemapReload() {
		return getMessage("BIOMEREMAP_RELOAD", "BiomeRemap was reloaded successfully.");
	}
	
	public String getBiomeRemapComplete() {
		return getMessage("BIOMEREMAP_REMAP_COMPLETE", "Remap complete.");
	}
	
	public String errorBiomeNotFound(String biome) {
		return getMessage("ERROR_BIOME_NOT_FOUND", "Biome id {BIOME_ID} was not found.").replace("{BIOME_ID}", biome);
	}
	
	public String errorNoPermissions() {
		return getMessage("ERROR_NO_PERMISSION", "You do not have permission to execute that command.");
	}
	
	public String errorWorldNotFound() {
		return getMessage("ERROR_WORLD_NOT_FOUND", "World name {WORLD_NAME} was not found.");
	}
	
	public String e(String world, String biome) {
		return getMessage("WORLD_BIOMEREMAP_MAP_INFO", "World {WORLD_NAME} was successfully mapped to biomemap {BIOME_ID}")
						.replace("{WORLD_NAME}", world)
						.replace("{BIOME_ID}", biome);
	}
	
	public String getMessage(String path, String def) {
		return ChatColor.translateAlternateColorCodes('&', getCustomConfig().getString(path, def));
	}

}
