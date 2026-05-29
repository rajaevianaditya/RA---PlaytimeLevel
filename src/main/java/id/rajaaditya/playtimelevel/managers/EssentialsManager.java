package id.rajaaditya.playtimelevel.managers;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsManager {

    private final PlaytimeLevel plugin;
    private IEssentials essentials;
    private boolean useEssentials = false;

    public EssentialsManager(PlaytimeLevel plugin) {
        this.plugin = plugin;
        setupEssentials();
    }

    private void setupEssentials() {
        try {
            Plugin essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentialsPlugin instanceof IEssentials) {
                this.essentials = (IEssentials) essentialsPlugin;
                this.useEssentials = true;
                plugin.getLogger().info("Successfully hooked into EssentialsX!");
            } else {
                plugin.getLogger().info("EssentialsX not found! Using Bukkit statistics for playtime.");
            }
        } catch (Exception e) {
            plugin.getLogger().info("Using Bukkit statistics for playtime: " + e.getMessage());
        }
    }

    public long getPlaytimeSeconds(Player player) {
        try {
            if (useEssentials && essentials != null) {
                // Coba method Essentials yang paling umum
                net.ess3.api.IUser user = essentials.getUser(player);
                if (user != null) {
                    try {
                        // Method getPlaytime() - EssentialsX versi baru
                        java.lang.reflect.Method getPlaytimeMethod = user.getClass().getMethod("getPlaytime");
                        Object result = getPlaytimeMethod.invoke(user);
                        if (result instanceof Long) {
                            long playtimeMillis = (Long) result;
                            return playtimeMillis / 1000; // Convert to seconds
                        }
                    } catch (Exception e) {
                        // Fallback ke Bukkit statistics
                        return getPlaytimeFromBukkit(player);
                    }
                }
            }
            // Default ke Bukkit statistics
            return getPlaytimeFromBukkit(player);
        } catch (Exception e) {
            return getPlaytimeFromBukkit(player);
        }
    }

    private long getPlaytimeFromBukkit(Player player) {
        try {
            // Bukkit Statistics API - PLAY_ONE_MINUTE dalam ticks (1 menit = 1200 ticks)
            int playTimeTicks = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
            long seconds = playTimeTicks / 20L; // Convert ticks to seconds
            return seconds;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isEssentialsAvailable() {
        return useEssentials && essentials != null;
    }
}