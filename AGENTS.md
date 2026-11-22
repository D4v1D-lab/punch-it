# AGENTS.md

Punch It is a **Windows-only** libGDX desktop game (package `com.mygdx.game`). The README is in Spanish and mostly documents the predecessor **CumbiaBall** plus historical notes; treat `build.gradle`, `settings.gradle`, `desktop/build.gradle`, `scripts\run.bat`, and `core/src/` as the source of truth.

## Run / build

- Preferred: `scripts\run.bat` from PowerShell (auto-detects JDK 8–15; JDK 11 Temurin). It launches fullscreen; close with `Alt+F4`.
- Compile-only check: `.\gradlew.bat :desktop:compileJava`.
- There are **no tests, lint, or CI**. Verification = compile + launch the game.
- **Stop a running game before any Gradle command.** `desktop:run` keeps the Gradle daemon busy and the next Gradle invocation can hang:
  ```powershell
  Get-CimInstance Win32_Process | Where-Object { $_.Name -eq "java.exe" -and $_.CommandLine -like "*DesktopLauncher*" } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
  ```
- From WSL the Linux `./gradlew` does not work (no JDK in WSL). Call `gradlew.bat` through `powershell.exe`/`cmd.exe` with a Windows `JAVA_HOME` pointing at the Temurin 11 install (`C:\Program Files\Eclipse Adoptium\jdk-11.x.x-hotspot`).
- Versions are pinned intentionally: Gradle **6.7.1** wrapper, libGDX **1.10.0**, JDK **8–15** (11). Do not upgrade; `build.gradle` throws on Java >15. If the wrapper was accidentally upgraded (Gradle 9 fails with “requires JVM 17”), run `git restore gradle/wrapper/gradle-wrapper.properties`.
- `core`/`desktop` use `sourceCompatibility = 1.7`: keep code Java 7 compatible (no lambdas, etc.).
- Only `:core` and `:desktop` exist; Android/other OS targets were removed. Do not re-add them.

## Sub-agents

- Use the `Task` tool to delegate. Use `explore` for read-only discovery, asset inspection, and independent review; use `general` for a narrowly scoped implementation or a second opinion.
- Parallelize only independent work. Safe in parallel: asset audits, code searches, research, and read-only reviews. Unsafe: two agents editing the same file, or one editing while another runs Gradle.
- Every sub-agent starts with a fresh context. Write a self-contained brief with the objective, exact file paths, already-agreed decisions/constants, whether it may edit, and the exact files it may touch. Never assume it can see this conversation.
- Include the project constraints in implementation briefs: Java 7 only, Windows/libGDX stack, asset keys use the folder prefix (`cars/`, `common/`, `plane/`, `boat/`), no comments, and no Gradle while a game is running.
- Assign one owner per file. A reliable split is: the main agent owns shared wiring (`PunchItGame`, `VehicleSelectScreen`, `GiftScreen`); one sub-agent owns a single new screen/file; another sub-agent reviews it.
- Do not ask sub-agents to commit, push, clean, reset, or run destructive git commands. Final integration, compile, and smoke tests remain with the main agent.
- Require a short report: files touched, key decisions, assumptions, checks performed, and remaining risks. Review every edit with `git diff` and a compile before continuing.
- Example for a new vehicle/mode: launch 3–4 read-only agents in parallel (asset audit with PIL/ffprobe; gameplay plan from `GameScreen`; selection/gift/wiring plan; verification/docs plan), synthesize their findings, implement shared files in the main agent, delegate the new screen file to one `general` agent, then compile and smoke-test.

## Assets

