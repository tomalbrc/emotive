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
  "fancy-hud": true,
  "command": "emotive",
  "show-player-name": true,
  "messages": {
    "player-not-found": "Player not found",
    "until-stopped": "Until stopped",
    "no-permission": "You do not have that emote.",
    "unknown-emote": "Unknown emote: %s",
    "config-reloaded": "Emotive config reloaded!",
    "only-player": "This command can only be run by a player!",
    "component-tooltip": "Emote: %s",
    "added": "%s was added to your emotes!"
  },
  "permissions": {
    "emotive.direct": 2,
    "emotive.command": 2,
    "emotive.list": 2,
    "emotive.remove": 2,
    "emotive.reload": 2,
    "emotive.give": 2
  },
  "selection-gui": {
    "title": "Emote Selectionxxx",
    "layout": [
      "         ",
      "BEEEEEEE ",
      "PEEEEEEEN",
      " EEEEEEE ",
      "         ",
      " FFFFFFF "
    ],
    "keys": {
      "N": {
        "type": "next_page",
        "title": "Next Page",
        "item": {
          "id": "minecraft:arrow",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "B": {
        "type": "browse",
        "title": "Browse all Emotes",
        "item": {
          "id": "minecraft:chest",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      " ": {
        "type": "empty",
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "P": {
        "type": "prev_page",
        "title": "Previous Page",
        "item": {
          "id": "minecraft:arrow",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "E": {
        "type": "emotes",
        "item": {
          "id": "minecraft:emerald",
          "count": 1
        },
        "lore": [
          "",
          "<gold>⌚</gold> Duration: <duration>",
          "<green>▶</green> Press <keybind:key.attack> to play",
          " ",
          "<color:#800080>↔</color> Press <keybind:key.use> to get as item",
          "",
          "<color:#800080>↔</color> Press <keybind:key.sneak> + <keybind:key.attack> to add to favourites"
        ],
        "alt-lore": [],
        "glint": false
      },
      "F": {
        "type": "favourites",
        "item": {
          "id": "minecraft:diamond",
          "count": 1
        },
        "lore": [
          "<gold>⌚</gold> Duration: <duration>",
          "<green>▶</green> Press <keybind:key.attack> to play",
          "<color:#800080>↔</color> Press <keybind:key.sneak> + <keybind:key.attack> to remove from favourites"
        ],
        "alt-lore": [],
        "glint": false
      }
    },
    "manipulate-player-slots": false
  },
  "browse-gui": {
    "title": "Browse Emotes",
    "layout": [
      "B        ",
      " EEEEEEE ",
      "PEEEEEEEN",
      " EEEEEEE ",
      " EEEEEEE ",
      "         "
    ],
    "keys": {
      " ": {
        "type": "empty",
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "B": {
        "type": "back",
        "title": "Back",
        "item": {
          "id": "minecraft:arrow",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "N": {
        "type": "next_page",
        "title": "Next Page",
        "item": {
          "id": "minecraft:arrow",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "E": {
        "type": "emotes",
        "item": {
          "id": "minecraft:emerald",
          "count": 1
        },
        "lore": [
          "",
          "<gold>You do not own this emote!"
        ],
        "alt-lore": [
          "",
          "<gold>⌚</gold> Duration: <duration>",
          "<green>You own this emote!"
        ],
        "glint": false
      },
      "P": {
        "type": "prev_page",
        "title": "Previous Page",
        "item": {
          "id": "minecraft:arrow",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      }
    },
    "manipulate-player-slots": false
  },
  "confirmation-gui": {
    "title": "Confirm",
    "layout": [
      " C     A "
    ],
    "keys": {
      "C": {
        "type": "cancel",
        "title": "Cancel",
        "item": {
          "id": "minecraft:red_concrete",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      " ": {
        "type": "empty",
        "lore": [],
        "alt-lore": [],
        "glint": false
      },
      "A": {
        "type": "confirm",
        "title": "Confirm",
        "item": {
          "id": "minecraft:lime_concrete",
          "count": 1
        },
        "lore": [],
        "alt-lore": [],
        "glint": false
      }
    },
    "manipulate-player-slots": false
  },
  "storage-type": "SQLITE",
  "database": {
    "host": "localhost",
    "port": 3306,
    "user": "username",
    "password": "secret",
    "max-pool-size": 10,
    "ssl-enabled": false,
    "database-name": "cosmetic",
    "connection-timeout": 30000,
    "idle-timeout": 600000,
    "keepalive-time": 300000,
    "validation-timeout": 5000,
    "use-srv": false
  },
  "mongo-db-collection": "emotes",
  "mongo-db-collection-favourites": "emotes_favourites"
}
```

### Config Options

**General:**

* `debug`: Enable debug logging
* `command`: Main command name
* `show-player-name`: Show player names during emotes

**Storage:**

* `storage-type`: Can be `LPMETA`, `MONGODB`, `MARIADB`, `POSTGRESQL`, or `SQLITE`
* `database`: Defines connection details for databases
* `mongo-db-collection`: MongoDB collection name for emote data
* `mongo-db-collection-favourites`: Collection for the fav selection

**GUI:**

`confirmation-gui`, `selection-gui`, `browse-gui`:

* Customize button positions, items, and menu layouts
* Adjust menu heights and titles
* Toggle confirmation menus

Available key types: 

- `empty`
- `emotes`
- `next_page`: Next page
- `prev_page`: Previous page
- `back`: Back button, returns to previous menu
- `favourites`: a favourite

**Permissions:**

Allows to assign vanilla permission levels for the luckperm nodes.

**Messages:**

Player facing messages / strings


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

## Fancy HUD:

- The fancy hud (triggered by Shift+F) requires bbmodels with the animations specified in your config, as well as 256x256 png renders named after the animation name ("wave.png").
- The files will be loaded from `config/emotive/models/`
    - `config/emotive/models/wave.png`
    - `config/emotive/models/mymodel.bbmodel` <- contains an animation named "wave"


BbModel, AjModel, AjBlueprint files are supported.

The images will be copied into the resourcepack and a font will be generated in `assets/emotive/font/emotes.json`

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

