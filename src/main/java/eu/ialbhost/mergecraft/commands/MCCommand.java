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

            if (args.length >= 2) {
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
                                user.setSQLNumber(user.getMultiplier() + 0.01, "MULTIPLIER");
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
                } else if (args[1].equals("stats")) {
                    showStats(user, sender);
                    if (args.length == 3) {
                        try {
                            targetPlayer = plugin.matchPlayer(args[2]);
                            if (targetPlayer == null) {
                                sender.sendMessage("Player not found");
                                return true;
                            }
                            targetUser = plugin.matchUser(targetPlayer);
                        } catch (NoSuchElementException exception) {
                            sender.sendMessage("Player not found, this seems to be a bug!");
                            plugin.getServer().getLogger().log(Level.SEVERE, "No matching user found, when player exists!", exception);
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
        Player player = user.getPlayer();
        sender.sendMessage(
                String.format("""
                                %s: STATS
                                Level: %.0f
                                Points: %.0f
                                Experience: [%.0f / %.0f]
                                Multiplier: %.2f
                                Owned Chunks: %d        
                                """, player.getDisplayName(), user.getLevel(), user.getPoints(), user.getCurrentExp(),
                        user.getNeededExp(), user.getMultiplier(), user.getChunks().size())
        );

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
