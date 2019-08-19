package me.ford.biomeremap.listeners;

import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeRemapper;

public class ChunkListener implements Listener {
	private final BiomeRemap br;
	
	public ChunkListener(BiomeRemap plugin) {
		br = plugin;
	}
	
	@EventHandler
	public void onChunkPopulate(ChunkPopulateEvent event) {
		ChunkSnapshot chunk = event.getChunk().getChunkSnapshot();
		BiomeRemap.debug("Populating chunk:" + chunk.getX() + "," + chunk.getZ());
		br.getServer().getScheduler().runTaskLater(br, () ->
			br.getServer().getScheduler().runTaskLaterAsynchronously(br, () -> BiomeRemapper.getInstance().remapChunk(chunk), 2L)
		, 2L);
	}

}
