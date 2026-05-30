# PlaytimeLevel — Documentation

**Version:** 1.6  
**Author:** RajaAditya  
**Website:** https://rajaaditya.my.id/  
**Supported:** Paper 1.21.x (including 1.21.10 & 1.21.11)  
**Dependencies:** EssentialsX (required), PlaceholderAPI (optional)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Requirements](#2-requirements)
3. [Installation](#3-installation)
4. [File Structure](#4-file-structure)
5. [Level System](#5-level-system)
6. [Commands](#6-commands)
7. [Permissions](#7-permissions)
8. [Configuration — config.yml](#8-configuration--configyml)
9. [Messages — messages.yml](#9-messages--messagesyml)
10. [PlaceholderAPI](#10-placeholderapi)
11. [Reward System](#11-reward-system)
12. [Auto-Claim Behaviour](#12-auto-claim-behaviour)
13. [Data Storage](#13-data-storage)
14. [Playtime Source](#14-playtime-source)
15. [FAQ](#15-faq)

---

## 1. Overview

PlaytimeLevel is a playtime-based leveling plugin for Paper/Spigot servers. Players earn levels automatically based on how long they have played — no commands, no grind tasks, just time spent on your server.

**Key features:**
- Progressive level system (Level 1–100, ~1,440 hours total at 8h/day ≈ 6 months)
- Fully configurable rewards per level (commands, messages, broadcast)
- One-time claimable rewards — each reward can only be claimed once per player
- **Auto-claim on join** — unclaimed rewards are given automatically when a player logs in
- **Auto-claim on level-up** — scheduler detects level-ups and immediately gives rewards
- Level color system — each level range has a configurable color, editable from `config.yml`
- PlaceholderAPI support with 20+ placeholders
- EssentialsX integration with Bukkit Statistics fallback
- Separate `messages.yml` for easy localization
- Auto-save player data with configurable interval

---

## 2. Requirements

| Dependency     | Type     | Version                          |
|----------------|----------|----------------------------------|
| Paper / Spigot | Server   | 1.21.x (including 1.21.10/11)    |
| Java           | Runtime  | 17 or higher                     |
| EssentialsX    | Required | 2.x (latest recommended)         |
| PlaceholderAPI | Optional | 2.11+                            |

---

## 3. Installation

1. Download `PlaytimeLevel-1.6.jar` and place it in your server's `plugins/` folder.
2. Ensure **EssentialsX** is already installed and running.
3. *(Optional)* Install **PlaceholderAPI** if you want to use placeholders in scoreboards, tab lists, etc.
4. Start or restart your server.
5. The plugin will auto-generate `config.yml`, `messages.yml`, and `data.yml` inside `plugins/PlaytimeLevel/`.
6. Edit the files as needed, then run `/level reload` to apply changes without restarting.

> **Tip:** No server restart is needed after editing `config.yml` or `messages.yml` — just run `/level reload`.

---

## 4. File Structure

### Plugin Data Files

```
plugins/
└── PlaytimeLevel/
    ├── config.yml       ← Main settings, rewards, placeholder format
    ├── messages.yml     ← All in-game messages (editable without recompiling)
    └── data.yml         ← Player level & reward data (auto-managed, do not edit manually)
```

### Source Structure (for Developers)

```
src/main/
├── java/id/rajaaditya/playtimelevel/
│   ├── PlaytimeLevel.java                  ← Main plugin class (scheduler + join auto-claim)
│   ├── commands/
│   │   └── LevelCommand.java               ← All /level subcommand logic
│   ├── gui/
│   │   └── ClaimRewardGUI.java             ← Chest GUI for manual reward claiming (multi-page fix)
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

## 5. Level System

### How It Works

Levels are calculated in real-time from the player's total playtime. The plugin runs a scheduler every N seconds (default: 60s) and recalculates each online player's level. If a level-up is detected, rewards are given automatically — no manual action required.

> **Auto-claim note:** Rewards are given automatically in two situations: (1) when the scheduler detects a level-up while the player is online, and (2) when the player joins the server and has any unclaimed rewards from previous sessions.

### Progressive Requirements

Each level requires more playtime than the previous one. The formula scales from ~3.02 hours for Level 2 up to ~39.67 hours for Level 100, totalling ~1,440 hours to reach the maximum level.

| Level Range | Hours Per Level  | Cumulative Total |
|-------------|------------------|------------------|
| 2 – 11      | 3.02 – 3.46 h    | ~29 h            |
| 12 – 21     | 3.52 – 4.36 h    | ~67 h            |
| 22 – 31     | 4.48 – 5.81 h    | ~117 h           |
| 32 – 41     | 5.99 – 7.92 h    | ~184 h           |
| 42 – 51     | 8.18 – 10.86 h   | ~275 h           |
| 52 – 61     | 11.20 – 14.78 h  | ~401 h           |
| 62 – 71     | 15.22 – 19.71 h  | ~570 h           |
| 72 – 81     | 20.25 – 25.64 h  | ~793 h           |
| 82 – 91     | 26.28 – 32.57 h  | ~1,079 h         |
| 92 – 100    | 33.32 – 39.67 h  | ~1,440 h         |

### Milestones (playing 8 hours/day)

| Level | Total Hours  | Estimated Time |
|-------|-------------|----------------|
| 10    | ~29 h       | ~4 days        |
| 20    | ~67 h       | ~8 days        |
| 30    | ~117 h      | ~15 days       |
| 50    | ~275 h      | ~34 days       |
| 70    | ~570 h      | ~71 days       |
| 90    | ~1,079 h    | ~135 days      |
| **100** | **~1,440 h** | **~6 months** |

### Max Level

Default max level is **100**, configurable via `config.yml`:

```yaml
level-system:
  max-level: 100
```

Once a player reaches max level, their progress shows 100% and the formatted placeholder displays the `max-level-format` text instead of a number.

---

## 6. Commands

Main command: `/level` — Aliases: `/lvl`, `/playtimelevel`

### Player Commands

| Command              | Description                                                   | Permission             |
|----------------------|---------------------------------------------------------------|------------------------|
| `/level`             | View your own level, playtime, and progress                   | `playtimelevel.use`    |
| `/level info`        | Detailed breakdown of your level progression                  | `playtimelevel.use`    |
| `/level rewards`     | List all rewards — shows claimed, claimable, and locked       | `playtimelevel.use`    |
| `/level claim <lv>`  | Claim the reward for a specific level                         | `playtimelevel.reward` |
| `/level claim all`   | Claim all unclaimed rewards you are eligible for              | `playtimelevel.reward` |

### Admin Commands

| Command                  | Description                                                        | Permission                     |
|--------------------------|--------------------------------------------------------------------|--------------------------------|
| `/level check <player>`  | View another player's level and stats (must be online)             | `playtimelevel.check.others`   |
| `/level reset <player>`  | Reset a player's claimed reward data (works for offline too)       | `playtimelevel.reset`          |
| `/level reload`          | Reload `config.yml` and `messages.yml` without restart             | `playtimelevel.reload`         |

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

## 7. Permissions

| Permission                    | Description                              | Default     |
|-------------------------------|------------------------------------------|-------------|
| `playtimelevel.use`           | Access `/level` to view own stats        | All players |
| `playtimelevel.reward`        | Use `/level claim` to claim rewards      | All players |
| `playtimelevel.check.others`  | Use `/level check <player>`              | OP only     |
| `playtimelevel.reload`        | Use `/level reload`                      | OP only     |
| `playtimelevel.reset`         | Use `/level reset <player>`              | OP only     |
| `playtimelevel.*`             | All permissions above                    | OP only     |

---

## 8. Configuration — config.yml

Full annotated reference:

```yaml
# PlaytimeLevel Configuration v1.6

settings:
  # How often player data is auto-saved to data.yml (in seconds)
  data-save-interval: 300

  # Use EssentialsX as the playtime source (recommended: true)
  # If false or EssentialsX is unavailable, falls back to Bukkit Statistics
  use-essentials-playtime: true

  # How often the plugin checks online players for level-ups (in seconds)
  # Lower = more responsive, higher = less server load
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

  # Define rewards per level — add as many levels as you want
  levels:
    1:
      commands:
        - "eco give %player% 1000"
      message: "§a§lLEVEL §6§l1 §a§lREWARD"
      broadcast: false

    10:
      commands:
        - "eco give %player% 10000"
        - "ac key give %player% votecrate 5"
      message: "§a§lLEVEL §6§l10 §a§lREWARD"
      broadcast: true
      broadcast-message: "§e%player% §7just hit §6Level 10§7!"

placeholder:
  level-format: "§e[ §6%level% §e] §f§l|"
  max-level-format: "§6§l[ §eMAX §6§l] §f§l|"
  level-color-format: "%color%[ %level% ] §f§l|"

  progress-bar:
    length: 10
    filled: "§a▊"
    empty: "§7▊"

  time-format:
    hours: "§b%hours% §ehours"
    minutes: "§b%minutes% §eminutes"
    seconds: "§b%seconds% §eseconds"

storage:
  type: "file"
  file: "data.yml"

# ──────────────────────────────────────────────────────────
# Level Color Settings
# Each entry defines the color for a level range.
#   min   : minimum level (inclusive)
#   max   : maximum level (inclusive)
#   color : Minecraft color code (§0–§f)
#
# Entries are checked top to bottom — first match wins.
# ──────────────────────────────────────────────────────────
level-colors:
  - min: 1    max: 9    color: "§7"   # Gray
  - min: 10   max: 19   color: "§f"   # White
  - min: 20   max: 29   color: "§6"   # Gold
  - min: 30   max: 39   color: "§b"   # Aqua
  - min: 40   max: 49   color: "§a"   # Green
  - min: 50   max: 59   color: "§3"   # Dark Aqua
  - min: 60   max: 69   color: "§4"   # Dark Red
  - min: 70   max: 79   color: "§d"   # Pink
  - min: 80   max: 89   color: "§9"   # Blue
  - min: 90   max: 99   color: "§5"   # Purple
  - min: 100  max: 100  color: "§e"   # Yellow
```

---

## 9. Messages — messages.yml

All player-facing messages are stored here. Edit freely and run `/level reload` to apply. Standard Minecraft color codes (`§` prefix) are fully supported.

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
```

### Available Placeholders in Messages

| Placeholder        | Used In                                          |
|--------------------|--------------------------------------------------|
| `%player%`         | player-not-found, player-not-online, reset-success, broadcast |
| `%level%`          | reward-received, reward-already-claimed, reward-no-rewards, reward-level-not-reached |
| `%current_level%`  | reward-level-not-reached                         |

---

## 10. PlaceholderAPI

Requires **PlaceholderAPI** to be installed. All placeholders use the prefix `%playtimelevel_`.

### Full Placeholder List

| Placeholder                                    | Returns                                    | Example        |
|------------------------------------------------|--------------------------------------------|----------------|
| `%playtimelevel_level%`                        | Current level as number                    | `25`           |
| `%playtimelevel_level_formatted%`              | Level with color formatting                | `[ 25 ] \|`   |
| `%playtimelevel_level_color%`                  | Minecraft color code for current level     | `§6`           |
| `%playtimelevel_level_colored%`                | Level number with its color code           | `§625`         |
| `%playtimelevel_level_color_formatted%`        | Level with auto color from config          | `§6[ 25 ] §f§l\|` |
| `%playtimelevel_playtime_hours%`               | Total playtime in hours                    | `120.5`        |
| `%playtimelevel_playtime_hours_formatted%`     | Formatted playtime                         | `120.5 hours`  |
| `%playtimelevel_progress_percent%`             | Progress to next level (%)                 | `67.3`         |
| `%playtimelevel_progress_bar%`                 | Visual progress bar                        | `▊▊▊▊▊▊▊▊▊▊`  |
| `%playtimelevel_next_level%`                   | Next level number                          | `26`           |
| `%playtimelevel_next_level_in_hours%`          | Hours remaining to next level              | `2.3`          |
| `%playtimelevel_next_level_in_hours_formatted%`| Formatted hours to next level              | `2.3 hours`    |
| `%playtimelevel_required_for_next_level%`      | Total hours required for next level        | `5.15`         |
| `%playtimelevel_required_for_next_level_formatted%` | Formatted version                     | `5.2 hours`    |
| `%playtimelevel_total_required_current%`       | Hours required to unlock current level     | `4.87`         |
| `%playtimelevel_total_current%`                | Cumulative hours needed for current level  | `116.6`        |
| `%playtimelevel_total_current_formatted%`      | Formatted version                          | `116.6 hours`  |
| `%playtimelevel_level_info%`                   | One-line level summary                     | `Level 25 (120.5/125.3h) - Need 2.3h to Level 26` |
| `%playtimelevel_max_level%`                    | Max level configured                       | `100`          |
| `%playtimelevel_is_max_level%`                 | Whether player is at max level             | `true / false` |
| `%playtimelevel_total_max_level%`              | Total hours to reach max level             | `1440.0`       |
| `%playtimelevel_total_max_level_formatted%`    | Formatted version                          | `1440.0 hours` |
| `%playtimelevel_reward_claimed_<level>%`       | Whether reward for a level is claimed      | `true / false` |

### Usage Examples

**Scoreboard line:**
```
Level: %playtimelevel_level_color_formatted%
Progress: %playtimelevel_progress_bar% %playtimelevel_progress_percent%%
Next in: %playtimelevel_next_level_in_hours%h
```

**Tab list prefix:**
```
%playtimelevel_level_color_formatted% %player_name%
```

**Check if a specific reward is claimed:**
```
%playtimelevel_reward_claimed_10%   →  true  (if level 10 reward was claimed)
```

**Custom colored text based on level:**
```
%playtimelevel_level_color%Level %playtimelevel_level% §r— %playtimelevel_playtime_hours%h played
```

---

## 11. Reward System

### How Rewards Work

1. Every `level-check-interval` seconds, the plugin checks if any online player has leveled up.
2. When a player **joins the server**, all unclaimed rewards for their current level are given automatically.
3. If a player leveled up and has an unclaimed reward, the reward is given **automatically** without any player action.
4. Players can also claim rewards manually using `/level claim <level>` or `/level claim all`.
5. Each reward can only be claimed **once per player**.
6. Reward commands are executed from **console** with `%player%` replaced by the player's name.

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

## 12. Auto-Claim Behaviour

Rewards are claimed automatically in three layers:

| Trigger       | When                                      | Behaviour                                                                 |
|---------------|-------------------------------------------|---------------------------------------------------------------------------|
| Scheduler     | Every `level-check-interval` seconds      | Detects level-up and immediately gives unclaimed rewards for new levels    |
| Player Join   | 1 second after joining the server         | Scans all eligible levels and gives any rewards not yet claimed            |
| Manual Claim  | On `/level claim` or GUI interaction      | Player claims a specific reward or all rewards on demand                   |

> **Why the 1-second delay on join?** EssentialsX playtime data may not be fully loaded the moment `PlayerJoinEvent` fires. The short delay ensures accurate playtime is available before level calculation runs.

---

## 13. Data Storage

Player data is stored in `plugins/PlaytimeLevel/data.yml`.

### Stored Per Player

| Field           | Type            | Description                                                              |
|-----------------|-----------------|--------------------------------------------------------------------------|
| `level`         | Integer         | Last known level — used to detect level-ups between scheduler checks     |
| `lastPlaytime`  | Long (seconds)  | Last recorded playtime in seconds                                        |
| `claimedRewards`| List\<Integer\> | List of level numbers whose rewards have been claimed                    |

### Auto-Save

Data is saved automatically every `data-save-interval` seconds (default: 5 minutes). Data is also saved immediately on level-up, reward claim, and when the plugin is disabled.

> **Warning:** Do not edit `data.yml` manually while the server is running. Changes will be overwritten by the next auto-save.

---

## 14. Playtime Source

The plugin uses a **two-tier fallback system** to retrieve playtime.

### Tier 1 — EssentialsX (Primary)

Hooks into EssentialsX via the `IEssentials` API and calls `user.getPlaytime()` using reflection. Returns playtime in milliseconds, converted to seconds internally. This is the recommended source because EssentialsX tracks playtime accurately across sessions, including AFK time handling if configured in EssentialsX.

### Tier 2 — Bukkit Statistics (Fallback)

Used automatically if:
- EssentialsX is not installed
- EssentialsX hook fails
- `use-essentials-playtime` is set to `false`

Reads `player.getStatistic(Statistic.PLAY_ONE_MINUTE)` which returns ticks, divided by 20 to get seconds. Note: this statistic may differ slightly from EssentialsX playtime.

### Which One Is Active?

Check your server console on startup — you will see one of:

```
[PlaytimeLevel] Successfully hooked into EssentialsX!
```
or
```
[PlaytimeLevel] EssentialsX not found! Using Bukkit statistics for playtime.
```

---

## 15. FAQ

**Q: A player's level is not updating.**  
A: The plugin checks every `level-check-interval` seconds (default: 60). Wait up to a minute after the player's playtime increases. If it still doesn't update, check that EssentialsX is running and the hook succeeded (see startup logs).

**Q: Rewards are not being given automatically on level-up.**  
A: Check that `rewards.enabled: true` in `config.yml`. Also verify the reward is defined for the exact level number, and that the player's level actually changed during the last check cycle. Check the console for any error messages containing the player's name.

**Q: The player joined but unclaimed rewards were not given.**  
A: Ensure the plugin is updated to the latest version with auto-claim-on-join support. Check the console for a line like: `Auto-claimed N unclaimed reward(s) for <player> on join.` If it is absent, verify the player's level is correctly calculated after login.

**Q: Can I change the total hours required to reach Level 100?**  
A: Yes, but it requires editing the `LEVEL_REQUIREMENTS` array in `LevelManager.java` and recompiling the plugin. The current values total ~1,440 hours (~6 months at 8 hours/day).

**Q: Can I add more than 100 levels?**  
A: Change `level-system.max-level` in `config.yml`. Levels beyond 100 will use a fallback of 20 hours per level since the requirements array only covers up to 100.

**Q: `/level reload` — does it reload `data.yml` too?**  
A: No. `/level reload` only reloads `config.yml` and `messages.yml`. Player data in `data.yml` is managed in memory and saved automatically.

**Q: Can I reset a player who is offline?**  
A: Yes. `/level reset <player>` works for both online and offline players as long as they have played on the server before.

**Q: Rewards are not being given automatically.**  
A: Check that `rewards.enabled: true` in `config.yml`. Also verify the reward is defined for the exact level number, and that the player's level actually increased during the last check cycle.

**Q: PlaceholderAPI placeholders are not working.**  
A: Make sure PlaceholderAPI is installed and enabled. Check the startup log for:  
`[PlaytimeLevel] PlaceholderAPI expansion registered successfully!`  
If it says "Failed to register", try running `/papi reload` then `/level reload`.

**Q: Can I use color codes in `messages.yml`?**  
A: Yes. Use `§` followed by a color code (e.g. `§a` for green, `§6` for gold, `§l` for bold). Standard Minecraft color codes are fully supported.

**Q: How do I change the level colors?**  
A: Edit the `level-colors` section in `config.yml`. Each entry defines a level range (`min`–`max`) and a Minecraft color code (`color`). Run `/level reload` after saving — no restart needed. Colors are automatically applied to `%playtimelevel_level_color%` and `%playtimelevel_level_colored%` placeholders.

**Q: The GUI next-page button only goes to page 2, not page 3+.**  
A: This was a bug in older versions where navigating between pages triggered `onInventoryClose`, which cleared the player's GUI state before the new page could open. This is fixed in the latest version.

**Q: Claim All in the GUI only works on page 1.**  
A: This was also a bug in older versions — `closeInventory()` was called before processing the claim, wiping the player's slot map. Fixed in the latest version alongside the pagination bug above.