- Runtime assets live in **`/assets`** (not `android/assets`). `desktop/build.gradle` sets `workingDir` to `../assets`. Layout: `assets/cars/` car-only, `assets/common/` shared/menu, `assets/plane/` plane-only, `assets/boat/` boat-only. Always use the matching prefix in `AssetManager` keys.
- libGDX cannot load GIFs. The game uses pre-generated sheets:
  - `common/load_gift_sheet.png`: 75 frames, 8×10 grid, 480×270, 1/15 s; shared by all `GiftScreen` instances.
  - `cars/car_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`GameScreen`).
  - `plane/avion_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`AirplaneScreen`).
  - `boat/bote_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`BoatScreen`).
  - The car and plane source GIFs are not in the repo. `assets/boat/bote_ending.gif` (480×852) is the source for the generated boat ending sheet; regenerate the sheet with the same grid/frame sizes and update the matching constants if the art changes.
- Cropped sprite regions: car `cars/carro.png` = `(190,134,120,227)`; cone `cars/cono.png` = `(192,169,116,162)`; plane `plane/avion.png` = `(256,287,512,490)`; sky obstacle `plane/cono_cielo.png` = `(159,194,182,112)`; boat `boat/bote.png` = `(179,80,142,341)`; sea obstacle `boat/cono_mar.png` = `(199,144,102,212)`.
- `cars/road.jpg` is 418×768 and the car lane bounds are tuned to it (`ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 398` in `GameScreen`). `plane/road_cielo.jpg` is 368×765 and the plane uses `ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 348` in `AirplaneScreen`. `boat/road_mar.jpg` is 494×768; `BoatScreen` draws it full-size and centered, using `ROAD_BORDER_TEX_PX = 20`, `ROAD_ASPHALT_TEX_PX = 454`. Changing any road art requires re-checking those constants.
- `plane/mosaico_cielo.jpg` and `boat/mosaico_mar.jpg` are 1376×768 and tiled vertically as scrolling backgrounds. Neither is perfectly seamless top-to-bottom; faint seams can appear in the side art.
- `cars/car_drive.wav` loops as a `Sound` at `0.3f`; `plane/avion_drive.mp3` and `boat/bote_drive.mp3` loop the same way at `0.3f`. `cars/car_horn.mp3` and `boat/bote_horn.mp3` play at `0.5f`. `cars/car_start.mp3`, `plane/avion_start.mp3`, and `boat/bote_start.mp3` play during their respective `load_gift_sheet` transitions; `plane/avion_drift.mp3` and `boat/bote_drift.mp3` are both the lane-change effects and ending sounds, at `0.4f`. `common/journey_awaits.mp3` plays on `MainMenuScreen` at `0.5f`; `common/racing_menu.ogg` loops on `VehicleSelectScreen` at `0.75f`. The boat start/horn are longer than the car timings; the current 5s gift and 2.5s horn timeout truncate them. Volumes are tuned manually by the user; do not “fix” them without being asked.
- Keep files under their matching asset folder and use the folder prefix in `AssetManager` keys; most runtime assets are still untracked in git.
- Box2D is a dependency but currently unused in code (kept for future gameplay).

## Flow / architecture

- `PunchItGame` (extends `Game`) owns the `AssetManager` and pre-creates all screens. Flow: `MainMenuScreen` → `VehicleSelectScreen` → car: `carGiftScreen` → `GameScreen`; plane: `planeGiftScreen` → `AirplaneScreen`; boat: `boatGiftScreen` → `BoatScreen`. All runs return to `VehicleSelectScreen`.
- `VehicleSelectScreen` shows car, plane, and boat side by side, equal height (`160`), bottoms near `y = 30`, centers spaced `240` apart. Left/Up selects the previous vehicle, Right/Down the next (clamped); Enter starts the selected mode. All branches enter a `GiftScreen` that shows `common/load_gift_sheet.png`; the car gift plays `cars/car_start.mp3`, the plane gift plays `plane/avion_start.mp3`, the boat gift plays `boat/bote_start.mp3`. `common/racing_menu.ogg` loops on this screen at `0.75` volume.
- All screens use `StretchViewport(1376×768)`; the desktop window is fullscreen via LWJGL 2.
- `GameScreen` phases: `INTRO` (`car_horn`, static) → `DRIVING` (scrolling mosaic, cones every 4 s, `car_drive` loop, 75 s clock) → `ENDED` (freeze; `car_horn` once, then `car_drift` once, then `car_drive` loop; `car_ending` overlay; then back to `VehicleSelectScreen`).
- `AirplaneScreen` phases: `DRIVING` (scrolling sky, sky obstacles every 4 s, `avion_drive` loop, 75 s clock) → `ENDED` (stop drive, `avion_drift` once, then `avion_drive` loop; `avion_ending` overlay for ~10 s; then back to `VehicleSelectScreen`). It has no intro phase and no horn.
- `BoatScreen` phases: `INTRO` (`bote_horn`, static) → `DRIVING` (scrolling sea, `cono_mar` every 4 s, `bote_drive` loop, 75 s clock) → `ENDED` (`bote_horn` once, `bote_drift` once, then `bote_drive` loop; `bote_ending` overlay; then back to `VehicleSelectScreen`).
- Gameplay input is **Enter only** (no mouse): it moves one lane, preferring a clear adjacent lane; at the edges it always steps inward.
- The car is confined to 4 lanes computed from `cars/road.jpg`; the plane uses 4 lanes computed from `plane/road_cielo.jpg`; the boat uses 4 lanes from the water-band crop of `boat/road_mar.jpg`. Obstacles spawn lane-centered and are inserted before the player in the Scene2D root so they render under it; each ending image is added after the player (on top).
- The `Illegal reflective access … ClassLoader.findLibrary` warning is expected from LWJGL 2 on JDK 11; it is not a bug.

## README

- `README.md` is current for Punch It and is written in Spanish. Keep it in sync with user-facing changes.

## Current state / next steps

- Car, plane, and boat modes are implemented and wired end-to-end.
- Volumes: `journey_awaits` `0.5`, `racing_menu` `0.75`, `car_horn`/`bote_horn` `0.5`, `car_drive`/`avion_drive`/`bote_drive` `0.3`, `avion_drift`/`bote_drift` `0.4`.
- Boat mode is implemented; clean compile and an automated shortened-run smoke test passed. Next: user visual verification, then commit only if requested.
- The latest commit is the README update; the boat implementation and most runtime assets are not committed.
