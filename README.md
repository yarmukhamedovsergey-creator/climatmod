# Climate Zones

A Fabric 1.21.1 mod that adds operator-defined coordinate climate zones with temperature simulation, survival mechanics, HUD, particles, fog, and ambient sounds.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.9+
- Fabric API
- Java 21

## Build locally

Requires **Java 21**.

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The built jar is in `build/libs/`.

## Build with GitHub Actions (no local Java)

1. Create a repository on GitHub.
2. Upload this project (see guide below).
3. Open **Actions** → workflow **Build Mod** → wait for green checkmark.
4. Open the completed run → download artifact **climatezones-mod**.
5. Use the `.jar` from the zip in your `mods` folder.

## Installation

Install on both server and all clients for full visuals and gameplay.

## Commands (OP level 2)

| Command | Description |
|---------|-------------|
| `/climate pos1` | Set first corner at your position |
| `/climate pos2` | Set second corner at your position |
| `/climate create <cold\|hot> <name>` | Create a zone from selection |
| `/climate save` | Save zones to disk |
| `/climate delete <name>` | Delete a zone |
| `/climate edit <name>` | Enter edit mode for a zone |
| `/climate list` | List all zones |
| `/climate info` | Show selection and current temperature |
| `/climate reload` | Reload config and zones |

## Config

`config/climatezones/config.json` — temperatures, damage, HUD, particles, fog, sounds, transition distance.

`config/climatezones/zones.json` — persisted zone definitions (auto-saved).

## Architecture

- `ClimateType` enum — extensible climate types (Cold, Hot)
- Server-authoritative zone and temperature data
- Client-side interpolation for smooth visual transitions
- Spatial range checks for performance

## License

MIT
