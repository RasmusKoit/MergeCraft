package eu.ialbhost.mergecraft;

import eu.ialbhost.mergecraft.commands.Points;
import eu.ialbhost.mergecraft.listeners.BlockMergeListener;
import eu.ialbhost.mergecraft.listeners.PlayerListener;
import eu.ialbhost.mergecraft.listeners.WorldListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MergeCraft extends JavaPlugin {
    private final Logger log = this.getLogger();
    private FileConfiguration customConfig = null;
    private Recipe recipe;

    public FileConfiguration getRecipesConfig() {
        return customConfig;
    }


    @Override
    public void onDisable(){
        log.log(Level.INFO, "Disabled version %s", getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        reloadConfigs();
        loadRecipes();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockMergeListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        registerCommand("points", new Points(this));


    }

    public void loadRecipes() {
        this.recipe = new Recipe(this);
    }

    public void reloadConfigs() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(false);
        saveConfig();
        File customConfigFile = new File(getDataFolder(), "recipes.yml");
        if(!customConfigFile.exists()) {
            saveResource("recipes.yml", false);
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        InputStream config = this.getResource("recipes.yml");
        if (config == null) {
            log.log(Level.SEVERE, "Error loading default config from plugin, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try (Reader defConfigStream = new InputStreamReader(config, StandardCharsets.UTF_8)) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        } catch (IOException exception) {
            log.log(Level.SEVERE, "Error loading config", exception);
        }
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

    public Recipe getRecipe() {
        return recipe;
    }


}
