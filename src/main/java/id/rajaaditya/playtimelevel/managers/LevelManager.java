package id.rajaaditya.playtimelevel.managers;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import id.rajaaditya.playtimelevel.utils.FormatUtils;
import org.bukkit.entity.Player;

public class LevelManager {

    private final PlaytimeLevel plugin;
    private final EssentialsManager essentialsManager;

    // ✅ Progressive requirements - Total ~1440 hours (6 months @ 8h/day) for level 100
    private final double[] LEVEL_REQUIREMENTS = {
        // Levels 2-11
        3.02, 3.06, 3.10, 3.14, 3.18, 3.22, 3.28, 3.34, 3.40, 3.46,
        // Levels 12-21
        3.52, 3.60, 3.68, 3.76, 3.84, 3.94, 4.04, 4.14, 4.24, 4.36,
        // Levels 22-31
        4.48, 4.60, 4.73, 4.87, 5.01, 5.15, 5.31, 5.47, 5.63, 5.81,
        // Levels 32-41
        5.99, 6.17, 6.37, 6.58, 6.78, 7.00, 7.22, 7.44, 7.68, 7.92,
        // Levels 42-51
        8.18, 8.45, 8.71, 8.99, 9.27, 9.57, 9.87, 10.19, 10.52, 10.86,
        // Levels 52-61
        11.20, 11.56, 11.92, 12.31, 12.69, 13.09, 13.49, 13.91, 14.34, 14.78,
        // Levels 62-71
        15.22, 15.68, 16.15, 16.63, 17.11, 17.61, 18.12, 18.64, 19.16, 19.71,
        // Levels 72-81
        20.25, 20.81, 21.37, 21.96, 22.54, 23.14, 23.75, 24.37, 24.99, 25.64,
        // Levels 82-91
        26.28, 26.94, 27.61, 28.29, 28.98, 29.68, 30.38, 31.11, 31.83, 32.57,
        // Levels 92-100
        33.32, 34.08, 34.85, 35.63, 36.42, 37.22, 38.02, 38.85, 39.67,
    };

    public LevelManager(PlaytimeLevel plugin) {
        this.plugin = plugin;
        this.essentialsManager = plugin.getEssentialsManager();
    }

    // Get settings from config
    private int getMaxLevel() {
        return plugin.getConfig().getInt("level-system.max-level", 100);
    }

    // Get hours needed for specific level
    public double getRequiredHoursForLevel(int level) {
        if (level <= 1) return 0;
        if (level > getMaxLevel()) return 0;
        
        // Levels 2-100 use progressive requirements
        if (level - 2 < LEVEL_REQUIREMENTS.length) {
            return LEVEL_REQUIREMENTS[level - 2];
        }
        
        return 20.0; // Default fallback
    }

