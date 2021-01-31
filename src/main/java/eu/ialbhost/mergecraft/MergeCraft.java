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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MergeCraft extends JavaPlugin {
    private final Logger log = this.getLogger();
    private FileConfiguration customConfig = null;
    private Recipe recipe;
    private Set<User> users = new HashSet<>();

    public FileConfiguration getRecipesConfig() {
        return customConfig;
    }


    @Override
    public void onDisable(){
        log.log(Level.INFO, "Disabled version %s", getDescription().getVersion());
        SqlDAO.closeConnection();

    }

    @Override
    public void onEnable() {
        initializeDB();
//        initializeUsers();
        reloadConfigs();
        loadRecipes();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockMergeListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        registerCommand("points", new Points(this));


    }

//    private void initializeUsers() {
//        List<Player> players = new ArrayList<>(this.getServer().getOnlinePlayers());
//        for (Player player : players){
//            User user = User.getUser(player);
//            if (user == null) { // user wasn't found in DB, lets add him to DB
//                user = User.initUser(player);
//            }
//            addUser(user);
//            ChunkAccess chunkAccess = new ChunkAccess(player);
//            chunkAccess.setChunks(user.getChunks());
//        }
//    }

    private void initializeDB() {
        String sqlString= """
                create table IF NOT EXISTS USER (
                    ID int auto_increment primary key,
                    UUID VARCHAR(36) not null unique,
                    POINTS double,
                    CHUNKS BLOB,
                    LEVEL int,
                    CURRENT_EXP double,
                    NEEDED_EXP double,
                    MULTIPLIER double
                );""";
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
        ){
            pst.executeUpdate();
        }catch (SQLException exception) {
            exception.printStackTrace();
            log.log(Level.SEVERE, "Error while initializing table");
            getServer().getPluginManager().disablePlugin(this);
        }
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

    public User matchUser(Player player) {
        Set<User> users = this.users;
        User foundUser = null;
        for (User user : users){
            if(user.getPlayer().equals(player)){
                foundUser = user;
            }
        }
        return foundUser;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public Set<User> getUsers() {
        return this.users;
    }

    public Player matchPlayer(String[] split) {
        Player player;
        List<Player> players = getServer().matchPlayer(split[0]);
        if (players.isEmpty()) {
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
