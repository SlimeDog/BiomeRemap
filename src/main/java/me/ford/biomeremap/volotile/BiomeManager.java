package me.ford.biomeremap.volotile;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

/**
 * BiomeManager
 */
public interface BiomeManager {

	@Deprecated
	public Biome getBiomeNMS(World world, int x, int z)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	@Deprecated
	public Biome getBiomeNMS(Chunk chunk, int nr)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	@Deprecated
	public void setBiomeNMS(World world, int x, int z, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	@Deprecated
	public void setBiomeNMS(Chunk chunk, int nr, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public int getBiomeIndex(Biome biome);

}