package me.ford.biomeremap.largetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.World;

import me.ford.biomeremap.BiomeRemap;

/**
 * LargeTempScanTask
 */
public class LargeTempScanTask extends LargeTask {
    private final Map<Double, Integer> temps = new HashMap<>();
    private final Consumer<TemperatureReport> tConsumer;
    private final int yLayer;

    public LargeTempScanTask(BiomeRemap plugin, World world, int minX, int maxX, int minZ, int maxZ, boolean debug,
            int progressStep, Consumer<String> progress, Consumer<TaskReport> ender, Consumer<TemperatureReport> tempConsumer, int yLayer) {
        super(plugin, world, minX, maxX, minZ, maxZ, debug, progressStep, progress, ender);
        this.tConsumer = tempConsumer;
        this.yLayer = yLayer;
    }


	@Override
	protected void doTaskForChunk(int chunkX, int chunkZ, boolean debug) {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        for (int curX = x; curX < x + 16; curX++) {
            for (int curZ = z; curZ < z + 16; curZ++) {
                double cTemp = getWorld().getTemperature(x, yLayer, z);
                Integer prev = temps.get(cTemp);
                if (prev == null) prev = 0;
                temps.put(cTemp, ++prev);
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