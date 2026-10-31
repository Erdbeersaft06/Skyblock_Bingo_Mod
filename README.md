# Skyblock Bingo

A client-side Fabric mod for **Minecraft 26.2** that adds a customizable Bingo game with an in-game HUD, built for Hypixel SkyBlock but usable with any goals on any server.

Create your own goals, generate a board from 3×3 to 7×7, race a friend in real time and mark fields as you complete them. The first player with a full row, column or diagonal wins.

> **Disclaimer:** This mod is not affiliated with, endorsed by, or connected to Hypixel Inc. or Mojang. It does not read any game data and does not interact with servers in any way. See [How it works](#how-it-works).

## Features

- **Custom goals**: write your own, import 30 built-in examples, or save and load multiple goal lists.
- **Flexible boards**: 3×3 up to 7×7, a random board is drawn from your goals each round.
- **Win detection**: rows, columns and both diagonals count. A win message appears instantly.
- **Lockout mode** (optional): a field claimed by one player is blocked for the other.
- **Timer** that counts up from the start of the round.
- **Multiplayer for 1–2 players** via a 6-character party code, or browse **public lobbies** and search for a host by name.
- **HUD overlay** that you can move by dragging and resize with the mouse wheel (similar to the editors of SkyHanni and other HUD mods).
- **Click to mark inside the inventory**: open your inventory (`E`), click a field on the HUD board to mark it.
- **Full in-game config menu**: everything can be set up without commands.
- **Themes**: light, dark, or fully custom colors with a built-in color picker.
- **Languages**: English, German, French and Spanish.
- Your in-game name (IGN) is used as your player name automatically.

## Requirements

| Requirement | Version |
|---|---|
| Minecraft | 26.2 |
| [Fabric Loader](https://fabricmc.net/use/installer/) | 0.19.3 or newer |
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.152.2+26.2 or newer |
| Java | 25 |

The mod is client-side only. It is not needed on the server.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Download the latest `.jar` file (see [Releases](../../releases)) and put it into your `mods` folder together with Fabric API.
3. Start the game.

## Quick start

1. Type `/sbb` to open the menu.
2. **Goals** tab: add your goals (or click the example button to load 30 sample goals).
3. **General** tab: choose the board size and Lockout on or off.
4. **Game** tab:
   - **Solo**: play alone, or
   - **Create party**: you get a 6-character code to give to a friend, or turn on **Public** first so others can find your party in the lobby list, or
   - **Join**: enter a friend's code or click **Find lobbies** to search by host name.
5. The host presses **Start round**. The board appears on your HUD and the timer starts.
6. Mark a field when you have completed it:
   - Open your inventory and click the field on the HUD board, or
   - Open the board with `/sbb board` and click the field, or
   - Use `/sbb mark <number>` (the number is shown in the top-left corner of every field).
7. Move and resize the board any time with `/sbb gui` (drag with the mouse, use the mouse wheel to resize).

## Commands

Everything is also available in the menu. All commands are under `/sbb`. (`/bingo` is intentionally not used, because Hypixel has its own command with that name.)

| Command | Description |
|---|---|
| `/sbb` | Open the config menu |
| `/sbb board` | Open the board window (click a field to mark it) |
| `/sbb gui` | Move (drag) and scale (mouse wheel) the HUD board |
| `/sbb goal add <text>` | Add a goal (separate several with `;`) |
| `/sbb goal list [page]` / `remove <nr>` / `clear` / `example` | Manage goals; `example` loads 30 samples |
| `/sbb list save <name>` / `load <name>` / `delete <name>` / `show` | Save and load goal lists |
| `/sbb size <3-7>` | Board size |
| `/sbb lockout <true/false>` | Lockout on or off |
| `/sbb solo` | Play alone |
| `/sbb host` | Create a party and show its code |
| `/sbb public <true/false>` | List your party in the public lobbies |
| `/sbb lobbies` | Open the public lobby browser |
| `/sbb join <code>` | Join a party |
| `/sbb start` | Start a round (host or solo) |
| `/sbb mark <nr>` | Mark or unmark a field |
| `/sbb end` / `leave` | End the round / leave the party |
| `/sbb status` | Show the current state |
| `/sbb theme dark/light/custom` | Change the theme |
| `/sbb color <bg/cell/text/muted/p0/p1/border> <RRGGBB>` | Set a custom color |
| `/sbb hud show/hide` | Show or hide the HUD |
| `/sbb lang <de/en/fr/es>` | Change the language |
| `/sbb help` | List all commands |

## How it works

The mod is a standalone game that runs next to Minecraft:

- It **does not read anything from the game**, the server or the Hypixel API. Progress is not detected automatically. You mark fields yourself, so the game relies on honesty between players (like a real-life bingo).
- It does not modify gameplay, send anything to a Minecraft server, or give any in-game advantage.

### Multiplayer

Multiplayer messages are sent through the free public service [ntfy.sh](https://ntfy.sh). No server of your own is needed, but an internet connection is.

- The party code is the name of the channel. Anyone who knows the code can join, so only share it with the people you want to play with.
- Public lobbies use one shared channel. Your host name (your IGN) and the board settings (size, Lockout) are visible to everyone while your lobby is listed. A lobby is removed from the list as soon as a second player joins, you leave, or you stop hosting.
- The public list is not verified: anyone could technically post fake entries.
- Game messages contain goal texts (limited to 50 characters each), marks and your IGN.

## Configuration

Settings and goals are saved automatically to `config/skyblockbingo.json` in your Minecraft folder. You can back it up or share it. Deleting the file resets the mod to defaults.

## Building from source

Requirements: Java 25 and Gradle 9.5.1.

```
gradle build
```

The jar is created in `build/libs/`.

You can also let GitHub build it for you: the included workflow (`.github/workflows/build.yml`) runs on every push. Open the **Actions** tab, select the latest run and download the `skyblock-bingo-jar` artifact.

## Troubleshooting

- **The mod does not show up / the game crashes on start:** check that you use Minecraft 26.2, Fabric Loader 0.19.3+, Java 25 and Fabric API in the `mods` folder.
- **`/bingo` says "There is no current Bingo active":** that is Hypixel's own command. Use `/sbb` instead.
- **Joining a party fails:** the code must be exactly 6 characters, and the host must have created the party first. Both players need an internet connection.
- **You can't mark fields by clicking in the inventory:** make sure the option is enabled in the General tab, a round is running, and the board does not sit behind inventory slots (move it with `/sbb gui`).

## Contributing

Issues and pull requests are welcome. If you report a bug, please include your Minecraft, Fabric Loader and mod versions, and the relevant part of `latest.log` or the crash report.

## License

See the [LICENSE](LICENSE) file.
