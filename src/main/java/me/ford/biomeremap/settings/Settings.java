package me.ford.biomeremap.settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import dev.ratas.slimedogcore.api.config.SDCConfiguration;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCSingleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.factory.SDCTripleContextMessageFactory;
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
				SDCSingleContextMessageFactory<String> msg = messages.errorBiomeMapIncomplete();
				br.getLogger().severe(msg.getMessage(msg.getContextFactory().getContext(key)).getFilled());
				continue;
			}
			BiomeMap map;
			try {
				map = new BiomeMap(messages, curMapSection);
			} catch (IncompleteBiomeMapException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorBiomeMapIncomplete();
				String filled = msg.getMessage(msg.getContextFactory().getContext(key)).getFilled();
				br.getLogger().severe(filled);
				issues.addIssue(filled);
				continue;
			} catch (MappingException e) {
				SDCSingleContextMessageFactory<String> msg = messages.errorNoBiomeMapAssigned();
				String filled = msg.getMessage(msg.getContextFactory().getContext(key)).getFilled();
				br.getLogger().severe(filled);
				issues.addIssue(filled);
				continue;
			} catch (IncompatibleFloorException e) {
				SDCDoubleContextMessageFactory<String, Integer> msg = messages.errorIncompatibleFloor();
				String filled = msg.getMessage(msg.getContextFactory().getContext(key, e.floor)).getFilled();
				br.getLogger().severe(filled);
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
					SDCSingleContextMessageFactory<String> msg = messages.errorDuplicateBiomeMapsForWorld();
					String filled = msg.getMessage(msg.getContextFactory().getContext(worldName)).getFilled();
					br.getLogger().severe(filled);
					issues.addIssue(filled);
				} else {
					if (br.getWorldProvider().getWorldByName(worldName) != null) {
						successes.add(worldName);
					} else {
						SDCSingleContextMessageFactory<String> msg = messages.errorWorldNotFound();
						String filled = msg.getMessage(msg.getContextFactory().getContext(worldName)).getFilled();
						br.getLogger().severe(filled);
						issues.addIssue(filled);
						map.removeWorld(worldName);
					}
				}
			}
		}
		successes.removeAll(duplicates);
		for (String worldName : successes) {
			BiomeMap map = worldMap.get(worldName);
			SDCDoubleContextMessageFactory<String, String> msg = messages.getInfoWorldMapped();
			String filled = msg.getMessage(msg.getContextFactory().getContext(worldName, map.getName())).getFilled();
			br.getLogger().info(filled);
			if (map.getFloor() != BiomeMap.DEFAULT_FLOOR) {
				SDCTripleContextMessageFactory<Integer, Integer, String> m = messages.getInfoChunkRemapFloor();
				String fill = m.getMessage(m.getContextFactory().getContext( map.getFloor(),BiomeMap.DEFAULT_FLOOR, worldName)).getFilled();
				br.getLogger().info(fill);
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
