package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.Experience;
import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.Recipe;
import eu.ialbhost.mergecraft.User;
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

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlockMergeListener implements Listener {
    private static final Set<BlockFace> DIRECTIONS = Set.of(BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST);

    private final MergeCraft plugin;
    private final Experience exp = new Experience();

    public BlockMergeListener(MergeCraft plugin) {
        this.plugin = plugin;
    }

    public static Set<Block> findBlocks(Block placed) {
        Set<Block> foundBlocks = new HashSet<>(); // blocks we find with algorithm
        Set<Block> searchedBlocks = new HashSet<>(); // blocks we have finished searching
        foundBlocks.add(placed);

        while ((foundBlocks.iterator().hasNext()) && (searchedBlocks.size() < 256)) { // hard limit our search to 1 chunk
            Block foundBlock = foundBlocks.iterator().next();
            for (BlockFace direction : DIRECTIONS) {
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

    private static void playMergeEffect(Location location, Player player) {
        double locX = location.getX() + 0.5;
        double locY = location.getY() + 0.2;
        double locZ = location.getZ() + 0.5;

        player.spawnParticle(Particle.REDSTONE, locX, locY, locZ,
                0, 0.001, 1, 0, 1,
                new Particle.DustOptions(Color.GREEN, 3));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if (plugin.hasRecipe(placedBlock.getType())) {
            try {
                mergeIfNeeded(placedBlock, event.getPlayer());
            } catch (SQLException exception) {
                event.getPlayer().kickPlayer("[MergeCraft] SQL Exception: Failed merging blocks");
                event.getPlayer().getServer().getLogger().log(Level.SEVERE, "Failed placing blocks", exception);
            }
        }
    }

    public void mergeIfNeeded(Block placed, Player player) throws SQLException {
        String placedBlockName = placed.getType().toString();
        Set<Block> searchedBlocks = findBlocks(placed);
        Map<Integer, Integer> merge = findMergeAmount(searchedBlocks);
        if (merge != null) {
            int key = merge.keySet().iterator().next();
            int value = merge.get(key);
            Recipe recipe = plugin.matchRecipe(placed.getType());
            for (int i = 0; i < value; i++) {
                Block transformBlock = searchedBlocks.iterator().next();
                transformBlock.setType(recipe.getMerge_to());
                playMergeEffect(transformBlock.getLocation(), player);
                searchedBlocks.remove(transformBlock);
            }
            for (int k = 0; k < (key - value); k++) {
                Block removeBlock = searchedBlocks.iterator().next();
                removeBlock.setType(Material.AIR);
                searchedBlocks.remove(removeBlock);
            }
            double xpGain = exp.calculateExpEarned(plugin.matchUser(player), recipe.getExp(), value);
            User user = plugin.matchUser(player);
            user.setSQLNumber(user.getPoints() + (value * user.getMultiplier()), "POINTS");
            player.sendMessage("You gained: " + xpGain);
            player.sendMessage("Merged " + placedBlockName.toLowerCase().replace("_", " ") +
                    " into " + value + " " +
                    recipe.getMerge_to().toString().toLowerCase().replace("_", " "));
        }

    }

    private Map<Integer, Integer> findMergeAmount(Set<Block> searchedBlocks) {
        if (searchedBlocks.size() < 3) return null;


        Map<Integer, Integer> mergeMap = new HashMap<>();
        for (String elem : plugin.getMergeAmounts()) {
            String[] splitByEqualsSign = elem.split("=");
            mergeMap.put(Integer.valueOf(splitByEqualsSign[0]), Integer.valueOf(splitByEqualsSign[1]));
        }

        List<Integer> mergeByAmount = mergeMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        int mergeAmountKey = 0;
        for (int amount : mergeByAmount) {
            if (searchedBlocks.size() - amount >= 0) {
                mergeAmountKey = amount;
            }
        }

        return Map.of(mergeAmountKey, mergeMap.get(mergeAmountKey));
    }


}
