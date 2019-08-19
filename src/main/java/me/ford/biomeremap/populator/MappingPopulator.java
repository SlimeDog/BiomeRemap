package me.ford.biomeremap.populator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import me.ford.biomeremap.mapping.BiomeRemapper;

public class MappingPopulator extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
//		BiomeRemapper.getInstance().remapChunk(source.getChunkSnapshot());
		BiomeRemapper.getInstance().remapChunk(source);
	}

}
