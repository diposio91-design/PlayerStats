package ru.anarchyserver.playerstats;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final PlayerStats plugin;

    public StatsCommand(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        PlayerData data = plugin.getStatsManager().getData(player);
        int level = data.getLevel();
        int maxLevel = plugin.getConfig().getInt("max-level", 85);
        long exp = data.getExp();
        long expRequired = plugin.getStatsManager().getExpRequired(level);
        long totalKills = data.getTotalKills();

        double hpBonus = level * plugin.getConfig().getDouble("hp-per-level", 0.5);
        double damageBonus = level * plugin.getConfig().getDouble("damage-per-level", 0.1);
        double currentHp = player.getHealth();
        double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

        String progressBar = buildProgressBar(exp, expRequired, 20);

        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §6⚔ §lСТАТИСТИКА §6⚔");
        player.sendMessage("§8§m                                        ");
        player.sendMessage("  §7Игрок: §f" + player.getName());
        player.sendMessage("  §7Уровень: " + getLevelColor(level) + "[" + level + "/" + maxLevel + "]");
        player.sendMessage("  §7Опыт: §e" + String.format("%,d", exp) + "§7/§e" + String.format("%,d", expRequired));
        player.sendMessage("  §7Прогресс: " + progressBar);
        player.sendMessage("  §7❤ Здоровье: §c" + String.format("%.1f", currentHp) +
            "§7/§c" + String.format("%.1f", maxHp) +
            " §8(+§c" + String.format("%.1f", hpBonus) + "§8 от уровня)");
        player.sendMessage("  §7⚔ Бонус урона: §c+" + String.format("%.1f", damageBonus));
        player.sendMessage("  §7💀 Убийств мобов: §c" + String.format("%,d", totalKills));
        player.sendMessage("§8§m                                        ");

        return true;
    }

    private String buildProgressBar(long current, long max, int length) {
        int filled = (int) ((double) current / max * length);
        filled = Math.min(filled, length);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < length; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return bar + " §e" + String.format("%.1f", (double) current / max * 100) + "%";
    }

    private String getLevelColor(int level) {
        if (level >= 75) return "§4";
        if (level >= 50) return "§c";
        if (level >= 35) return "§6";
        if (level >= 20) return "§e";
        if (level >= 10) return "§a";
        return "§7";
    }
}
