package me.ford.biomeremap.mapping.settings;

import org.bukkit.World;

public interface RemapArea {

    World getWorld();

    boolean describesRegion();

    int getAreaX();

    int getAreaZ();

}
