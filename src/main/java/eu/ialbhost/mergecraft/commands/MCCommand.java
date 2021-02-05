package eu.ialbhost.mergecraft.commands;

import eu.ialbhost.mergecraft.MergeCraft;
import eu.ialbhost.mergecraft.User;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class MCCommand implements TabCompleter, CommandExecutor {
    private final MergeCraft plugin;

    public MCCommand(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, String[] args) {
        if (args.length > 2) return false; // too long
        if (!(sender instanceof Player)) return false; //console execution
        User user;
        user = plugin.matchUser((Player) sender);
        double amount;

        // TODO: correct permissions to use mergecraft command
        if (!sender.hasPermission("mergecraft.points.use")) {
            sender.sendMessage("no perms");
            return true;
        }

        if (args.length >= 1) {
            if (!(args[0].equals("buy") || args[0].equals("show") || args[0].equals("shop"))) {
                sender.sendMessage("wrong cmd");
                return false;
            }

            if (args.length == 2) {
                if (args[0].equals("buy") && !args[1].equals("chunk")) {
                    sender.sendMessage("wrong cmd");
                    return false;
                } else if (args[0].equals("show") && !args[1].equals("stats")) {
                    sender.sendMessage("wrong cmd");
                    return false;
                } else if (args[0].equals("shop")) {
                    sender.sendMessage("wrong cmd");
                    return false;
                }

                if (args[1].equals("chunk") && !sender.hasPermission("mergecraft.chunk.buy")) {
                    sender.sendMessage("no perms");
                    return true;
                } else if (args[1].equals("stats") && !sender.hasPermission("mergecraft.show.stats")) {
                    sender.sendMessage("no perms");
                    return true;
                } else if (args[0].equals("shop") && !sender.hasPermission("mergecraft.use.shop")) {
                    sender.sendMessage("no perms");
                    return true;
                }

                if (args[1].equals("chunk")) {
                    if (user.getActiveChunk() != null) {
                        amount = user.getChunks().size() * 1000;
                        if (user.hasPoints(amount)) {
                            try {
                                user.setSQLNumber(user.getPoints() - amount, "POINTS");
                                Set<Chunk> chunkSet = user.getChunks();
                                chunkSet.add(user.getActiveChunk());
                                user.setSQLChunks(chunkSet);
                                user.setSQLNumber(user.getMultiplier() + 0.1, "MULTIPLIER");
                                sender.sendMessage("You have purchased this chunk!");
                                user.rmHologram();
                                user.setActiveChunk(null);
                                return true;
                            } catch (SQLException exception) {
                                user.getPlayer().kickPlayer("[MergeCraft] SQL Exception: Purchasing chunk failed");
                                sender.getServer().getLogger().log(Level.SEVERE,
                                        "SQL Exception purchasing chunk failed", exception);
                            }
                        } else {
                            sender.sendMessage("You don't have enough points!");
                        }
                    } else {
                        sender.sendMessage("You have no chunks active for purchase!");
                        return true;
                    }
                }
            }
        }


        return true;
    }

    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {

        return null;
    }
}
