package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;

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


}
