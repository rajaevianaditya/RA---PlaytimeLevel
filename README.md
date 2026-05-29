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
12. [Auto-Claim Behaviour](#auto-claim-behaviour)
13. [Data Storage](#data-storage)
14. [Playtime Source](#playtime-source)
15. [FAQ](#faq)

---

## Overview

PlaytimeLevel is a playtime-based leveling plugin for Paper/Spigot servers. Players earn levels automatically based on total time played — no grinding, no quests, just active server playtime.

Key features:

* Progressive level system (Level 1–100)
* Fully configurable rewards
* One-time claimable rewards
* Auto-claim on join
* Auto-claim on level-up
* Level color system
* PlaceholderAPI support
* EssentialsX integration
* Separate `messages.yml`
* Auto-save player data

---

## Requirements

| Dependency     | Type     | Version      |
| -------------- | -------- | ------------ |
| Paper / Spigot | Server   | 1.21.x       |
| Java           | Runtime  | 17 or higher |
| EssentialsX    | Required | 2.x          |
| PlaceholderAPI | Optional | 2.11+        |

---

## Installation

1. Download `PlaytimeLevel-1.6.jar`
2. Put it inside your server `plugins/` folder
3. Ensure EssentialsX is installed
4. (Optional) Install PlaceholderAPI
5. Start or restart the server
6. Plugin automatically generates:

   * `config.yml`
   * `messages.yml`
   * `data.yml`
7. Run `/level reload` after editing configuration

> No restart required after editing configuration files.

---

## File Structure

```txt
plugins/
└── PlaytimeLevel/
    ├── config.yml
    ├── messages.yml
    └── data.yml
```

### Source Structure

```txt
src/main/
├── java/id/rajaaditya/playtimelevel/
│   ├── PlaytimeLevel.java
│   ├── commands/
│   ├── gui/
│   ├── managers/
│   ├── placeholders/
│   └── utils/
└── resources/
```

---

## Level System

### How It Works

Levels are calculated automatically based on total player playtime.

The plugin scheduler runs every configurable interval and recalculates online player levels automatically.

### Progressive Requirements

| Level Range | Hours Per Level | Cumulative Total |
| ----------- | --------------- | ---------------- |
| 2–11        | 3.02 – 3.46 h   | ~29 h            |
| 12–21       | 3.52 – 4.36 h   | ~67 h            |
| 22–31       | 4.48 – 5.81 h   | ~117 h           |
| 32–41       | 5.99 – 7.92 h   | ~184 h           |
| 42–51       | 8.18 – 10.86 h  | ~275 h           |
| 52–61       | 11.20 – 14.78 h | ~401 h           |
| 62–71       | 15.22 – 19.71 h | ~570 h           |
| 72–81       | 20.25 – 25.64 h | ~793 h           |
| 82–91       | 26.28 – 32.57 h | ~1079 h          |
| 92–100      | 33.32 – 39.67 h | ~1440 h          |

### Milestones (8h/day)

| Level | Total Hours | Estimated Time |
| ----- | ----------- | -------------- |
| 10    | ~29 h       | ~4 days        |
| 20    | ~67 h       | ~8 days        |
| 30    | ~117 h      | ~15 days       |
| 50    | ~275 h      | ~34 days       |
| 70    | ~570 h      | ~71 days       |
| 90    | ~1079 h     | ~135 days      |
| 100   | ~1440 h     | ~6 months      |

### Max Level

```yaml
level-system:
  max-level: 100
```

Once max level is reached, progress becomes 100%.

---

## Commands

Main command: `/level`

Aliases:

```txt
/lvl
/playtimelevel
```

### Player Commands

| Command                | Description                | Permission             |
| ---------------------- | -------------------------- | ---------------------- |
| `/level`               | View stats                 | `playtimelevel.use`    |
| `/level info`          | Detailed level progression | `playtimelevel.use`    |
| `/level rewards`       | View rewards               | `playtimelevel.use`    |
| `/level claim <level>` | Claim reward               | `playtimelevel.reward` |
| `/level claim all`     | Claim all rewards          | `playtimelevel.reward` |

### Admin Commands

| Command                 | Description        | Permission                   |
| ----------------------- | ------------------ | ---------------------------- |
| `/level check <player>` | Check player stats | `playtimelevel.check.others` |
| `/level reset <player>` | Reset reward data  | `playtimelevel.reset`        |
| `/level reload`         | Reload config      | `playtimelevel.reload`       |

### Command Output Example

```txt
=== Level Stats ===
Level: [ 25 ] |
Playtime: 120.5 hours
Progress: 67.3%
▊▊▊▊▊▊▊▊▊▊
To Level 26: 2.3 hours
Required for Level 26: 5.15 hours
```

---

## Permissions

| Permission                   | Description         | Default     |
| ---------------------------- | ------------------- | ----------- |
| `playtimelevel.use`          | Use `/level`        | All players |
| `playtimelevel.reward`       | Claim rewards       | All players |
| `playtimelevel.check.others` | Check other players | OP          |
| `playtimelevel.reload`       | Reload plugin       | OP          |
| `playtimelevel.reset`        | Reset player data   | OP          |
| `playtimelevel.*`            | All permissions     | OP          |

---

## Configuration — config.yml

### settings

```yaml
settings:
  data-save-interval: 300
  use-essentials-playtime: true
  level-check-interval: 60
```

### rewards example

```yaml
rewards:
  levels:
    25:
      commands:
        - "eco give %player% 25000"
        - "give %player% diamond 10"
      message: "§d§lLEVEL 25 MILESTONE REWARD!"
      broadcast: true
      broadcast-message: "§e%player% §7just hit §dLevel 25§7!"
```

### level-colors

```yaml
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
```

---

## Messages — messages.yml

All player-facing messages are editable from `messages.yml`.

```yaml
no-permission: "§cYou don't have permission!"
reload-success: "§aConfiguration reloaded successfully!"
reward-received: "§aReward received!"
```

---

## PlaceholderAPI

Requires PlaceholderAPI.

Prefix:

```txt
%playtimelevel_
```

### Example Placeholders

| Placeholder                        | Example |
| ---------------------------------- | ------- |
| `%playtimelevel_level%`            | `25`    |
| `%playtimelevel_progress_percent%` | `67.3`  |
| `%playtimelevel_next_level%`       | `26`    |
| `%playtimelevel_level_color%`      | `§6`    |

### Usage Examples

#### Scoreboard

```txt
Level: %playtimelevel_level_formatted%
Progress: %playtimelevel_progress_bar%
```

#### Tablist

```txt
%playtimelevel_level_color_formatted% %player_name%
```

---

## Reward System

### How Rewards Work

1. Plugin checks player level periodically
2. Rewards are given automatically on level-up
3. Players can also claim manually
4. Rewards are one-time only
5. Commands execute from console

### Reward Security

The plugin verifies:

* Reward system enabled
* Reward exists
* Player reached required level
* Reward not already claimed

---

## Auto-Claim Behaviour

| Trigger      | Behaviour             |
| ------------ | --------------------- |
| Scheduler    | Detects level-up      |
| Player Join  | Gives pending rewards |
| Manual Claim | `/level claim`        |

---

## Data Storage

Player data is stored in:

```txt
plugins/PlaytimeLevel/data.yml
```

### Stored Data

* `level`
* `lastPlaytime`
* `claimedRewards`

### Auto Save

Data is automatically saved every configured interval.

> Do not edit `data.yml` while server is running.

---

## Playtime Source

### Tier 1 — EssentialsX

Primary playtime source using `user.getPlaytime()`.

### Tier 2 — Bukkit Statistics

Fallback if EssentialsX unavailable.

### Startup Example

```txt
[PlaytimeLevel] Successfully hooked into EssentialsX!
```

or

```txt
[PlaytimeLevel] EssentialsX not found! Using Bukkit statistics for playtime.
```

---

## FAQ

### Level not updating

Wait until scheduler interval completes.

### Rewards not working

Check:

```yaml
rewards.enabled: true
```

### PlaceholderAPI not working

Ensure PlaceholderAPI is installed correctly.

### Can I add more than 100 levels?

Yes, by changing:

```yaml
level-system:
  max-level: 200
```

### How to change level colors?

Edit `level-colors` inside `config.yml` then run:

```txt
/level reload
```

---

# PlaytimeLevel v1.6

Documentation by RajaAditya
