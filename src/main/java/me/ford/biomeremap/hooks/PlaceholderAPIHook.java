package me.ford.biomeremap.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ford.biomeremap.BiomeRemap;

public class PlaceholderAPIHook extends PlaceholderExpansion {
	private final BiomeRemap br;

	public PlaceholderAPIHook(BiomeRemap plugin) {
		br = plugin;
		register();
	}

	@Override
	public String getAuthor() {
		return String.join(", ", br.getDescription().getAuthors());
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String getIdentifier() {
		return br.getDescription().getName().toLowerCase();
	}

	@Override
	public String getVersion() {
		return br.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		if (player == null) {
			return "";
		}

		Location loc = player.getLocation();
		loc.setY(0.0D);
		if (identifier.equals("biome_name")) {
			return loc.getBlock().getBiome().name();
		}

		if (identifier.equals("biome_id")) {
			return String.valueOf(br.getBiomeManager().getBiomeIndex(loc.getBlock().getBiome()));
		}

		return null;

	}

}
