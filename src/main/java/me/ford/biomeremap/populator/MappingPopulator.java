package me.ford.biomeremap.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeRemapper;

public class MappingPopulator extends BlockPopulator {
	private final BiomeRemapper mapper;
	private Runnable whenDone = null;
	
	public MappingPopulator(BiomeRemapper mapper) {
		this.mapper = mapper;
	}

	public void setWhenDone(Runnable whenDone) {
		this.whenDone = whenDone;
	}

	@Override
	public void populate(World world, Random random, Chunk source) {
		mapper.remapChunk(source, JavaPlugin.getPlugin(BiomeRemap.class).getSettings().debugAutoremap(), () -> afterRemap());
	}

	private void afterRemap() {
		final Runnable whenDone = this.whenDone;
		if (whenDone != null) {
			// BiomeRemap br = JavaPlugin.getPlugin(BiomeRemap.class);
			// br.getServer().getScheduler().runTask(br, whenDone);
			whenDone.run();
		}
		this.whenDone = null; // reset
	}

}
