package ru.anarchyserver.playerstats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsAdminCommand implements CommandExecutor {

    private final PlayerStats plugin;

    public StatsAdminCommand(PlayerStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("playerstats.admin")) {
            sender.sendMessage("§cНет прав!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /statsadmin reload
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getMobExpConfig().reload();
                sender.sendMessage("§a✔ Конфиг перезагружен!");
            }

            // /statsadmin setlevel <игрок> <уровень>
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /statsadmin setlevel <игрок> <уровень>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cИгрок не найден!"); return true; }
                int level;
                try { level = Integer.parseInt(args[2]); }
                catch (NumberFormatException e) { sender.sendMessage("§cНеверный уровень!"); return true; }
                int maxLevel = plugin.getConfig().getInt("max-level", 85);
                level = Math.min(level, maxLevel);
                PlayerData data = plugin.getStatsManager().getData(target);
                data.setLevel(level);
                data.setExp(0);
                plugin.getStatsManager().applyStats(target, data);
                plugin.getStatsManager().saveAll();
                sender.sendMessage("§a✔ Уровень игрока §f" + target.getName() + " §aустановлен на §f" + level);
                target.sendMessage("§6Администратор установил ваш уровень: §c[" + level + "]");
            }

            // /statsadmin addexp <игрок> <количество>
            case "addexp" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /statsadmin addexp <игрок> <количество>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cИгрок не найден!"); return true; }
                long exp;
                try { exp = Long.parseLong(args[2]); }
                catch (NumberFormatException e) { sender.sendMessage("§cНеверное количество!"); return true; }
                plugin.getStatsManager().addExp(target, exp);
                sender.sendMessage("§a✔ Выдано §f" + String.format("%,d", exp) + " EXP §aигроку §f" + target.getName());
            }

            // /statsadmin reset <игрок>
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /statsadmin reset <игрок>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("§cИгрок не найден!"); return true; }
                PlayerData data = plugin.getStatsManager().getData(target);
                data.setLevel(0);
                data.setExp(0);
                data.setTotalKills(0);
                plugin.getStatsManager().applyStats(target, data);
                plugin.getStatsManager().saveAll();
                sender.sendMessage("§a✔ Статистика игрока §f" + target.getName() + " §aсброшена!");
                target.sendMessage("§cВаша статистика была сброшена администратором.");
            }

            // /statsadmin bloodmoon <on/off>
            case "bloodmoon" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /statsadmin bloodmoon <on/off>");
                    return true;
                }
                boolean active = args[1].equalsIgnoreCase("on");
                plugin.getConfig().set("bloodmoon-active", active);
                plugin.saveConfig();
                if (active) {
                    Bukkit.broadcastMessage("§4§l☽ КРОВАВАЯ ЛУНА НАСТУПАЕТ! ☽");
                    Bukkit.broadcastMessage("§c§lМОНСТРЫ ДАЮТ x5 ОПЫТА!");
                } else {
                    Bukkit.broadcastMessage("§7§l☽ Кровавая Луна отступила...");
                }
                sender.sendMessage("§a✔ Кровавая Луна: " + (active ? "§cВКЛ" : "§7ВЫКЛ"));
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("  §6⚔ §lPlayerStats Admin §6⚔");
        sender.sendMessage("§8§m                                        ");
        sender.sendMessage("  §e/statsadmin reload §7— перезагрузить конфиг");
        sender.sendMessage("  §e/statsadmin setlevel <игрок> <ур> §7— установить уровень");
        sender.sendMessage("  §e/statsadmin addexp <игрок> <кол> §7— выдать опыт");
        sender.sendMessage("  §e/statsadmin reset <игрок> §7— сбросить статистику");
        sender.sendMessage("  §e/statsadmin bloodmoon <on/off> §7— кровавая луна");
        sender.sendMessage("§8§m                                        ");
    }
}
