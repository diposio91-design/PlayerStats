package ru.anarchyserver.playerstats;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final PlayerStats plugin;
    private final Map<UUID, PlayerData> dataMap = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public StatsManager(PlayerStats plugin) {
        this.plugin = plugin;
        loadData();
    }

    public PlayerData getData(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(), id -> new PlayerData(id, player.getName()));
    }

    public void addExp(Player player, long exp) {
        PlayerData data = getData(player);
        int maxLevel = plugin.getConfig().getInt("max-level", 85);

        if (data.getLevel() >= maxLevel) {
            player.sendMessage("§6⚔ §eВы достигли максимального уровня §6[§c" + maxLevel + "§6]§e!");
            return;
        }

        data.addExp(exp);

        while (data.getExp() >= getExpRequired(data.getLevel()) && data.getLevel() < maxLevel) {
            data.setExp(data.getExp() - getExpRequired(data.getLevel()));
            data.setLevel(data.getLevel() + 1);
            onLevelUp(player, data);
        }

        applyStats(player, data);
        savePlayerData(data);
    }

    private void onLevelUp(Player player, PlayerData data) {
        int level = data.getLevel();
        int maxLevel = plugin.getConfig().getInt("max-level", 85);

        // Применяем статы сначала, чтобы получить новый maxHealth
        applyStats(player, data);

        // Восстанавливаем HP до нового максимума при левел-апе
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHp != null) {
            player.setHealth(maxHp.getValue());
        }

        player.sendMessage("");
        player.sendMessage("§4☠ §cПОВЫШЕНИЕ УРОВНЯ! §4☠");
        player.sendMessage("§7Вы достигли уровня §6[§c" + level + "§6]§7!");
        player.sendMessage("§7❤ HP: §c+" + String.format("%.1f", plugin.getConfig().getDouble("hp-per-level", 0.5)));
        player.sendMessage("§7⚔ Урон: §c+" + String.format("%.1f", plugin.getConfig().getDouble("damage-per-level", 0.1)));
        player.sendMessage("§7До следующего уровня: §e" + String.format("%,d", getExpRequired(level)) + " EXP");
        player.sendMessage("");

        if (level % 5 == 0 || level == maxLevel) {
            Bukkit.broadcastMessage("§4[PlayerStats] §c" + player.getName() +
                    " §7достиг уровня §6[§c" + level + "§6]§7! 🎉");
        }

        if (level == maxLevel) {
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage("§4§l[!!!] " + player.getName() +
                    " достиг МАКСИМАЛЬНОГО уровня [" + maxLevel + "]! ☠");
            Bukkit.broadcastMessage("§c§lЭТО ПОЧТИ НЕВОЗМОЖНО. СКЛОНИТЕСЬ ПЕРЕД НИМ.");
            Bukkit.broadcastMessage(" ");
        }

        player.getWorld().spawnParticle(
                org.bukkit.Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0),
                80, 0.6, 1.5, 0.6, 0.15
        );
        player.playSound(player.getLocation(),
                org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
    }

    public void applyStats(Player player, PlayerData data) {
        double hpBonus = data.getLevel() * plugin.getConfig().getDouble("hp-per-level", 0.5);
        double damageBonus = data.getLevel() * plugin.getConfig().getDouble("damage-per-level", 0.1);

        // ── HP ──────────────────────────────────────────────
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.getModifiers().stream()
                    .filter(m -> m.getName().equals("playerstats.health"))
                    .forEach(maxHealth::removeModifier);
            maxHealth.addModifier(new AttributeModifier(
                    UUID.randomUUID(), "playerstats.health",
                    hpBonus, AttributeModifier.Operation.ADD_NUMBER
            ));
        }

        // ✅ ФИКС: всегда показывать 10 сердец на экране (20 = 10 сердец)
        // Реальный HP остаётся высоким, но UI не раздувается
        player.setHealthScaled(true);
        player.setHealthScale(20.0);

        // ── УРОН ────────────────────────────────────────────
        AttributeInstance attackDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.getModifiers().stream()
                    .filter(m -> m.getName().equals("playerstats.damage"))
                    .forEach(attackDamage::removeModifier);
            attackDamage.addModifier(new AttributeModifier(
                    UUID.randomUUID(), "playerstats.damage",
                    damageBonus, AttributeModifier.Operation.ADD_NUMBER
            ));
        }

        updateNameTag(player, data);
    }

    public void updateNameTag(Player player, PlayerData data) {
        int level = data.getLevel();
        String color = getLevelColor(level);
        player.setCustomName(color + "[" + level + "] §f" + player.getName());
        player.setCustomNameVisible(true);
    }

    public void updateAllNameTags() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            applyStats(player, getData(player));
        }
    }

    private String getLevelColor(int level) {
        if (level >= 75) return "§4";
        if (level >= 50) return "§c";
        if (level >= 35) return "§6";
        if (level >= 20) return "§e";
        if (level >= 10) return "§a";
        return "§7";
    }

    /**
     * ЭКСТРЕМАЛЬНАЯ формула: 2000 * level^3
     *
     * Уровень 1  →         16 000 EXP
     * Уровень 5  →        432 000 EXP
     * Уровень 10 →      2 662 000 EXP
     * Уровень 25 →     33 750 000 EXP
     * Уровень 50 →    265 302 000 EXP
     * Уровень 85 →  1 250 564 000 EXP
     */
    public long getExpRequired(int level) {
        return (long) (2000 * Math.pow(level + 1, 3));
    }

    // ══════════════════════════════════
    //        СОХРАНЕНИЕ ДАННЫХ
    // ══════════════════════════════════

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String uuidStr : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String name = dataConfig.getString(uuidStr + ".name", "Unknown");
                int level = dataConfig.getInt(uuidStr + ".level", 0);
                long exp = dataConfig.getLong(uuidStr + ".exp", 0);
                long totalKills = dataConfig.getLong(uuidStr + ".totalKills", 0);

                PlayerData pd = new PlayerData(uuid, name);
                pd.setLevel(level);
                pd.setExp(exp);
                pd.setTotalKills(totalKills);
                dataMap.put(uuid, pd);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неверный UUID в playerdata.yml: " + uuidStr);
            }
        }
        plugin.getLogger().info("Загружено " + dataMap.size() + " профилей игроков.");
    }

    public void saveAll() {
        for (PlayerData data : dataMap.values()) {
            savePlayerData(data);
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void savePlayerData(PlayerData data) {
        String path = data.getUuid().toString();
        dataConfig.set(path + ".name", data.getName());
        dataConfig.set(path + ".level", data.getLevel());
        dataConfig.set(path + ".exp", data.getExp());
        dataConfig.set(path + ".totalKills", data.getTotalKills());
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
