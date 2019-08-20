package me.ford.biomeremap.mapping;

import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;

public class LargeMappingTask {
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
	
	public LargeMappingTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug, Consumer<String> progress, Consumer<TaskReport> ender) {
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
		int step = br.getSettings().getRegionRemapProgressStep();
		if (step == 0) step = 100; // disable if set to 0
		progressStep = step;
		nextProgress = progressStep;
		remapChunks();
	}
	
	private void remapChunks() {
		long start = System.currentTimeMillis();
		try {
			while (System.currentTimeMillis() - start < 20 && !done) doChunk(); // 20 ms max
		} catch (Throwable e) {
			br.getLogger().log(Level.SEVERE, "Issue while remapping chunk:", e);
		}
		long curTime = System.currentTimeMillis() - start;
		time += curTime;
		ticks++;
		if (!done) {
			br.getServer().getScheduler().runTaskLater(br, () -> remapChunks(), curTime>40?2:1);
		} else {
			ender.accept(new TaskReport(chunks, ticks, time));
		}
		double progress = ((double) chunks)/((double) totalChunks);
		if (progress * 100 > nextProgress) {
			this.progress.accept(String.format("%d%%", nextProgress));
			nextProgress += progressStep;
		}
	}
	
	private void doChunk() {
		if (!world.isChunkGenerated(x, z)) {
			world.getChunkAt(x, z); // populator takes care
		} else {
			BiomeRemapper.getInstance().remapChunk(world.getChunkAt(x, z), debug);
		}
		z++;
		if (z >= maxZ) {
			x++;
			z = minZ;
		}
		if (x >= maxX) done = true;
		chunks++;
	}
	
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