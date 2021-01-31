package eu.ialbhost.mergecraft.commands;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

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
        Player targetPlayer = plugin.matchPlayer(args);
        User targetUser = plugin.matchUser(targetPlayer);
        double amount = 0;
        //args checking
        if (!sender.hasPermission("mergecraft.points.use")) {
            sender.sendMessage("You don't have permission to use /points command");
            return true;
        }
        if (args.length >= 1) {
            if (!sender.hasPermission("mergecraft.points.other") && !targetPlayer.equals(sender)) {
                sender.sendMessage("You don't have permission to check others points");
                return true;
            }
            if (targetPlayer == null) {
                sender.sendMessage("Player not found");
                return false;
            }

            if (targetUser == null) {
                sender.sendMessage("Player not found");
                return false;
            }

        }
        // todo this and the upper block can both be called. is this intentional?
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
        }

        // output
        // todo repeated conditions. maybe you can merge something?
        if (args.length >= 1) {
            if (args.length == 2) {
                // take senders points away
                user.setSQLNumber(user.getPoints() - amount, "POINTS");
                // add target users points
                targetUser.setSQLNumber(targetUser.getPoints() + amount, "POINTS");
                sender.sendMessage("You have sent " + amount + " points to " + targetPlayer.getDisplayName());
                targetPlayer.sendMessage(((Player) sender).getDisplayName() + " has sent you " + amount + " points");
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
