package me.ford.biomeremap.mapping;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.settings.Settings;

public final class BiomeScanner {
	private static final int BIOME_SIZE = 4;
	private final Settings settings;
	private PopulatorQueue forPopulator;

	public BiomeScanner(Settings settings) {
		this.settings = settings;
	}

	public boolean addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ) {
		return addBiomesFor(map, world, chunkX, chunkZ, world.getMinHeight(), world.getMaxHeight());
	}

	public boolean addBiomesFor(Map<Biome, Integer> map, World world, int chunkX, int chunkZ, int minLayer,
			int maxLayer) {
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		if (!world.isChunkGenerated(chunkX, chunkZ)) {
			if (forPopulator != null)
				forPopulator.add(chunkX, chunkZ);
			world.getChunkAt(chunkX, chunkZ);
			if (settings.getApplicableBiomeMap(world.getName()) == null) { // world not being remapped
				addBiomesForInternal(world, chunkX, chunkZ, startX, startZ, minLayer, maxLayer, map);
				return true;
			}
			// allow populator to do its thing -> OnMappingDone counts them
			return false;
		} else {
			addBiomesForInternal(world, chunkX, chunkZ, startX, startZ, minLayer, maxLayer, map);
			return true;
		}
	}

	private void addBiomesForInternal(World world, int chunkX, int chunkZ, int startX, int startZ, int minLayer,
			int maxLayer, Map<Biome, Integer> map) {
		if (forPopulator != null)
			forPopulator.remove(chunkX, chunkZ); // if present
		world.getChunkAt(chunkX, chunkZ);
		addBiomes(world, startX, startZ, minLayer, maxLayer, map);
	}

	private void addBiomes(World world, int startX, int startZ, int minLayer, int maxLayer, Map<Biome, Integer> map) {
		for (int x = startX; x < startX + 16; x += BIOME_SIZE) {
			for (int z = startZ; z < startZ + 16; z += BIOME_SIZE) {
				for (int y = minLayer; y < maxLayer; y += BIOME_SIZE) {
					addBiome(map, world.getBiome(x, y, z));
				}
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

	public void setPopulatorQueue(PopulatorQueue queue) {
		this.forPopulator = queue;
	}

	public void tickPopulatorQueue() {
		if (forPopulator != null) {
			forPopulator.tick();
		}
	}

	private void addBiomesForInternal(ChunkLoc loc, PopulatorQueue queue) {
		int chunkX = loc.getX();
		int chunkZ = loc.getZ();
		int startX = chunkX * 16;
		int startZ = chunkZ * 16;
		addBiomesForInternal(queue.getWorld(), chunkX, chunkZ, startX, startZ, queue.getMinLayer(), queue.getMaxLayer(),
				queue.getMap());
	}

	public void finalizePopulatorQueue() {
		if (forPopulator != null) {
			for (ChunkLoc loc : forPopulator.doAll()) {
				addBiomesForInternal(loc, forPopulator);
			}
		}
		forPopulator = null;
	}

}
