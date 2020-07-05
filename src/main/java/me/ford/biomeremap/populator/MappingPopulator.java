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

	public MappingPopulator(BiomeRemapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public void populate(World world, Random random, Chunk source) {
		mapper.remapChunk(source, JavaPlugin.getPlugin(BiomeRemap.class).getSettings().debugAutoremap());
	}

}
