package eu.ialbhost.mergecraft.commands;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import static eu.ialbhost.mergecraft.Permissions.*;
import static eu.ialbhost.mergecraft.Text.*;


public class PointsCommand implements TabCompleter, CommandExecutor {
    private final MergeCraft plugin;

    public PointsCommand(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, String[] args) {
        if (args.length > 3) return false; // too long
        if (!(sender instanceof Player)) return false; //console execution
        User user;
        Player targetPlayer;
        User targetUser;
        double amount;
        /*

          /points [ cmd ][ <user> ][ amount ]
          /points
          /points  show     user
          /points  give     user     100.0

        */


        if (!sender.hasPermission(PERM_POINTS_USE)) {
            sender.sendMessage(MSG_NO_PERM);
            return true;
        }

        if (args.length >= 1) {
            if (!(args[0].equals("show") || args[0].equals("give"))) {
                sender.sendMessage(MSG_NO_CMD);
                return false;
            }
            //mergecraft.points.show or mergecraft.points.give
            if (args[0].equals("show") && !sender.hasPermission(PERM_POINTS_SHOW)) {
                sender.sendMessage(MSG_NO_PERM);
                return true;
            }
            if (args[0].equals("give") && !sender.hasPermission(PERM_POINTS_GIVE)) {
                sender.sendMessage(MSG_NO_PERM);
                return true;
            }
            if (args.length >= 2) {
                try {
                    targetPlayer = plugin.matchPlayer(args[1]);
                    if (targetPlayer == null) {
                        sender.sendMessage(MSG_NO_PLAYER);
                        return true;
                    }

                    targetUser = plugin.matchUser(targetPlayer);

                } catch (NoSuchElementException exception) {
                    sender.sendMessage(MC_HDR + MSG_SQL_EXCEPTION_NO_PLAYER);
                    plugin.getLogger().log(Level.SEVERE, MSG_SQL_EXCEPTION_NO_PLAYER, exception);
                    return true;
                }
                if (args.length == 3) {
                    if (targetPlayer == sender) {
                        sender.sendMessage(MSG_POINTS_SELF);
                        return true;
                    }
                    user = plugin.matchUser((Player) sender);
                    try {
                        amount = Double.parseDouble(args[2]);
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(MSG_INVALID_ARG);
                        return false;
                    }
                    if (amount <= 0) {
                        sender.sendMessage(MSG_NEG_NUMBER);
                        return false;
                    }
                    if (user.hasPoints(amount)) {
                        sender.sendMessage(MSG_NO_POINTS);
                        return true;
                    }
                    // give command
                    try {
                        // take senders points away
                        user.setSQLNumber(user.getPoints() - amount, "POINTS");
                        // add target users points
                        targetUser.setSQLNumber(targetUser.getPoints() + amount, "POINTS");
                        sender.sendMessage(msgPointsSent(amount, targetPlayer));
                        targetPlayer.sendMessage(msgPointsReceived(amount, (Player) sender));
                    } catch (SQLException exception) {
                        targetPlayer.kickPlayer(MC_HDR + MSG_SQL_EXCEPTION_POINTS);
                        user.getPlayer().kickPlayer(MC_HDR + MSG_SQL_EXCEPTION_POINTS);
                        plugin.getLogger().log(Level.SEVERE, MSG_SQL_EXCEPTION_POINTS, exception);
                    }
                    // give command ending
                } else {

                    // check command
                    sender.sendMessage(msgPointsShowOther(targetUser));
                    // check command ending
                }
                return true;

            }
        }
        user = plugin.matchUser((Player) sender);
        sender.sendMessage(msgPointsShow(user));
        return true;
    }

    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        List<String> tabHints = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("points")) {
            switch (args.length) {
                case 1:
                    tabHints.addAll(Arrays.asList("show", "give"));
                    return tabHints;
                case 2:
                    return null;
                case 3:
                    User user = plugin.matchUser((Player) sender);
                    if (args[0].equals("give")) {
                        tabHints.add(user.getPoints().toString());
                    }
                    return tabHints;
                default:
                    return tabHints;
            }
        }
        return null;
    }


}
