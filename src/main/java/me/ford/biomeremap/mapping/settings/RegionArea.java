package me.ford.biomeremap.mapping.settings;

import org.bukkit.World;

public class RegionArea implements RemapArea {
    private final World world;
    private final int x;
    private final int z;

    public RegionArea(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override
    public World getWorld() {
        return world;
    }

    public int getRegionX() {
        return x;
    }

    public int getRegionZ() {
        return z;
    }

    @Override
    public boolean describesRegion() {
        return true;
    }

    @Override
    public int getAreaX() {
        return getRegionX();
    }

    @Override
    public int getAreaZ() {
        return getRegionZ();
    }

}
