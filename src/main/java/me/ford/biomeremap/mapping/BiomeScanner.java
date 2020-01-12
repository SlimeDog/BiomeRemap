package me.ford.biomeremap.mapping;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

public final class BiomeScanner {
	
	public void addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ) {
		addBiomesFor(map, world, chunkX, chunkZ, 0);
	}
	
	public void addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ, int yLayer) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		world.getChunkAt(chunkX, chunkZ);
		for (int x = startX; x < startX + 16; x++) { // not sure why, but I still need to set the data for all the positions
			for (int z = startZ; z < startZ + 16; z++) { // not sure why, but I still need to set the data for all the positions
				addBiome(map, world.getBiome(x, yLayer, z)); // TODO - in the future, we might need to change/get biomes at other y values, but for now only y=0 has an effect
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
