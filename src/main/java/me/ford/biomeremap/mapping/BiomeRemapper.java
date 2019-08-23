package me.ford.biomeremap.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.BiomeRemap;

public class BiomeRemapper {
	private static BiomeRemapper instance = null;
	
	private final BiomeRemap br;
	
	private BiomeRemapper(BiomeRemap plugin) { 
		br = plugin;
	}
	
	public static BiomeRemapper getInstance() {
		if (instance == null) instance = new BiomeRemapper(JavaPlugin.getPlugin(BiomeRemap.class));
		return instance;
	}
	
	public long remapChunk(Chunk chunk) {
		return remapChunk(chunk, true);
	}
	
	public long remapChunk(Chunk chunk, BiomeMap map) {
		return remapChunk(chunk, false, map);
	}
	
	public long remapChunk(Chunk chunk, boolean debug) {
		BiomeMap map = br.getSettings().getApplicableBiomeMap(chunk.getWorld().getName());
		return remapChunk(chunk, debug, map);
	}
	
	public long remapChunk(Chunk chunk, boolean debug, BiomeMap map) {
		long start = System.currentTimeMillis();
		if (debug) BiomeRemap.debug("Looking for biomes to remap (SYNC) in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		World world = chunk.getWorld();
		if (map == null) return 0;
		if (debug) BiomeRemap.debug(world.getName() + "->Mapping " + map.getName());
		int chunkX = chunk.getX() * 16;
		int chunkZ = chunk.getZ() * 16;
		Map<Integer, Biome> toChange = new HashMap<>();
		Map<Biome, Biome> changes = new HashMap<>();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome cur = world.getBiome(chunkX + x, chunkZ + z);
				Biome req = map.getBiomeFor(cur);
				if (req != null) {
					toChange.put(x * 16 + z, req);
				}
				if (!changes.containsKey(cur)) {
					changes.put(cur, req);
				}
			}
		}
		if (!toChange.isEmpty()) {
			if (debug) BiomeRemap.debug("Found:" + changes);
			doMapping(chunk, toChange, debug);
		}
		return System.currentTimeMillis() - start;
	}
	
	private void doMapping(Chunk chunk, Map<Integer, Biome> toChange, boolean debug) {
		if (debug) BiomeRemap.debug("Remapping biomes");
		World world = chunk.getWorld();
		int startX = chunk.getX() * 16;
		int startZ = chunk.getZ() * 16;
		for (Entry<Integer, Biome> entry : toChange.entrySet()) {
			int x = entry.getKey()/16;
			int z = entry.getKey()%16;
			world.setBiome(startX + x, startZ + z, entry.getValue());
		}
	}

}
