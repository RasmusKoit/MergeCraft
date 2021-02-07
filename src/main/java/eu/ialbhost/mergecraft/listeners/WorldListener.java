package eu.ialbhost.mergecraft.listeners;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.HashSet;
import java.util.Set;

public class WorldListener implements Listener {
    private final MergeCraft plugin;

    public WorldListener(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDispenserUse(BlockDispenseEvent event) {
        if (!plugin.getConfig().getBoolean("world.dispenser.bonemeal.allow")) {
            if (event.getItem().getType().equals(Material.BONE_MEAL)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void dragonEggTpEvent(BlockFromToEvent event) {
        if (event.getBlock().getType().equals(Material.DRAGON_EGG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            Chunk ignitedChunk = event.getBlock().getChunk();
            Set<Chunk> allRenderedChunks = new HashSet<>(plugin.getUsers().size());
            for (User user : plugin.getUsers()) {
                allRenderedChunks.addAll(user.getChunksToRender());
            }
            if (!allRenderedChunks.contains(ignitedChunk)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void preEntitySpawn(PreCreatureSpawnEvent event) {
        Chunk creatureChunk = event.getSpawnLocation().getChunk();
        if (!plugin.getRenderedChunks().contains(creatureChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Chunk creatureChunk = event.getEntity().getLocation().getChunk();
        if (!plugin.getRenderedChunks().contains(creatureChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        if (event.getFrom().getChunk() != event.getTo().getChunk()) {
            if (!plugin.getRenderedChunks().contains(event.getTo().getChunk())) {
                // cool whoosh effect
                event.getEntity().remove(); // remove suicidal void walking entities!
            }
        }
    }


}
