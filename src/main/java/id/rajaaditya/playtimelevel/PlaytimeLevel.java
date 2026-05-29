package id.rajaaditya.playtimelevel;

import id.rajaaditya.playtimelevel.commands.LevelCommand;
import id.rajaaditya.playtimelevel.managers.DataManager;
import id.rajaaditya.playtimelevel.managers.EssentialsManager;
import id.rajaaditya.playtimelevel.managers.LevelManager;
import id.rajaaditya.playtimelevel.managers.RewardManager;
import id.rajaaditya.playtimelevel.placeholders.PlaytimeLevelExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PlaytimeLevel extends JavaPlugin {

    private static PlaytimeLevel instance;
    private DataManager dataManager;
    private LevelManager levelManager;
    private EssentialsManager essentialsManager;
    private RewardManager rewardManager;
    private PlaytimeLevelExpansion expansion;

    // Messages config
    private FileConfiguration messagesConfig;
    private File messagesFile;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Load config.yml
            saveDefaultConfig();

            // Load messages.yml
            loadMessages();

            // Initialize managers
            this.dataManager = new DataManager(this);
            this.essentialsManager = new EssentialsManager(this);
            this.levelManager = new LevelManager(this);
            this.rewardManager = new RewardManager(this);

            // Register commands
            LevelCommand levelCommand = new LevelCommand(this);
            getCommand("level").setExecutor(levelCommand);
            getCommand("level").setTabCompleter(levelCommand);

            // Start level checking task
            startLevelCheckTask();

            // Register PlaceholderAPI if available
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.expansion = new PlaytimeLevelExpansion(this);
                if (expansion.register()) {
                    getLogger().info("PlaceholderAPI expansion registered successfully!");
                } else {
                    getLogger().warning("Failed to register PlaceholderAPI expansion!");
                }
            } else {
                getLogger().info("PlaceholderAPI not found, placeholders will not work.");
            }

            getLogger().info("========================================");
            getLogger().info("  PlaytimeLevel v" + getDescription().getVersion() + " - Enabled!");
            getLogger().info("  Author  : " + getDescription().getAuthors());
            getLogger().info("  Website : https://rajaaditya.my.id/");
            getLogger().info("  Max Level: " + levelManager.getMaxLevelConfig());
            getLogger().info("  Total Hours for Max: " + String.format("%.1f", levelManager.getTotalHoursForMaxLevel()) + " hours");
            getLogger().info("========================================");

        } catch (Exception e) {
            getLogger().severe("Fatal error enabling PlaytimeLevel: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    // Load atau reload messages.yml
    public void loadMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // Getter untuk messages config
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    // Reload both config.yml and messages.yml
    public void reloadAllConfigs() {
        reloadConfig();
        loadMessages();
    }

    private void startLevelCheckTask() {
        int intervalSeconds = getConfig().getInt("settings.level-check-interval", 60);
        long intervalTicks = intervalSeconds * 20L;

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                try {
                    int currentLevel = levelManager.getPlayerLevel(player);
                    DataManager.PlayerData data = dataManager.getPlayerData(player.getUniqueId());

                    if (currentLevel > data.getLevel()) {
                        int oldLevel = data.getLevel();

                        for (int level = oldLevel + 1; level <= currentLevel; level++) {
                            final int rewardLevel = level;
                            if (rewardManager.hasRewardForLevel(rewardLevel) && !rewardManager.hasPlayerClaimedReward(player, rewardLevel)) {
                                getServer().getScheduler().runTask(this, () -> {
                                    rewardManager.giveReward(player, rewardLevel);
                                });
                                getLogger().info("Auto-gave level " + rewardLevel + " reward to " + player.getName());
                            }
                        }

                        data.setLevel(currentLevel);
                        dataManager.updatePlayerData(player.getUniqueId(), data);
                        dataManager.saveAllData();
                    }

                } catch (Exception e) {
                    getLogger().warning("Error checking level for " + player.getName() + ": " + e.getMessage());
                }
            }
        }, 20L, intervalTicks);
    }

    @Override
    public void onDisable() {
        try {
            getServer().getScheduler().cancelTasks(this);
            if (dataManager != null) {
                dataManager.saveAllData();
                getLogger().info("All player data saved successfully.");
            }
            getLogger().info("PlaytimeLevel v" + getDescription().getVersion() + " disabled!");
        } catch (Exception e) {
            getLogger().warning("Error disabling PlaytimeLevel: " + e.getMessage());
        }
    }

    public static PlaytimeLevel getInstance() { return instance; }
    public DataManager getDataManager() { return dataManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public EssentialsManager getEssentialsManager() { return essentialsManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}