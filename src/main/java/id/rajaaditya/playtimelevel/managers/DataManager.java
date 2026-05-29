package id.rajaaditya.playtimelevel.managers;

import id.rajaaditya.playtimelevel.PlaytimeLevel;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private final PlaytimeLevel plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final HashMap<UUID, PlayerData> playerData = new HashMap<>();

    public DataManager(PlaytimeLevel plugin) {
        this.plugin = plugin;
        setupDataFile();
        loadAllData();
        startAutoSave();
    }

    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadAllData() {
        playerData.clear();
        if (dataConfig.getConfigurationSection("players") != null) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int level = dataConfig.getInt("players." + uuidString + ".level", 1);
                long lastPlaytime = dataConfig.getLong("players." + uuidString + ".lastPlaytime", 0);
                
                // Load claimed rewards
                List<Integer> claimedRewards = new ArrayList<>();
                if (dataConfig.contains("players." + uuidString + ".claimedRewards")) {
                    claimedRewards = dataConfig.getIntegerList("players." + uuidString + ".claimedRewards");
                }
                
                playerData.put(uuid, new PlayerData(level, lastPlaytime, claimedRewards));
            }
        }
    }

    public void saveAllData() {
        for (UUID uuid : playerData.keySet()) {
            PlayerData data = playerData.get(uuid);
            dataConfig.set("players." + uuid.toString() + ".level", data.getLevel());
            dataConfig.set("players." + uuid.toString() + ".lastPlaytime", data.getLastPlaytime());
            dataConfig.set("players." + uuid.toString() + ".claimedRewards", data.getClaimedRewards());
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    private void startAutoSave() {
        int interval = plugin.getConfig().getInt("settings.data-save-interval", 300) * 20;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveAllData, interval, interval);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerData(1, 0, new ArrayList<>()));
    }

    public void updatePlayerData(UUID uuid, PlayerData data) {
        playerData.put(uuid, data);
    }

    public static class PlayerData {
        private int level;
        private long lastPlaytime;
        private List<Integer> claimedRewards;

        public PlayerData(int level, long lastPlaytime, List<Integer> claimedRewards) {
            this.level = level;
            this.lastPlaytime = lastPlaytime;
            this.claimedRewards = claimedRewards;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public long getLastPlaytime() {
            return lastPlaytime;
        }

        public void setLastPlaytime(long lastPlaytime) {
            this.lastPlaytime = lastPlaytime;
        }

        public List<Integer> getClaimedRewards() {
            return claimedRewards;
        }

        public void setClaimedRewards(List<Integer> claimedRewards) {
            this.claimedRewards = claimedRewards;
        }

        public void addClaimedReward(int level) {
            if (!claimedRewards.contains(level)) {
                claimedRewards.add(level);
            }
        }

        public boolean hasClaimedReward(int level) {
            return claimedRewards.contains(level);
        }
    }
}