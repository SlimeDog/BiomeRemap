package me.ford.biomeremap.volotile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;

/**
 * VolotileBiomeManager
 */
public class VolatileBiomeManager implements BiomeManager {
	private final BiomeRemap br;
	private final Map<Biome, Integer> byBiome = new HashMap<>();
	private final Class<?> biomeStorageClass;
	private final Class<?> biomeBaseClass;
	private final Class<?> craftChunkClass;
	private final Class<?> nmsChunkClass;
	private final Class<?> craftBlockClass;
	private final Method getHandleMethod;
	private final Method getBiomeIndexMethod;
	private final Method biomeToBiomeBaseMethod;
	private final Method biomeBasetoBiomeMethod;
	private final Field biomeBaseField;

	public VolatileBiomeManager(BiomeRemap br)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		this.br = br;
		String version = this.br.getServer().getClass().getPackage().getName().split("\\.")[3];
		// assume post 1.17

		// get classes needed for biome getting and biome setting methods
		biomeStorageClass = Class.forName("net.minecraft.world.level.chunk.BiomeStorage");
		biomeBaseClass = Class.forName("net.minecraft.world.level.biome.BiomeBase");
		craftChunkClass = Class.forName("org.bukkit.craftbukkit." + version + ".CraftChunk");
		nmsChunkClass = Class.forName("net.minecraft.world.level.chunk.Chunk");
		craftBlockClass = Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftBlock");
		// get methods needed for biome getting and setting methods
		getHandleMethod = craftChunkClass.getMethod("getHandle");
		getBiomeIndexMethod = nmsChunkClass.getMethod("getBiomeIndex");
		biomeToBiomeBaseMethod = craftBlockClass.getMethod("biomeToBiomeBase", Biome.class);
		biomeBasetoBiomeMethod = craftBlockClass.getMethod("biomeBaseToBiome", biomeBaseClass);
		// get fields needed for biome getting and setting methods
		biomeBaseField = biomeStorageClass.getDeclaredField("g");
		biomeBaseField.setAccessible(true);

		// map biomes with reflection

		Class<?> biomesClass = Class.forName("net.minecraft.server." + version + ".Biomes");

		Class<?> iRegistryClass = Class.forName("net.minecraft.server." + version + ".IRegistry");
		Class<?> registryMaterialsClass = Class.forName("net.minecraft.server." + version + ".RegistryMaterials");
		Field biomeField = iRegistryClass.getDeclaredField("BIOME");
		Method getIdMethod = null;
		for (Method method : registryMaterialsClass.getMethods()) {
			if (!method.getName().equals("a"))
				continue;
			Class<?>[] types = method.getParameterTypes();
			if (method.getReturnType() != int.class || types.length != 1 || types[0] != Object.class)
				continue;
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
					biome = (Biome) biomeBasetoBiomeMethod.invoke(null, base);
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
	public Biome getBiomeNMS(World world, int x, int z)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Chunk chunk = world.getChunkAt(new Location(world, x, 0, z));
		int nr = x >> 2 << 2 | z >> 2;
		return getBiomeNMS(chunk, nr);
	}

	@Override
	public Biome getBiomeNMS(Chunk chunk, int nr)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object nmsChunk = getHandleMethod.invoke(chunk);
		Object biomeStorage = getBiomeIndexMethod.invoke(nmsChunk);

		Object[] bases = (Object[]) biomeBaseField.get(biomeStorage);
		return (Biome) biomeBasetoBiomeMethod.invoke(null, bases[nr]);
	}

	@Override
	public int getBiomeIndex(Biome biome) {
		return byBiome.get(biome);
	}

	@Override
	public void setBiomeNMS(World world, int x, int z, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Chunk chunk = world.getChunkAt(new Location(world, x, 0, z));
		int nr = x >> 2 << 2 | z >> 2;
		setBiomeNMS(chunk, nr, biome);
	}

	@Override
	public void setBiomeNMS(Chunk chunk, int nr, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nr < 0 || nr > 15)
			br.getLogger().info("Trying to set biome at an incorrect number (NMS):" + nr);
		Object nmsChunk = getHandleMethod.invoke(chunk);
		Object biomeStorage = getBiomeIndexMethod.invoke(nmsChunk);

		Object[] bases = (Object[]) biomeBaseField.get(biomeStorage);
		Object nmsBiomeBase = biomeToBiomeBaseMethod.invoke(null, biome);
		bases[nr] = nmsBiomeBase;
	}

}