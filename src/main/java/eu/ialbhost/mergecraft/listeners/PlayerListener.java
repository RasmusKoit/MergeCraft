package eu.ialbhost.mergecraft.listeners;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.logging.Level;


public final class PlayerListener implements Listener {
    private final MergeCraft plugin;

    //    private final UserDAO dao;
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
                player.sendMessage("You CANT access this chunk");
                event.setCancelled(true);
            }
        }
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
        if (user == null) { // user wasn't found in DB, lets add him to DB
            try {
                user = User.initSQLUser(player);
            } catch (SQLException exception) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "SQL Exception: User initialization failed");
                event.getPlayer().getServer().getLogger().log(Level.SEVERE, "User initialization failed", exception);
            }
        }
        if (user != null) {
            plugin.addUser(user);
            event.getPlayer().sendMessage(user.getChunks().toString());
        }
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent event) {
        User user = plugin.matchUser(event.getPlayer());
        plugin.removeUser(user);
    }
}
