package me.ford.biomeremap.mapping;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import net.minecraft.server.v1_15_R1.BiomeBase;
import net.minecraft.server.v1_15_R1.BiomeStorage;
import net.minecraft.server.v1_15_R1.Chunk;

public final class BiomeScanner {
	private final Class<? extends BiomeStorage> biomeStorageClass = net.minecraft.server.v1_15_R1.BiomeStorage.class;
	private final Field biomeBaseField;

	public BiomeScanner() {
		try {
			biomeBaseField = biomeStorageClass.getDeclaredField("g");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error getting BiomeBase field!");
		}
		biomeBaseField.setAccessible(true);
	}

	public void addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ) {
		addBiomesFor(map, world, chunkX, chunkZ, 0, false);
	}

	public void addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ, int yLayer, boolean useNMS) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		if (!world.isChunkGenerated(chunkX, chunkZ)) {
			// allow populator to do its thing -> OnMappingDone counts them
			world.getChunkAt(chunkX, chunkZ);
		} else {
			addBiomesForInternal(world, chunkX, chunkZ, useNMS, startX, startZ, yLayer, map);
		}
	}

	private void addBiomesForInternal(World world, int chunkX, int chunkZ, boolean useNMS, int startX, int startZ, int yLayer, Map<Biome, Integer> map) {
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
		// TODO - this is version specific!
		Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
		BiomeStorage storage = nmsChunk.getBiomeIndex();
		BiomeBase[] bases;
		try {
			bases = (BiomeBase[]) biomeBaseField.get(storage);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			org.bukkit.Bukkit.getLogger().warning("Error while checking NMS biomes!");
			return;
		}
		// 16 per ylayer 
		final int startNr = yLayer << 4;
		for (int nr = startNr; nr < startNr + 16; nr++) {
			BiomeBase bb = bases[nr];
			addBiome(map, CraftBlock.biomeBaseToBiome(bb));
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
