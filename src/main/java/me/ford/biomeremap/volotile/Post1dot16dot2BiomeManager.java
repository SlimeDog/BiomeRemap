package me.ford.biomeremap.volotile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

import me.ford.biomeremap.BiomeRemap;
import me.ford.biomeremap.mapping.BiomeMap;

public class Post1dot16dot2BiomeManager implements BiomeManager {
	private final BiomeRemap br;
	private final Map<Environment, Map<Biome, Integer>> perEnvByBiome = new EnumMap<Environment, Map<Biome, Integer>>(
			Environment.class);
	private final Class<?> biomeStorageClass;
	private final Class<?> biomeBaseClass;
	private final Class<?> craftChunkClass;
	private final Class<?> nmsChunkClass;
	private final Class<?> craftBlockClass;
	private final Method getHandleMethod;
	private final Method getBiomeIndexMethod;
	private final Method biomeToBiomeBaseMethod;
	private final Method biomeBaseToBiomeMethod;
	private final Field storageRegistryField;
	private final Field biomeBaseField;
	private final boolean post1dot16dot3;
	private final boolean post1dot16dot5;
	private final boolean post1dot17;

	public Post1dot16dot2BiomeManager(BiomeRemap br)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.br = br;
		String version = this.br.getServer().getClass().getPackage().getName().split("\\.")[3];
		Pattern pattern = Pattern.compile(Pattern.quote("MC: ") + "(\\d)\\.(\\d\\d)(\\.(\\d))?");
		Matcher matcher = pattern.matcher(this.br.getServer().getVersion());
		matcher.find();
		String minor = matcher.group(2);
		String revision = matcher.group(4); // optional
		int min = Integer.parseInt(minor);
		int rev;
		if (revision != null && !revision.isEmpty()) {
			rev = Integer.parseInt(revision);
		} else {
			rev = 0;
		}
		post1dot16dot3 = min > 16 || (min == 16 && rev >= 3);
		post1dot16dot5 = min > 16 || (min == 16 && rev >= 5);
		post1dot17 = min > 16 || (min == 16 && rev >= 0);

		Chunk tempChunk = Bukkit.getServer().getWorlds().get(0).getChunkAt(0, 0);
		craftChunkClass = tempChunk.getClass();
		getHandleMethod = craftChunkClass.getMethod("getHandle");
		Object nmsChunk = getHandleMethod.invoke(tempChunk);
		nmsChunkClass = nmsChunk.getClass();
		getBiomeIndexMethod = nmsChunkClass.getMethod("getBiomeIndex");
		Object biomeStorage = getBiomeIndexMethod.invoke(nmsChunk);
		biomeStorageClass = biomeStorage.getClass();
		storageRegistryField = biomeStorageClass
				.getDeclaredField(post1dot16dot5 ? (post1dot17 ? "e" : "registry") : "g");
		storageRegistryField.setAccessible(true);
		Object registry = storageRegistryField.get(biomeStorage);
		Class<?> iRegistryClass = registry.getClass();

		craftBlockClass = Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftBlock");
		biomeToBiomeBaseMethod = craftBlockClass.getMethod("biomeToBiomeBase",
				iRegistryClass.getSuperclass().getSuperclass(), Biome.class);
		Object nmsBiome = biomeToBiomeBaseMethod.invoke(null, registry, Biome.values()[0]);
		biomeBaseClass = nmsBiome.getClass();
		biomeBaseToBiomeMethod = craftBlockClass.getMethod("biomeBaseToBiome",
				iRegistryClass.getSuperclass().getSuperclass(), biomeBaseClass);
		biomeBaseField = biomeStorageClass.getDeclaredField(post1dot17 ? "f" : "h");
		biomeBaseField.setAccessible(true);

		// map biomes with reflection

		Class<?> biomesClass = Class.forName(
				(post1dot17 ? "net.minecraft.world.level.biome" : "net.minecraft.server." + version) + ".Biomes");

		Class<?> registryMaterialsClass = Class.forName(
				(post1dot17 ? "net.minecraft.core" : "net.minecraft.server." + version) + ".RegistryMaterials");

		Method getIdMethod = null;
		for (Method method : registryMaterialsClass.getMethods()) {
			if (!method.getName().equals("a"))
				continue;
			Class<?>[] types = method.getParameterTypes();
			if (method.getReturnType() != int.class || types.length != 1 || types[0] != Object.class)
				continue;
			getIdMethod = method;
			break;
		}
		if (getIdMethod == null) {
			br.getLogger().log(Level.SEVERE, "Issue while getting NMS BiomeBase#a method");
			return;
		}

