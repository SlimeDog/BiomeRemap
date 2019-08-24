package me.ford.biomeremap.hooks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ford.biomeremap.BiomeRemap;

public class PlaceholderAPIHook extends PlaceholderExpansion {
	private final BiomeRemap br;
	private Map<Biome, Integer> byBiome = new HashMap<>();
	
	public PlaceholderAPIHook(BiomeRemap plugin) {
		br = plugin;
		register();
		mapBiomesWithReflection();
	}
	
	private void mapBiomesWithReflection() { // TODO - there this is version specific
		String version = br.getServer().getClass().getPackage().getName().split("\\.")[3];
		Class<?> biomesClass;
		try {
			biomesClass = Class.forName("net.minecraft.server." + version + ".Biomes");
		} catch (ClassNotFoundException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS Biomes class:", e1);
			return;
		}
		Class<?> craftBlockClass;
		try {
			craftBlockClass = Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftBlock");
		} catch (ClassNotFoundException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting CraftBlock class:", e1);
			return;
		}
		Class<?> biomeBaseClass;
		try {
			biomeBaseClass = Class.forName("net.minecraft.server." + version + ".BiomeBase");
		} catch (ClassNotFoundException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS BiomeBase class:", e1);
			return;
		}
		Class<?> iRegistryClass;
		try {
			iRegistryClass = Class.forName("net.minecraft.server." + version + ".IRegistry");
		} catch (ClassNotFoundException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS IRegistry class:", e1);
			return;
		}
		Class<?> registryMaterialsClass;
		try {
			registryMaterialsClass = Class.forName("net.minecraft.server." + version + ".RegistryMaterials");
		} catch (ClassNotFoundException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS RegistryMaterials class:", e1);
			return;
		}
		Method biomeBaseToBiomeField;
		try {
			biomeBaseToBiomeField = craftBlockClass.getDeclaredMethod("biomeBaseToBiome", biomeBaseClass);
		} catch (NoSuchMethodException | SecurityException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting CraftBlock#biomeBaseToBiome method:", e1);
			return;
		}
		Field biomeField;
		try {
			biomeField = iRegistryClass.getDeclaredField("BIOME");
		} catch (NoSuchFieldException | SecurityException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting IRegistry.BIOME field:", e1);
			return;
		}
		Method getIdMethod = null;
		for (Method method : registryMaterialsClass.getMethods()) {
			if (!method.getName().equals("a")) continue;
			Class<?>[] types = method.getParameterTypes();
			if (method.getReturnType() != int.class || types.length != 1 || types[0] != Object.class) continue;
			getIdMethod = method;
		}
		if (getIdMethod == null) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS BiomeBase#a method");
			return;
		}
		Object biomeRegistry;
		try {
			biomeRegistry = biomeField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS IRegistry.BIOME field", e1);
			return;
		}
		for (Field f : biomesClass.getDeclaredFields()) {
			if (f.getName().length() > 1) { // ignore the OCEAN duplicate Biomes.b
				Object base;
				try {
					base = f.get(null);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					br.getLogger().log(Level.SEVERE, "Issue while getting field of NMS Biomes class:", e);
					continue;
				}
				Biome biome;
				try {
					biome = (Biome) biomeBaseToBiomeField.invoke(null, base);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					br.getLogger().log(Level.SEVERE, "Issue while invoking CraftBlock#biomeBaseToBiome method:", e);
					continue;
				}
				int nr;
				try {
					nr = (int) getIdMethod.invoke(biomeRegistry, base);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					br.getLogger().log(Level.SEVERE, "Issue while getting invoking RegistryMaterials#a method:", e);
					continue;
				}
				byBiome.put(biome, nr);
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
