package me.ford.biomeremap.mapping;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

public final class BiomeScanner {
	
	private static BiomeScanner instance = null;
	
	private BiomeScanner() { }
	
	public static BiomeScanner getInstance() {
		if (instance == null) {
			instance = new BiomeScanner();
		}
		return instance;
	}
	
	public void addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		world.getChunkAt(chunkX, chunkZ);
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				addBiome(map, world.getBiome(x, z));
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
