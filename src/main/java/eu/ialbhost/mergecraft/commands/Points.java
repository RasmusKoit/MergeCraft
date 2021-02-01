package eu.ialbhost.mergecraft.commands;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class Points implements CommandExecutor {
    private final MergeCraft plugin;

    public Points(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, String[] args) {
        if (args.length > 2) return false; // too long
        if (plugin.checkPlayer(sender) && args.length == 0) return false; //console execution

        sender.sendMessage("Total users size: " + plugin.getUsers().size());
        User user = plugin.matchUser((Player) sender);
        Player targetPlayer;
        User targetUser;
        double amount;
        //args checking
        if (!sender.hasPermission("mergecraft.points.use")) {
            sender.sendMessage("You don't have permission to use /points command");
            return true;
        }
        if (args.length >= 1) {
            targetPlayer = plugin.matchPlayer(args);
            if (!sender.hasPermission("mergecraft.points.other") && !targetPlayer.equals(sender)) {
                sender.sendMessage("You don't have permission to check others points");
                return true;
            }
            if (targetPlayer == null) {
                sender.sendMessage("Player not found");
                return true;
            }
            try {
                targetUser = plugin.matchUser(targetPlayer);
            } catch (NoSuchElementException exception) {
                exception.printStackTrace();
                return false;
            }
            if (args.length == 2) {
                if (!sender.hasPermission("mergecraft.points.give")) {
                    sender.sendMessage("You don't have permission to use /points command");
                    return true;
                }
                if (targetPlayer.equals(sender)) {
                    sender.sendMessage("You are trying to send yourself points");
                    return true;
                }
                try {
                    amount = Double.parseDouble(args[1]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage("Second argument must be number!");
                    return false;
                }
                if (!has(user, amount)) {
                    sender.sendMessage("You do not have enough points to give");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage("This is not a valid amount!");
                    return false;
                }
                try {
                    // take senders points away
                    user.setSQLNumber(user.getPoints() - amount, "POINTS");
                    // add target users points
                    targetUser.setSQLNumber(targetUser.getPoints() + amount, "POINTS");
                    sender.sendMessage("You have sent " + amount + " points to " + targetPlayer.getDisplayName());
                    targetPlayer.sendMessage(((Player) sender).getDisplayName() + " has sent you " + amount + " points");
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }

            } else {
                sender.sendMessage(targetPlayer.getDisplayName() + " has: " + targetUser.getPoints() + " points");
            }
        }
        sender.sendMessage("You have: " + user.getPoints() + " points");
        return true;
    }


    public boolean has(User user, double amount) {
        return user.getPoints() - amount >= 0;
    }
}
