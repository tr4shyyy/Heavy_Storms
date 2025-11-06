# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/nolyn/heavystorms/...` holds mod entry, block, block-entity, and capability code; keep new systems under coherent subpackages.
- `src/main/resources` contains `META-INF/neoforge.mods.toml` and assets. Use `assets/heavy_storms/{blockstates,models,lang}` for data-driven content; register textures under `textures/block` and `textures/item`.
- `runs/` stores generated dev configs; treat them as disposable and avoid committing local changes.
- `TODO.md` tracks session checkpoints; update it when priorities shift between art, code, and balance.

## Build, Test, and Development Commands
- `.\gradlew.bat --no-daemon build` compiles, runs NeoForge processors, and produces jars in `build/libs`.
- `.\gradlew.bat --no-daemon runClient` launches the NeoForge dev client for in-game verification; the first run downloads dependencies.
- `.\gradlew.bat --no-daemon genIntelliJRuns` regenerates IDE launch configs after mapping or run-config changes.
- Add `clean` (`.\gradlew.bat --no-daemon clean build`) when Gradle cache issues surface.

## Coding Style & Naming Conventions
- Target Java 21 with 4-space indentation and same-line braces; mark classes `final` when not intended for extension.
- Package names stay lowercase; registry holders follow the plural pattern (`HeavyStormsBlocks`, `HeavyStormsItems`).
- Asset JSON names mirror registry IDs (`lightning_capacitor.json`), and textures remain snake_case PNGs.
- Keep vanilla block and item models under `assets/heavy_storms/models/`; add specialized asset folders only when a feature demands them.

## Testing Guidelines
- No automated tests yet; validate features by running `.\gradlew.bat --no-daemon runClient` and reproducing lightning scenarios.
- When touching FE integrations, verify Mekanism cable interactions and capacitor charge/extract loops.
- Log debug info through the NeoForge logger, but strip noisy entries before committing.

## Commit & Pull Request Guidelines
- Prefer Conventional Commit prefixes (`feat:`, `fix:`, `docs:`) to summarize intent; e.g., `feat: add emissive capacitor model`.
- Reference tracking issues in the body and note manual test coverage (`Tested: runClient, thunderstorm simulation`).
- Include asset screenshots or short clips when changing visuals; attach updated config snippets if defaults move.

## Assets & Configuration Tips
- Generated `heavy_storms-server.toml` lives in the run directory; delete it before shipping new defaults.
- Store large reference textures outside the repo; commit only optimized PNGs.
