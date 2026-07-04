package ru.anarchyserver.playerstats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final PlayerStats plugin;

    public PlayerListener(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStatsManager().getData(player);

        data.setName(player.getName());
        plugin.getStatsManager().applyStats(player, data);

        player.sendMessage("");
        player.sendMessage("§6⚔ §eДобро пожаловать, §6" + player.getName() + "§e!");
        player.sendMessage("§7Твой уровень: §6[§c" + data.getLevel() + "§6]");
        player.sendMessage("§7Опыт: §e" + data.getExp() + "§7/§e" +
            plugin.getStatsManager().getExpRequired(data.getLevel()));
        player.sendMessage("§7Убийств: §c" + data.getTotalKills());
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getStatsManager().saveAll();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PlayerData data = plugin.getStatsManager().getData(player);
            plugin.getStatsManager().applyStats(player, data);
            player.sendMessage("§7Твои статы восстановлены. Уровень: §6[§c" + data.getLevel() + "§6]");
        }, 1L);
    }
}
