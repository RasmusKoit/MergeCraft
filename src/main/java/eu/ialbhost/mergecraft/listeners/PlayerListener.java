package eu.ialbhost.mergecraft.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;


public final class PlayerListener implements Listener {
    private final MergeCraft plugin;
    private long timeMs = System.currentTimeMillis();
    private Hologram hologram;

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
            if (user.hasChunk(toLocation.getChunk())) {
                player.sendMessage("You can access this chunk");
            } else {
                long currentTime = System.currentTimeMillis();
                if (currentTime - this.timeMs >= 5000) {
                    player.sendMessage("You CANT access this chunk");
                    setTimestamp(currentTime);
                    if (hologram != null) {
                        hologram.delete();
                    }
                    Location middleChunkBlock = toLocation.getChunk().getBlock(8, (int) player.getLocation().getY(), 8).getLocation();
                    setHologram(HologramsAPI.createHologram(plugin, middleChunkBlock.add(0.0, 3.0, 0.0)));
                    TextLine textLine = hologram.appendTextLine("Purchase this chunk");
                    TextLine textLine3 = hologram.appendTextLine("/points buy [" +
                                                                 toLocation.getChunk().getX() + ", " +
                                                                 toLocation.getChunk().getZ() + "]");

                    TextLine textLine1 = hologram.appendTextLine("...");
                    TextLine textLine2 = hologram.insertTextLine(0, "...");

                    ItemLine itemLine1 = hologram.appendItemLine(new ItemStack(Material.STONE));
                    ItemLine itemLine2 = hologram.insertItemLine(0, new ItemStack(Material.STONE));
                    user.setActiveChunk(toLocation.getChunk());


                }
                event.setCancelled(true);
            }
        }
    }

    private void setHologram(Hologram hologram) {
        this.hologram = hologram;
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
        plugin.getLogger().log(Level.INFO, player.getDisplayName() + ": chunk is " + player.getChunk().toString());
        if (user == null) { // user wasn't found in DB, lets add him to DB
            try {
                user = new User(player);
                user.initSQLUser();
            } catch (SQLException exception) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "SQL Exception: User initialization failed");
                plugin.getServer().getLogger().log(Level.SEVERE, "User initialization failed", exception);
            }
        }
        plugin.addUser(user);
        event.getPlayer().sendMessage(user.getChunks().toString());

        plugin.getLogger().log(Level.INFO, player.getDisplayName() + ": chunk is " + player.getChunk().toString());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        User user = User.getSQLUser(player);
        if (user.getChunks() == null) {
            Set<Chunk> chunkSet = new HashSet<>(1);
            chunkSet.add(event.getSpawnLocation().getChunk());
            try {
                user.setSQLChunks(chunkSet);
            } catch (SQLException exception) {
                player.kickPlayer("User initialization failed, please try again");
                plugin.getServer().getLogger().log(Level.SEVERE, "User initialization failed, setting chunks", exception);
            }

        }
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent event) {
        User user = plugin.matchUser(event.getPlayer());
        plugin.removeUser(user);
    }
}
