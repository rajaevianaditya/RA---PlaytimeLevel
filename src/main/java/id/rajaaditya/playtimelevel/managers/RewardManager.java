package id.rajaaditya.playtimelevel.managers;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import id.rajaaditya.playtimelevel.utils.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class RewardManager {

    private final PlaytimeLevel plugin;
    private final DataManager dataManager;

    public RewardManager(PlaytimeLevel plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    public boolean isRewardEnabled() {
        return plugin.getConfig().getBoolean("rewards.enabled", true);
    }

    public boolean isBroadcastEnabled() {
        return plugin.getConfig().getBoolean("rewards.broadcast-enabled", true);
    }

    public Set<String> getRewardLevels() {
        if (!plugin.getConfig().contains("rewards.levels")) {
            return Set.of();
        }
        return plugin.getConfig().getConfigurationSection("rewards.levels").getKeys(false);
    }

    public boolean hasRewardForLevel(int level) {
        return plugin.getConfig().contains("rewards.levels." + level);
    }

    public List<String> getRewardCommands(int level) {
        return plugin.getConfig().getStringList("rewards.levels." + level + ".commands");
    }

    public String getRewardMessage(int level) {
        return plugin.getConfig().getString("rewards.levels." + level + ".message", 
                plugin.getConfig().getString("rewards.reward-message", "§6§lREWARD! §eYou reached level %level% and received rewards!")
                        .replace("%level%", String.valueOf(level)));
    }

    public boolean isBroadcastEnabledForLevel(int level) {
        return plugin.getConfig().getBoolean("rewards.levels." + level + ".broadcast", true);
    }

    public String getBroadcastMessage(int level) {
        String customMessage = plugin.getConfig().getString("rewards.levels." + level + ".broadcast-message");
        if (customMessage != null && !customMessage.isEmpty()) {
            return customMessage;
        }
        return plugin.getConfig().getString("rewards.broadcast-message", 
                "§6§l🎉 REWARD ANNOUNCEMENT! §e%player% §7reached §6Level %level% §7and received amazing rewards! 🎉");
    }

    public boolean hasPlayerClaimedReward(Player player, int level) {
        DataManager.PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return data.hasClaimedReward(level);
    }

    public void giveReward(Player player, int level) {
        if (!isRewardEnabled()) {
            player.sendMessage(FormatUtils.colorize("§cReward system is disabled!"));
            return;
        }

        if (!hasRewardForLevel(level)) {
            player.sendMessage(getMessage("reward-no-rewards").replace("%level%", String.valueOf(level)));
            return;
        }

        // ✅ SECURITY CHECK: Player must have reached the level
        int playerLevel = plugin.getLevelManager().getPlayerLevel(player);
        if (level > playerLevel) {
            player.sendMessage(getMessage("reward-level-not-reached")
                    .replace("%level%", String.valueOf(level))
                    .replace("%current_level%", String.valueOf(playerLevel)));
            return;
        }

        if (hasPlayerClaimedReward(player, level)) {
            // PERBAIKAN: Jangan tampilkan pesan jika reward diberikan otomatis
            // player.sendMessage(getMessage("reward-already-claimed").replace("%level%", String.valueOf(level)));
            return;
        }

        // Execute reward commands
        List<String> commands = getRewardCommands(level);
        for (String command : commands) {
            String formattedCommand = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
        }

        // Mark as claimed
        DataManager.PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        data.addClaimedReward(level);
        plugin.getDataManager().updatePlayerData(player.getUniqueId(), data);

        // PERBAIKAN: Save data immediately after giving reward
        plugin.getDataManager().saveAllData();

        // Send reward message to player
        String rewardMessage = getRewardMessage(level);
        player.sendMessage(FormatUtils.colorize(rewardMessage));
        
        // Send confirmation message
        player.sendMessage(getMessage("reward-received").replace("%level%", String.valueOf(level)));

        // Broadcast announcement if enabled
        if (isBroadcastEnabled() && isBroadcastEnabledForLevel(level)) {
            String broadcastMessage = getBroadcastMessage(level)
                    .replace("%player%", player.getName())
                    .replace("%level%", String.valueOf(level));
            Bukkit.broadcastMessage(FormatUtils.colorize(broadcastMessage));
        }
        
        // PERBAIKAN: Log reward distribution
        plugin.getLogger().info("Reward for level " + level + " given to " + player.getName());
    }

    // NEW METHOD: Claim all eligible rewards
    public void giveAllRewards(Player player) {
        if (!isRewardEnabled()) {
            player.sendMessage(FormatUtils.colorize("§cReward system is disabled!"));
            return;
        }

        int playerLevel = plugin.getLevelManager().getPlayerLevel(player);
        int claimedCount = 0;
        int totalEligible = 0;

        // Count total eligible rewards
        for (String levelStr : getRewardLevels()) {
            try {
                int rewardLevel = Integer.parseInt(levelStr);
                if (rewardLevel <= playerLevel && !hasPlayerClaimedReward(player, rewardLevel)) {
                    totalEligible++;
                }
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        if (totalEligible == 0) {
            player.sendMessage(FormatUtils.colorize("§cNo rewards available to claim!"));
            player.sendMessage(FormatUtils.colorize("§7You have already claimed all rewards for your current level."));
            return;
        }

        player.sendMessage(FormatUtils.colorize("§6§lCLAIMING REWARDS... §eProcessing §6" + totalEligible + " §eeligible rewards..."));

        // Claim all eligible rewards
        for (String levelStr : getRewardLevels()) {
            try {
                int rewardLevel = Integer.parseInt(levelStr);
                if (rewardLevel <= playerLevel && !hasPlayerClaimedReward(player, rewardLevel)) {
                    giveReward(player, rewardLevel);
                    claimedCount++;
                }
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        // Send summary
        if (claimedCount > 0) {
            player.sendMessage(FormatUtils.colorize("§a§lSUCCESS! §eClaimed §6" + claimedCount + " §erewards!"));
            if (claimedCount < totalEligible) {
                player.sendMessage(FormatUtils.colorize("§7Some rewards failed to claim. Check console for details."));
            }
        } else {
            player.sendMessage(FormatUtils.colorize("§cNo rewards were claimed. There might be an issue."));
        }
    }

    public void checkAndGiveRewards(Player player, int newLevel) {
        if (!isRewardEnabled()) return;

        for (String levelStr : getRewardLevels()) {
            try {
                int rewardLevel = Integer.parseInt(levelStr);
                // ✅ Only give rewards for levels that player has reached
                if (rewardLevel <= newLevel && !hasPlayerClaimedReward(player, rewardLevel)) {
                    giveReward(player, rewardLevel);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid reward level in config: " + levelStr);
            }
        }
    }

    private String getMessage(String path) {
        return plugin.getConfig().getString("messages." + path, "§cMessage not found: " + path);
    }
}