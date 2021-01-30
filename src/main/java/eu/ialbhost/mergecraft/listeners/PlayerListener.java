package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.ChunkAccess;
import eu.ialbhost.mergecraft.MergeCraft;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.logging.Level;


public final class PlayerListener implements Listener {
    ChunkAccess chunkAccess;
    private final MergeCraft plugin;
    public PlayerListener (MergeCraft plugin) {
        this.plugin = plugin;
    }
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
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if(!event.getPlayer().hasPermission("mergecraft.use.bonemeal")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player player = event.getPlayer();
                if(player.getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL)) {
                    event.getPlayer().sendMessage("Cant use bonemeal");
                    event.setCancelled(true);
                } else if (player.getInventory().getItemInOffHand().getType().equals(Material.BONE_MEAL)) {
                    event.getPlayer().sendMessage("Cant use bonemeal");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onServerJoin(PlayerJoinEvent event) {
        Bukkit.broadcastMessage("Player: " + event.getPlayer().getDisplayName());
        chunkAccess = new ChunkAccess(event.getPlayer());
        Chunk currentChunk = event.getPlayer().getChunk();
        if(chunkAccess.getChunks().isEmpty()) {
            chunkAccess.addChunk(currentChunk);
        }
    }
}
