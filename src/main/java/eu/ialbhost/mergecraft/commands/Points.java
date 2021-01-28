package eu.ialbhost.mergecraft.commands;

import eu.ialbhost.mergecraft.MergeCraft;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Points implements CommandExecutor {
    private final MergeCraft plugin;

    public Points(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 2) return false; // too long
        if (plugin.checkPlayer(sender) && args.length == 0) return false; //console execution

        Player player = (Player) sender;
//        Economy econ = MergeCraft.getEconomy();
        double amount = 0;

        //points <player>
        if (args.length == 1) {
            player = plugin.matchPlayer(args, sender);
            if (player == null) return false;
        }
        if (args.length == 2) {
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
                return false;
            }
        }
        // permission handling
        if ((player == sender) && (!sender.hasPermission("mergecraft.points.use"))) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to check your points");
            return true;
        } else if (!sender.hasPermission("mergecraft.points.other")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to check other player points");
            return true;
        } else if ((args.length == 2) && (!sender.hasPermission("mergecraft.points.give"))) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to give others points");
            return true;
        }

        //points <player> <amount>
        if (args.length == 2) {
//            if (!(econ.has((OfflinePlayer) sender, amount))) {
            if (true) {
                sender.sendMessage(ChatColor.RED + "You don't have enough points to give!");
                return true;
            } else {
//                econ.withdrawPlayer((OfflinePlayer) sender, amount);
//                econ.depositPlayer(player, amount);
                sender.sendMessage("You have given " + amount + "points to " + player.getDisplayName());
            }
        } else {
            sender.sendMessage(player.getDisplayName() + " points: " + amount);
        }
        return true;
    }
}
