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


        if (!sender.hasPermission("mergecraft.points.use")) {
            sender.sendMessage("no perms");
            return true;
        }

        if (args.length >= 1) {
            if (!(args[0].equals("show") || args[0].equals("give"))) {
                sender.sendMessage("wrong cmd");
                return false;
            }
            if (!sender.hasPermission("mergecraft.points." + args[0])) {
                sender.sendMessage("no perms");
                return true;
            }
            if (args.length >= 2) {
                try {
                    targetPlayer = plugin.matchPlayer(args[1]);
                    if (targetPlayer == null) {
                        sender.sendMessage("Player not found");
                        return true;
                    }

                    targetUser = plugin.matchUser(targetPlayer);

                } catch (NoSuchElementException exception) {
                    sender.sendMessage("Player not found, this seems to be a bug!");
                    plugin.getServer().getLogger().log(Level.SEVERE, "No matching user found, when player exists!", exception);
                    return true;
                } catch (NumberFormatException exception) {
                    sender.sendMessage("not valid number");
                    return false;
                }
                if (args.length == 3) {
                    if (targetPlayer == sender) {
                        sender.sendMessage("You can't give yourself points");
                        return true;
                    }
                    user = plugin.matchUser((Player) sender);
                    amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage("neg number");
                        return false;
                    }
                    if (user.hasPoints(amount)) {
                        sender.sendMessage("not enough points");
                        return true;
                    }
                    // give command
                    try {
                        // take senders points away
                        user.setSQLNumber(user.getPoints() - amount, "POINTS");
                        // add target users points
                        targetUser.setSQLNumber(targetUser.getPoints() + amount, "POINTS");
                        sender.sendMessage("You have sent " + amount + " points to " + targetPlayer.getDisplayName());
                        targetPlayer.sendMessage(((Player) sender).getDisplayName() + " has sent you " + amount + " points");
                    } catch (SQLException exception) {
                        targetPlayer.kickPlayer("[MergeCraft] SQL Exception: Setting points failed");
                        user.getPlayer().kickPlayer("[MergeCraft] SQL Exception: Removing points failed");
                        sender.getServer().getLogger().log(Level.SEVERE,
                                "SQL Exception setting/removing points for users", exception);
                    }
                    // give command ending
                } else {

                    // check command
                    sender.sendMessage(targetPlayer.getDisplayName() + " has total of: " +
                            targetUser.getPoints() + " points");

                    // check command ending
                }
                return true;

            }
        }
        user = plugin.matchUser((Player) sender);
        sender.sendMessage("You have total of: " + user.getPoints() + " points!");
        return true;
    }


    /*
      /points [ cmd ][ <user> ][ amount ]
      /points
      /points  show     user
      /points  give     user     100.0
    */

    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        List<String> tabHints = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("points")) {
            switch (args.length) {
                case 1:
                    String[] commandHints = new String[]{"show", "give"};
                    tabHints.addAll(Arrays.asList(commandHints));
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