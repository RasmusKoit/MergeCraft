package eu.ialbhost.mergecraft;

import eu.ialbhost.mergecraft.commands.Points;
import eu.ialbhost.mergecraft.listeners.BlockMergeListener;
import eu.ialbhost.mergecraft.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MergeCraft extends JavaPlugin {
//    private static Economy econ = null;
    private final Logger log = this.getLogger();

    @Override
    public void onDisable(){
        log.log(Level.INFO, "Disabled version %s", getDescription().getVersion());
    }
    @Override
    public void onEnable() {
//        if (!setupEconomy() ) {

        //
//            getServer().getPluginManager().disablePlugin(this);
//        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockMergeListener(), this);

        registerCommand("points", new Points(this));


    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = this.getCommand(name);
        if (command == null) {
            log.log(Level.WARNING, "This command: " + name + " is not found in plugin.yml");
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
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
