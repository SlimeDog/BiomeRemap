package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;

import dev.ratas.slimedogcore.api.SlimeDogPlugin;

/**
 * LargeTempScanTask
 */
public class LargeTempScanTask extends LargeTask {
    private static final int BIOME_SIZE = 4;
    private final Map<Double, Integer> temps = new HashMap<>();
    private final Consumer<TemperatureReport> tConsumer;
    private final int minLayer;
    private final int maxLayer;

    public LargeTempScanTask(SlimeDogPlugin plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
            int progressStep, Consumer<String> progress, Consumer<TaskReport> ender,
            Consumer<TemperatureReport> tempConsumer, int minLayer, int maxLayer) {
        super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
        this.tConsumer = tempConsumer;
        this.minLayer = minLayer;
        this.maxLayer = maxLayer;
    }

    @Override
    protected void doTaskForChunk(int chunkX, int chunkZ, boolean debug) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        for (int curX = x; curX < x + 16; curX += BIOME_SIZE) {
            for (int curZ = z; curZ < z + 16; curZ += BIOME_SIZE) {
                for (int curY = minLayer; curY < maxLayer; curY += BIOME_SIZE) {
                    double cTemp = getWorld().getTemperature(x, curY, z);
                    Integer prev = temps.get(cTemp);
                    if (prev == null)
                        prev = 0;
                    temps.put(cTemp, ++prev);
                }
            }
        }
    }

    @Override
    protected void whenDone() {
        tConsumer.accept(new TemperatureReport(temps));
    }

    public static class TemperatureReport {
        private final Map<Double, Integer> temps;

        public TemperatureReport(Map<Double, Integer> temps) {
            this.temps = temps;
        }

        public Map<Double, Integer> getTemps() {
            return temps;
        }

    }

}