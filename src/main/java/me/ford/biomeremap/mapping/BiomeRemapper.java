package me.ford.biomeremap.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
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
	
	public long remapChunk(Chunk chunk, boolean debug) {
//		if (Bukkit.isPrimaryThread()) {
//			br.getLogger().warning("Chunk remap attempted in sync! Falling back async.");
//			br.getServer().getScheduler().runTaskAsynchronously(br, () -> remapChunk(chunk));
//		}
		long start = System.currentTimeMillis();
		if (debug) BiomeRemap.debug("Looking for biomes to remap (SYNC) in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		World world = chunk.getWorld();
		BiomeMap map = br.getSettings().getApplicableBiomeMap(world.getName());
		if (map == null) return 0;
		if (debug) BiomeRemap.debug(world.getName() + "->Mapping " + map.getName() + ":" + map.getMapping());
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
			// TODO - might want to spread it out? But then again, if they are gonna load chunks, they'll do many at a time...
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
	
	
	public void remapChunk(ChunkSnapshot chunk) {
		if (Bukkit.isPrimaryThread()) {
			br.getLogger().warning("Chunk remap attempted in sync! Falling back async.");
			br.getServer().getScheduler().runTaskAsynchronously(br, () -> remapChunk(chunk));
		}
		BiomeRemap.debug("Looking for biomes to remap in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		BiomeMap map = br.getSettings().getBiomeMap(chunk.getWorldName());
		Map<Integer, Biome> toChange = new HashMap<>();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome cur = chunk.getBiome(x, z);
				Biome req = map.getBiomeFor(cur);
				if (req != null) {
					toChange.put(x * 16 + z, req);
				}
			}
		}
		if (!toChange.isEmpty()) {
			BiomeRemap.debug("Found:" + toChange);
			// TODO - might want to spread it out? But then again, if they are gonna load chunks, they'll do many a
			br.getServer().getScheduler().runTask(br, () -> doMapping(chunk, toChange));
		}
	}
	
	private void doMapping(ChunkSnapshot chunk, Map<Integer, Biome> toChange) {
		BiomeRemap.debug("Remapping biomes");
		World world = br.getServer().getWorld(chunk.getWorldName());
		if (world == null) {
			br.getLogger().severe("World of a chunk snapshot does not exist!");
			return;
		}
		int startX = chunk.getX() * 16;
		int startZ = chunk.getZ() * 16;
		for (Entry<Integer, Biome> entry : toChange.entrySet()) {
			int x = entry.getKey()/16;
			int z = entry.getKey()%16;
			world.setBiome(startX + x, startZ + z, entry.getValue());
		}
	}

}
