package eu.ialbhost.mergecraft;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkData {
    private final int x;
    private final int z;

    public ChunkData(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Chunk toChunk(World world) {
        return world.getChunkAt(x, z);
    }
}
