package me.ford.biomeremap.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.commands.SubCommand;
import me.ford.biomeremap.largetasks.LargeAreaMappingTaskStarter;
import me.ford.biomeremap.largetasks.OnMappingDone;
import me.ford.biomeremap.mapping.settings.RemapOptions;

public class BiomeRemapper {
	private static final int BIOME_SIZE = 4;
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

	public void remapArea(RemapOptions options) {
		new LargeAreaMappingTaskStarter(br, options, options.getEndRunnable());
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
		return remapChunk(chunk, debug, map, SubCommand.MAX_Y);
	}

	public long remapChunk(final Chunk chunk, boolean debug, final BiomeMap map, int maxY) {
		final int maxy;
		if (maxY > SubCommand.MAX_Y) {
			maxy = SubCommand.MAX_Y;
		} else {
			maxy = maxY;
		}
		long start = System.currentTimeMillis();
		if (debug)
			BiomeRemap
					.debug("Looking for biomes to remap (SYNC) in chunk:" + chunk.getX() + "," + chunk.getZ() + "...");
		if (map == null)
			return 0;
		if (debug)
			BiomeRemap.debug(chunk.getWorld().getName() + "->Mapping " + map.getName());
		List<BiomeChange> toChange = new ArrayList<>();
		ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, true, false);
		br.getServer().getScheduler().runTaskAsynchronously(br, () -> {
			for (int x = 0; x < 16; x += BIOME_SIZE) {
				for (int z = 0; z < 16; z += BIOME_SIZE) {
					for (int y = chunk.getWorld().getMinHeight(); y <= maxy; y += BIOME_SIZE) {
						Biome cur;
						try {
							cur = snapshot.getBiome(x, y, z);
						} catch (NullPointerException e) {
							br.getLogger().warning(
									"Problem geting biome in snapshot " + snapshot + " at " + x + "," + y + "," + z);
							try {
								java.lang.reflect.Field field = snapshot.getClass().getDeclaredField("biome");
								field.setAccessible(true);
								Object bs = field.get(snapshot);
								java.lang.reflect.Method getBiomeMethod = bs.getClass().getMethod("getBiome", int.class,
										int.class, int.class);
								Object bb = getBiomeMethod.invoke(bs, x >> 2, 0 >> 2, z >> 2);
								br.getLogger().warning("Found base:" + bb);
							} catch (Exception e2) {
								e2.printStackTrace();
							}
							continue;
						}
						Biome req = map.getBiomeFor(cur);
						if (req != null) {
							toChange.add(new BiomeChange(x, y, z, cur, req));
						}
					}
				}
			}
			if (!toChange.isEmpty()) {
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

	private void doMapping(Chunk chunk, List<BiomeChange> toChange, boolean debug) {
		long start = System.currentTimeMillis();
		if (debug)
			BiomeRemap.debug("Remapping biomes");
		Map<BiomePair, Integer> countedChanges = debug ? new HashMap<>() : null;
		for (BiomeChange change : toChange) {
			int setX = (chunk.getX() << 4) + change.x;
			int setY = change.y;
			int setZ = (chunk.getZ() << 4) + change.z;
			try {
				chunk.getWorld().setBiome(setX, setY, setZ, change.to);
				if (debug) {
					countedChanges.compute(new BiomePair(change.from, change.to), (k, v) -> v == null ? 1 : v + 1);
				}
			} catch (IllegalArgumentException e) {
				br.getLogger().severe("Problem setting biome!");
				e.printStackTrace();
				continue;
			}
		}
		if (debug) {
			System.out.println("Counted: " + countedChanges);
		}
		br.getTeleportListener().sendUpdatesIfNeeded(chunk);
		runAfterRemaps(chunk);
		timeLastTick += (System.currentTimeMillis() - start);
	}

	private void runAfterRemaps(Chunk chunk) {
		for (OnMappingDone checker : doneCheckers) {
			checker.afterRemap(chunk);
		}
	}

	private class BiomePair {
		private final Biome one, two;

		private BiomePair(Biome one, Biome two) {
			this.one = one;
			this.two = two;
		}

		@Override
		public int hashCode() {
			return Objects.hash(one.name(), two.name());
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof BiomePair)) {
				return false;
			}
			BiomePair o = (BiomePair) other;
			return o.one == one && o.two == two;
		}

		@Override
		public String toString() {
			return String.format("{%s -> %s}", one.name(), two.name());
		}

	}

	private class BiomeChange {
		private final int x;
		private final int y;
		private final int z;
		private final Biome from;
		private final Biome to;

		private BiomeChange(int x, int y, int z, Biome from, Biome to) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof BiomeChange)) {
				return false;
			}
			BiomeChange o = (BiomeChange) other;
			return o.x == x && o.y == y && o.z == z && o.from == from && o.to == to;
		}

		@Override
		public int hashCode() {
			return Objects.hash(x, y, z, from, to);
		}

		@Override
		public String toString() {
			return String.format("{%s -> %s @ %d, %d, %d}", from.name(), to.name(), x, y, z);
		}

	}

}
