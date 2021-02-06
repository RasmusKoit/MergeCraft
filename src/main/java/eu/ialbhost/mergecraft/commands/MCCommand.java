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
import java.util.*;
import java.util.logging.Level;

import static eu.ialbhost.mergecraft.Permissions_and_Text.*;

public class MCCommand implements TabCompleter, CommandExecutor {
    private final MergeCraft plugin;

    public MCCommand(MergeCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, String[] args) {
        if (args.length > 3) return false; // too long
        if (!(sender instanceof Player)) return false; //console execution
        User user;
        user = plugin.matchUser((Player) sender);
        double amount;
        Player targetPlayer;
        User targetUser;

        if (!sender.hasPermission(PERM_MC_USE)) {
            sender.sendMessage(MSG_NO_PERM);
            return true;
        }

        if (args.length >= 1) {
            if (!(args[0].equals("buy") || args[0].equals("show") || args[0].equals("shop"))) {
                sender.sendMessage(MSG_NO_CMD);
                return false;
            }

            if (args.length >= 2) {
                if (args[0].equals("buy") && !args[1].equals("chunk")) {
                    sender.sendMessage(MSG_NO_CMD);
                    return false;
                } else if (args[0].equals("show") && !args[1].equals("stats")) {
                    sender.sendMessage(MSG_NO_CMD);
                    return false;
                } else if (args[0].equals("shop")) {
                    sender.sendMessage(MSG_NO_CMD);
                    return false;
                }

                if (args[1].equals("chunk") && !sender.hasPermission(PERM_MC_CHUNK)) {
                    sender.sendMessage(MSG_NO_PERM);
                    return true;
                } else if (args[1].equals("stats") && !sender.hasPermission(PERM_MC_SHOW)) {
                    sender.sendMessage(MSG_NO_PERM);
                    return true;
                } else if (args[0].equals("shop") && !sender.hasPermission(PERM_MC_SHOP)) {
                    sender.sendMessage(MSG_NO_PERM);
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
                                user.setSQLNumber(user.getMultiplier() + 0.01, "MULTIPLIER");
                                sender.sendMessage(MSG_CHUNK_PURCHASE);
                                user.rmHologram();
                                user.setActiveChunk(null);
                                return true;
                            } catch (SQLException exception) {
                                user.getPlayer().kickPlayer(MC_HDR + MSG_SQL_EXCEPTION_CHUNK_PURCHASE);
                                plugin.getLogger().log(Level.SEVERE,
                                        MSG_SQL_EXCEPTION_CHUNK_PURCHASE, exception);
                            }
                        } else {
                            sender.sendMessage(MSG_NO_POINTS);
                        }
                    } else {
                        sender.sendMessage(MSG_NO_ACTIVE_CHUNK);
                        return true;
                    }
                } else if (args[1].equals("stats")) {
                    showStats(user, sender);
                    if (args.length == 3) {
                        if (!sender.hasPermission(PERM_MC_SHOW_OTHER)) {
                            sender.sendMessage(MSG_NO_PERM);
                            return true;
                        }
                        try {
                            targetPlayer = plugin.matchPlayer(args[2]);
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
                        showStats(targetUser, sender);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public void showStats(User user, CommandSender sender) {
        sender.sendMessage(MSG_MC_STATS(user));

    }

    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        List<String> tabHints = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("mergecraft")) {
            switch (args.length) {
                case 1:
                    tabHints.addAll(Arrays.asList("show", "buy", "shop"));
                    return tabHints;
                case 2:
                    if (args[0].equals("show")) {
                        tabHints.addAll(Collections.singletonList("stats"));
                    } else if (args[0].equals("buy")) {
                        tabHints.addAll(Collections.singletonList("chunk"));
                    }
                    return tabHints;
                case 3:
                    if (args[1].equals("stats")) {
                        return null;
                    }
                default:
                    return tabHints;
            }
        }
        return null;
    }
}
