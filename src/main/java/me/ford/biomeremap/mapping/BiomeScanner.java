package me.ford.biomeremap.mapping;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;

public final class BiomeScanner {
	private final BiomeRemap br;

	public BiomeScanner(BiomeRemap br) {
		this.br = br;
	}

	public boolean addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ) {
		return addBiomesFor(map, world, chunkX, chunkZ, 0, false);
	}

	public boolean addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ, int yLayer,
			boolean useNMS) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		if (!world.isChunkGenerated(chunkX, chunkZ)) {
			world.getChunkAt(chunkX, chunkZ);
			if (br.getSettings().getApplicableBiomeMap(world.getName()) == null) { // world not being remapped
				addBiomesForInternal(world, chunkX, chunkZ, useNMS, startX, startZ, yLayer, map);
				return true;
			}
			// allow populator to do its thing -> OnMappingDone counts them
			return false;
		} else {
			addBiomesForInternal(world, chunkX, chunkZ, useNMS, startX, startZ, yLayer, map);
			return true;
		}
	}

	private void addBiomesForInternal(World world, int chunkX, int chunkZ, boolean useNMS, int startX, int startZ,
			int yLayer, Map<Biome, Integer> map) {
		world.getChunkAt(chunkX, chunkZ);
		if (!useNMS) {
			addBiomes(world, startX, startZ, yLayer, map);
		} else {
			addBiomesNMS(world.getChunkAt(chunkX, chunkZ), map, yLayer);
		}
	}

	private void addBiomes(World world, int startX, int startZ, int yLayer, Map<Biome, Integer> map) {
		for (int x = startX; x < startX + 16; x++) { // not sure why, but I still need to set the data for all the
														// positions
			for (int z = startZ; z < startZ + 16; z++) { // not sure why, but I still need to set the data for all the
															// positions
				addBiome(map, world.getBiome(x, yLayer, z)); // TODO - in the future, we might need to change/get biomes
																// at other y values, but for now only y=0 has an effect
			}
		}
	}

	private void addBiomesNMS(org.bukkit.Chunk chunk, Map<Biome, Integer> map, int yLayer) {
		// 16 per ylayer
		final int startNr = yLayer << 4;
		for (int nr = startNr; nr < startNr + 16; nr++) {
			try {
				addBiome(map, br.getBiomeManager().getBiomeNMS(chunk, nr));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				br.getLogger().severe("Problem getting biome!");
				e.printStackTrace();
				continue;
			}
		}
	}

	private void addBiome(Map<Biome, Integer> map, Biome biome) {
		Integer cur = map.get(biome);
		if (cur == null) {
			cur = 0;
		}
		cur++;
		map.put(biome, cur);
	}

}
