package com.amongopin.playertime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public final class PlayerTime extends JavaPlugin implements CommandExecutor, TabCompleter {

    private final Map<UUID, Integer> dailyTime = new HashMap<>();
    private final Map<UUID, Integer> weeklyTime = new HashMap<>();
    private final Map<UUID, Integer> monthlyTime = new HashMap<>();
    private final Map<UUID, Integer> yearlyTime = new HashMap<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    private int currentDay;
    private int currentMonth;
    private int currentYear;

    @Override
    public void onEnable() {
        this.dataFile = new File(getDataFolder(), "userdata.yml");
        if (!dataFile.exists()) {
            getDataFolder().mkdirs();
            try { boolean created = dataFile.createNewFile(); } catch (IOException e) { getLogger().severe(e.getMessage()); }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        LocalDate now = LocalDate.now();
        this.currentDay = now.getDayOfYear();
        this.currentMonth = now.getMonthValue();
        this.currentYear = now.getYear();

        loadPlayerData();

        getServer().getCommandMap().register("playertime", new org.bukkit.command.Command("playertime") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                return onCommand(sender, null, commandLabel, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                return onTabComplete(sender, null, alias, args);
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            checkDateReset();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                dailyTime.put(uuid, dailyTime.getOrDefault(uuid, 0) + 1);
                weeklyTime.put(uuid, weeklyTime.getOrDefault(uuid, 0) + 1);
                monthlyTime.put(uuid, monthlyTime.getOrDefault(uuid, 0) + 1);
                yearlyTime.put(uuid, yearlyTime.getOrDefault(uuid, 0) + 1);
            }
            savePlayerData();
        }, 1200L, 1200L);

        getLogger().info("PlayerTime plugin by amongopin successfully enabled!");

        checkUpdates();
    }

    private void checkUpdates() {
        getLogger().info("Checking for updates...");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                java.net.URL url = java.net.URI.create("https://github.com/amongopin/PlayerTime/releases").toURL();
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "PlayerTime-Updater");
                try (java.io.InputStream inputStream = connection.getInputStream();
                     java.util.Scanner scanner = new Scanner(inputStream)) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) { response.append(scanner.next()); }
                    String json = response.toString();
                    if (json.contains("\"tag_name\":\"")) {
                        String[] parts = json.split("\"tag_name\":\"");
                        if (parts.length > 1) {
                            String latestVersion = parts[1].split("\"")[0].replace("v", "");
                            if (!getDescription().getVersion().equalsIgnoreCase(latestVersion)) {
                                getLogger().warning("A new update for PlayerTime is available! Version: v" + latestVersion);
                                getLogger().warning("Download it here: https://github.com/amongopin/PlayerTime");
                            } else {
                                getLogger().info("You are running the latest version!");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().warning("Unable to check for updates: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDisable() {
        savePlayerData();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("playertime.use")) {
            sender.sendMessage(Component.text("You do not have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("=== PlayerTime ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("This plugin tracks detailed player playtime stats.", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Developer: ", NamedTextColor.GRAY).append(Component.text("amongopin", NamedTextColor.YELLOW)));
            sender.sendMessage(Component.text("Usage: /playertime <player> [day/week/month/year/all]", NamedTextColor.AQUA));
            return true;
        }

        OfflinePlayer targetPlayer;
        String type = "all";
        boolean showAllStats = false;

        String firstArg = args[0].toLowerCase(Locale.ROOT);
        if (firstArg.equals("day") || firstArg.equals("week") || firstArg.equals("month") || firstArg.equals("year") || firstArg.equals("all")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Console must specify a player name!", NamedTextColor.RED));
                return true;
            }
            targetPlayer = (Player) sender;
            type = firstArg;
        } else {
            targetPlayer = Bukkit.getOfflinePlayer(args[0]);
            if (args.length > 1) {
                type = args[1].toLowerCase(Locale.ROOT);
            } else {
                showAllStats = true;
            }
        }

        UUID uuid = targetPlayer.getUniqueId();
        String name = targetPlayer.getName() != null ? targetPlayer.getName() : args[0];

        if (showAllStats) {
            int d = dailyTime.getOrDefault(uuid, 0);
            int w = weeklyTime.getOrDefault(uuid, 0);
            int m = monthlyTime.getOrDefault(uuid, 0);
            int y = yearlyTime.getOrDefault(uuid, 0);
            int total;
            if (targetPlayer.isOnline() && targetPlayer.getPlayer() != null) {
                total = targetPlayer.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
            } else {
                total = dataConfig.getInt("players." + uuid + ".all-offline-cache", 0);
            }

            sender.sendMessage(Component.text("=== Playtime for " + name + " ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Today: ", NamedTextColor.GRAY).append(Component.text(formatTime(d), NamedTextColor.AQUA)));
            sender.sendMessage(Component.text("This week: ", NamedTextColor.GRAY).append(Component.text(formatTime(w), NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("This month: ", NamedTextColor.GRAY).append(Component.text(formatTime(m), NamedTextColor.LIGHT_PURPLE)));
            sender.sendMessage(Component.text("This year: ", NamedTextColor.GRAY).append(Component.text(formatTime(y), NamedTextColor.YELLOW)));
            sender.sendMessage(Component.text("Total: ", NamedTextColor.GRAY).append(Component.text(formatTime(total), NamedTextColor.GOLD)));
            return true;
        }

        int minutes;
        NamedTextColor color;
        String typeName;

        switch (type) {
            case "day" -> { minutes = dailyTime.getOrDefault(uuid, 0); color = NamedTextColor.AQUA; typeName = "today"; }
            case "week" -> { minutes = weeklyTime.getOrDefault(uuid, 0); color = NamedTextColor.GREEN; typeName = "this week"; }
            case "month" -> { minutes = monthlyTime.getOrDefault(uuid, 0); color = NamedTextColor.LIGHT_PURPLE; typeName = "this month"; }
            case "year" -> { minutes = yearlyTime.getOrDefault(uuid, 0); color = NamedTextColor.YELLOW; typeName = "this year"; }
            case "all" -> {
                if (targetPlayer.isOnline() && targetPlayer.getPlayer() != null) {
                    minutes = targetPlayer.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
                } else {
                    minutes = dataConfig.getInt("players." + uuid + ".all-offline-cache", 0);
                }
                color = NamedTextColor.GOLD;
                typeName = "total";
            }
            default -> {
                sender.sendMessage(Component.text("Unknown period! Use: day, week, month, year, all", NamedTextColor.RED));
                return true;
            }
        }

        sender.sendMessage(Component.text("Playtime for " + name + " (" + typeName + "): ", NamedTextColor.GRAY).append(Component.text(formatTime(minutes), color)));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("day", "week", "month", "year", "all"));
            for (Player player : Bukkit.getOnlinePlayers()) { completions.add(player.getName()); }
            return completions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        } else if (args.length == 2) {
            completions.addAll(Arrays.asList("day", "week", "month", "year", "all"));
            return completions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
        }
        return Collections.emptyList();
    }

    private String formatTime(int totalMinutes) {
        if (totalMinutes <= 0) return "0 min";
        long days = totalMinutes / 1440;
        long hours = (totalMinutes % 1440) / 60;
        long minutes = totalMinutes % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || sb.length() == 0) sb.append(minutes).append("min");
        return sb.toString().trim();
    }

    private void checkDateReset() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfYear();
        int month = now.getMonthValue();
        int year = now.getYear();
        if (day != currentDay) { dailyTime.clear(); currentDay = day; }
        if (now.getDayOfWeek().getValue() == 1 && day != currentDay) { weeklyTime.clear(); }
        if (month != currentMonth) { monthlyTime.clear(); currentMonth = month; }
        if (year != currentYear) { yearlyTime.clear(); currentYear = year; }
    }

    private void loadPlayerData() {
        if (dataConfig.getConfigurationSection("players") == null) return;
        for (String key : Objects.requireNonNull(dataConfig.getConfigurationSection("players")).getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            dailyTime.put(uuid, dataConfig.getInt("players." + key + ".day", 0));
            weeklyTime.put(uuid, dataConfig.getInt("players." + key + ".week", 0));
            monthlyTime.put(uuid, dataConfig.getInt("players." + key + ".month", 0));
            yearlyTime.put(uuid, dataConfig.getInt("players." + key + ".year", 0));
        }
    }

    private void savePlayerData() {
        Set<UUID> allUUIDs = new HashSet<>();
        allUUIDs.addAll(dailyTime.keySet());
        allUUIDs.addAll(weeklyTime.keySet());
        allUUIDs.addAll(monthlyTime.keySet());
        allUUIDs.addAll(yearlyTime.keySet());
        for (UUID uuid : allUUIDs) {
            dataConfig.set("players." + uuid + ".day", dailyTime.getOrDefault(uuid, 0));
            dataConfig.set("players." + uuid + ".week", weeklyTime.getOrDefault(uuid, 0));
            dataConfig.set("players." + uuid + ".month", monthlyTime.getOrDefault(uuid, 0));
            dataConfig.set("players." + uuid + ".year", yearlyTime.getOrDefault(uuid, 0));
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                dataConfig.set("players." + uuid + ".all-offline-cache", p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60);
            }
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { getLogger().severe(e.getMessage()); }
    }
}