    // Calculate total hours needed to reach a level
    public double getTotalHoursForLevel(int level) {
        if (level <= 1) return 0;
        
        double total = 0;
        for (int i = 2; i <= level; i++) {
            total += getRequiredHoursForLevel(i);
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // Calculate player level based on playtime - WITH MAX LEVEL HANDLING
    public int calculateLevel(double playtimeHours) {
        try {
            int maxLevel = getMaxLevel();
            double totalForMax = getTotalHoursForLevel(maxLevel);
            
            // If player exceeds max level requirements, return max level
            if (playtimeHours >= totalForMax) {
                return maxLevel;
            }
            
            // Check each level from 2 to max level
            for (int targetLevel = 2; targetLevel <= maxLevel; targetLevel++) {
                double totalHoursNeeded = getTotalHoursForLevel(targetLevel);
                if (playtimeHours < totalHoursNeeded) {
                    return targetLevel - 1;
                }
            }
            
            return maxLevel; // Fallback
        } catch (Exception e) {
            return 1;
        }
    }

    public long getPlaytimeSeconds(Player player) {
        try {
            if (player == null) return 0;
            return essentialsManager.getPlaytimeSeconds(player);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getPlayerLevel(Player player) {
        try {
            if (player == null) return 1;
            double playtimeHours = getPlaytimeHours(player);
            return calculateLevel(playtimeHours);
        } catch (Exception e) {
            return 1;
        }
    }

    public String getFormattedLevel(Player player) {
        try {
            int level = getPlayerLevel(player);
            if (isMaxLevel(player)) {
                // NEW: Get custom max level format from config
                String maxLevelFormat = plugin.getConfig().getString("placeholder.max-level-format", "§6§l[ §eMAX §6§l]");
                return FormatUtils.colorize(maxLevelFormat);
            }
            String levelFormat = plugin.getConfig().getString("placeholder.level-format", "§e[ §6%level% §e]");
            return FormatUtils.colorize(levelFormat.replace("%level%", String.valueOf(level)));
        } catch (Exception e) {
            return "[1]";
        }
    }

    public double getPlaytimeHours(Player player) {
        try {
            if (player == null) return 0;
            long playtimeSeconds = getPlaytimeSeconds(player);
            return Math.round((playtimeSeconds / 3600.0) * 100.0) / 100.0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Calculate progress percentage - WITH MAX LEVEL HANDLING
    public double getProgressPercent(Player player) {
        try {
            if (player == null) return 0;
            
            // If max level reached, always show 100%
            if (isMaxLevel(player)) {
                return 100.0;
            }
            
            int currentLevel = getPlayerLevel(player);
            double currentPlaytimeHours = getPlaytimeHours(player);
            double totalForCurrent = getTotalHoursForLevel(currentLevel);
            double totalForNext = getTotalHoursForLevel(currentLevel + 1);
            
            // Progress = how far between current and next level
            double progress = (currentPlaytimeHours - totalForCurrent) / (totalForNext - totalForCurrent) * 100;
            return Math.max(0, Math.min(100, Math.round(progress * 100.0) / 100.0));
        } catch (Exception e) {
            return 0;
        }
    }

    public String getProgressBar(Player player) {
        try {
            double progressPercent = getProgressPercent(player);
            return FormatUtils.createProgressBar(progressPercent);
        } catch (Exception e) {
            return "==========";
        }
    }

    // Hours needed to reach next level - WITH MAX LEVEL HANDLING
    public double getNextLevelInHours(Player player) {
        try {
            if (player == null) return getRequiredHoursForLevel(2);
            
            // If max level, return 0 (no next level)
            if (isMaxLevel(player)) {
                return 0;
            }
            
            int currentLevel = getPlayerLevel(player);
            double currentPlaytimeHours = getPlaytimeHours(player);
            double totalForNext = getTotalHoursForLevel(currentLevel + 1);
            
            double hoursToNextLevel = totalForNext - currentPlaytimeHours;
            return Math.max(0, Math.round(hoursToNextLevel * 100.0) / 100.0);
        } catch (Exception e) {
            return getRequiredHoursForLevel(2);
        }
    }

    public int getNextLevel(Player player) {
        try {
            int currentLevel = getPlayerLevel(player);
            
            // If max level, next level is same as current (no next level)
            if (isMaxLevel(player)) {
                return currentLevel;
            }
            
            return currentLevel + 1;
        } catch (Exception e) {
            return 2;
        }
    }

    // Get level requirement info
    public String getLevelRequirements(int level) {
        if (level <= 1) return "Level 1 (Starter)";
        double requiredHours = getRequiredHoursForLevel(level);
        return String.format("Level %d: %.2f hours", level, requiredHours);
    }

    // Get detailed player level info - WITH MAX LEVEL HANDLING
    public String getPlayerLevelInfo(Player player) {
        int level = getPlayerLevel(player);
        double playtimeHours = getPlaytimeHours(player);
        
        if (isMaxLevel(player)) {
            return String.format("§6§lMAX LEVEL! §eLevel %d (%.1f hours total)", level, playtimeHours);
        }
        
        double nextLevelHours = getNextLevelInHours(player);
        double totalForNext = getTotalHoursForLevel(level + 1);
        double requiredForNext = getRequiredHoursForLevel(level + 1);
        
        return String.format(
            "Level %d (%.1f/%.1f hours) - Need %.1f hours to Level %d",
            level, playtimeHours, totalForNext, nextLevelHours, level + 1
        );
    }

    // Hours required for next level - WITH MAX LEVEL HANDLING
    public double getRequiredForNextLevel(Player player) {
        int currentLevel = getPlayerLevel(player);
        
        if (isMaxLevel(player)) {
            return 0;
        }
        
        return getRequiredHoursForLevel(currentLevel + 1);
    }

    // Hours required for current level
    public double getRequiredForCurrentLevel(Player player) {
        int currentLevel = getPlayerLevel(player);
        return getRequiredHoursForLevel(currentLevel);
    }

    // Total hours for current level
    public double getTotalForCurrentLevel(Player player) {
        int currentLevel = getPlayerLevel(player);
        return getTotalHoursForLevel(currentLevel);
    }

    // Check if player reached max level
    public boolean isMaxLevel(Player player) {
        return getPlayerLevel(player) >= getMaxLevel();
    }

    // Get max level setting
    public int getMaxLevelConfig() {
        return getMaxLevel();
    }

    /**
     * Ambil warna Minecraft untuk level tertentu dari config.yml (level-colors).
     * Fallback ke §7 (abu-abu) jika tidak ditemukan.
     */
    public String getLevelColor(int level) {
        java.util.List<java.util.Map<?, ?>> entries = plugin.getConfig().getMapList("level-colors");
        for (java.util.Map<?, ?> entry : entries) {
            int min = entry.containsKey("min") ? (int) entry.get("min") : 1;
            int max = entry.containsKey("max") ? (int) entry.get("max") : 100;
            if (level >= min && level <= max) {
                Object color = entry.get("color");
                return color != null ? String.valueOf(color) : "§7";
            }
        }
        return "§7"; // fallback
    }

    public String getLevelColorForPlayer(Player player) {
        return getLevelColor(getPlayerLevel(player));
    }

    /**
     * Angka level yang sudah diwarnai.
     * Contoh: level 25 → "§625"
     */
    public String getColoredLevel(Player player) {
        int level = getPlayerLevel(player);
        return getLevelColor(level) + level;
    }

    /**
     * Level dengan format dari config (level-color-format),
     * warna otomatis ngikut range level dari level-colors.
     * Contoh: level 25 → "§6[ 25 ] §f§l|"
     */
    public String getColorFormattedLevel(Player player) {
        try {
            if (isMaxLevel(player)) {
                String maxFmt = plugin.getConfig().getString("placeholder.max-level-format", "§6§l[ §eMAX §6§l] §f§l|");
                return FormatUtils.colorize(maxFmt);
            }
            int level = getPlayerLevel(player);
            String color = getLevelColor(level);
            String fmt = plugin.getConfig().getString("placeholder.level-color-format", "%color%[ %level% ] §f§l|");
            return FormatUtils.colorize(fmt.replace("%color%", color).replace("%level%", String.valueOf(level)));
        } catch (Exception e) {
            return "[" + getPlayerLevel(player) + "]";
        }
    }

    // Total hours needed for max level
    public double getTotalHoursForMaxLevel() {
        return getTotalHoursForLevel(getMaxLevel());
    }
}