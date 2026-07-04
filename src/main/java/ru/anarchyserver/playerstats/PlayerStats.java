package ru.anarchyserver.playerstats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerStats extends JavaPlugin {

    private StatsManager statsManager;
    private MobExpConfig mobExpConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.mobExpConfig = new MobExpConfig(this);
        this.statsManager = new StatsManager(this);

        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("statsadmin").setExecutor(new StatsAdminCommand(this));

        // Обновлять ники над головой каждые 20 тиков (1 сек)
        Bukkit.getScheduler().runTaskTimer(this, () ->
            statsManager.updateAllNameTags(), 0L, 20L);

        getLogger().info("PlayerStats успешно загружен!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.saveAll();
        }
        getLogger().info("PlayerStats выгружен, данные сохранены.");
    }

    public StatsManager getStatsManager() { return statsManager; }
    public MobExpConfig getMobExpConfig() { return mobExpConfig; }
}
