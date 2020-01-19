package me.ford.biomeremap.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;

public class BiomeRemapper {
	private final BiomeRemap br;
	private final Class<? extends net.minecraft.server.v1_15_R1.BiomeStorage> biomeStorageClass = net.minecraft.server.v1_15_R1.BiomeStorage.class;
	private final java.lang.reflect.Field biomeBaseField;
	
	public BiomeRemapper(BiomeRemap plugin) { 
		br = plugin;
		try {
			biomeBaseField = biomeStorageClass.getDeclaredField("g");
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error getting BiomeBase field!");
		}
		biomeBaseField.setAccessible(true);
	}
	
	public long remapChunk(Chunk chunk) {
		return remapChunk(chunk, true);
	}
	
	public long remapChunk(Chunk chunk, BiomeMap map) {
		return remapChunk(chunk, false, map);
	}
	
	public long remapChunk(Chunk chunk, boolean debug) {
		return remapChunk(chunk, debug, (Runnable) null);
	}
	
	public long remapChunk(Chunk chunk, boolean debug, Runnable whenDone) {
		BiomeMap map = br.getSettings().getApplicableBiomeMap(chunk.getWorld().getName());
		return remapChunk(chunk, debug, map, whenDone);
	}

	public long remapChunk(final Chunk chunk, boolean debug, final BiomeMap map) {
		return remapChunk(chunk, debug, map, null);
	}
	
	public long remapChunk(final Chunk chunk, boolean debug, final BiomeMap map, Runnable whenDone) {
		long start = System.currentTimeMillis();
		if (debug) BiomeRemap.debug("Looking for biomes to remap (SYNC) in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		if (map == null) return 0;
		if (debug) BiomeRemap.debug(chunk.getWorld().getName() + "->Mapping " + map.getName());
		Map<Integer, BiomeChoice> toChange = new HashMap<>();
		Map<Biome, Biome> changes = new HashMap<>();
		ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, true, false);
		br.getServer().getScheduler().runTaskAsynchronously(br, () -> {
			for (int x = 0; x < 16; x++) { // the grid only has 4 sections per horizontal axis
				for (int z = 0; z < 16; z++) { // the grid only has 4 sections per horizontal axis
					Biome cur = snapshot.getBiome(x, 0, z); // TODO - in the future, we might need to change/get biomes at other y values, but for now only y=0 has an effect
					Biome req = map.getBiomeFor(cur);
					if (req != null) {
						int key = x >> 2 << 2 | z >> 2;
						if (!toChange.containsKey(key)) {
							toChange.put(key, new BiomeChoice());
						}
						toChange.get(key).addChoice(req);
					}
					if (!changes.containsKey(cur)) {
						changes.put(cur, req);
					}
				}
			}
			if (!toChange.isEmpty()) {
				if (debug) BiomeRemap.debug("Found:" + changes);
				br.getServer().getScheduler().runTask(br, () -> doMapping(chunk, toChange, debug, whenDone));
			}
		});
		return System.currentTimeMillis() - start;
	}
	
	private void doMapping(Chunk chunk, Map<Integer, BiomeChoice> toChange, boolean debug, Runnable whenDone) {
		if (debug) BiomeRemap.debug("Remapping biomes");
		for (Entry<Integer, BiomeChoice> entry : toChange.entrySet()) {
			changeBiomeInChunk(chunk, entry.getKey(), entry.getValue().choose());
			br.getTeleportListener().sendUpdatesIfNeeded(chunk);
		}
		if (whenDone != null) whenDone.run();
	}

	private void changeBiomeInChunk(Chunk chunk, int nr, Biome biome) {
		// TODO - this is VERSION SPECIFIC
		if (nr < 0 || nr > 15) br.getLogger().info("NR:" + nr);
		org.bukkit.craftbukkit.v1_15_R1.CraftChunk craftChunk = (org.bukkit.craftbukkit.v1_15_R1.CraftChunk) chunk;
		net.minecraft.server.v1_15_R1.Chunk nmsChunk = craftChunk.getHandle();
		net.minecraft.server.v1_15_R1.BiomeStorage biomes = nmsChunk.getBiomeIndex();
		net.minecraft.server.v1_15_R1.BiomeBase[] bases;
		try {
			bases = (net.minecraft.server.v1_15_R1.BiomeBase[]) biomeBaseField.get(biomes);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		net.minecraft.server.v1_15_R1.BiomeBase bb = org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock.biomeToBiomeBase(biome);
		bases[nr] = bb;
	}

	private class BiomeChoice {
		private Map<Biome, Integer> choices = new HashMap<>();

		private void addChoice(Biome biome) {
			if (choices.containsKey(biome)) {
				choices.put(biome, choices.get(biome) + 1);
			} else {
				choices.put(biome, 1);
			}
		}

		private Biome choose() { // arbitraily, the first with the max value is used (which could lead to the same biome being prioritized)
			int maxi = 0;
			Biome biome = null;
			for (Entry<Biome, Integer> entry : choices.entrySet()) {
				if (entry.getValue() > maxi) {
					maxi = entry.getValue();
					biome = entry.getKey();
				}
			}
			return biome;
		}
	}

}
