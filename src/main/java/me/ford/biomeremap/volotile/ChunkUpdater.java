package me.ford.biomeremap.volotile;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * ChunkUpdater
 */
public interface ChunkUpdater {

	public void updateChunk(Player player, Chunk chunk)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException;

}