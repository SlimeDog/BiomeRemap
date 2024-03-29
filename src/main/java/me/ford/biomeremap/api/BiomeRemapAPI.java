package me.ford.biomeremap.api;

import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.largetasks.LargeAreaMappingTaskStarter;
import me.ford.biomeremap.largetasks.LargeScanTask.BiomeReport;
import me.ford.biomeremap.largetasks.LargeScanTaskStarter;
import me.ford.biomeremap.mapping.BiomeMap;
import me.ford.biomeremap.mapping.settings.RegionArea;
import me.ford.biomeremap.mapping.settings.RemapOptions;
import me.ford.biomeremap.mapping.settings.SingleReportTarget;

public class BiomeRemapAPI {

	private BiomeRemapAPI() {
		throw new IllegalStateException("API should not be initialized!");
	}

	private static BiomeRemap getPlugin() {
		return JavaPlugin.getPlugin(BiomeRemap.class);
	}

	public static void remapChunk(World world, int chunkX, int chunkZ, BiomeMap map) {
		Validate.notNull(map);
		Chunk chunk = world.getChunkAt(chunkX, chunkZ);
		getPlugin().getRemapper().remapChunk(chunk, map);
	}

	public static void remapRegion(World world, int regionX, int regionZ, BiomeMap map) {
		RegionArea area = new RegionArea(world, regionX, regionZ);
		RemapOptions options = new RemapOptions.Builder().withArea(area)
				.withTarget(getPlugin().getConsoleRecipient()).withMap(map).build();
		new LargeAreaMappingTaskStarter(getPlugin(), getPlugin().getSettings(), getPlugin().getMessages(),
				getPlugin().getRemapper(), getPlugin().getScanner(), options, null);
	}

	public static void scanRegion(World world, int regionX, int regionZ, Consumer<BiomeReport> report) {
		new LargeScanTaskStarter(getPlugin(), getPlugin().getSettings(), getPlugin().getMessages(),
				getPlugin().getRemapper(), getPlugin().getScanner(), world,
				new SingleReportTarget(getPlugin().getConsoleRecipient()), regionX, world.getMinHeight(),
				world.getMaxHeight(), regionZ, true, false, null, report);
	}

}
