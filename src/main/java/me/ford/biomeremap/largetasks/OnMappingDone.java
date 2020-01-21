package me.ford.biomeremap.largetasks;

import java.util.function.BiConsumer;

import org.bukkit.Chunk;

/**
 * OnMappingDone
 */
public class OnMappingDone {
    private final int minX, minZ, maxX, maxZ;
    private final BiConsumer<Integer, Integer> onPopulate;
    private int count = 0;

    public OnMappingDone(BiConsumer<Integer, Integer> onPopulate, int minX, int minZ, int maxX, int maxZ) {
        this.onPopulate = onPopulate;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    public void afterRemap(Chunk chunk) {
        int curX = chunk.getX();
        int curZ = chunk.getZ();
        if (curX < minX || curX >= maxX || curZ < minZ || curZ >= maxZ) return; // only those in range!
        onPopulate.accept(chunk.getX(), chunk.getZ());
        count++;
    }

    public int getCount() {
        return count;
    }
    
}