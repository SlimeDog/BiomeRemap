package me.ford.biomeremap.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ford.biomeremap.volotile.BiomeManager;

public class PlaceholderAPIHook extends PlaceholderExpansion {
	private final SlimeDogPlugin br;
	private final BiomeManager biomeManager;

	public PlaceholderAPIHook(SlimeDogPlugin plugin, BiomeManager biomeManager) {
		br = plugin;
		this.biomeManager = biomeManager;
		register();
	}

	@Override
	public String getAuthor() {
		return String.join(", ", br.getPluginInformation().getAuthors());
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return br.getPluginInformation().getPluginName().toLowerCase();
	}

	@Override
	public String getVersion() {
		return br.getPluginInformation().getPluginVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		if (player == null) {
			return "";
		}

		Location loc = player.getLocation();
		if (identifier.equals("biome_name")) {
			return loc.getBlock().getBiome().name();
		}

		if (identifier.equals("biome_id")) {
			return String.valueOf(biomeManager.getBiomeIndex(loc.getBlock().getBiome()));
		}

		return null;

	}

}
