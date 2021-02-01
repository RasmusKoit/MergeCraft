package eu.ialbhost.mergecraft;

import eu.ialbhost.mergecraft.commands.Points;
import eu.ialbhost.mergecraft.listeners.BlockMergeListener;
import eu.ialbhost.mergecraft.listeners.PlayerListener;
import eu.ialbhost.mergecraft.listeners.WorldListener;
import org.bukkit.Material;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MergeCraft extends JavaPlugin {
    private final Logger log = this.getLogger();
    private final Set<User> users = new HashSet<>();
    private final Set<Recipe> recipes = new HashSet<>();
    private final List<String> mergeAmounts = new ArrayList<>();
    private FileConfiguration recipeConfig;


    @Override
    public void onDisable() {
        log.log(Level.INFO, "Disabled version %s", getDescription().getVersion());
        SqlDAO.closeConnection();

    }

    @Override
    public void onEnable() {
        reloadConfigs();
        initializeDB();
        loadRecipes();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockMergeListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        registerCommand("points", new Points(this));


    }

    private void initializeDB() {
        String sqlString = """
                CREATE TABLE IF NOT EXISTS USER (
                    ID int auto_increment PRIMARY KEY,
                    UUID VARCHAR(36) NOT NULL UNIQUE,
                    POINTS double,
                    CHUNKS BLOB,
                    LEVEL double,
                    CURRENT_EXP double,
                    NEEDED_EXP double,
                    MULTIPLIER double
                )""";
        if (!this.getConfig().getBoolean("sql.use")) {
            log.log(Level.SEVERE, "You need to configure and use MySQL access in order to use this plugin!");
            log.log(Level.SEVERE, "Config is found in config.yml. This error is caused by mysql.use being false");
            getServer().getPluginManager().disablePlugin(this);
            this.setEnabled(false);
            return;
        }
        SqlDAO.setJdbcUrl(this.getConfig().getString("sql.jdbcUrl"));
        SqlDAO.setUsername(this.getConfig().getString("sql.username"));
        SqlDAO.setPassword(this.getConfig().getString("sql.password"));
        SqlDAO.setupDataSource();
        try (Connection con = SqlDAO.getConnection()) {
            PreparedStatement pst = con.prepareStatement(sqlString);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException exception) {
            log.log(Level.SEVERE, "Error while initializing table", exception);
            getServer().getPluginManager().disablePlugin(this);
            this.setEnabled(false);

        }
    }

    //This is so ugly :S
    @SuppressWarnings("unchecked")
    private void loadRecipes() {
        if (recipeConfig.getList("recipes") != null) {
            List<?> recipeListFromConfig = recipeConfig.getList("recipes");
            if (recipeListFromConfig != null) {
                for (Object recipeObject : recipeListFromConfig) {
                    if (recipeObject != null) {
                        Map<String, Map<String, Object>> recipeObjToMap = (Map<String, Map<String, Object>>) recipeObject;
                        Map<String, Object> reMap = recipeObjToMap.get("recipe");
                        Recipe recipe = new Recipe(
                                Material.matchMaterial(reMap.get("merge_from").toString()),
                                Material.matchMaterial(reMap.get("merge_to").toString()),
                                (Double) reMap.get("exp"));
                        addRecipe(recipe);
                    }
                }
            }
        }
        mergeAmounts.addAll(recipeConfig.getStringList("amounts"));
        log.log(Level.INFO, "Total loaded recipe size: " + recipes.size());

    }

    public void reloadConfigs() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(false);
        saveConfig();
        File recipesConfigFile = new File(getDataFolder(), "recipes.yml");
        if (!recipesConfigFile.exists()) {
            saveResource("recipes.yml", false);
        }
        this.recipeConfig = YamlConfiguration.loadConfiguration(recipesConfigFile);
        InputStream config = this.getResource("recipes.yml");
        if (config == null) {
            log.log(Level.SEVERE, "Error loading default config from plugin, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            this.setEnabled(false);

            return;
        }

        try (Reader defConfigStream = new InputStreamReader(config, StandardCharsets.UTF_8)) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.recipeConfig.setDefaults(defConfig);
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute that command, you need to be a player!");
            return true;
        }

        return false;
    }

    public Set<Recipe> getRecipes() {
        return this.recipes;
    }

    public User matchUser(Player player) {
        Set<User> users = this.users;
        return users.stream()
                .filter(u -> u.getPlayer().equals(player))
                .findFirst()
                .orElseThrow();
    }

    public void addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
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
        List<Player> players = getServer().matchPlayer(split[0]);
        return players.isEmpty()
                ? null
                : players.get(0);
    }


    public boolean hasRecipe(Material type) {
        return this.recipes.stream().anyMatch(p -> p.getMerge_from() == type);

    }

    public Recipe matchRecipe(Material type) {
        Set<Recipe> recipes = this.recipes;
        return recipes.stream()
                .filter(u -> u.getMerge_from().equals(type))
                .findFirst()
                .orElseThrow();
    }

    public List<String> getMergeAmounts() {
        return this.mergeAmounts;
    }
}
