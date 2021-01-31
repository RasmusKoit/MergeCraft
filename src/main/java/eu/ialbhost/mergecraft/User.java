package eu.ialbhost.mergecraft;

import com.google.gson.Gson;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


    public User(Player player) {
        this(player, 0.0, new HashSet<>(), 1.0, 0.0, 100.0, 1.0);
    }

    public User(Player player, Double points, Set<Chunk> chunks, Double level, Double currentExp, Double neededExp, Double multiplier) {
        this.player = player;
        this.points = points;
        if (chunks.isEmpty()) {
            chunks.add(player.getChunk());
        }
        this.chunks = chunks;
        this.level = level;
        this.currentExp = currentExp;
        this.neededExp = neededExp;
        this.multiplier = multiplier;
    }

    public static User getSQLUser(Player player) {
        String sqlString = "SELECT * FROM USER WHERE UUID = ?";
        User user = new User(player);
        try (Connection con = SqlDAO.getConnection()) {
            PreparedStatement pst = con.prepareStatement(sqlString);
            pst.setString(1, player.getUniqueId().toString());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                user.populate(rs, player);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return user;
    }

    public static String chunksToStr(Set<Chunk> chunks) {
//        HashSet<Chunk> chunks = user.getChunks();
        List<ChunkData> chunkDataList = new ArrayList<>();
        for (Chunk chunk : chunks) {
            ChunkData data = new ChunkData(chunk.getX(), chunk.getZ());
            chunkDataList.add(data);
        }
        return GSON.toJson(chunkDataList);
    }

    public static User initSQLUser(Player player) {
        User user = new User(player);
        String sqlString = """
                INSERT INTO USER
                (UUID, POINTS, CHUNKS, LEVEL, CURRENT_EXP, NEEDED_EXP, MULTIPLIER) 
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (Connection con = SqlDAO.getConnection()) {
            PreparedStatement pst = con.prepareStatement(sqlString);
            pst.setString(1, user.getPlayer().getUniqueId().toString());
            pst.setDouble(2, user.getPoints());
            pst.setString(3, chunksToStr(user.getChunks()));
            pst.setDouble(4, user.getLevel());
            pst.setDouble(5, user.getCurrentExp());
            pst.setDouble(6, user.getNeededExp());
            pst.setDouble(7, user.getMultiplier());
            pst.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return user;
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
        for (ChunkData data : chunkData) {
            chunkSet.add(data.toChunk(player.getWorld()));
        }
        setChunks(chunkSet);
    }

    public void setSQLNumber(Double amount, String col) {
        String sqlString = """
                UPDATE USER
                SET ? = ?
                WHERE UUID=?""";
        try (Connection con = SqlDAO.getConnection()) {
            PreparedStatement pst = con.prepareStatement(sqlString);
            pst.setString(1, col);
            pst.setDouble(2, amount);
            pst.setString(3, getPlayer().getUniqueId().toString());
            pst.executeUpdate();
            pickNumberSetter(col, amount);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setSQLChunks(Set<Chunk> chunkSet, String col) {
        String sqlString = """
                UPDATE USER
                SET ? = ?
                WHERE UUID=?""";
        try (Connection con = SqlDAO.getConnection()) {
            PreparedStatement pst = con.prepareStatement(sqlString);
            pst.setString(1, col);
            pst.setString(2, chunksToStr(chunkSet));
            pst.setString(3, getPlayer().getUniqueId().toString());
            pst.executeUpdate();
            setChunks(chunkSet);

        } catch (SQLException exception) {
            exception.printStackTrace();
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

    private boolean getNewLevel(double experience) {
        if (getLevel() <= exp.getMaxLevel()) {
            return experience + getCurrentExp() >= getNeededExp();
        }

        return false;
    }

    // TODO do something here
    public void addExperience(double expGained) {
        while (getNewLevel(expGained)) {
            double newExpNeeded = exp.calcExperienceNeeded(getLevel());

        }
    }
}
