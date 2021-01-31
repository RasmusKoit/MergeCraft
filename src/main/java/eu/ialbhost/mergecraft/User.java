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

public class User {
    private final static Gson gson = new Gson();
    private final Player player;
    private Double points;
    private HashSet<Chunk> chunks;
    private Double level;
    private Double currentExp;
    private Double neededExp;
    private Double multiplier;


    public User(Player player) {
        this(player, 0.0, new HashSet<>(), 0.0, 0.0, 100.0, 1.0);
    }

    public User(Player player, Double points, HashSet<Chunk> chunks, Double level, Double currentExp, Double neededExp, Double multiplier) {
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
        String sqlString = String.format("SELECT * FROM USER WHERE UUID='%s'", player.getUniqueId().toString());
        User user = null;
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString);
                ResultSet rs = pst.executeQuery()
        ) {
            while (rs.next()) {
                user = new User(player);
                user.populate(rs, player);

            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return user;
    }

    public static String chunksToStr(HashSet<Chunk> chunks) {
//        HashSet<Chunk> chunks = user.getChunks();
        List<ChunkData> chunkDataList = new ArrayList<>();
        for (Chunk chunk : chunks) {
            ChunkData data = new ChunkData(chunk.getX(), chunk.getZ());
            chunkDataList.add(data);
        }
        return gson.toJson(chunkDataList);
    }

    public static User initSQLUser(Player player) {
        User user = new User(player);
        String sqlString = String.format("""
                        INSERT INTO USER
                            (UUID, POINTS, CHUNKS, LEVEL, CURRENT_EXP, NEEDED_EXP, MULTIPLIER)\s
                            VALUES ('%s', %s, '%s', %s, %s, %s, %s);""",
                user.getPlayer().getUniqueId().toString(), user.getPoints(), chunksToStr(user.getChunks()), user.getLevel(),
                user.getCurrentExp(), user.getNeededExp(), user.getMultiplier());
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
        ) {
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

    public HashSet<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(HashSet<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void populate(ResultSet rs, Player player) throws SQLException {
        setPoints(rs.getDouble("POINTS"));
        setLevel(rs.getDouble("LEVEL"));
        setCurrentExp(rs.getDouble("CURRENT_EXP"));
        setNeededExp(rs.getDouble("NEEDED_EXP"));
        setMultiplier(rs.getDouble("MULTIPLIER"));
        HashSet<Chunk> chunkSet = new HashSet<>();
        ChunkData[] chunkData = gson.fromJson(rs.getString("CHUNKS"), (Type) ChunkData[].class);
        for (ChunkData data : chunkData) {
            chunkSet.add(data.toChunk(player.getWorld()));
        }
        setChunks(chunkSet);
    }

    public void setSQLNumber(Double amount, String col) {
        String sqlString = String.format("""
                UPDATE USER\s
                SET %s = %s\s
                where UUID='%s';""", col, amount, getPlayer().getUniqueId().toString());
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
        ) {
            pst.executeUpdate();
            pickNumberSetter(col, amount);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setSQLChunks(HashSet<Chunk> chunkSet, String col) {
        String sqlString = String.format("""
                UPDATE USER\s
                SET %s = '%s'\s
                where UUID='%s';""", col, chunksToStr(chunkSet), getPlayer().getUniqueId().toString());
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
        ) {
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

}
