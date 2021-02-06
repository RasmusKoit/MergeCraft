package eu.ialbhost.mergecraft;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.gson.Gson;
import eu.ialbhost.mergecraft.database.SqlDAO;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static eu.ialbhost.mergecraft.Permissions_and_Text.*;

public class User {
    private final static Gson GSON = new Gson();

    private final Experience exp = new Experience();
    private final Player player;
    private Double points;
    private Set<Chunk> chunks;
    private Double level;
    private Double currentExp;
    private Double neededExp;
    private Double multiplier;
    private Chunk activeChunk;
    private Hologram hologram;


    public User(Player player) {
        this(player, 0.0, null, 1.0, 0.0, 100.0, 1.0);
    }

    public User(Player player, Double points, Set<Chunk> chunks, Double level, Double currentExp, Double neededExp, Double multiplier) {
        this.player = player;
        this.points = points;
        this.chunks = chunks;
        this.level = level;
        this.currentExp = currentExp;
        this.neededExp = neededExp;
        this.multiplier = multiplier;
    }

    public static User getSQLUser(Player player) {
        String sqlString = "SELECT * FROM USER WHERE UUID = ?";
        User user = null;
        try (Connection con = SqlDAO.getConnection();
             PreparedStatement pst = con.prepareStatement(sqlString)) {
            pst.setString(1, player.getUniqueId().toString());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                user = new User(player);
                user.populate(rs, player);
            }
            rs.close();
        } catch (SQLException exception) {
            player.kickPlayer(MC_HDR + MSG_SQL_EXCEPTION_USER_QUERY);
            player.getServer().getLogger().log(Level.SEVERE, MC_HDR + MSG_SQL_EXCEPTION_USER_QUERY, exception);
        }
        return user;
    }

    public static String chunksToStr(Set<Chunk> chunks) {
        List<ChunkData> chunkDataList = new ArrayList<>(chunks.size());
        for (Chunk chunk : chunks) {
            ChunkData data = new ChunkData(chunk.getX(), chunk.getZ());
            chunkDataList.add(data);
        }
        return GSON.toJson(chunkDataList);
    }

    public void initSQLUser() throws SQLException {
        String sqlString = """
                INSERT INTO USER
                (UUID, POINTS, CHUNKS, LEVEL, CURRENT_EXP, NEEDED_EXP, MULTIPLIER) 
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (Connection con = SqlDAO.getConnection();
             PreparedStatement pst = con.prepareStatement(sqlString)) {
            pst.setString(1, getPlayer().getUniqueId().toString());
            pst.setDouble(2, getPoints());
            pst.setNull(3, Types.NULL);
            pst.setDouble(4, getLevel());
            pst.setDouble(5, getCurrentExp());
            pst.setDouble(6, getNeededExp());
            pst.setDouble(7, getMultiplier());
            pst.executeUpdate();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Double getLevel() {
        return level;
    }

    public void setLevel(Double level) {
        this.level = level;
    }

    public Double getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(Double currentExp) {
        this.currentExp = currentExp;
    }

    public Double getNeededExp() {
        return neededExp;
    }

    public void setNeededExp(Double neededExp) {
        this.neededExp = neededExp;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public Set<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(Set<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void populate(ResultSet rs, Player player) throws SQLException {
        setPoints(rs.getDouble("POINTS"));
        setLevel(rs.getDouble("LEVEL"));
        setCurrentExp(rs.getDouble("CURRENT_EXP"));
        setNeededExp(rs.getDouble("NEEDED_EXP"));
        setMultiplier(rs.getDouble("MULTIPLIER"));
        HashSet<Chunk> chunkSet = new HashSet<>();
        ChunkData[] chunkData = GSON.fromJson(rs.getString("CHUNKS"), (Type) ChunkData[].class);
        if (chunkData != null) {
            for (ChunkData data : chunkData) {
                chunkSet.add(data.toChunk(player.getWorld()));
            }
            setChunks(chunkSet);
        }

    }

    public void setSQLNumber(Double amount, String col) throws SQLException {
        String sqlString = String.format("""
                UPDATE USER
                SET %s = ?
                WHERE UUID=?""", col);
        try (Connection con = SqlDAO.getConnection();
             PreparedStatement pst = con.prepareStatement(sqlString)) {
            pst.setDouble(1, amount);
            pst.setString(2, getPlayer().getUniqueId().toString());
            pst.executeUpdate();
            pickNumberSetter(col, amount);
        }
    }

    public void setSQLChunks(Set<Chunk> chunkSet) throws SQLException {
        String sqlString = """
                UPDATE USER
                SET CHUNKS = ?
                WHERE UUID=?""";
        try (Connection con = SqlDAO.getConnection();
             PreparedStatement pst = con.prepareStatement(sqlString)) {
            pst.setString(1, chunksToStr(chunkSet));
            pst.setString(2, getPlayer().getUniqueId().toString());
            pst.executeUpdate();
            setChunks(chunkSet);

        }
    }

    private void pickNumberSetter(String col, Double value) {
        switch (col) {
            case "POINTS" -> setPoints(value);
            case "LEVEL" -> setLevel(value);
            case "CURRENT_EXP" -> setCurrentExp(value);
            case "NEEDED_EXP" -> setNeededExp(value);
            case "MULTIPLIER" -> setMultiplier(value);
        }
    }


    public boolean hasChunk(Chunk chunk) {
        return getChunks().contains(chunk);

    }

    public boolean hasPoints(double amount) {
        return getPoints() - amount >= 0;
    }

    private boolean getNewLevel(double experience, double needed_exp, double level) {
        if (level <= exp.getMaxLevel()) {
            return experience >= needed_exp;
        }

        return false;
    }

    public void addExperience(double experience) throws SQLException {
        double level = getLevel();
        experience = experience + getCurrentExp();
        double newExpNeeded = getNeededExp();

        while (getNewLevel(experience, newExpNeeded, level)) {
            level += 1;
            newExpNeeded = exp.calcExperienceNeeded(level);
            if (experience - newExpNeeded < 0) break;
            experience = experience - newExpNeeded;

        }
        if (level != getLevel()) {
            getPlayer().sendMessage(MSG_LEVEL_UP(level));
            setSQLNumber(level, "LEVEL");
            setSQLNumber(exp.calcExperienceNeeded(level), "NEEDED_EXP");
            setLevel(level);
            setNeededExp(exp.calcExperienceNeeded(level));
        }
        setSQLNumber(experience, "CURRENT_EXP");

    }

    public Chunk getActiveChunk() {
        return this.activeChunk;
    }

    public void setActiveChunk(Chunk chunk) {
        this.activeChunk = chunk;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public void rmHologram() {
        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }
    }
}
