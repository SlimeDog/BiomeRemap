package me.ford.biomeremap.mapping;

import java.util.Objects;

public final class ChunkLoc {
    private final int x, z;

    public ChunkLoc(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ChunkLoc)) return false;
        ChunkLoc other = (ChunkLoc) o;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

}