package eu.ialbhost.mergecraft;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkData {
    private final int x;
    private final int z;
    private final static ChunkData WEST = new ChunkData(8, 14);
    private final static ChunkData EAST = new ChunkData(8, 2);
    private final static ChunkData SOUTH = new ChunkData(14, 8);
    private final static ChunkData NORTH = new ChunkData(2, 8);
    private final static ChunkData SOUTHWEST = new ChunkData(14, 14);
    private final static ChunkData SOUTHEAST = new ChunkData(14, 2);
    private final static ChunkData NORTHWEST = new ChunkData(2, 14);
    private final static ChunkData NORTHEAST = new ChunkData(2, 2);

    private final static ChunkData EAST_DIR = new ChunkData(0, -1);
    private final static ChunkData WEST_DIR = new ChunkData(0, 1);
    private final static ChunkData NORTH_DIR = new ChunkData(-1, 0);
    private final static ChunkData SOUTH_DIR = new ChunkData(1, 0);
    private final static ChunkData SOUTHWEST_DIR = new ChunkData(1, 1);
    private final static ChunkData SOUTHEAST_DIR = new ChunkData(1, -1);
    private final static ChunkData NORTHWEST_DIR = new ChunkData(-1, 1);
    private final static ChunkData NORTHEAST_DIR = new ChunkData(-1, -1);


    public ChunkData(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Chunk toChunk(World world) {
        return world.getChunkAt(x, z);
    }

    public static ChunkData getDirection(ChunkData fromChunk, ChunkData toChunk) {
        ChunkData searchChunk = new ChunkData(fromChunk.x - toChunk.x, fromChunk.z - toChunk.z);
        if (WEST_DIR.getX() == searchChunk.getX() && WEST_DIR.getZ() == searchChunk.getZ()) {
            return WEST;
        } else if (EAST_DIR.getX() == searchChunk.getX() && EAST_DIR.getZ() == searchChunk.getZ()) {
            return EAST;
        } else if (SOUTH_DIR.getX() == searchChunk.getX() && SOUTH_DIR.getZ() == searchChunk.getZ()) {
            return SOUTH;
        } else if (NORTH_DIR.getX() == searchChunk.getX() && NORTH_DIR.getZ() == searchChunk.getZ()) {
            return NORTH;
        } else if (SOUTHWEST_DIR.getX() == searchChunk.getX() && SOUTHWEST_DIR.getZ() == searchChunk.getZ()) {
            return SOUTHWEST;
        } else if (SOUTHEAST_DIR.getX() == searchChunk.getX() && SOUTHEAST_DIR.getZ() == searchChunk.getZ()) {
            return SOUTHEAST;
        } else if (NORTHWEST_DIR.getX() == searchChunk.getX() && NORTHWEST_DIR.getZ() == searchChunk.getZ()) {
            return NORTHWEST;
        } else if (NORTHEAST_DIR.getX() == searchChunk.getX() && NORTHEAST_DIR.getZ() == searchChunk.getZ()) {
            return NORTHEAST;
        }
        return null;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
