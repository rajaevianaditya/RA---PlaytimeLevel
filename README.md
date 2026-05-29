# PlaytimeLevel — Documentation
**Version:** 1.6  
**Author:** RajaAditya  
**Website:** https://rajaaditya.my.id/  
**Supported:** Paper 1.21.x (including 1.21.10 & 1.21.11)  
**Dependencies:** EssentialsX (required), PlaceholderAPI (optional)

---

## Table of Contents
1. [Overview](#overview)
2. [Requirements](#requirements)
3. [Installation](#installation)
4. [File Structure](#file-structure)
5. [Level System](#level-system)
6. [Commands](#commands)
7. [Permissions](#permissions)
8. [Configuration — config.yml](#configuration--configyml)
9. [Messages — messages.yml](#messages--messagesyml)
10. [PlaceholderAPI](#placeholderapi)
11. [Reward System](#reward-system)
12. [Data Storage](#data-storage)
13. [Playtime Source](#playtime-source)
14. [FAQ](#faq)

---

## Overview

PlaytimeLevel is a playtime-based leveling plugin for Paper/Spigot servers. Players earn levels
automatically based on how long they have played on the server. The level system is **progressive**
— each level requires slightly more playtime than the previous one, scaling from ~3 hours per level
at the start up to ~40 hours per level near the top.

Key features:
- Progressive level system (Level 1–100, ~1440 hours total at 8h/day = ~6 months)
- Fully configurable rewards per level (commands, messages, broadcast)
- One-time claimable rewards — players can also claim manually
- **Level color system** — each level range has a configurable color, fully editable from `config.yml`
- PlaceholderAPI support with 20+ placeholders
- EssentialsX integration with Bukkit Statistics fallback
- Separate messages.yml for easy localization
- Auto-save player data with configurable interval

---

## Requirements

| Dependency     | Type     | Version       |
|----------------|----------|---------------|
| Paper / Spigot | Server   | 1.21.x        |
| Java           | Runtime  | 17 or higher  |
| EssentialsX    | Required | 2.x (latest)  |
| PlaceholderAPI | Optional | 2.11+         |

---

## Installation

1. Download `PlaytimeLevel-1.6.jar` and place it in your server's `plugins/` folder.
2. Make sure **EssentialsX** is already installed and running.
3. (Optional) Install **PlaceholderAPI** if you want to use placeholders in scoreboards, tab lists, etc.
4. Start or restart your server.
5. The plugin will generate `config.yml`, `messages.yml`, and `data.yml` automatically
   inside `plugins/PlaytimeLevel/`.
6. Edit the files as needed, then run `/level reload` to apply changes without restarting.

---

## File Structure

```
plugins/
└── PlaytimeLevel/
    ├── config.yml       ← Main settings, rewards, placeholder format
    ├── messages.yml     ← All in-game messages (editable without recompiling)
    └── data.yml         ← Player level & reward data (auto-managed, do not edit manually)
```

Source structure (for developers):

```
src/main/
├── java/id/rajaaditya/playtimelevel/
│   ├── PlaytimeLevel.java                  ← Main plugin class
│   ├── commands/
│   │   └── LevelCommand.java               ← All /level subcommand logic
│   ├── managers/
│   │   ├── DataManager.java                ← Read/write player data (data.yml)
│   │   ├── EssentialsManager.java          ← EssentialsX hook & playtime source
│   │   ├── LevelManager.java               ← Level calculation & progression
│   │   └── RewardManager.java              ← Reward logic (give, claim, broadcast)
│   ├── placeholders/
│   │   └── PlaytimeLevelExpansion.java     ← PlaceholderAPI expansion
│   └── utils/
│       └── FormatUtils.java                ← Color, progress bar, time formatting
└── resources/
    ├── plugin.yml
    ├── config.yml
    ├── messages.yml
    └── data.yml
```

---

## Level System

### How It Works

Levels are calculated in real-time from the player's total playtime. There is no event-based
trigger — the plugin checks all online players every N seconds (default: 60 seconds) and
recalculates their level automatically.

### Progressive Requirements

Each level requires more hours than the previous one. The formula scales from ~3.02 hours
for Level 2 up to ~39.67 hours for Level 100.

| Level Range | Hours Per Level | Cumulative Total |
|-------------|-----------------|-----------------|
| 2–11        | 3.02 – 3.46 h   | ~29 h           |
| 12–21       | 3.52 – 4.36 h   | ~67 h           |
| 22–31       | 4.48 – 5.81 h   | ~117 h          |
| 32–41       | 5.99 – 7.92 h   | ~184 h          |
| 42–51       | 8.18 – 10.86 h  | ~275 h          |
| 52–61       | 11.20 – 14.78 h | ~401 h          |
| 62–71       | 15.22 – 19.71 h | ~570 h          |
| 72–81       | 20.25 – 25.64 h | ~793 h          |
| 82–91       | 26.28 – 32.57 h | ~1079 h         |
| 92–100      | 33.32 – 39.67 h | ~1440 h         |

### Milestones (playing 8 hours/day)

| Level | Total Hours | Estimated Time |
|-------|-------------|----------------|
| 10    | ~29 h       | ~4 days        |
| 20    | ~67 h       | ~8 days        |
| 30    | ~117 h      | ~15 days       |
| 50    | ~275 h      | ~34 days       |
| 70    | ~570 h      | ~71 days       |
| 90    | ~1079 h     | ~135 days      |
| **100** | **~1440 h** | **~6 months** |

### Max Level

Default max level is **100**, configurable via `config.yml`:
```yaml
level-system:
  max-level: 100
```
Once a player reaches max level, their progress shows 100% and the formatted level
displays the `max-level-format` text instead of a number.

---

## Commands

Main command: `/level` — Aliases: `/lvl`, `/playtimelevel`

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/level` | View your own level, playtime, and progress | `playtimelevel.use` |
| `/level info` | Detailed breakdown of your level progression | `playtimelevel.use` |
| `/level rewards` | List all rewards — shows claimed, claimable, and locked | `playtimelevel.use` |
| `/level claim <level>` | Claim the reward for a specific level | `playtimelevel.reward` |
| `/level claim all` | Claim all unclaimed rewards you are eligible for | `playtimelevel.reward` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/level check <player>` | View another player's level and stats (must be online) | `playtimelevel.check.others` |
| `/level reset <player>` | Reset a player's claimed reward data (works offline too) | `playtimelevel.reset` |
| `/level reload` | Reload config.yml and messages.yml without restart | `playtimelevel.reload` |

### Command Output Example

```
=== Level Stats ===
Level: [ 25 ] |
Playtime: 120.5 hours
Progress: 67.3%
▊▊▊▊▊▊▊▊▊▊ (filled progress bar)
To Level 26: 2.3 hours
Required for Level 26: 5.15 hours
```

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playtimelevel.use` | Access `/level` to view own stats | `true` (all players) |
| `playtimelevel.reward` | Use `/level claim` to claim rewards | `true` (all players) |
| `playtimelevel.check.others` | Use `/level check <player>` | `op` |
| `playtimelevel.reload` | Use `/level reload` | `op` |
| `playtimelevel.reset` | Use `/level reset <player>` | `op` |
| `playtimelevel.*` | All permissions above | `op` |

---

## Configuration — config.yml

Full annotated reference:

```yaml
# PlaytimeLevel Configuration v1.6

settings:
  # How often player data is auto-saved to data.yml (in seconds)
  # Default: 300 (5 minutes)
  data-save-interval: 300

  # Use EssentialsX as the playtime source (recommended: true)
  # If false or EssentialsX is unavailable, falls back to Bukkit Statistics
  use-essentials-playtime: true

  # How often the plugin checks online players for level-ups (in seconds)
  # Lower = more responsive, higher = less server load
  # Default: 60
  level-check-interval: 60

level-system:
  # Maximum level players can reach
  max-level: 100

rewards:
  # Enable or disable the entire reward system
  enabled: true

  # Broadcast reward announcements to ALL online players
  broadcast-enabled: true
  broadcast-message: "§6§lANNOUNCEMENT! §e%player% §7reached §6Level %level% §7and received amazing rewards!"
  # Placeholders: %player%, %level%

  # Default message sent to the player when a reward is given
  reward-message: "§6§lREWARD! §eYou reached level %level% and received rewards!"
  already-claimed: "§cYou have already claimed the reward for level %level%"

  # Define rewards per level
  # You can add as many levels as you want
  levels:
    1:
      commands:
        - "eco give %player% 1000"         # Any console command, %player% = player name
      message: "§a§lLEVEL §6§l1 §a§lREWARD"  # Message shown only to the player
      broadcast: false                          # Override global broadcast for this level

    10:
      commands:
        - "eco give %player% 10000"
        - "ac key give %player% votecrate 5"
      message: "§a§lLEVEL §6§l10 §a§lREWARD"
      broadcast: true
      broadcast-message: "§e%player% §7just hit §6Level 10§7!"  # Optional per-level broadcast

placeholder:
  # Format used by %playtimelevel_level_formatted%
  level-format: "§e[ §6%level% §e] §f§l|"
  # Format used when player is at max level
  max-level-format: "§6§l[ §eMAX §6§l] §f§l|"

  # Format used by %playtimelevel_level_color_formatted%
  # %color% diganti warna otomatis dari level-colors, %level% diganti angka level
  level-color-format: "%color%[ %level% ] §f§l|"

  progress-bar:
    length: 10          # Total number of characters in the bar
    filled: "§a▊"       # Character for completed portion
    empty: "§7▊"        # Character for remaining portion

  time-format:
    hours: "§b%hours% §ehours"
    minutes: "§b%minutes% §eminutes"
    seconds: "§b%seconds% §eseconds"

storage:
  type: "file"          # Only "file" is fully implemented; "mysql" is planned
  file: "data.yml"

# ============================================================
# Level Color Settings
# Setiap entry mendefinisikan warna untuk range level tertentu.
#   min   : level minimum (inklusif)
#   max   : level maksimum (inklusif)
#   color : kode warna Minecraft (§0–§f)
#
# Kode warna Minecraft:
#   §0 Hitam    §1 Dark Blue   §2 Dark Green  §3 Dark Aqua
#   §4 Dark Red §5 Ungu        §6 Emas        §7 Abu-abu
#   §8 Dark Gray §9 Biru       §a Hijau       §b Aqua
#   §c Merah    §d Pink        §e Kuning      §f Putih
#
# Urutan dicek dari atas ke bawah — entry pertama yang cocok dipakai.
# ============================================================
level-colors:
  - min: 1
    max: 9
    color: "§7"
  - min: 10
    max: 19
    color: "§f"
  - min: 20
    max: 29
    color: "§6"
  - min: 30
    max: 39
    color: "§b"
  - min: 40
    max: 49
    color: "§a"
  - min: 50
    max: 59
    color: "§3"
  - min: 60
    max: 69
    color: "§4"
  - min: 70
    max: 79
    color: "§d"
  - min: 80
    max: 89
    color: "§9"
  - min: 90
    max: 99
    color: "§5"
  - min: 100
    max: 100
    color: "§e"
```

---

## Messages — messages.yml

All player-facing messages are stored here. Edit freely — use `/level reload` to apply.

```yaml
# General
no-permission: "§cYou don't have permission!"
player-not-found: "§cPlayer §e%player% §cnot found!"
player-not-online: "§cPlayer §e%player% §cis not online!"
reload-success: "§aConfiguration reloaded successfully!"

# Level
max-level-reached: "§6§lMAX LEVEL! §eYou have reached the highest level!"

# Rewards
reward-received: "§a§lREWARD! §eYou received level %level% rewards!"
reward-already-claimed: "§cYou have already claimed reward for level %level%"
reward-no-rewards: "§cNo rewards available for level %level%"
reward-level-not-reached: "§cYou haven't reached level %level% yet! Your current level is %current_level%"

# Admin
reset-success: "§a✓ Successfully reset reward data for §e%player%"

# Stats display, help messages (multi-line using | syntax)
self-stats: |
  ...
```

### Available Placeholders in Messages

| Placeholder | Used In |
|-------------|---------|
| `%player%` | player-not-found, player-not-online, reset-success, broadcast |
| `%level%` | reward-received, reward-already-claimed, reward-no-rewards, reward-level-not-reached |
| `%current_level%` | reward-level-not-reached |
| `%playtime_hours%` | self-stats, other-stats |
| `%progress_percent%` | self-stats, other-stats |
| `%progress_bar%` | self-stats, other-stats |
| `%next_level%` | self-stats, other-stats |
| `%next_level_in%` | self-stats, other-stats |
| `%required_for_next%` | self-stats, other-stats |
| `%max_level_message%` | self-stats, other-stats |

---

## PlaceholderAPI

Requires **PlaceholderAPI** to be installed. All placeholders use the prefix `%playtimelevel_`.

### Full Placeholder List

| Placeholder | Returns | Example |
|-------------|---------|---------|
| `%playtimelevel_level%` | Current level as number | `25` |
| `%playtimelevel_level_formatted%` | Level with color formatting | `[ 25 ] \|` |
| `%playtimelevel_playtime_hours%` | Total playtime in hours | `120.5` |
| `%playtimelevel_playtime_hours_formatted%` | Formatted playtime | `120.5 hours` |
| `%playtimelevel_progress_percent%` | Progress to next level (%) | `67.3` |
| `%playtimelevel_progress_bar%` | Visual progress bar | `▊▊▊▊▊▊▊▊▊▊` |
| `%playtimelevel_next_level%` | Next level number | `26` |
| `%playtimelevel_next_level_in_hours%` | Hours remaining to next level | `2.3` |
| `%playtimelevel_next_level_in_hours_formatted%` | Formatted hours to next level | `2.3 hours` |
| `%playtimelevel_required_for_next_level%` | Total hours required for next level | `5.15` |
| `%playtimelevel_required_for_next_level_formatted%` | Formatted version | `5.2 hours` |
| `%playtimelevel_total_required_current%` | Hours required to unlock current level | `4.87` |
| `%playtimelevel_total_required_current_formatted%` | Formatted version | `4.9 hours` |
| `%playtimelevel_total_current%` | Cumulative hours needed for current level | `116.6` |
| `%playtimelevel_total_current_formatted%` | Formatted version | `116.6 hours` |
| `%playtimelevel_level_info%` | One-line level summary | `Level 25 (120.5/125.3 hours) - Need 2.3 hours to Level 26` |
| `%playtimelevel_max_level%` | Max level configured | `100` |
| `%playtimelevel_is_max_level%` | Whether player is at max level | `true` / `false` |
| `%playtimelevel_total_max_level%` | Total hours to reach max level | `1440.0` |
| `%playtimelevel_total_max_level_formatted%` | Formatted version | `1440.0 hours` |
| `%playtimelevel_reward_claimed_<level>%` | Whether reward for a level is claimed | `true` / `false` |
| `%playtimelevel_level_color%` | Minecraft color code untuk level saat ini | `§6` |
| `%playtimelevel_level_colored%` | Angka level dengan kode warnanya | `§625` |
| `%playtimelevel_level_color_formatted%` | Level dengan format & warna dari config (`level-color-format`) | `§6[ 25 ] §f§l|` |

> **Example:** `%playtimelevel_reward_claimed_10%` returns `true` if the player has claimed level 10's reward.

> **Example:** `%playtimelevel_level_color%` returns `§6` for a player at level 20–29 (Gold range). Use it to colorize any text based on the player's level.

### Usage Examples

**Scoreboard line:**
```
Level: %playtimelevel_level_formatted%
Progress: %playtimelevel_progress_bar% %playtimelevel_progress_percent%%
Next in: %playtimelevel_next_level_in_hours%h
```

**Tab list prefix:**
```
%playtimelevel_level_formatted% %player_name%
```

**Colored level number (menggunakan warna dari `level-colors` di config):**
```
Level: %playtimelevel_level_colored%
```

**Level formatted dengan warna otomatis (pengganti `level_formatted` yang warnanya dinamis):**
```
Level: %playtimelevel_level_color_formatted%
```

**Custom colored text berdasarkan level:**
```
%playtimelevel_level_color%Level %playtimelevel_level% §r— %playtimelevel_playtime_hours%h played
```

---

## Reward System

### How Rewards Work

1. Every `level-check-interval` seconds, the plugin checks if any online player has leveled up.
2. If a player leveled up and has an unclaimed reward for that level, the reward is given **automatically**.
3. Players can also claim rewards manually using `/level claim <level>` or `/level claim all`.
4. Each reward can only be claimed **once per player**.
5. Reward commands are executed from **console** with the player's name replacing `%player%`.

### Adding a New Reward Level

In `config.yml` under `rewards.levels`, add a new entry:

```yaml
rewards:
  levels:
    25:
      commands:
        - "eco give %player% 25000"
        - "give %player% diamond 10"
        - "crate key give %player% legendary 1"
      message: "§d§lLEVEL 25 MILESTONE REWARD!"
      broadcast: true
      broadcast-message: "§e%player% §7reached §dLevel 25§7! Amazing!"
```

Then run `/level reload` — no restart needed.

### Reward Security

Before giving any reward, the plugin verifies:
- The reward system is enabled (`rewards.enabled: true`)
- A reward is configured for that level
- The player has actually reached that level (anti-cheat)
- The player has not already claimed the reward

---

## Data Storage

Player data is stored in `plugins/PlaytimeLevel/data.yml`.

### Stored Per Player
- `level` — last known level (used to detect level-ups between checks)
- `lastPlaytime` — last recorded playtime in seconds
- `claimedRewards` — list of level numbers whose rewards have been claimed

### Auto-Save
Data is saved automatically every `data-save-interval` seconds (default: 5 minutes).
Data is also saved immediately on level-up, reward claim, and when the plugin is disabled.

### Manual Save
No manual save command exists — data is handled automatically. Use `/level reload` only for configs.

> ⚠️ Do not edit `data.yml` manually while the server is running. Changes will be overwritten by the next auto-save.

---

## Playtime Source

The plugin uses a **two-tier fallback system** to get playtime:

### Tier 1 — EssentialsX (primary)
Hooks into EssentialsX via `IEssentials` API and calls `user.getPlaytime()` using reflection.
Returns playtime in **milliseconds**, converted to seconds internally.

This is the recommended source because EssentialsX tracks playtime accurately across sessions,
including AFK time handling if configured in EssentialsX.

### Tier 2 — Bukkit Statistics (fallback)
Used automatically if:
- EssentialsX is not installed
- EssentialsX hook fails
- `user.getPlaytime()` throws an exception

Reads `player.getStatistic(Statistic.PLAY_ONE_MINUTE)` which returns **ticks**, divided by 20
to get seconds. Note: this statistic is stored per-world by default in vanilla Minecraft and
may differ slightly from EssentialsX playtime.

### Which One Is Active?
Check your server console on startup. You will see one of:
```
[PlaytimeLevel] Successfully hooked into EssentialsX!
```
or
```
[PlaytimeLevel] EssentialsX not found! Using Bukkit statistics for playtime.
```

---

## FAQ

**Q: A player's level is not updating.**  
A: The plugin checks every `level-check-interval` seconds (default: 60). Wait up to a minute after
the player's playtime increases. If it still doesn't update, check that EssentialsX is running and
the hook was successful (see console startup logs).

**Q: Can I change the total hours required to reach Level 100?**  
A: Yes, but it requires editing the `LEVEL_REQUIREMENTS` array in `LevelManager.java` and
recompiling the plugin. The current values total ~1440 hours (~6 months at 8 hours/day).

**Q: Can I add more than 100 levels?**  
A: Change `level-system.max-level` in `config.yml`. However, levels beyond 100 will use a
fallback of 20 hours per level since the requirements array only covers up to 100.

**Q: `/level reload` — does it reload data.yml too?**  
A: No. `/level reload` only reloads `config.yml` and `messages.yml`. Player data in `data.yml`
is managed in memory and saved automatically.

**Q: Can I reset a player who is offline?**  
A: Yes. `/level reset <player>` works for both online and offline players as long as they have
played on the server before.

**Q: Rewards are not being given automatically.**  
A: Check that `rewards.enabled: true` in `config.yml`. Also verify the reward is defined for the
exact level number, and that the player's level actually increased during the last check cycle.

**Q: PlaceholderAPI placeholders are not working.**  
A: Make sure PlaceholderAPI is installed and enabled. Check the startup log for:
`[PlaytimeLevel] PlaceholderAPI expansion registered successfully!`
If it says "Failed to register", try running `/papi reload` then `/level reload`.

**Q: Can I use color codes in messages.yml?**  
A: Yes. Use `§` followed by a color code (e.g. `§a` for green, `§6` for gold, `§l` for bold).
Standard Minecraft color codes are fully supported.

**Q: Bagaimana cara mengubah warna level?**  
A: Edit section `level-colors` di `config.yml`. Setiap entry mendefinisikan range level (`min`–`max`) dan kode warna Minecraft (`color`). Setelah selesai, jalankan `/level reload` — tidak perlu restart server. Warna ini juga otomatis dipakai oleh placeholder `%playtimelevel_level_color%` dan `%playtimelevel_level_colored%`.