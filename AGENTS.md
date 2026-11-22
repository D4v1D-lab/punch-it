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

## Assets

- Runtime assets live in **`/assets`** (not `android/assets`). `desktop/build.gradle` sets `workingDir` to `../assets`. Layout: `assets/cars/` car-only, `assets/common/` shared/menu, `assets/plane/` plane-only, `assets/boat/` reserved and currently empty. Always use the matching prefix in `AssetManager` keys.
- libGDX cannot load GIFs. The game uses pre-generated sheets:
  - `common/load_gift_sheet.png`: 75 frames, 8×10 grid, 480×270, 1/15 s; shared by both `GiftScreen` instances.
  - `cars/car_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`GameScreen`).
  - `plane/avion_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`AirplaneScreen`).
  - The car source GIFs are not in the repo. `assets/plane/avion_ending.gif` (480×852) is the source for the generated plane ending sheet; regenerate the sheet with the same grid/frame sizes and update the matching constants if the art changes.
- Cropped sprite regions: car `cars/carro.png` = `(190,134,120,227)`; cone `cars/cono.png` = `(192,169,116,162)`; plane `plane/avion.png` = `(256,287,512,490)`; sky obstacle `plane/cono_cielo.png` = `(159,194,182,112)`.
- `cars/road.jpg` is 418×768 and the car lane bounds are tuned to it (`ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 398` in `GameScreen`). `plane/road_cielo.jpg` is 368×765 and the plane uses `ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 348` in `AirplaneScreen`. Changing either road art requires re-checking those constants.
- `plane/mosaico_cielo.jpg` is 1376×768 and is tiled vertically as the scrolling sky. It is not perfectly seamless top-to-bottom; a faint seam can appear in the side clouds.
- `cars/car_drive.wav` loops as a `Sound` at `0.3f`; `plane/avion_drive.mp3` loops the same way at `0.3f`. `cars/car_horn.mp3` plays at `0.5f`. `cars/car_start.mp3` and `plane/avion_start.mp3` play during their respective `load_gift_sheet` transitions; `plane/avion_drift.mp3` is both the lane-change effect and the plane-run ending sound at `0.4f`. `common/journey_awaits.mp3` plays on `MainMenuScreen` at `0.5f`; `common/racing_menu.ogg` loops on `VehicleSelectScreen` at `0.75f`. Volumes are tuned manually by the user; do not “fix” them without being asked.
- The runtime asset folders are currently untracked in git; keep files under their matching folders and use the folder prefix in `AssetManager` keys.
- Box2D is a dependency but currently unused in code (kept for future gameplay).

## Flow / architecture

- `PunchItGame` (extends `Game`) owns the `AssetManager` and pre-creates all screens. Flow: `MainMenuScreen` → `VehicleSelectScreen` → car: `GiftScreen` → `GameScreen`; plane: `planeGiftScreen` → `AirplaneScreen`. Both runs return to `VehicleSelectScreen`.
- `VehicleSelectScreen` shows the car and plane side by side, equal height (`200`), bottoms near `y = 50`. Left/Up selects the car, Right/Down selects the plane; Enter starts the selected mode. Both branches enter a `GiftScreen` that shows `common/load_gift_sheet.png`; the car gift plays `cars/car_start.mp3`, the plane gift plays `plane/avion_start.mp3`. `common/racing_menu.ogg` loops on this screen at `0.75` volume.
- All screens use `StretchViewport(1376×768)`; the desktop window is fullscreen via LWJGL 2.
- `GameScreen` phases: `INTRO` (`car_horn`, static) → `DRIVING` (scrolling mosaic, cones every 4 s, `car_drive` loop, 75 s clock) → `ENDED` (freeze; `car_horn` once, then `car_drift` once, then `car_drive` loop; `car_ending` overlay; then back to `VehicleSelectScreen`).
- `AirplaneScreen` phases: `DRIVING` (scrolling sky, sky obstacles every 4 s, `avion_drive` loop, 75 s clock) → `ENDED` (stop drive, `avion_drift` once, then `avion_drive` loop; `avion_ending` overlay for ~10 s; then back to `VehicleSelectScreen`). It has no intro phase and no horn.
- Gameplay input is **Enter only** (no mouse): it moves one lane, preferring a clear adjacent lane; at the edges it always steps inward.
- The car is confined to 4 lanes computed from `cars/road.jpg`; the plane uses 4 lanes computed from `plane/road_cielo.jpg`. Obstacles spawn lane-centered and are inserted before the player in the Scene2D root so they render under it; each ending image is added after the player (on top).
- The `Illegal reflective access … ClassLoader.findLibrary` warning is expected from LWJGL 2 on JDK 11; it is not a bug.

## README

- `README.md` is stale for the current game (CumbiaBall docs + historical context). Do not rewrite or delete it unless explicitly asked; prefer code/config.

## Current state / next steps

- Car and plane modes are implemented and wired end-to-end; the latest change tuned audio volumes.
- Volumes: `journey_awaits` `0.5`, `racing_menu` `0.75`, `car_horn` `0.5`, `car_drive`/`avion_drive` `0.3`, `avion_drift` `0.4`.
- User is verifying the build/audio. Next session: apply any feedback, then commit only if requested.
- No commit has been made; the runtime `assets/` folders are untracked.
