package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.Recipe;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class BlockMergeListener implements Listener {
    private final MergeCraft plugin;
    private final Recipe recipe;

    public BlockMergeListener(MergeCraft plugin) {
        this.plugin = plugin;
        this.recipe = plugin.getRecipe();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if (recipe.containsRecipe(placedBlock.getType().toString())) {
            mergeIfNeeded(placedBlock, event.getPlayer());
        }
    }

    public void mergeIfNeeded(Block placed, Player player) {
        String placedBlockName = placed.getType().toString();
        Set<Block> searchedBlocks = findBlocks(placed);
        LinkedHashMap<Integer, Integer> merge = findMergeAmount(searchedBlocks);
        if (merge != null) {
            int key = merge.keySet().iterator().next();
            int value = merge.get(key);
            Material mergeMat = recipe.getRecipe(placedBlockName);
            for (int i = 0; i < value; i++) {
                Block transformBlock = searchedBlocks.iterator().next();
                transformBlock.setType(mergeMat);
                playMergeEffect(transformBlock.getLocation(), player);
                searchedBlocks.remove(transformBlock);
            }
            for (int k = 0; k < (key - value); k++) {
                Block removeBlock = searchedBlocks.iterator().next();
                removeBlock.setType(Material.AIR);
                searchedBlocks.remove(removeBlock);
            }
            player.sendMessage("Merged " + placedBlockName.toLowerCase().replace("_", " ") +
                    " into " + value + " " + mergeMat.toString().toLowerCase().replace("_", " "));
        }

    }


    private LinkedHashMap<Integer, Integer> findMergeAmount(Set<Block> searchedBlocks) {
        if (searchedBlocks.size() < 3) return null;
        LinkedHashMap<Integer, Integer> found = new LinkedHashMap<>();
        int mergeAmountKey = 0;
        List<String> recipeMergeAmountsList = this.plugin.getRecipe().getMergeAmounts();
        Map<Integer, Integer> mergeMapList = new HashMap<>();
        for (String elem : recipeMergeAmountsList) {
            mergeMapList.put(Integer.valueOf(elem.split("=")[0]), Integer.valueOf(elem.split("=")[1]));
        }
        List<Integer> mergeByAmount = new ArrayList<>(mergeMapList.keySet());
        Collections.sort(mergeByAmount);
        for (Integer amount : mergeByAmount) {
            if ((searchedBlocks.size() - amount) >= 0) {
                mergeAmountKey = amount;
            }
        }
        int value = mergeMapList.get(mergeAmountKey);
        found.put(mergeAmountKey, value);
        return found;

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


    private void playMergeEffect(Location location, Player player) {
        double locX = location.getX() + 0.5;
        double locY = location.getY() + 0.2;
        double locZ = location.getZ() + 0.5;

        player.spawnParticle(Particle.REDSTONE, locX, locY, locZ,
                0, 0.001, 1, 0, 1,
                new Particle.DustOptions(Color.GREEN, 3));
    }


}
