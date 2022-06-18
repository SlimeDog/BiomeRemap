package me.ford.biomeremap.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Biome;

import dev.ratas.slimedogcore.api.config.SDCConfiguration;
import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.settings.Messages;

public class BiomeMap {
	public static final int DEFAULT_FLOOR = -64;
	private static final int MIN_FLOOR = -64;
	private static final int MAX_FLOOR = 0;
	private static final int DEFAULT_CEILING = 320;
	private final String name;
	private final String description;
	private final List<String> worldNames;
	private final Map<Biome, Biome> biomeMap = new HashMap<>();
	private final int floor;
	private final int ceiling;

	public BiomeMap(Messages messages, SDCConfiguration section) {
		name = section.getName();
		description = section.getString("description", "");
		worldNames = section.getStringList("enabled-worlds");

		// setup biome mapping
		SDCConfiguration mapSection = section.getConfigurationSection("biomes");
		if (mapSection == null || mapSection.getKeys(false).isEmpty()) {
			throw new IncompleteBiomeMapException();
		}
		for (String key : mapSection.getKeys(false)) {
			Biome from;
			try {
				from = Biome.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().severe(messages.errorBiomeNotFound(key));
				throw new MappingException();
			}
			SDCConfiguration curSection = mapSection.getConfigurationSection(key);
			if (curSection == null) {
				BiomeRemap.logger().severe(messages.errorConfigMapincomplete(key, from.name()));
				throw new MappingException();
			}
			String toName = curSection.getString("replacement-biome", "");
			Biome to;
			try {
				to = Biome.valueOf(toName.toUpperCase());
			} catch (IllegalArgumentException e) {
				BiomeRemap.logger().severe(messages.errorBiomeNotFound(toName));
				throw new MappingException();
			}
			biomeMap.put(from, to);
		}
		floor = section.getInt("floor", DEFAULT_FLOOR);
		if (floor < MIN_FLOOR || floor > MAX_FLOOR) {
			throw new IncompatibleFloorException(floor);
		}
		ceiling = DEFAULT_CEILING; // section.getInt("ceiling", DEFAULT_CEILING);
		if (ceiling > DEFAULT_CEILING || ceiling <= floor) {
			throw new IncompatibleCeilingException(floor, ceiling);
		}
	}

	public int getFloor() {
		return floor;
	}

	public int getCeiling() {
		return ceiling;
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

	public static final class IncompatibleFloorException extends IllegalStateException {

		private static final long serialVersionUID = 1L;

		public final int floor;

		public IncompatibleFloorException(int floor) {
			super(String.format("Floor should be between %d and %d, found %d", MIN_FLOOR, MAX_FLOOR, floor));
			this.floor = floor;
		}

	}

	public static final class IncompatibleCeilingException extends IllegalStateException {

		private static final long serialVersionUID = 1L;

		public final int floor, ceiling;

		public IncompatibleCeilingException(int floor, int ceiling) {
			super(String.format("Ceiling should be above the floor (%d) and not above max (%d), found %d", floor,
					DEFAULT_CEILING, ceiling));
			this.floor = floor;
			this.ceiling = ceiling;
		}

	}

}
