# emotive

Emotes using cobblemon NPCs

# config

"Animations" jsons in config/emotive/animations/<file.json>
"Categories with animation" jsons in config/emotive/categories/<file.json>
mod config: config/emotive/config.json

Options:
```json
{
  "debug": false,
  "command": "emotive",
  "show-player-name": true,
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
    "emotive.add": 2,
    "emotive.list": 2,
    "emotive.remove": 2,
    "emotive.reload": 2
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
    "confirmation-menu-height": 1,
    "confirmation-menu-title": "Confirm",
    "add-browse-button": true,
    "browse-button-item": "minecraft:arrow",
    "browse-button-location": {
      "x": 1,
      "y": 6
    }
  }
}
```

## Persistent storage
Either using luckperms or mongodb

