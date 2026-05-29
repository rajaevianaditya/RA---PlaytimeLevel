package id.rajaaditya.playtimelevel.commands;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import id.rajaaditya.playtimelevel.managers.DataManager;
import id.rajaaditya.playtimelevel.managers.LevelManager;
import id.rajaaditya.playtimelevel.utils.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LevelCommand implements CommandExecutor, TabCompleter {

    private final PlaytimeLevel plugin;
    private final LevelManager levelManager;

    public LevelCommand(PlaytimeLevel plugin) {
        this.plugin = plugin;
        this.levelManager = plugin.getLevelManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0) {
                // /level - check yourself
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command is for players only!");
                    return true;
                }
                
                if (!sender.hasPermission("playtimelevel.use")) {
                    sender.sendMessage(getMessage("no-permission"));
                    return true;
                }
                
                showSelfStats((Player) sender);
                return true;
            }
            
            switch (args[0].toLowerCase()) {
                case "check":
                    // /level check <player>
                    if (!sender.hasPermission("playtimelevel.check.others")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /level check <player>");
                        return true;
                    }
                    
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(getMessage("player-not-found").replace("%player%", args[1]));
                        return true;
                    }
                    
                    showOtherStats(sender, target);
                    break;
                    
                case "reload":
                    // /level reload
                    if (!sender.hasPermission("playtimelevel.reload")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    plugin.reloadAllConfigs();
                    sender.sendMessage(getMessage("reload-success"));
                    break;
                    
                case "info":
                    // /level info - detailed level info
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cThis command is for players only!");
                        return true;
                    }
                    
                    if (!sender.hasPermission("playtimelevel.use")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    showDetailedInfo((Player) sender);
                    break;
                    
                case "claim":
                    // /level claim <level|all>
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cThis command is for players only!");
                        return true;
                    }
                    
                    if (!sender.hasPermission("playtimelevel.reward")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /level claim <level|all>");
                        sender.sendMessage("§7Available reward levels: " + String.join(", ", plugin.getRewardManager().getRewardLevels()));
                        return true;
                    }
                    
                    if (args[1].equalsIgnoreCase("all")) {
                        // Claim all eligible rewards
                        plugin.getRewardManager().giveAllRewards((Player) sender);
                    } else {
                        try {
                            int level = Integer.parseInt(args[1]);
                            plugin.getRewardManager().giveReward((Player) sender, level);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cInvalid level number! Use 'all' to claim all rewards.");
                        }
                    }
                    break;
                    
                case "rewards":
                    // /level rewards - list available rewards
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cThis command is for players only!");
                        return true;
                    }
                    
                    if (!sender.hasPermission("playtimelevel.use")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    showRewardsList((Player) sender);
                    break;
                    
                case "reset":
                    // /level reset <player>
                    if (!sender.hasPermission("playtimelevel.reset")) {
                        sender.sendMessage(getMessage("no-permission"));
                        return true;
                    }
                    
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /level reset <player>");
                        return true;
                    }
                    
                    resetPlayerData(sender, args[1]);
                    break;
                    
                default:
                    showHelp(sender);
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage("§cError executing command. Check console for details.");
            plugin.getLogger().severe("Error executing level command: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }

    // Reset player reward data
    private void resetPlayerData(CommandSender sender, String playerName) {
        try {
            // Cari player online dulu
            Player onlinePlayer = Bukkit.getPlayer(playerName);
            UUID playerUUID;
            String targetName;
            
            if (onlinePlayer != null) {
                playerUUID = onlinePlayer.getUniqueId();
                targetName = onlinePlayer.getName();
            } else {
                // Cari offline player
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer.hasPlayedBefore()) {
                    playerUUID = offlinePlayer.getUniqueId();
                    targetName = offlinePlayer.getName();
                } else {
                    sender.sendMessage(getMessage("player-not-found").replace("%player%", playerName));
                    return;
                }
            }
            
            DataManager dataManager = plugin.getDataManager();
            DataManager.PlayerData playerData = dataManager.getPlayerData(playerUUID);
            
            // Simpan data sebelum reset untuk logging
            int oldLevel = playerData.getLevel();
            List<Integer> oldClaimedRewards = new ArrayList<>(playerData.getClaimedRewards());
            
            // Reset data
            playerData.setClaimedRewards(new ArrayList<>());
            dataManager.updatePlayerData(playerUUID, playerData);
            
            // ✅ INSTANT SAVE KE FILE
            dataManager.saveAllData();
            
            // Kirim konfirmasi
            sender.sendMessage(FormatUtils.colorize("§a✓ Successfully reset reward data for §e" + targetName));
            sender.sendMessage(FormatUtils.colorize("§7Reset §b" + oldClaimedRewards.size() + " §7claimed rewards"));
            
            if (!oldClaimedRewards.isEmpty()) {
                sender.sendMessage(FormatUtils.colorize("§7Previously claimed levels: §e" + oldClaimedRewards));
            }
            
            // Log ke console
            plugin.getLogger().info("Player " + targetName + "'s reward data has been reset by " + sender.getName());
            plugin.getLogger().info("Reset " + oldClaimedRewards.size() + " claimed rewards: " + oldClaimedRewards);
            
        } catch (Exception e) {
            sender.sendMessage("§cError resetting player data. Check console for details.");
            plugin.getLogger().severe("Error resetting player data for " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSelfStats(Player player) {
        try {
            int level = levelManager.getPlayerLevel(player);
            double playtimeHours = levelManager.getPlaytimeHours(player);
            double progressPercent = levelManager.getProgressPercent(player);
            String progressBar = levelManager.getProgressBar(player);
            double nextLevelIn = levelManager.getNextLevelInHours(player);
            int nextLevel = levelManager.getNextLevel(player);
            double requiredForNext = levelManager.getRequiredForNextLevel(player);
            
            String maxLevelMessage = "";
            if (levelManager.isMaxLevel(player)) {
                maxLevelMessage = getMessage("max-level-reached");
            }
            
            String message = getMessage("self-stats")
                    .replace("%level%", levelManager.getFormattedLevel(player))
                    .replace("%playtime_hours%", String.format("%.1f", playtimeHours))
                    .replace("%progress_percent%", String.format("%.1f", progressPercent))
                    .replace("%progress_bar%", progressBar)
                    .replace("%next_level_in%", String.format("%.1f hours", nextLevelIn))
                    .replace("%next_level%", String.valueOf(nextLevel))
                    .replace("%required_for_next%", String.format("%.1f", requiredForNext))
                    .replace("%max_level_message%", maxLevelMessage);
            
            player.sendMessage(FormatUtils.colorize(message));
        } catch (Exception e) {
            player.sendMessage("§cError displaying level statistics.");
            plugin.getLogger().severe("Error showing self stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showOtherStats(CommandSender sender, Player target) {
        try {
            int level = levelManager.getPlayerLevel(target);
            double playtimeHours = levelManager.getPlaytimeHours(target);
            double progressPercent = levelManager.getProgressPercent(target);
            String progressBar = levelManager.getProgressBar(target);
            double nextLevelIn = levelManager.getNextLevelInHours(target);
            int nextLevel = levelManager.getNextLevel(target);
            double requiredForNext = levelManager.getRequiredForNextLevel(target);
            
            String maxLevelMessage = "";
            if (levelManager.isMaxLevel(target)) {
                maxLevelMessage = getMessage("max-level-reached");
            }
            
            String message = getMessage("other-stats")
                    .replace("%player%", target.getName())
                    .replace("%level%", levelManager.getFormattedLevel(target))
                    .replace("%playtime_hours%", String.format("%.1f", playtimeHours))
                    .replace("%progress_percent%", String.format("%.1f", progressPercent))
                    .replace("%progress_bar%", progressBar)
                    .replace("%next_level_in%", String.format("%.1f hours", nextLevelIn))
                    .replace("%next_level%", String.valueOf(nextLevel))
                    .replace("%required_for_next%", String.format("%.1f", requiredForNext))
                    .replace("%max_level_message%", maxLevelMessage);
            
            sender.sendMessage(FormatUtils.colorize(message));
        } catch (Exception e) {
            sender.sendMessage("§cError displaying player statistics.");
            plugin.getLogger().severe("Error showing other stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Show detailed level information
    private void showDetailedInfo(Player player) {
        try {
            int level = levelManager.getPlayerLevel(player);
            double playtimeHours = levelManager.getPlaytimeHours(player);
            double nextLevelIn = levelManager.getNextLevelInHours(player);
            double requiredForNext = levelManager.getRequiredForNextLevel(player);
            int nextLevel = levelManager.getNextLevel(player);
            double progressPercent = levelManager.getProgressPercent(player);
            int maxLevel = levelManager.getMaxLevelConfig();
            double totalForCurrent = levelManager.getTotalForCurrentLevel(player);
            
            player.sendMessage(FormatUtils.colorize("§6=== §eDetailed Level Information §6==="));
            player.sendMessage(FormatUtils.formatDetailedLevelInfo(level, playtimeHours, requiredForNext, nextLevelIn));
            player.sendMessage(FormatUtils.colorize("§7Progress: §b" + String.format("%.1f", progressPercent) + "%"));
            player.sendMessage(FormatUtils.colorize("§7Progress Bar: " + levelManager.getProgressBar(player)));
            player.sendMessage(FormatUtils.colorize("§7Next Level: §e" + nextLevel));
            player.sendMessage(FormatUtils.colorize("§7Max Level: §6" + maxLevel));
            player.sendMessage(FormatUtils.colorize("§7Total Hours for Current Level: §b" + String.format("%.1f", totalForCurrent)));
            
            if (levelManager.isMaxLevel(player)) {
                player.sendMessage(FormatUtils.colorize("§6§l✓ MAX LEVEL REACHED!"));
                player.sendMessage(FormatUtils.colorize("§7You have achieved the highest possible level!"));
            } else if (level <= 1) {
                player.sendMessage(FormatUtils.colorize("§7Need §b" + levelManager.getRequiredHoursForLevel(2) + " hours §7to reach §eLevel 2"));
            } else {
                player.sendMessage(FormatUtils.colorize("§7Need §b" + String.format("%.1f", requiredForNext) + " hours §7for §eLevel " + nextLevel));
            }
        } catch (Exception e) {
            player.sendMessage("§cError displaying detailed information.");
            plugin.getLogger().severe("Error showing detailed info: " + e.getMessage());
        }
    }

    // Show rewards list
    private void showRewardsList(Player player) {
        try {
            int currentLevel = levelManager.getPlayerLevel(player);
            player.sendMessage(FormatUtils.colorize("§6=== §eAvailable Rewards §6==="));
            
            boolean hasRewards = false;
            int availableCount = 0;
            
            for (String levelStr : plugin.getRewardManager().getRewardLevels()) {
                try {
                    int rewardLevel = Integer.parseInt(levelStr);
                    boolean claimed = plugin.getRewardManager().hasPlayerClaimedReward(player, rewardLevel);
                    boolean canClaim = currentLevel >= rewardLevel && !claimed;
                    boolean futureReward = currentLevel < rewardLevel;
                    
                    if (canClaim) {
                        availableCount++;
                    }
                    
                    String status;
                    if (claimed) {
                        status = "§a§l✓ CLAIMED";
                    } else if (canClaim) {
                        status = "§e§l! CLAIM NOW";
                    } else {
                        status = "§7§l● LOCKED";
                    }
                    
                    String levelDisplay = String.format("§eLevel §6%d", rewardLevel);
                    if (futureReward) {
                        levelDisplay += String.format(" §7(need %d more levels)", rewardLevel - currentLevel);
                    }
                    
                    player.sendMessage(FormatUtils.colorize(String.format("  %s §8- §f%s", levelDisplay, status)));
                    hasRewards = true;
                    
                } catch (NumberFormatException e) {
                    // Skip invalid level entries
                }
            }
            
            if (!hasRewards) {
                player.sendMessage(FormatUtils.colorize("§7No rewards configured."));
            } else {
                player.sendMessage("");
                if (availableCount > 0) {
                    player.sendMessage(FormatUtils.colorize("§7You have §e" + availableCount + " §7rewards available to claim!"));
                    player.sendMessage(FormatUtils.colorize("§7Use §e/level claim all §7to claim all rewards at once!"));
                }
                player.sendMessage(FormatUtils.colorize("§7Use §e/level claim <level> §7to claim specific rewards!"));
                player.sendMessage(FormatUtils.colorize("§7You can only claim rewards for levels you've reached."));
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError displaying rewards list.");
            plugin.getLogger().severe("Error showing rewards list: " + e.getMessage());
        }
    }

    // Show command help
    private void showHelp(CommandSender sender) {
        sender.sendMessage(FormatUtils.colorize(getMessage("help-header")));
        sender.sendMessage(FormatUtils.colorize(getMessage("help-basic")));
        
        if (sender.hasPermission("playtimelevel.check.others") || 
            sender.hasPermission("playtimelevel.reset") || 
            sender.hasPermission("playtimelevel.reload")) {
            sender.sendMessage(FormatUtils.colorize(getMessage("help-admin")));
        }
        
        sender.sendMessage(FormatUtils.colorize(getMessage("help-info")));
        
        // Add personal level status for players
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (levelManager.isMaxLevel(player)) {
                sender.sendMessage(FormatUtils.colorize("§6§l✓ You have reached MAX LEVEL!"));
            } else {
                double currentHours = levelManager.getPlaytimeHours(player);
                double hoursNeeded = levelManager.getTotalHoursForMaxLevel() - currentHours;
                sender.sendMessage(FormatUtils.colorize("§7You need §e" + String.format("%.1f", hoursNeeded) + " hours §7to reach Max Level"));
            }
        }
    }

    private String getMessage(String path) {
        // Baca dari messages.yml, fallback ke "Message not found"
        String msg = plugin.getMessagesConfig().getString(path, null);
        if (msg == null) {
            plugin.getLogger().warning("Missing message key in messages.yml: " + path);
            return "§cMessage not found: " + path;
        }
        return msg;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        try {
            if (args.length == 1) {
                completions.add("check");
                completions.add("info");
                completions.add("rewards");
                completions.add("claim");
                
                if (sender.hasPermission("playtimelevel.reset")) {
                    completions.add("reset");
                }
                
                if (sender.hasPermission("playtimelevel.reload")) {
                    completions.add("reload");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("playtimelevel.check.others") || sender.hasPermission("playtimelevel.reset")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            completions.add(player.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("claim")) {
                    // Show available reward levels + "all"
                    completions.add("all");
                    completions.addAll(plugin.getRewardManager().getRewardLevels());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in tab complete: " + e.getMessage());
        }
        
        return completions;
    }
}