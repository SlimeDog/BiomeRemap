package me.ford.biomeremap.volotile;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class APIBiomeManager implements BiomeManager {

    @Override
    public Biome getBiomeNMS(World world, int x, int z)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return world.getBiome(x, 0, z);
    }

    @Override
    public Biome getBiomeNMS(Chunk chunk, int nr)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return chunk.getBlock((nr << 2 >> 2) - (nr % 4), 0, (nr % 4) << 2).getBiome();
    }

    @Override
    public void setBiomeNMS(World world, int x, int z, Biome biome)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        world.setBiome(x, 0, z, biome);
    }

    @Override
    public void setBiomeNMS(Chunk chunk, int nr, Biome biome)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        chunk.getBlock((nr << 2 >> 2) - (nr % 4), 0, (nr % 4) << 2).setBiome(biome);
    }

    @Override
    public int getBiomeIndex(Biome biome) {
        return biome.ordinal(); // TODO - make better?
    }

}
