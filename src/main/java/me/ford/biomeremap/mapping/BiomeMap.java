package me.ford.biomeremap.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.settings.Messages;

public class BiomeMap {
	private final String name;
	private final String description;
	private final List<String> worldNames;
	private final Map<Biome, Biome> biomeMap = new HashMap<>();
	
	public BiomeMap(Messages messages, ConfigurationSection section) {
		name = section.getName();
		description = section.getString("description", "");
		worldNames = section.getStringList("enabled-worlds");
		
		// setup biome mapping
		ConfigurationSection mapSection = section.getConfigurationSection("biomes");
		if (mapSection == null || mapSection.getKeys(false).isEmpty()) {
			throw new IncompleteBiomeMapException();
		}
		for (String key : mapSection.getKeys(false)) {
			Biome from;
			try {
				from = Biome.valueOf(key);
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().severe(messages.errorBiomeNotFound(key));
				continue;
			}
			ConfigurationSection curSection = mapSection.getConfigurationSection(key);
			if (section == null) {
				BiomeRemap.logger().severe(messages.errorConfigMapincomplete(key, from.name()));
				throw new MappingException();
			}
			String toName = curSection.getString("replacement-biome", "");
			Biome to;
			try {
				to = Biome.valueOf(toName);
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().severe(messages.errorBiomeNotFound(toName));
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

	public boolean removeWorld(String worldName) {
		return worldNames.remove(worldName);
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

	public static final class IncompleteBiomeMapException extends IllegalStateException {

		private static final long serialVersionUID = 1L;

		public IncompleteBiomeMapException() {
			super("BiomeMap incomplete!");
		}

	}

	public static final class MappingException extends IllegalStateException {

		private static final long serialVersionUID = 1L;

		public MappingException() {
			super("Problem mapping biomes");
		}
		
	}

}
