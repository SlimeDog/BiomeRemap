package me.ford.biomeremap.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeMap.IncompleteBiomeMapException;

public class Settings {
	private final BiomeRemap br;
	private final Map<String, BiomeMap> maps = new HashMap<>();
	private final Map<String, BiomeMap> worldMap = new HashMap<>();
	
	public Settings(BiomeRemap plugin) {
		br = plugin;
		reload();
	}
	
	public void reload() {
		maps.clear();
		worldMap.clear();
		ConfigurationSection mapsSection = br.getConfig().getConfigurationSection("biomemaps"); 
		for (String key : mapsSection.getKeys(false)) {
			ConfigurationSection curMapSection = mapsSection.getConfigurationSection(key);
			if (curMapSection == null) {
				br.getLogger().severe(br.getMessages().errorBiomeMapIncomplete(key));
				continue;
			}
			BiomeMap map;
			try {
				map = new BiomeMap(br.getMessages(), curMapSection);
			} catch (IncompleteBiomeMapException e) {
				br.getLogger().severe(br.getMessages().errorBiomeMapIncomplete(key));
				continue;
			}
			maps.put(key, map);
		}
		Set<String> duplicates = new HashSet<>();
		List<BiomeMap> mapsWithDuplicateWorlds = new ArrayList<>();
		Set<String> successes = new HashSet<>();
		for (BiomeMap map : maps.values()) {
			for (String worldName : map.getApplicableWorldNames()) {
				BiomeMap prev = worldMap.put(worldName, map);
				if (prev != null) {
					duplicates.add(worldName);
					mapsWithDuplicateWorlds.add(prev);
					mapsWithDuplicateWorlds.add(map);
					br.getLogger().severe(br.getMessages().errorDuplicateBiomeMapsForWorld(worldName));
				} else {
					successes.add(worldName);
				}
			}
		}
		for (String worldName : duplicates) {
			for (BiomeMap map : mapsWithDuplicateWorlds) {
				map.removeWorld(worldName);
			}
		}
		successes.removeAll(duplicates);
		for (String worldName : successes) {
			br.logMessage(br.getMessages().getInfoWorldMapped(worldName, worldMap.get(worldName).getName()));
		}
		for (String worldName : duplicates) { //otherwise the third (or 5th, so on) duplicate would stay
			worldMap.remove(worldName);
		}
	}
	
	public String getVersion() {
		return br.getConfig().getString("version");
	}
	
	public boolean checkForUpdates() {
		return br.getConfig().getBoolean("check-for-updates");
	}
	
	public boolean enableMetrics() {
		return br.getConfig().getBoolean("enable-metrics");
	}
	
	public Set<String> getBiomeMapNames() {
		return maps.keySet();
	}
	
	public boolean debugAutoremap() {
		return br.getConfig().getBoolean("debug-autoremap", false);
	}
	
	public int getRegionRemapProgressStep() {
		return br.getConfig().getInt("report-region-remap-progress", 5);
	}
	
	public int getScanProgressStep() {
		return br.getConfig().getInt("report-region-scan-progress", 0);
	}
	
	public BiomeMap getBiomeMap(String name) {
		return maps.get(name);
	}
	
	public BiomeMap getApplicableBiomeMap(String worldName) {
		return worldMap.get(worldName);
	}

}
