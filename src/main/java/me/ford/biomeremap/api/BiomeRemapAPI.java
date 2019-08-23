package me.ford.biomeremap.api;

import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeMappingTaskStarter;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeScanTaskStarter;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.BiomeRemapper;

public class BiomeRemapAPI {
	
	private BiomeRemapAPI() { throw new IllegalStateException("API should not be initialized!"); }
	
	private static BiomeRemap getPlugin() {
		return JavaPlugin.getPlugin(BiomeRemap.class);
	}
	
	public static void remapChunk(World world, int chunkX, int chunkZ, BiomeMap map) {
		Validate.notNull(map);
		Chunk chunk = world.getChunkAt(chunkX, chunkZ);
		BiomeRemapper.getInstance().remapChunk(chunk, map);
	}
	
	public static void remapRegion(World world, int regionX, int regionZ, BiomeMap map) {
		new LargeMappingTaskStarter(getPlugin(), world, getPlugin().getServer().getConsoleSender(), 
						regionX, regionZ, true, false, null, false, map);
	}
	
	public static void scanRegion(World world, int regionX, int regionZ, Consumer<BiomeReport> report) {
		new LargeScanTaskStarter(getPlugin(), world, getPlugin().getServer().getConsoleSender(),
						regionX, regionZ, true, false, null, report);
	}

}
