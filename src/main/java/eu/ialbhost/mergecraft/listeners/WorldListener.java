package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.MergeCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;

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


}
