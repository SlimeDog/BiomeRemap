package me.ford.biomeremap.settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.config.SDCConfiguration;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeMap.IncompatibleCeilingException;
import me.ford.biomeremap.mapping.BiomeMap.IncompatibleFloorException;
import me.ford.biomeremap.mapping.BiomeMap.IncompleteBiomeMapException;
import me.ford.biomeremap.mapping.BiomeMap.MappingException;

public class Settings {
	private final SlimeDogPlugin br;
	private final Map<String, BiomeMap> maps = new HashMap<>();
	private final Map<String, BiomeMap> worldMap = new HashMap<>();
	private final Messages messages;

	public Settings(SlimeDogPlugin plugin, Messages messages) {
		br = plugin;
		this.messages = messages;
		reload();
	}

	public ReloadIssues reload() {
		ReloadIssues issues = new ReloadIssues();
		maps.clear();
		worldMap.clear();
		SDCConfiguration mapsSection = br.getDefaultConfig().getConfig().getConfigurationSection("biomemaps");
		for (String key : mapsSection.getKeys(false)) {
			SDCConfiguration curMapSection = mapsSection.getConfigurationSection(key);
			if (curMapSection == null) {
				br.getLogger().severe(messages.errorBiomeMapIncomplete(key));
				continue;
			}
			BiomeMap map;
			try {
				map = new BiomeMap(messages, curMapSection);
			} catch (IncompleteBiomeMapException e) {
				br.getLogger().severe(messages.errorBiomeMapIncomplete(key));
				issues.addIssue(messages.errorBiomeMapIncomplete(key));
				continue;
			} catch (MappingException e) {
				br.getLogger().severe(messages.errorNoBiomeMapAssigned(key));
				issues.addIssue(messages.errorNoBiomeMapAssigned(key));
				continue;
			} catch (IncompatibleFloorException e) {
				br.getLogger().severe(messages.errorIncompatibleFloor(key, e.floor));
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
					br.getLogger().severe(messages.errorDuplicateBiomeMapsForWorld(worldName));
					issues.addIssue(messages.errorDuplicateBiomeMapsForWorld(worldName));
				} else {
					if (br.getWorldProvider().getWorldByName(worldName) != null) {
						successes.add(worldName);
					} else {
						br.getLogger().severe(messages.errorWorldNotFound(worldName));
						issues.addIssue(messages.errorWorldNotFound(worldName));
						map.removeWorld(worldName);
					}
				}
			}
		}
		successes.removeAll(duplicates);
		for (String worldName : successes) {
			BiomeMap map = worldMap.get(worldName);
			br.getLogger().info(messages.getInfoWorldMapped(worldName, map.getName()));
			if (map.getFloor() != BiomeMap.DEFAULT_FLOOR) {
				br.getLogger().info(messages.getInfoChunkRemapFloor(map.getFloor(), BiomeMap.DEFAULT_FLOOR, worldName));
			}
		}
		for (String worldName : duplicates) { // otherwise the third (or 5th, so on) duplicate would stay
			worldMap.remove(worldName);
		}
		return issues;
	}

	public String getVersion() {
		return br.getDefaultConfig().getConfig().getString("version");
	}

	public boolean checkForUpdates() {
		return br.getDefaultConfig().getConfig().getBoolean("check-for-updates");
	}

	public boolean enableMetrics() {
		return br.getDefaultConfig().getConfig().getBoolean("enable-metrics");
	}

	public Set<String> getBiomeMapNames() {
		return maps.keySet();
	}

	public boolean debugAutoremap() {
		return br.getDefaultConfig().getConfig().getBoolean("debug-autoremap", false);
	}

	public int getRegionRemapProgressStep() {
		return br.getDefaultConfig().getConfig().getInt("report-region-remap-progress", 5);
	}

	public int getScanProgressStep() {
		return br.getDefaultConfig().getConfig().getInt("report-region-scan-progress", 0);
	}

	public BiomeMap getBiomeMap(String name) {
		return maps.get(name);
	}

	public BiomeMap getApplicableBiomeMap(String worldName) {
		return worldMap.get(worldName);
	}

	public long getTeleportCacheTime() {
		return br.getDefaultConfig().getConfig().getLong("teleport-cache-time-ticks", 20L);
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
