package me.ford.biomeremap.mapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Biome;

public class PopulatorQueue {
	private final BiomeScanner scanner;
	private final Map<Biome, Integer> map;
	private final World world;
	private final int minLayer;
	private final int maxLayer;
	private final Set<ChunkLoc> one = new HashSet<>();
	private final Set<ChunkLoc> two = new HashSet<>();
	private final Set<ChunkLoc> three = new HashSet<>();

	public PopulatorQueue(Map<Biome, Integer> map, World world, BiomeScanner scanner) {
		this(map, world, scanner, world.getMinHeight(), world.getMaxHeight());
	}

	public PopulatorQueue(Map<Biome, Integer> map, World world, BiomeScanner scanner, int minLayer, int maxLayer) {
		this.scanner = scanner;
		this.map = map;
		this.world = world;
		this.minLayer = minLayer;
		this.maxLayer = maxLayer;
	}

	public Map<Biome, Integer> getMap() {
		return map; // mutable
	}

	public World getWorld() {
		return world;
	}

	public int getMinLayer() {
		return minLayer;
	}

	public int getMaxLayer() {
		return maxLayer;
	}

	public void add(int x, int z) {
		add(new ChunkLoc(x, z));
	}

	public void add(ChunkLoc loc) {
		one.add(loc);
	}

	public void remove(int x, int z) {
		remove(new ChunkLoc(x, z));
	}

	public void remove(ChunkLoc loc) {
		one.remove(loc);
		two.remove(loc);
		three.remove(loc);
	}

	public void tick() {
		// do three + clear
		for (ChunkLoc loc : new HashSet<>(three)) { // they should be removed when scanned
			scanner.addBiomesFor(map, world, loc.getX(), loc.getZ(), minLayer, maxLayer);
		}
		three.clear();
		// two -> three + clear
		three.addAll(two);
		two.clear();
		// one -> two + clear
		two.addAll(one);
		one.clear();
	}

	public Set<ChunkLoc> doAll() {
		tick();
		tick();
		tick();
		Set<ChunkLoc> locs = new HashSet<>();
		locs.addAll(one);
		locs.addAll(two);
		locs.addAll(three);
		return locs;
	}

}