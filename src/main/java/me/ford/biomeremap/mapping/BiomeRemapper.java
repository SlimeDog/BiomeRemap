package me.ford.biomeremap.mapping;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.OnMappingDone;

public class BiomeRemapper {
	private final BiomeRemap br;
	private final Set<OnMappingDone> doneCheckers = new HashSet<>();
	private long timeLastTick = 0L;

	public BiomeRemapper(BiomeRemap plugin) {
		br = plugin;
	}

	public void addDoneChecker(OnMappingDone checker) {
		doneCheckers.add(checker);
	}

	public void removeDoneCheker(OnMappingDone checker) {
		doneCheckers.remove(checker);
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

	public long remapChunk(final Chunk chunk, boolean debug, final BiomeMap map) {
		long start = System.currentTimeMillis();
		if (debug)
			BiomeRemap
					.debug("Looking for biomes to remap (SYNC) in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		if (map == null)
			return 0;
		if (debug)
			BiomeRemap.debug(chunk.getWorld().getName() + "->Mapping " + map.getName());
		Map<Integer, BiomeChoice> toChange = new HashMap<>();
		Map<Biome, Biome> changes = new HashMap<>();
		ChunkSnapshot snapshot = chunk.getChunkSnapshot(false, true, false);
		br.getServer().getScheduler().runTaskAsynchronously(br, () -> {
			for (int x = 0; x < 16; x++) { // the grid only has 4 sections per horizontal axis
				for (int z = 0; z < 16; z++) { // the grid only has 4 sections per horizontal axis
					Biome cur;
					try {
						cur = snapshot.getBiome(x, 0, z); // TODO - in the future, we might need to change/get biomes
															// at other y values, but for now only y=0 has an effect
					} catch (NullPointerException e) {
						br.getLogger().warning("Problem geting biome in snapshot " + snapshot + " at " + x + "," + z);
						try {
							java.lang.reflect.Field field = snapshot.getClass().getDeclaredField("biome");
							field.setAccessible(true);
							Object bs = field.get(snapshot);
							java.lang.reflect.Method getBiomeMethod = bs.getClass().getMethod("getBiome", int.class, int.class, int.class);
							Object bb = getBiomeMethod.invoke(bs, x >> 2, 0 >> 2, z >> 2);
							br.getLogger().warning("Found base:" + bb);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						continue;
					}
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
				if (debug)
					BiomeRemap.debug("Found:" + changes);
				br.getServer().getScheduler().runTask(br, () -> doMapping(chunk, toChange, debug));
			} else {
				br.getServer().getScheduler().runTask(br, () -> {
					long sstart = System.currentTimeMillis();
					runAfterRemaps(chunk);
					timeLastTick += (System.currentTimeMillis() - sstart);
				});
			}
		});
		long timeSpent = System.currentTimeMillis() - start + timeLastTick;
		timeLastTick = 0L;
		return timeSpent;
	}

	private void doMapping(Chunk chunk, Map<Integer, BiomeChoice> toChange, boolean debug) {
		long start = System.currentTimeMillis();
		if (debug)
			BiomeRemap.debug("Remapping biomes");
		for (Entry<Integer, BiomeChoice> entry : toChange.entrySet()) {
			try {
				br.getBiomeManager().setBiomeNMS(chunk, entry.getKey(), entry.getValue().choose());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				br.getLogger().severe("Problem setting biome!");
				e.printStackTrace();
				continue;
			}
			br.getTeleportListener().sendUpdatesIfNeeded(chunk);
		}
		runAfterRemaps(chunk);
		timeLastTick += (System.currentTimeMillis() - start);
	}

	private void runAfterRemaps(Chunk chunk) {
		for (OnMappingDone checker : doneCheckers) {
			checker.afterRemap(chunk);
		}
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

		private Biome choose() { // arbitraily, the first with the max value is used (which could lead to the
									// same biome being prioritized)
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
