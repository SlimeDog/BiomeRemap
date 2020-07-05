package me.ford.biomeremap.largetasks;

import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;

public abstract class LargeTask {
	private final BiomeRemap br;
	private final World world;
	private final int minX;
	private final int maxX;
	private final int minZ;
	private final int maxZ;
	private final int totalChunks;
	private final boolean debug;
	private final Consumer<String> progress;
	private final Consumer<TaskReport> ender;
	private final int progressStep;

	private int x;
	private int z;
	private int chunks = 0;
	private int ticks = 0;
	private long time = 0;
	private boolean done = false;
	private int nextProgress;
	// private final boolean[][] checked = new boolean[32][32];

	public LargeTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
			int progressStep, Consumer<String> progress, Consumer<TaskReport> ender) {
		this.br = plugin;
		this.world = world;
		this.minX = minX;
		this.maxX = maxX;
		this.minZ = minZ;
		this.maxZ = maxZ;
		this.totalChunks = (this.maxX - this.minX) * (this.maxZ - this.minZ);
		this.debug = debug;
		this.progress = progress;
		this.ender = ender;
		x = this.minX;
		z = this.minZ;
		if (progressStep == 0)
			progressStep = 100;
		this.progressStep = progressStep;
		nextProgress = this.progressStep;
		br.getServer().getScheduler().runTask(br, () -> remapChunks());
	}

	private void remapChunks() {
		long start = System.currentTimeMillis();
		try {
			while (System.currentTimeMillis() - start < 20 && !done)
				doChunk(); // 20 ms max
		} catch (Throwable e) {
			br.getLogger().log(Level.SEVERE, "Issue while running a large task on chunk:", e);
		}
		long curTime = System.currentTimeMillis() - start;
		time += curTime;
		ticks++;
		if (!done) {
			br.getServer().getScheduler().runTaskLater(br, () -> remapChunks(), curTime > 40 ? 2 : 1);
		} else {
			ender.accept(new TaskReport(chunks, ticks, time));
			// DEBUG
			// StringBuilder builder = new StringBuilder();
			// for (int x = 0; x < 32; x++) {
			// builder.append("\n");
			// for (int z = 0; z < 32; z++) {
			// builder.append(checked[x][z]?".":"*");
			// }
			// }
			// getPlugin().getLogger().info("INTERNAL:" + builder);
			// DEBUG
			whenDone();
			return;
		}
		double progress = ((double) chunks) / ((double) totalChunks) * 100;
		if (progress > nextProgress) {
			while (this.nextProgress < progress)
				nextProgress += progressStep;
			nextProgress -= progressStep; // back one step
			if (this.progress != null)
				this.progress.accept(String.format("%d%%", nextProgress));
			nextProgress += progressStep;
		}
	}

	private void doChunk() {
		// checked[x - minX][z - minZ] = true;
		doTaskForChunk(x, z, debug);
		z++;
		if (z >= maxZ) {
			x++;
			z = minZ;
		}
		if (x >= maxX)
			done = true;
		chunks++;
	}

	protected BiomeRemap getPlugin() {
		return br;
	}

	public int getMinX() {
		return minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinZ() {
		return minZ;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public World getWorld() {
		return world;
	}

	protected abstract void doTaskForChunk(int x, int z, boolean debug);

	protected abstract void whenDone();

	public class TaskReport {
		private final int chunksDone;
		private final int ticksUsed;
		private final long compTime;

		public TaskReport(int chunksDone, int ticksUsed, long compTime) {
			this.chunksDone = chunksDone;
			this.ticksUsed = ticksUsed;
			this.compTime = compTime;
		}

		public int getChunksDone() {
			return chunksDone;
		}

		public int getTicksUsed() {
			return ticksUsed;
		}

		public long getCompTime() {
			return compTime;
		}

	}

}
