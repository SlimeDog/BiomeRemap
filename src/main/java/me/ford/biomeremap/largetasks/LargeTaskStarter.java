package me.ford.biomeremap.largetasks;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.settings.ReportTarget;

public abstract class LargeTaskStarter {
	private final BiomeRemap br;
	private final World world;
	private final ReportTarget owner;
	private final int x, z;
	private final int chunkX;
	private final int chunkZ;
	private final int stopX;
	private final int stopZ;
	private final boolean region;
	private final boolean debug;

	public LargeTaskStarter(BiomeRemap plugin, World world, ReportTarget owner, int x, int z, boolean region,
			boolean debug) {
		this.br = plugin;
		this.world = world;
		this.owner = owner;

		this.x = x;
		this.z = z;
		if (region) {
			x *= 32;
			z *= 32;
		}
		this.chunkX = x;
		this.chunkZ = z;
		this.region = region;
		this.debug = debug;
		if (region) {
			stopX = chunkX + 32;
			stopZ = chunkZ + 32;
		} else {
			stopX = chunkX + 1;
			stopZ = chunkZ + 1;
		}
		br.getServer().getScheduler().runTask(br, () -> startTask());
	}

	protected abstract void startTask();

	protected BiomeRemap br() {
		return br;
	}

	protected World world() {
		return world;
	}

	protected ReportTarget owner() {
		return owner;
	}

	protected int x() {
		return x;
	}

	protected int z() {
		return z;
	}

	protected int chunkX() {
		return chunkX;
	}

	protected int chunkZ() {
		return chunkZ;
	}

	protected int stopX() {
		return stopX;
	}

	protected int stopZ() {
		return stopZ;
	}

	protected boolean region() {
		return region;
	}

	protected boolean debug() {
		return debug;
	}

}
