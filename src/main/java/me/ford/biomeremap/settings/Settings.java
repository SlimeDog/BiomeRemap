package me.ford.biomeremap.settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeMap.IncompatibleCeilingException;
import me.ford.biomeremap.mapping.BiomeMap.IncompatibleFloorException;
import me.ford.biomeremap.mapping.BiomeMap.IncompleteBiomeMapException;
import me.ford.biomeremap.mapping.BiomeMap.MappingException;

public class Settings {
	private final BiomeRemap br;
	private final Map<String, BiomeMap> maps = new HashMap<>();
	private final Map<String, BiomeMap> worldMap = new HashMap<>();

	public Settings(BiomeRemap plugin) {
		br = plugin;
		reload();
	}

	public ReloadIssues reload() {
		ReloadIssues issues = new ReloadIssues();
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
				issues.addIssue(br.getMessages().errorBiomeMapIncomplete(key));
				continue;
			} catch (MappingException e) {
				br.getLogger().severe(br.getMessages().errorNoBiomeMapAssigned(key));
				issues.addIssue(br.getMessages().errorNoBiomeMapAssigned(key));
				continue;
			} catch (IncompatibleFloorException e) {
				br.getLogger().severe(br.getMessages().errorIncompatibleFloor(key, e.floor));
				continue;
			} catch (IncompatibleCeilingException e) {
				br.getLogger().severe("Problem with ceiling of biome map (this should not happen!)");
				e.printStackTrace();
				continue;
			}
			maps.put(key, map);
		}
		Set<String> duplicates = new HashSet<>();
		Set<String> successes = new HashSet<>();
		for (BiomeMap map : maps.values()) {
			for (String worldName : map.getApplicableWorldNames()) {
				BiomeMap prev = worldMap.put(worldName, map);
				if (prev != null) {
					duplicates.add(worldName);
					prev.removeWorld(worldName);
					map.removeWorld(worldName);
					br.getLogger().severe(br.getMessages().errorDuplicateBiomeMapsForWorld(worldName));
					issues.addIssue(br.getMessages().errorDuplicateBiomeMapsForWorld(worldName));
				} else {
					if (br.getServer().getWorld(worldName) != null) {
						successes.add(worldName);
					} else {
						br.getLogger().severe(br.getMessages().errorWorldNotFound(worldName));
						issues.addIssue(br.getMessages().errorWorldNotFound(worldName));
						map.removeWorld(worldName);
					}
				}
			}
		}
		successes.removeAll(duplicates);
		for (String worldName : successes) {
			BiomeMap map = worldMap.get(worldName);
			br.logMessage(br.getMessages().getInfoWorldMapped(worldName, map.getName()));
			if (map.getFloor() != BiomeMap.DEFAULT_FLOOR) {
				br.logMessage(br.getMessages().getInfoChunkRemapFloor(map.getFloor(), worldName));
			}
		}
		for (String worldName : duplicates) { // otherwise the third (or 5th, so on) duplicate would stay
			worldMap.remove(worldName);
		}
		return issues;
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

	public long getTeleportCacheTime() {
		return br.getConfig().getLong("teleport-cache-time-ticks", 20L); // TODO - add and/or change config path
	}

	public static class ReloadIssues {
		private Set<String> errors = new HashSet<>();

		public void addIssue(String name) {
			errors.add(name);
		}

		public Set<String> getIssues() {
			return new HashSet<>(errors);
		}

		public boolean hasIssues() {
			return !errors.isEmpty();
		}

	}

}
