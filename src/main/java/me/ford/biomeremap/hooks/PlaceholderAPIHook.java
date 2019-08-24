package me.ford.biomeremap.hooks;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ford.biomeremap.BiomeRemap;
import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.Biomes;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.RegistryMaterials;

public class PlaceholderAPIHook extends PlaceholderExpansion {
	private final BiomeRemap br;
	private Map<Biome, Integer> byBiome = new HashMap<>();
	
	public PlaceholderAPIHook(BiomeRemap plugin) {
		br = plugin;
		register();
		mapBiomesWithReflection();
	}
	
	private void mapBiomesWithReflection() { // TODO - there this is version specific
		for (Field f : Biomes.class.getDeclaredFields()) {
			if (f.getName().length() > 1) { // ignore the OCEAN duplicate Biomes.b
				BiomeBase base;
				try {
					base = (BiomeBase) f.get(null);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
				Biome biome = CraftBlock.biomeBaseToBiome(base);
				int nr = ((RegistryMaterials<BiomeBase>) IRegistry.BIOME).a(base);
				byBiome.put(biome, nr);
				br.getLogger().info(biome.name() + ":" + nr);
			}
		}
	}

	@Override
	public String getAuthor() {
		return String.join(", ", br.getDescription().getAuthors());
	}
	
	@Override
    public boolean persist(){
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
    public String onPlaceholderRequest(Player player, String identifier){
    	
    	if (player == null){
            return "";
        }

        if (identifier.equals("biome_name")){
        	return player.getLocation().getBlock().getBiome().name();
        }

        if (identifier.equals("biome_id")){
        	return String.valueOf(byBiome.get(player.getLocation().getBlock().getBiome()));
        			
        }
        
        return null;
        
    }

}
