package me.ford.biomeremap.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import me.ford.biomeremap.BiomeRemap;

public class BiomeMap {
	private final String name;
	private final String description;
	private final List<String> worldNames;
	private final Map<Biome, Biome> biomeMap = new HashMap<>();
	private final String prefix;
	
	public BiomeMap(ConfigurationSection section) {
		name = section.getName();
		description = section.getString("description", "");
		worldNames = section.getStringList("enabled-worlds");
		prefix = "[BiomeMap:" + name + "] ";
		
		// setup biome mapping
		ConfigurationSection mapSection = section.getConfigurationSection("biomes");
		if (mapSection == null) {
			BiomeRemap.logger().warning(prefix + "No mapping of biomes detected!");
			return;
		}
		for (String key : mapSection.getKeys(false)) {
			Biome from;
			try {
				from = Biome.valueOf(key);
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().warning(prefix + "Unrecognized Biome Enum defined: " + key);
				continue;
			}
			ConfigurationSection curSection = mapSection.getConfigurationSection(key);
			if (section == null) {
				continue;
			}
			String toName = curSection.getString("replacement-biome");
			Biome to;
			try {
				to = Biome.valueOf(toName);
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().warning(prefix + "Unrecognized Biome Enum defined as replacement: " + toName);
				continue;
			}
			biomeMap.put(from, to);
		}
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<String> getApplicableWorldNames() {
		return new ArrayList<>(worldNames);
	}
	
	public Map<Biome, Biome> getMapping() {
		return new HashMap<>(biomeMap);
	}
	
	public Biome getBiomeFor(Biome biome) {
		return biomeMap.get(biome);
	}

}
