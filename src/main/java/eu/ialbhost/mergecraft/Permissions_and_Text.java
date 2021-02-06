package eu.ialbhost.mergecraft;

import org.bukkit.entity.Player;

public final class Permissions_and_Text {
    // points command permissions
    public static final String PERM_POINTS_USE = "mergecraft.points.use";
    public static final String PERM_POINTS_SHOW = "mergecraft.points.show";
    public static final String PERM_POINTS_GIVE = "mergecraft.points.give";
    // world related permissions
    public static final String PERM_BONEMEAL_USE = "mergecraft.use.bonemeal";
    // Mergecraft command permissions
    public static final String PERM_MC_USE = "mergecraft.mc.use";
    public static final String PERM_MC_CHUNK = "mergecraft.mc.chunk";
    public static final String PERM_MC_SHOW = "mergecraft.mc.show";
    public static final String PERM_MC_SHOW_OTHER = "mergecraft.mc.show.other";
    public static final String PERM_MC_SHOP = "mergecraft.mc.shop";

    // Permission related text
    // General
    public static final String MC_HDR = "[MergeCraft]: ";
    public static final String MSG_NO_PERM = MC_HDR + "You have no permission to use that command!";
    public static final String MSG_NO_BONEMEAL = MC_HDR + "You don't have permission to use bonemeal!";
    public static final String MSG_NEG_NUMBER = MC_HDR + "This number can't be negative";
    public static final String MSG_INVALID_ARG = MC_HDR + "This argument is not valid!";
    public static final String MSG_NO_CMD = MC_HDR + "There is no such command available!";
    public static final String MSG_NO_PLAYER = MC_HDR + "No player found!";
    public static final String MSG_PLAYER_ONLY = MC_HDR + "You have to be a player!";
    public static final String MSG_SQL_EXCEPTION_USER_QUERY = "SQL Exception: Get user query failed";
    // Exceptions and Errors
    public static final String MSG_SQL_EXCEPTION_NO_PLAYER = "No player found, this seems to be a bug!";
    public static final String MSG_SQL_ERROR_CONFIGURE = "You need to configure and use MySQL access in order to use this plugin!";
    public static final String MSG_SQL_ERROR_CONFIGURE_USE = "Config is found in config.yml. This error is caused by mysql.use being false";
    public static final String MSG_SQL_EXCEPTION_USER_INIT = "SQL Exception: User initialization failed";
    public static final String MSG_SQL_EXCEPTION_POINTS = "SQL Exception: Setting or removing points failed!";
    public static final String MSG_SQL_EXCEPTION_CHUNK_PURCHASE = "SQL Exception: Purchasing chunk failed!";
    public static final String MSG_ERROR_LOAD_CONFIG = "Error loading config from plugin, disabling plugin";
    public static final String MSG_SQL_EXCEPTION_MERGE_BLOCKS = "SQL Exception: Failed merging blocks";
    // MergeCraft
    public static final String MSG_NO_ACTIVE_CHUNK = MC_HDR + "You don't have chunks selected!";
    // Points cmd
    public static final String MSG_NO_POINTS = MC_HDR + "You don't have enough points!";
    public static final String MSG_POINTS_SELF = MC_HDR + "You cant give yourself points";

    public static String MSG_LEVEL_UP(Double level) {
        return (String.format("You have leveled up to level: %.0f", level));
    }

    public static final String MSG_CHUNK_PURCHASE = MC_HDR + "You have purchased a chunk!";
    public static final String MSG_CHUNK_ACCESS = MC_HDR + "You can't access this chunk!";

    public static String MSG_XP_GAIN(Double xpGain) {
        return (String.format("You gained: %.0f", xpGain));
    }

    public static String MSG_MERGE_BLOCKS(String blockName, Integer value, Recipe recipe) {
        return (String.format("Merged %s into %d %s",
                blockName.toLowerCase().replace("_", " "),
                value,
                recipe.getMerge_to().toString().toLowerCase().replace("_", " ")
        ));
    }

    public static String MSG_WARNING_CMD_NOT_FOUND(String name) {
        return ("This command: " + name + " is not found in plugin.yml");
    }

    public static String MSG_POINTS_SENT(Double amount, Player player) {
        return (MC_HDR + "You have sent " + amount + " points to " + player.getDisplayName());
    }

    public static String MSG_POINTS_RECEIVED(Double amount, Player player) {
        return (MC_HDR + player.getDisplayName() + " has sent you " + amount + " points");
    }

    public static String MSG_POINTS_SHOW_OTHER(User user) {
        return (MC_HDR + user.getPlayer().getDisplayName() + " has total of: " + user.getPoints() + " points");
    }

    public static String MSG_POINTS_SHOW(User user) {
        return (MC_HDR + "You have total of: " + user.getPoints() + " points");
    }

    public static String MSG_MC_STATS(User user) {
        return (String.format("""
                        %s: STATS
                        Level: %.0f
                        Points: %.0f
                        Experience: [%.0f / %.0f]
                        Multiplier: %.2f
                        Owned Chunks: %d
                        """, user.getPlayer().getDisplayName(), user.getLevel(), user.getPoints(),
                user.getCurrentExp(), user.getNeededExp(), user.getMultiplier(), user.getChunks().size()));
    }


}
