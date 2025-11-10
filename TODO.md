## Heavy Storms – Stopping Point (2025-11-03)

Latest status:
- Forge 1.20.1 project uses the bundled Gradle wrapper. `./gradlew --no-daemon build` succeeds.
- Lightning Capacitor is a block with block entity + FE capability. Charges only when a lightning rod sits on top and a bolt hits. Mekanism cables can interact.
- Item form is a plain block item; no inventory charging anymore.
- Basic blockstate/model/loot/lang in `assets/heavy_storms/*`. Placeholder visuals still use vanilla copper textures.
- README updated to explain new block behavior.

Open follow-ups:
1. Create proper block/item textures + update models (currently reusing cut copper).
2. Optional: add GUI/HUD feedback (Stored FE bar, block renderer with emissive layer).
3. Consider data generators for loot tables & lang if workflows require.
4. Test in-game with `./gradlew runClient` to confirm lightning + Mekanism integration.

Environment reminders:
- Use wrapper: `gradlew.bat --no-daemon build`.
- Config file generated at runtime: `heavy_storms-server.toml`.

Next session can start with in-game testing or art pass. Good night! :crescent_moon:
