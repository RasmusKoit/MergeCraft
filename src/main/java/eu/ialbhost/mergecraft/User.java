package eu.ialbhost.mergecraft;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class User {
    private final Player player;
    private Double points;
    private HashSet<Chunk> chunks;
    private Integer level;
    private Double currentExp;
    private Double neededExp;
    private Double multiplier;


    public User(Player player){
        this.player = player;
        this.points = 0.0;
        this.chunks = new HashSet<>();
        this.level = 0;
        this.currentExp = 0.0;
        this.neededExp = 0.0;
        this.multiplier = 1.0;
    }

    public User(Player player, HashSet<Chunk> chunks){
        this.player = player;
        this.points = 0.0;
        this.chunks = chunks;
        this.level = 0;
        this.currentExp = 0.0;
        this.neededExp = 0.0;
        this.multiplier = 1.0;
    }

    public User(Player player, Double points, HashSet<Chunk> chunks, Integer level,
                Double currentExp, Double neededExp, Double multiplier) {
        this.player = player;
        this.points = points;
        this.chunks = chunks;
        this.level = level;
        this.currentExp = currentExp;
        this.neededExp = neededExp;
        this.multiplier = multiplier;
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

    public HashSet<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(HashSet<Chunk> chunks) {
        this.chunks = chunks;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
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

}
