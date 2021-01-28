package eu.ialbhost.mergecraft;

import eu.ialbhost.mergecraft.commands.Points;
import eu.ialbhost.mergecraft.listeners.BlockMergeListener;
import eu.ialbhost.mergecraft.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;


public class MergeCraft extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
//    private static Economy econ = null;

    @Override
    public void onDisable(){
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }
    @Override
    public void onEnable() {
//        if (!setupEconomy() ) {
//            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
//            getServer().getPluginManager().disablePlugin(this);
//        }
        log.info(String.format("[%s] version %s has been enabled!", getDescription().getName(), getDescription().getVersion()));
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockMergeListener(), this);

        getCommand("points").setExecutor(new Points(this));


    }

//    private boolean setupEconomy() {
//        if (getServer().getPluginManager().getPlugin("Vault") == null) {
//            return false;
//        }
//        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
//        if (rsp == null) {
//            return false;
//        }
//        econ = rsp.getProvider();
//        return true;
//    }


    public boolean checkPlayer(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute that command, you need to be a player!");
            return true;
        } else {
            return false;
        }
    }

    public Player matchPlayer(String[] split, CommandSender sender) {
        Player player;
        List<Player> players = getServer().matchPlayer(split[0]);
        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unknown player");
            player = null;
        } else {
            player = players.get(0);
        }
        return player;
    }


//    public static Economy getEconomy() {
//        return econ;
//    }
}
