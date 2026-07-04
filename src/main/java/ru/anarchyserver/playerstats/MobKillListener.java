package ru.anarchyserver.playerstats;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {

    private final PlayerStats plugin;

    public MobKillListener(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;

        EntityType type = event.getEntityType();
        long baseExp = plugin.getMobExpConfig().getExp(type);
        if (baseExp <= 0) return;

        boolean isBloodMoon = plugin.getConfig().getBoolean("bloodmoon-active", false);
        long finalExp = isBloodMoon ? baseExp * 5 : baseExp;

        plugin.getStatsManager().getData(player).addKill();
        plugin.getStatsManager().addExp(player, finalExp);

        if (plugin.getConfig().getBoolean("show-exp-gain", true)) {
            String moonBonus = isBloodMoon ? " §c§l[x5 КРОВАВАЯ ЛУНА!]" : "";
            player.sendMessage("§7[+§6" + finalExp + " EXP§7] §8" +
                formatMobName(type) + moonBonus);
        }
    }

    private String formatMobName(EntityType type) {
        return switch (type) {
            case ZOMBIE -> "Зомби";
            case SKELETON -> "Скелет";
            case CREEPER -> "Крипер";
            case ENDERMAN -> "Эндермен";
            case SPIDER -> "Паук";
            case CAVE_SPIDER -> "Пещерный паук";
            case WITCH -> "Ведьма";
            case BLAZE -> "Блейз";
            case WITHER_SKELETON -> "Скелет-иссушитель";
            case DROWNED -> "Утопленник";
            case HUSK -> "Гнилой зомби";
            case STRAY -> "Бродяга";
            case PILLAGER -> "Разбойник";
            case RAVAGER -> "Опустошитель";
            case ELDER_GUARDIAN -> "Старший страж";
            case GUARDIAN -> "Страж";
            case WITHER -> "Иссушитель";
            case ENDER_DRAGON -> "Дракон Края";
            case WARDEN -> "Хранитель";
            default -> type.name();
        };
    }
}
