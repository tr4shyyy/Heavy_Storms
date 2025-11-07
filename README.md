![Heavy Storms Preview](https://github.com/tr4shyyy/Heavy_Storms/blob/master/Heavy_storms_1.gif?raw=true)

# Heavy Storms (NeoForge 1.21.1)

Heavy Storms amplifies thunderstorms and introduces a Forge Energy compatible Lightning Capacitor block for Minecraft 1.21.1 using NeoForge.

## Features

- Spawns additional lightning strikes around players during thunderstorms with configurable frequency and radius.
- Adds the Lightning Capacitor block that only charges when placed with a lightning rod mounted on top, gaining FE from real lightning strikes.
- Exposes Forge Energy via the modern NeoForge capability system so automation mods (e.g., Mekanism universal cables) can interact with the capacitor.

## Building & Development

Prerequisites:

- JDK 21 (matching Mojang's requirement for 1.21+)
- Gradle is bundled via the wrapper; no local installation needed.

Common commands run from the project root:

```bash
./gradlew genIntelliJRuns   # or genEclipseRuns / genVSCodeRuns as needed
./gradlew runClient         # launches the game with the mod loaded
./gradlew build             # builds the mod jar under build/libs
```

The default run configurations download NeoForge `21.1.210` automatically on first launch.

## Configuration

A server-side config file `heavy_storms-server.toml` (created after the first run) exposes the following options:

| Key | Description | Default |
| --- | --- | --- |
| `lightning.extraAttemptsPerTick` | Extra lightning attempts each tick during thunderstorms | `3` |
| `lightning.extraStrikeChance` | Per-attempt spawn chance (0.0 - 1.0) | `0.35` |
| `lightning.extraStrikeRadius` | Max horizontal distance from players (blocks) | `64` |
| `capacitor.capacity` | Total FE storage of the Lightning Capacitor | `1_000_000` |
| `capacitor.maxReceive` | Max FE accepted per transfer | `20_000` |
| `capacitor.maxExtract` | Max FE output per transfer | `20_000` |
| `capacitor.chargePerStrike` | FE gained per lightning hit | `500_000` |

Adjust the values and restart the world/server to apply changes where required.

## Source Layout

```
src/main/java/com/nolyn/heavystorms/HeavyStorms.java      # Mod entry point
src/main/java/com/nolyn/heavystorms/config/...           # Config spec
src/main/java/com/nolyn/heavystorms/block/...            # Block registrations and definitions
src/main/java/com/nolyn/heavystorms/blockentity/...       # Block entity implementations
src/main/java/com/nolyn/heavystorms/item/...             # Block item registration
src/main/java/com/nolyn/heavystorms/world/...            # Event handlers (lightning control, charging)
src/main/resources/META-INF/neoforge.mods.toml           # Mod metadata
```

## License

MIT (see `license` entry in `neoforge.mods.toml`). Update this section if you choose a different license.
