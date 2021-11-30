package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.PopulatorQueue;

public class LargeScanTask extends LargeTask {
	private final Map<Biome, Integer> biomeMap = new HashMap<>();
	private final Consumer<BiomeReport> biomes;
	private final int yLayer;
	private final OnMappingDone onMappingDone;
	private final PopulatorQueue queue;
	private final boolean[][] checked = new boolean[32][32];
	// private final boolean[][] doubled = new boolean[32][32];
	// private final boolean[][] noDice = new boolean[32][32];
	// private final boolean[][] wasGenerated = new boolean[32][32];

	public LargeScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, Consumer<BiomeReport> biomes,
			int yLayer) {
		super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
		this.biomes = biomes;
		this.yLayer = yLayer;
		this.onMappingDone = new OnMappingDone((x, z) -> findBiomes(x, z, debug), world, minX, minZ, maxX, maxZ);
		getPlugin().getRemapper().addDoneChecker(onMappingDone); // checks the newly generated ones
		queue = new PopulatorQueue(biomeMap, world, getPlugin().getScanner(), yLayer);
		getPlugin().getScanner().setPopulatorQueue(queue);
	}

	@Override
	protected void remapChunks() {
		super.remapChunks();
		queue.tick();
	}

	@Override
	protected void doTaskForChunk(int x, int z, boolean debug) {
		findBiomes(x, z, debug);
	}

	private void findBiomes(int chunkX, int chunkZ, boolean debug) {
		if (checked[chunkX - getMinX()][chunkZ - getMinZ()]) {
			// doubled[chunkX - getMinX()][chunkZ - getMinZ()] = true;
			return; // already done
		}
		// wasGenerated[chunkX - getMinX()][chunkZ - getMinZ()] =
		// getWorld().isChunkGenerated(chunkX, chunkZ);
		if (getPlugin().getScanner().addBiomesFor(biomeMap, getWorld(), chunkX, chunkZ, yLayer)) {
			checked[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		} else { // else will be done later, after the remap
			// noDice[chunkX - getMinX()][chunkZ - getMinZ()] = true;
		}
	}

	@Override
	protected void whenDone() {
		getPlugin().getScanner().finalizePopulatorQueue();
		getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
			biomes.accept(new BiomeReport(biomeMap));
			getPlugin().getRemapper().removeDoneCheker(onMappingDone);
			// DEBUG
			// StringBuilder builder = new StringBuilder();
			// StringBuilder doubleBuilder = new StringBuilder();
			// StringBuilder rejectedBuilder = new StringBuilder();
			// StringBuilder generatedBuilder = new StringBuilder();
			// for (int x = 0; x < 32; x++) {
			// builder.append("\n");
			// doubleBuilder.append("\n");
			// rejectedBuilder.append("\n");
			// generatedBuilder.append("\n");
			// for (int z = 0; z < 32; z++) {
			// builder.append(checked[x][z]?".":"*");
			// doubleBuilder.append(doubled[x][z]?".":"*");
			// rejectedBuilder.append(noDice[x][z]?".":"*");
			// generatedBuilder.append(wasGenerated[x][z]?".":"*");
			// }
			// }
			// getPlugin().getLogger().info(". -> scanned, * -> unscanned:" + builder);
			// getPlugin().getLogger().info("Attempt for DOUBLE:" + doubleBuilder);
			// getPlugin().getLogger().info("Went for remapping:" + rejectedBuilder);
			// getPlugin().getLogger().info("Was generated at check time:" +
			// generatedBuilder);
			// DEBUG
		}, 10L); // make sure they all get remapped and scanned
	}

	public static class BiomeReport {
		private final Map<Biome, Integer> biomes;

		public BiomeReport(Map<Biome, Integer> biomes) {
			this.biomes = biomes;
		}

		public Map<Biome, Integer> getBiomes() {
			return biomes;
		}
	}

}
