package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.Recipe;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class BlockMergeListener implements Listener {
    private MergeCraft plugin;
    public BlockMergeListener(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        mergeNeeded(placedBlock, event.getPlayer());
    }

    public void mergeNeeded(Block placed, Player player) {
        // TODO:
        //  Check if block material has a recipe
        //  Based on match merge blocks
    }

    public Set<Block> findBlocks(Block placed) {
        Set<Block> foundBlocks = new HashSet<>(); // blocks we find with algorithm
        Set<Block> searchedBlocks = new HashSet<>(); // blocks we have finished searching
        foundBlocks.add(placed);
        final BlockFace[] directions = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        while ((foundBlocks.iterator().hasNext()) && (searchedBlocks.size() < 256)) { // hard limit our search to 1 chunk
            Block foundBlock = foundBlocks.iterator().next();
            for (BlockFace direction : directions) {
                if (foundBlock.getRelative(direction).getType() == placed.getType()) {
                    if (!searchedBlocks.contains(foundBlock.getRelative(direction))) {
                        foundBlocks.add(foundBlock.getRelative(direction));
                    }
                }
            }
            searchedBlocks.add(foundBlock);
            foundBlocks.remove(foundBlock);
        }
        return searchedBlocks;

    }


}
