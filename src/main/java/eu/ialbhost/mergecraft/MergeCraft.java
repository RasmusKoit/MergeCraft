package eu.ialbhost.mergecraft;

import eu.ialbhost.mergecraft.commands.MCCommand;
import eu.ialbhost.mergecraft.commands.PointsCommand;
import eu.ialbhost.mergecraft.database.SqlDAO;
import eu.ialbhost.mergecraft.listeners.BlockInteractListener;
import eu.ialbhost.mergecraft.listeners.PacketListener;
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

import static eu.ialbhost.mergecraft.Text.*;


public class MergeCraft extends JavaPlugin {
    private final Logger log = this.getLogger();
    private final Set<User> users = new HashSet<>();
    private final Set<Recipe> recipes = new HashSet<>();
    private final List<String> mergeAmounts = new ArrayList<>();
    private FileConfiguration recipeConfig;
    private static MergeCraft instance;


    @Override
    public void onDisable() {
        log.log(Level.INFO, "Disabled version %s", getDescription().getVersion());
        SqlDAO.closeConnection();
        users.clear();

    }

    public static MergeCraft getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        reloadConfigs();
        initializeDB();
        loadRecipes();
        new PacketListener(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        registerCommand("points", new PointsCommand(this));
        registerCommand("mergecraft", new MCCommand(this));
        initializeOnlineUsers();

    }

    private void initializeOnlineUsers() {
        for (Player player : getServer().getOnlinePlayers()) {
            User user = User.getSQLUser(player);
            if (user == null) { // user wasn't found in DB, lets add him to DB
                try {
                    user = new User(player);
                    user.initSQLUser();
                } catch (SQLException exception) {
                    player.kickPlayer(MC_HDR + MSG_SQL_EXCEPTION_USER_INIT);
                    getLogger().log(Level.SEVERE, MSG_SQL_EXCEPTION_USER_INIT, exception);
                }
            }
            addUser(user);
        }
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
            log.log(Level.SEVERE, MSG_SQL_ERROR_CONFIGURE);
            log.log(Level.SEVERE, MSG_SQL_ERROR_CONFIGURE_USE);
            getServer().getPluginManager().disablePlugin(this);
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
            log.log(Level.SEVERE, MSG_ERROR_LOAD_CONFIG);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try (Reader defConfigStream = new InputStreamReader(config, StandardCharsets.UTF_8)) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.recipeConfig.setDefaults(defConfig);
        } catch (IOException exception) {
            log.log(Level.SEVERE, MSG_ERROR_LOAD_CONFIG, exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = this.getCommand(name);
        if (command == null) {
            log.log(Level.WARNING, msgWarningCmdNotFound(name));
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

    @SuppressWarnings("unused")
    public boolean checkPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MSG_PLAYER_ONLY);
            return true;
        }

        return false;
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

    public Player matchPlayer(String playerUUID) {
        List<Player> players = getServer().matchPlayer(playerUUID);
        return players.isEmpty()
                ? null
                : players.get(0);
    }

    public Set<User> getUsers() {
        return users;
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
