package id.rajaaditya.playtimelevel.placeholders;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import id.rajaaditya.playtimelevel.managers.LevelManager;
import id.rajaaditya.playtimelevel.utils.FormatUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaytimeLevelExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    private final PlaytimeLevel plugin;
    private final LevelManager levelManager;

    public PlaytimeLevelExpansion(PlaytimeLevel plugin) {
        this.plugin = plugin;
        this.levelManager = plugin.getLevelManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "playtimelevel";
    }

    @Override
    public @NotNull String getAuthor() {
        return "RajaAditya";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.6"; // DIUBAH: 1.3 -> 1.5
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!offlinePlayer.isOnline()) {
            return getDefaultValue(params);
        }

        Player player = offlinePlayer.getPlayer();

        switch (params.toLowerCase()) {
            case "level":
                return String.valueOf(levelManager.getPlayerLevel(player));
                
            case "level_formatted":
                return levelManager.getFormattedLevel(player);
                
            case "playtime_hours":
                return String.format("%.1f", levelManager.getPlaytimeHours(player));
                
            case "playtime_hours_formatted":
                return FormatUtils.formatTimeHours(levelManager.getPlaytimeHours(player));
                
            case "progress_percent":
                return String.format("%.1f", levelManager.getProgressPercent(player));
                
            case "progress_bar":
                return levelManager.getProgressBar(player);
                
            case "next_level_in_hours":
                return String.format("%.1f", levelManager.getNextLevelInHours(player));
                
            case "next_level_in_hours_formatted":
                double hours = levelManager.getNextLevelInHours(player);
                return FormatUtils.formatTimeHours(hours);
                
            // Required hours for next level
            case "required_for_next_level":
                double requiredHours = levelManager.getRequiredForNextLevel(player);
                return String.format("%.1f", requiredHours);
                
            case "required_for_next_level_formatted":
                double requiredHours2 = levelManager.getRequiredForNextLevel(player);
                return FormatUtils.formatTimeHours(requiredHours2);
                
            // Total hours for current level
            case "total_required_current":
                double totalRequired = levelManager.getRequiredForCurrentLevel(player);
                return String.format("%.1f", totalRequired);
                
            case "total_required_current_formatted":
                double totalRequired2 = levelManager.getRequiredForCurrentLevel(player);
                return FormatUtils.formatTimeHours(totalRequired2);
                
            // Cumulative hours for current level
            case "total_current":
                double totalCurrent = levelManager.getTotalForCurrentLevel(player);
                return String.format("%.1f", totalCurrent);
                
            case "total_current_formatted":
                double totalCurrent2 = levelManager.getTotalForCurrentLevel(player);
                return FormatUtils.formatTimeHours(totalCurrent2);
                
            // Level info detail
            case "level_info":
                return levelManager.getPlayerLevelInfo(player);
                
            // Next level number
            case "next_level":
                return String.valueOf(levelManager.getNextLevel(player));
                
            // Max level
            case "max_level":
                return String.valueOf(levelManager.getMaxLevelConfig());
                
            // Is max level
            case "is_max_level":
                return levelManager.isMaxLevel(player) ? "true" : "false";
                
            // Level color code (e.g. §6 untuk level 20-29)
            case "level_color":
                return levelManager.getLevelColorForPlayer(player);

            // Level number colored with its color (e.g. §625)
            case "level_colored":
                return levelManager.getColoredLevel(player);

            // Level formatted with color from config level-color-format (e.g. §6[ 25 ] §f§l|)
            case "level_color_formatted":
                return levelManager.getColorFormattedLevel(player);

            // Total hours for max level
            case "total_max_level":
                double totalMax = levelManager.getTotalHoursForMaxLevel();
                return String.format("%.1f", totalMax);
                
            case "total_max_level_formatted":
                double totalMax2 = levelManager.getTotalHoursForMaxLevel();
                return FormatUtils.formatTimeHours(totalMax2);

            // Reward claimed status
            case "reward_claimed_%level%":
                try {
                    int checkLevel = Integer.parseInt(params.split("_")[2]);
                    return plugin.getRewardManager().hasPlayerClaimedReward(player, checkLevel) ? "true" : "false";
                } catch (Exception e) {
                    return "false";
                }
                
            default:
                return null;
        }
    }

    private String getDefaultValue(String params) {
        switch (params.toLowerCase()) {
            case "level_color":
                return "§7";
            case "level_colored":
                return "§71";
            case "level_color_formatted":
                return "§7[ 1 ] §f§l|";
            case "level":
            case "level_formatted":
            case "next_level":
                return "1";
            case "playtime_hours":
            case "playtime_hours_formatted":
            case "next_level_in_hours":
            case "next_level_in_hours_formatted":
            case "required_for_next_level":
            case "required_for_next_level_formatted":
            case "total_required_current":
            case "total_required_current_formatted":
            case "total_current":
            case "total_current_formatted":
                return "0";
            case "progress_percent":
                return "0.0";
            case "progress_bar":
                return FormatUtils.createProgressBar(0);
            case "level_info":
                return "Level 1 - 0.0/1.0 hours";
            case "max_level":
                return "100";
            case "is_max_level":
                return "false";
            case "total_max_level":
            case "total_max_level_formatted":
                return "720.0";
            case "reward_claimed_%level%":
                return "false";
            default:
                return "";
        }
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    // Manual register method
    public boolean register() {
        return super.register();
    }
}