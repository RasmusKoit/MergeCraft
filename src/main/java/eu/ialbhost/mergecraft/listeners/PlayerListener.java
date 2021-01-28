package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.ChunkAccess;
import eu.ialbhost.mergecraft.MergeCraft;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public final class PlayerListener implements Listener {
    ChunkAccess chunkAccess;
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        //temp chunkaccess generator
        if (chunkAccess == null) {
            chunkAccess = new ChunkAccess(player);
            chunkAccess.addChunk(player.getChunk());
        }
        Location toLocation = event.getTo();
        Location fromLocation = event.getFrom();

        if (fromLocation.getChunk() != toLocation.getChunk()) {
            if (chunkAccess.hasAccess(toLocation.getChunk())) {
                player.sendMessage("You can access this chunk");
            } else {
                player.sendMessage("You CANT access this chunk");
                player.teleport(fromLocation);
            }
        }
    }
    @EventHandler
    public void onServerJoin(PlayerJoinEvent event) {
        Bukkit.broadcastMessage("Player: " + event.getPlayer().getDisplayName());
        chunkAccess = new ChunkAccess(event.getPlayer());
        Chunk currentChunk = event.getPlayer().getChunk();
        if(chunkAccess.getChunks().size() == 0) {
            chunkAccess.addChunk(currentChunk);
        }
    }
}
