package eu.ialbhost.mergecraft;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class ChunkAccess {
    final private Player player;
    private HashSet<Chunk> chunks;


    public ChunkAccess(Player player) {
        this(player, new HashSet<>());
    }

    public ChunkAccess(Player player, HashSet<Chunk> chunks) {
        this.player = player;
        this.chunks = chunks;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashSet<Chunk> getChunks() {
        return this.chunks;
    }

    public void setChunks(HashSet<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public boolean hasAccess(Chunk searchChunk) {

        return chunks.contains(searchChunk);

    }
}
