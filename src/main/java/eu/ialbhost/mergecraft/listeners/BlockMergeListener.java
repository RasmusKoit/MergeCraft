package eu.ialbhost.mergecraft.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class BlockMergeListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        mergeNeeded(placedBlock, event.getPlayer());
    }

    public void mergeNeeded(Block placed, Player player) {
        Set<Block> foundBlocks = new HashSet<>() {{add(placed);}};
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if(placed.getType() == placed.getRelative(x, 0, z).getType()) {
                    foundBlocks.add(placed.getRelative(x, 0, z));
                    player.sendMessage("Found a match");
                }
            }
        }
        if (foundBlocks.size() >= 3 && placed.getType() == Material.POPPY) {
            player.sendMessage("Found 3 or more");
            for (Block block : foundBlocks) {
                block.setType(Material.AIR);
            }
            placed.setType(Material.RED_TULIP);
        }
    }


}
