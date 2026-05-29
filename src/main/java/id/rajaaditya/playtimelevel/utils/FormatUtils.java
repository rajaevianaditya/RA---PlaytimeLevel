package id.rajaaditya.playtimelevel.utils;

import org.bukkit.ChatColor;

public class FormatUtils {

    public static String formatLevel(int level) {
        try {
            String format = "§e[ §6%level% §e]"; // Default format
            return colorize(format.replace("%level%", String.valueOf(level)));
        } catch (Exception e) {
            return "[" + level + "]";
        }
    }

    public static String createProgressBar(double percent) {
        try {
            int length = 10;
            String filledChar = "§a▊";
            String emptyChar = "§7▊";
            
            filledChar = colorize(filledChar);
            emptyChar = colorize(emptyChar);
            
            int filledLength = (int) (length * (percent / 100.0));
            int emptyLength = length - filledLength;
            
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < filledLength; i++) {
                bar.append(filledChar);
            }
            for (int i = 0; i < emptyLength; i++) {
                bar.append(emptyChar);
            }
            
            return bar.toString();
        } catch (Exception e) {
            return "[==========]";
        }
    }

    public static String formatTimeHours(double hours) {
        try {
            String format = "§b%hours% §ehours";
            return colorize(format.replace("%hours%", String.format("%.1f", hours)));
        } catch (Exception e) {
            return String.format("%.1f hours", hours);
        }
    }

    public static String colorize(String message) {
        try {
            return ChatColor.translateAlternateColorCodes('§', message);
        } catch (Exception e) {
            return message.replace("§", "");
        }
    }

    public static String stripColor(String message) {
        try {
            return ChatColor.stripColor(message);
        } catch (Exception e) {
            return message;
        }
    }

    // METHOD: Format level requirements
    public static String formatLevelRequirements(int level, double requiredHours) {
        try {
            String format = "§eLevel §6%d §e- §b%.1f hours";
            return colorize(String.format(format, level, requiredHours));
        } catch (Exception e) {
            return "Level " + level + " - " + requiredHours + " hours";
        }
    }

    // METHOD: Format level progression info
    public static String formatLevelProgression(int currentLevel, double currentHours, double nextLevelHours) {
        try {
            String format = "§eLevel §6%d §7| §b%.1fh §7| §a%.1fh to next level";
            return colorize(String.format(format, currentLevel, currentHours, nextLevelHours));
        } catch (Exception e) {
            return String.format("Level %d | %.1fh | %.1fh next", currentLevel, currentHours, nextLevelHours);
        }
    }

    // METHOD: Format detailed level info
    public static String formatDetailedLevelInfo(int level, double currentHours, double requiredForNext, double nextInHours) {
        try {
            String format = "§6Level §e%d §7| §b%.1fh §7| §6Next: §e%.1fh §7| §a%.1fh needed";
            return colorize(String.format(format, level, currentHours, requiredForNext, nextInHours));
        } catch (Exception e) {
            return String.format("Lvl %d | %.1fh | Next: %.1fh | Need: %.1fh", 
                level, currentHours, requiredForNext, nextInHours);
        }
    }
}