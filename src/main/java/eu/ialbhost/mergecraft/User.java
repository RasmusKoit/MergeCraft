package eu.ialbhost.mergecraft;

import co.aikar.util.JSONUtil;
import com.google.gson.Gson;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

public class User {
    private final Player player;
    private Double points;
    private HashSet<Chunk> chunks;
    private Integer level;
    private Double currentExp;
    private Double neededExp;
    private Double multiplier;
    private final static Gson gson = new Gson();



    public Player getPlayer() {
        return player;
    }

    public Double getPoints() {
        return points;
    }

    public Integer getLevel() {
        return level;
    }

    public Double getCurrentExp() {
        return currentExp;
    }

    public Double getNeededExp() {
        return neededExp;
    }

    public Double getMultiplier() {
        return multiplier;
    }


    public User(Player player) {
        this(player, 0.0, new HashSet<>(), 0, 0.0, 100.0, 1.0);
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public void setChunks(HashSet<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setCurrentExp(Double currentExp) {
        this.currentExp = currentExp;
    }

    public void setNeededExp(Double neededExp) {
        this.neededExp = neededExp;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public HashSet<Chunk> getChunks() {
        return chunks;
    }

    public User(Player player, Double points, HashSet<Chunk> chunks, Integer level, Double currentExp, Double neededExp, Double multiplier) {
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


    public void populate(ResultSet rs, Player player) throws SQLException {
        setPoints(rs.getDouble("POINTS"));
        setLevel(rs.getInt("LEVEL"));
        setCurrentExp(rs.getDouble("CURRENT_EXP"));
        setNeededExp(rs.getDouble("NEEDED_EXP"));
        setMultiplier(rs.getDouble("MULTIPLIER"));
        HashSet<Chunk> chunkList = new HashSet<>();
        ChunkData[] chunkData = gson.fromJson(rs.getString("CHUNKS"), (Type) ChunkData[].class);
        for (ChunkData data : chunkData) {
            chunkList.add(data.toChunk(player.getWorld()));
        }
        setChunks(chunkList);
    }


    public static User getSQLUser(Player player) {
        String sqlString = String.format("SELECT * FROM USER WHERE UUID='%s'", player.getUniqueId().toString());
        User user = null;
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString);
                ResultSet rs = pst.executeQuery()
            ){
            while(rs.next()){
                user = new User(player);
                user.populate(rs, player);

            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return user;
    }

    public static String chunksToStr(User user){
        HashSet<Chunk> chunks = user.getChunks();
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
                user.getPlayer().getUniqueId().toString(), user.getPoints(), chunksToStr(user), user.getLevel(),
                user.getCurrentExp(), user.getNeededExp(), user.getMultiplier());
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
        ){
            pst.executeUpdate();

        }catch (SQLException exception) {
            exception.printStackTrace();
        }
        return user;
    }

    public void setSQLPoints(Double amount) {
        String sqlString = String.format("""
                UPDATE USER\s
                SET POINTS = %s\s
                where UUID='%s';""", amount, getPlayer().getUniqueId().toString());
        try (
                Connection con = SqlDAO.getConnection();
                PreparedStatement pst = con.prepareStatement(sqlString)
            ){
            pst.executeUpdate();
            setPoints(amount);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean hasChunk(Chunk chunk){
        return getChunks().contains(chunk);

    }

    }
