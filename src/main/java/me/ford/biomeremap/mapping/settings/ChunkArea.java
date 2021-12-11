package me.ford.biomeremap.mapping.settings;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkArea implements RemapArea {
    private final World world;
    private final int x;
    private final int z;

    public ChunkArea(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public World getWorld() {
        return world;
    }

    public int getChunkX() {
        return x;
    }

    public int getChunkZ() {
        return z;
    }

    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }

    @Override
    public boolean describesRegion() {
        return false;
    }

    @Override
    public int getAreaX() {
        return getChunkX();
    }

    @Override
    public int getAreaZ() {
        return getChunkZ();
    }

}
