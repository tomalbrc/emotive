# Emotive

A Minecraft mod that adds emotes using Cobblemon NPCs.

## Quick Start

1. Install Cobblemon and Emotive in your `mods` folder
2. Use `/emotive` to open the emote selection menu
3. Click on emotes to play them or browse all available emotes

## Configuration

### Main Config

**Location:** `config/emotive/config.json`

```json
{
  "debug": false,
  "command": "emotive",
  "show-player-name": true,
  "storage-type": "MARIADB",
  "database": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "secret",
    "database": "emotes_db",
    "max-pool-size": 10,
    "ssl-enabled": false
  },
  "mongo-db-collection": "emotes",
  "messages": {
    "player-not-found": "Player not found",
    "back": "Back",
    "until-stopped": "Until stopped",
    "browse": "Browse All Emotes",
    "no-permission": "You do not have that emote.",
    "unknown-emote": "Unknown emote: %s",
    "config-reloaded": "Emotive config reloaded!",
    "only-player": "This command can only be run by a player!",
    "emote-duration-tooltip": "<gold>⌚</gold> Duration: %s",
    "emote-play-tooltip": "<green>▶</green> Press <keybind:key.attack> to play",
    "prev": "Previous Page",
    "next": "Next Page"
  },
  "permissions": {
    "emotive.command": 2,
    "emotive.give": 2,
    "emotive.list": 2,
    "emotive.remove": 2,
    "emotive.reload": 2,
    "emotive.direct": 2
  },
  "gui": {
    "add-back-button": true,
    "back-button-item": "minecraft:arrow",
    "back-button-location": {
      "x": 1,
      "y": 1
    },
    "selection-menu-height": 6,
    "selection-menu-title": "Select Emote",
    "prev-button-item": "minecraft:arrow",
    "next-button-item": "minecraft:arrow",
    "prev-button-location": {
      "x": 8,
      "y": 6
    },
    "next-button-location": {
      "x": 9,
      "y": 6
    },
    "browse-menu-height": 6,
    "browse-menu-title": "Browse Emotes",
    "enable-confirmation-menu": true,
    "confirmation-menu-title": "Confirm",
    "add-browse-button": true,
    "browse-button-item": "minecraft:book",
    "browse-button-location": {
      "x": 1,
      "y": 6
    }
  }
}
```

### Config Options

**General:**

* `debug`: Enable debug logging
* `command`: Main command name
* `show-player-name`: Show player names during emotes

**Storage:**

* `storage-type`: Can be `LPMETA`, `MONGODB`, `MARIADB`, `POSTGRESQL`, or `SQLITE`
* `database`: Defines connection details for SQL databases
* `mongo-db-collection`: MongoDB collection name for emote data

**GUI:**

* Customize button positions, items, and menu layouts
* Adjust menu heights and titles
* Toggle confirmation menus

## Creating Emotes

### Animation Files

**Location:** `config/emotive/animations/`

JSON files defining emote animations:

```json
{
  "animations": {
    "cobblemon:recall": {
      "title": "<rainbow>Recall",
      "npc-class": "cobblemon:standard",
      "animation-name": "recall",
      "duration": 5,
      "item": "minecraft:emerald_block",
      "glint": true,
      "permission": "high.rank.perm"
    },
    "cobblemon:punch_left": {
      "title": "<rainbow>Punch Left",
      "npc-class": "cobblemon:standard",
      "animation-name": "punch_left",
      "duration": 5,
      "item": "minecraft:charcoal",
      "glint": false
    }
  }
}
```

The "permission" and "permission-level" fields are used for the component-based emote token items

### Category Files

**Location:** `config/emotive/categories/`

Organize emotes into categories:

```json
{
  "id": "example_category",
  "title": "<green>Example Category</green>",
  "item": "minecraft:knowledge_book",
  "lore": ["Test"],
  "glint": false,
  "animations": {
    "cobblemon:win": {
      "title": "<rainbow>Win",
      "npc-class": "cobblemon:standard",
      "animation-name": "win",
      "duration": 5,
      "item": "minecraft:charcoal",
      "glint": false,
      "permission-level": 0
    },
    "cobblemon:lose": {
      "title": "<rainbow>Lose",
      "npc-class": "cobblemon:standard",
      "animation-name": "lose",
      "duration": 5,
      "item": "minecraft:charcoal",
      "glint": false,
      "permission-level": 0
    }
  }
}
```

## Commands

* `/emotive` - Open emote menu
* `/emotive reload` - Reload config
* `/emotive list <player>` - List emotes
* `/emotive give <player> <emote>` - Give emote to player
* `/emotive give <player> *` - Give all emotes to player
* `/emotive remove <player> <emote>` - Remove emote from player
* `/emotive remove <player> *` - Remove all emotes from player

## Permissions

* `emotive.command (2)` - Menu access
* `emotive.give (2)` - Give emotes
* `emotive.list (2)` - List emotes
* `emotive.remove (2)` - Remove emotes
* `emotive.reload (2)` - Reload config
* `emotive.direct (2)` - Directly play an emote without gui

## Data Storage

* **LuckPerms (default)** - Uses LuckPerms meta values
* **MongoDB** - Alternative database storage
* **MariaDB / MySQL** - SQL database storage
* **PostgreSQL** - Advanced SQL database storage
* **SQLite** - Local file storage

## File Structure

```
config/
└── emotive/
    ├── config.json
    ├── animations/
    │   └── [emote-name].json
    └── categories/
        └── [category-name].json
```