		Field resourceKeyField = iRegistryClass.getDeclaredField("ay");
		Object resourceKey = resourceKeyField.get(null);
		Method getHandleMethod = Bukkit.getServer().getWorlds().get(0).getClass().getMethod("getHandle");
		for (World world : Bukkit.getWorlds()) {
			Map<Biome, Integer> curMap = new EnumMap<>(Biome.class);
			Object handle = getHandleMethod.invoke(world);
			Method getMinecraftServer = handle.getClass().getMethod("getMinecraftServer");
			Object mcServer = getMinecraftServer.invoke(handle);
			Method aXMethod;
			if (post1dot16dot3) {
				aXMethod = mcServer.getClass().getMethod("getCustomRegistry");
			} else {
				aXMethod = mcServer.getClass().getMethod("aX");
			}
			Object iRegistryCustomDimension = aXMethod.invoke(mcServer);
			Method dimensionAMethod = null;
			for (Method method : iRegistryCustomDimension.getClass().getMethods()) {
				if (!method.getName().equals("a")) {
					continue;
				}
				if (!Optional.class.equals(method.getReturnType())) {
					continue;
				}
				if (method.getParameterTypes().length != 1
						|| method.getParameterTypes()[0].isAssignableFrom(iRegistryCustomDimension.getClass())) {
					continue;
				}
				dimensionAMethod = method;
				break;
			}
			Object optionalBiomeRegistry = dimensionAMethod.invoke(iRegistryCustomDimension, resourceKey);
			Object biomeRegistry = ((Optional<?>) optionalBiomeRegistry).get();
			Method customRegistryAMethod = biomeRegistry.getClass().getMethod("a", resourceKey.getClass());
			for (Field f : biomesClass.getDeclaredFields()) {
				if (f.getName().length() > 1) { // ignore the OCEAN duplicate Biomes.b
					Object base;
					try {
						base = f.get(null);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						br.getLogger().log(Level.SEVERE, "Issue while getting field of NMS Biomes class:", e);
						throw e;
					}
					Biome biome;
					Object bb = customRegistryAMethod.invoke(biomeRegistry, base);
					try {
						biome = (Biome) biomeBaseToBiomeMethod.invoke(null, biomeRegistry, bb);
					} catch (IllegalArgumentException e) {
						br.getLogger().log(Level.SEVERE, "Issue while invoking CraftBlock#biomeBaseToBiome method:", e);
						throw e;
					}
					if (bb == null) {
						br.getLogger().warning("BiomeBase is null:" + base + "->" + bb + " ... " + biome);
						continue;
					}
					int nr;
					try {
						nr = (int) getIdMethod.invoke(biomeRegistry, bb);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						br.getLogger().log(Level.SEVERE, "Issue while getting invoking RegistryMaterials#a method:", e);
						throw e;
					}
					curMap.put(biome, nr);
				}
			}
			Map<Biome, Integer> prev = perEnvByBiome.get(world.getEnvironment());
			if (prev == null) {
				prev = new EnumMap<>(Biome.class);
			}
			prev.putAll(curMap);
			perEnvByBiome.put(world.getEnvironment(), prev);
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
		Object registry = storageRegistryField.get(biomeStorage);

		Object[] bases = (Object[]) biomeBaseField.get(biomeStorage);
		return (Biome) biomeBaseToBiomeMethod.invoke(null, registry, bases[nr]);
	}

	@Override
	public int getBiomeIndex(Biome biome) {
		for (Map<Biome, Integer> map : perEnvByBiome.values()) {
			Integer val = map.get(biome);
			if (val == null) {
				continue;
			} else {
				return val;
			}
		}
		throw new IllegalArgumentException("Could not find biome:" + biome);
	}

	@Override
	public void setBiomeNMS(World world, int x, int z, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Chunk chunk = world.getChunkAt(new Location(world, x, 0, z));
		int nr = x >> 2 << 2 | z >> 2;
		setBiomeNMS(chunk, nr, biome);
	}

	@Override
	public void setBiomeNMS(Chunk chunk, final int nr, Biome biome)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nr < 0 || nr > 15)
			br.getLogger().info("Trying to set biome at an incorrect number (NMS):" + nr);
		Object nmsChunk = getHandleMethod.invoke(chunk);
		Object biomeStorage = getBiomeIndexMethod.invoke(nmsChunk);

		Object[] bases = (Object[]) biomeBaseField.get(biomeStorage);
		Object registry = storageRegistryField.get(biomeStorage);
		Object nmsBiomeBase = biomeToBiomeBaseMethod.invoke(null, registry, biome);
		bases[nr] = nmsBiomeBase;
		BiomeMap map = br.getSettings().getApplicableBiomeMap(chunk.getWorld().getName());
		if (map.remapEntireChunk()) {
			remapHigherBiomes(bases, nmsBiomeBase, nr);
		}
	}

	public void remapHigherBiomes(Object[] bases, Object nmsBiomeBase, int nr) {
		int yNr = nr;
		for (int y = 0; y < 63; y++) {
			yNr += 16;
			bases[yNr] = nmsBiomeBase;
		}
	}

}