package ru.anarchyserver.playerstats;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private int level;
    private long exp;
    private long totalKills;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.level = 0;
        this.exp = 0;
        this.totalKills = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public long getExp() { return exp; }
    public void setExp(long exp) { this.exp = exp; }
    public void addExp(long amount) { this.exp += amount; }

    public long getTotalKills() { return totalKills; }
    public void setTotalKills(long totalKills) { this.totalKills = totalKills; }
    public void addKill() { this.totalKills++; }
}
