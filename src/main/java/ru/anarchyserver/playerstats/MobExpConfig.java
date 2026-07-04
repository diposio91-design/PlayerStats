package ru.anarchyserver.playerstats;

import org.bukkit.entity.EntityType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class MobExpConfig {

    private final PlayerStats plugin;
    private final Map<EntityType, Long> expMap = new HashMap<>();

    public MobExpConfig(PlayerStats plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("mob-exp");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase());
                long exp = section.getLong(key);
                expMap.put(type, exp);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неизвестный тип моба в конфиге: " + key);
            }
        }
        plugin.getLogger().info("Загружено " + expMap.size() + " типов мобов.");
    }

    public long getExp(EntityType type) {
        return expMap.getOrDefault(type, 0L);
    }

    public void reload() {
        expMap.clear();
        load();
    }
}
