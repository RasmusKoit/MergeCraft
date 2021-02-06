package eu.ialbhost.mergecraft.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import eu.ialbhost.mergecraft.ChunkData;
import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;


public final class PlayerListener implements Listener {
    private final MergeCraft plugin;
    private long timeMs = System.currentTimeMillis();

    public PlayerListener(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        User user = plugin.matchUser(player);
        Location toLocation = event.getTo();
        Location fromLocation = event.getFrom();

        if (fromLocation.getChunk() != toLocation.getChunk()) {
            if (!user.hasChunk(toLocation.getChunk())) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - this.timeMs >= 5000) {
                    player.sendMessage("You CANT access this chunk");
                    setTimestamp(currentTime);
                    Hologram hologram = user.getHologram();
                    if (hologram != null) {
                        user.rmHologram();
                    }
                    ChunkData fromChunk = new ChunkData(fromLocation.getChunk().getX(), fromLocation.getChunk().getZ());
                    ChunkData toChunk = new ChunkData(toLocation.getChunk().getX(), toLocation.getChunk().getZ());
                    ChunkData placeInto = ChunkData.getDirection(fromChunk, toChunk);
                    if (placeInto != null) {
                        Location middleChunkBlock = toLocation.getChunk().getBlock(placeInto.getX(), (int) player.getLocation().getY(), placeInto.getZ()).getLocation();
                        user.setHologram(HologramsAPI.createHologram(plugin, middleChunkBlock.add(0.0, 3.0, 0.0)));
                        hologram = user.getHologram();
                        hologram.appendTextLine("Purchase this chunk").setTouchHandler(p -> p.performCommand("mc buy chunk"));
                        hologram.appendTextLine("Click me to purchase").setTouchHandler(p -> p.performCommand("mc buy chunk"));
                        hologram.appendTextLine("or").setTouchHandler(p -> p.performCommand("mc buy chunk"));
                        hologram.appendTextLine("/mc buy chunk").setTouchHandler(p -> p.performCommand("mc buy chunk"));
                        hologram.appendTextLine("Cost: " + (user.getChunks().size() * 1000) + " points")
                                .setTouchHandler(p -> p.performCommand("mc buy chunk"));

                        hologram.appendTextLine("...");
                        hologram.insertTextLine(0, "...");

                        hologram.appendItemLine(new ItemStack(Material.STONE));
                        hologram.insertItemLine(0, new ItemStack(Material.STONE));
                        user.setActiveChunk(toLocation.getChunk());
                    }

                }
                event.setCancelled(true);
            }
        }
    }


    private void setTimestamp(long currentTimeMillis) {
        this.timeMs = currentTimeMillis;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission("mergecraft.use.bonemeal")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player player = event.getPlayer();
                if (player.getInventory().getItemInMainHand().getType().equals(Material.BONE_MEAL)) {
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
    public void onServerJoin(PlayerLoginEvent event) {
        //Initialize player
        Player player = event.getPlayer();
        User user = User.getSQLUser(player);
        plugin.getLogger().log(Level.INFO, "%s: chunk is %s",
                new Object[]{player.getDisplayName(), player.getChunk().toString()});
        if (user == null) { // user wasn't found in DB, lets add him to DB
            try {
                user = new User(player);
                user.initSQLUser();
            } catch (SQLException exception) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "SQL Exception: User initialization failed");
                plugin.getLogger().log(Level.SEVERE, "User initialization failed", exception);
            }
        }
        plugin.addUser(user);

        plugin.getLogger().log(Level.INFO, "%s: chunk is %s",
                new Object[]{player.getDisplayName(), player.getChunk().toString()});

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = plugin.matchUser(player);
        if (user.getChunks() == null) {
            Set<Chunk> chunkSet = new HashSet<>(1);
            chunkSet.add(event.getPlayer().getChunk());
            try {
                user.setSQLChunks(chunkSet);
            } catch (SQLException | NoSuchElementException exception) {
                player.kickPlayer("User initialization failed, please try again");
                plugin.getLogger().log(Level.SEVERE, "User initialization failed, setting chunks", exception);
            }

        }
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent event) {
        User user = plugin.matchUser(event.getPlayer());
        user.rmHologram();

        plugin.removeUser(user);
    }
}
