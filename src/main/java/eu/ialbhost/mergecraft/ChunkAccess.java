package eu.ialbhost.mergecraft;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ChunkAccess {
    private Player player;
    private List<Chunk> chunks;

    public ChunkAccess(Player player) {
        this.player = player;
        this.chunks = new ArrayList<>();
    }

    public ChunkAccess(Player player, List<Chunk> chunks) {
        this.player = player;
        this.chunks = chunks;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void addChunk(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public boolean hasAccess(Chunk searchChunk) {
        try {
            return chunks.contains(searchChunk);
        } catch (NullPointerException err) {
            return false;
        }
    }
}
