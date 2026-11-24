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
- Include the project constraints in implementation briefs: Java 7 only, Windows/libGDX stack, asset keys use the folder prefix (`cars/`, `common/`, `plane/`, `boat/`, `train/`), no comments, and no Gradle while a game is running.
- Assign one owner per file. A reliable split is: the main agent owns shared wiring (`PunchItGame`, `VehicleSelectScreen`, `GiftScreen`); one sub-agent owns a single new screen/file; another sub-agent reviews it.
- Do not ask sub-agents to commit, push, clean, reset, or run destructive git commands. Final integration, compile, and smoke tests remain with the main agent.
- Require a short report: files touched, key decisions, assumptions, checks performed, and remaining risks. Review every edit with `git diff` and a compile before continuing.
- Example for a new vehicle/mode: launch 3–4 read-only agents in parallel (asset audit with PIL/ffprobe; gameplay plan from `GameScreen`; selection/gift/wiring plan; verification/docs plan), synthesize their findings, implement shared files in the main agent, delegate the new screen file to one `general` agent, then compile and smoke-test.

## Assets

- Runtime assets live in **`/assets`** (not `android/assets`). `desktop/build.gradle` sets `workingDir` to `../assets`. Layout: `assets/cars/` car-only, `assets/common/` shared/menu, `assets/plane/` plane-only, `assets/boat/` boat-only, `assets/train/` train-only. Always use the matching prefix in `AssetManager` keys.
- libGDX cannot load GIFs. The game uses pre-generated sheets:
  - `common/load_gift_sheet.png`: 75 frames, 8×10 grid, 480×270, 1/15 s; shared by all `GiftScreen` instances.
  - `cars/car_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`GameScreen`).
  - `plane/avion_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`AirplaneScreen`).
  - `boat/bote_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`BoatScreen`).
  - `train/tren_ending_sheet.png`: 150 frames, 15×10 grid, 288×511 each, 1/15 s, ~10 s (`TrainScreen`); generated from `assets/train/tren_ending.gif` (480×852). The GIF itself is not loaded.
  - The car and plane source GIFs are not in the repo. `assets/boat/bote_ending.gif` is not present in the working tree; the game only loads `boat/bote_ending_sheet.png`. Regenerate sheets with the same grid/frame sizes and update the matching constants if the art changes.
- Cropped sprite regions: car `cars/carro.png` = `(190,134,120,227)`; cone `cars/cono.png` = `(192,169,116,162)`; plane `plane/avion.png` = `(256,287,512,490)`; sky obstacle `plane/cono_cielo.png` = `(159,194,182,112)`; boat `boat/bote.png` = `(179,80,142,341)`; sea obstacle `boat/cono_mar.png` = `(199,144,102,212)`; train `train/tren.png` = `(209,47,82,391)` including rails; ground obstacle `train/cono_tierra.png` = `(145,89,206,262)` including smoke.
- `cars/road.jpg` is 418×768 and the car lane bounds are tuned to it (`ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 398` in `GameScreen`). `plane/road_cielo.jpg` is 368×765 and the plane uses `ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 348` in `AirplaneScreen`. `boat/road_mar.jpg` is 494×768; `BoatScreen` draws it full-size and centered, using `ROAD_BORDER_TEX_PX = 20`, `ROAD_ASPHALT_TEX_PX = 454`. `train/road_tierra.jpg` is 167×754, drawn at fixed width `330` and full world height, centered, with `ROAD_BORDER_TEX_PX = 20`, `ROAD_ASPHALT_TEX_PX = 127` in `TrainScreen`. Changing any road art requires re-checking those constants.
- `plane/mosaico_cielo.jpg` and `boat/mosaico_mar.jpg` are 1376×768 and tiled vertically as scrolling backgrounds. `train/mosaico_tren.jpg` is 800×1328, drawn at `1376` wide and `2284.16` tall as two vertically tiled copies, scrolled at `90 px/s`. None of these mosaics is perfectly seamless top-to-bottom; faint seams can appear in the side art.
- `common/resumir.png` and `common/menu.png` are the pause-menu buttons, cropped `(39,155,422,190)` and `(79,155,342,191)` respectively and rendered at a standardized `360×160` by `PauseMenu`.
- `common/acerca.jpg`, `common/metricas.jpg`, and `common/reinicio.jpg` are the Options screen buttons, standardized to `360×160` by `OptionsScreen`. `assets/artifacts/acerca.html` is the styled about/dedication page opened by Acerca. Metrics reports are generated from `assets/artifacts/punch_it_metricas.html` and `assets/artifacts/punch_it_guia.html` into `Gdx.files.local("punch_it_metricas.html")` and `punch_it_guia.html`; the old `common/acerca.html`/`common/metricas.html` placeholders are no longer used.
- `GestorMetricas` persists metrics in `PunchIt_Metricas_Save` and `MetricasInputListener` counts ENTER presses/holds. Game screens record resolved threats, dodges/hits, streaks, anticipation, and driving time per mode.
- Audio: `cars/car_drive.wav` loops as a `Sound` at `0.3f`; `plane/avion_drive.mp3` and `boat/bote_drive.mp3` loop the same way at `0.3f`. `cars/car_horn.mp3` and `boat/bote_horn.mp3` play at `0.5f`. `cars/car_start.mp3`, `plane/avion_start.mp3`, and `boat/bote_start.mp3` play during their respective `load_gift_sheet` transitions; `plane/avion_drift.mp3` and `boat/bote_drift.mp3` are both the lane-change effects and ending sounds, at `0.4f`. Train audio: `train/tren_start.mp3` plays in the train `GiftScreen`; `train/tren_horn.mp3` is a `Music` at `0.5f` with `HORN_TIMEOUT = 3.2 s`; `train/tren_drive_loop.mp3` (trimmed from `tren_drive.mp3`) loops at `0.3f`; `train/tren_drift.mp3` is the lane-change and ending sound at `0.4f` with `TRAIN_DRIFT_DURATION = 2.4 s`. `common/journey_awaits.mp3` plays on `MainMenuScreen` at `0.5f`; `common/racing_menu.ogg` loops on `VehicleSelectScreen` and `OptionsScreen` at `0.75f`. The boat start/horn are longer than the car timings; the current 5s gift and 2.5s horn timeout truncate them. Volumes are tuned manually by the user; do not “fix” them without being asked.
- Keep files under their matching asset folder and use the folder prefix in `AssetManager` keys; most runtime assets are still untracked in git.
- Box2D is a dependency but currently unused in code (kept for future gameplay).

## Flow / architecture

- `PunchItGame` (extends `Game`) owns the `AssetManager` and pre-creates all screens. Flow: `MainMenuScreen` → `VehicleSelectScreen` (Jugar) or `OptionsScreen` (Opciones); from vehicle select: car: `carGiftScreen` → `GameScreen`; plane: `planeGiftScreen` → `AirplaneScreen`; boat: `boatGiftScreen` → `BoatScreen`; train: `trainGiftScreen` → `TrainScreen`. All runs return to `VehicleSelectScreen`.
- `OptionsScreen` shows Métricas (top), Reiniciar (middle), and Acerca (bottom) at `360×160`, with Métricas selected by default. Métricas calls `GestorMetricas.abrirPaginaMetricas()`; Acerca opens `assets/artifacts/acerca.html`; Reiniciar uses two Enter presses to confirm and calls `GestorMetricas.reiniciarMetricas()`. ESC cancels a pending reset or returns to `MainMenuScreen`.
- `VehicleSelectScreen` shows car, plane, boat, and train side by side, equal height (`130`), bottoms at `y = 45`, centers spaced `200` apart at `388`, `588`, `788`, `988`. Left/Up selects the previous vehicle, Right/Down the next (clamped); Enter starts the selected mode. All branches enter a `GiftScreen` that shows `common/load_gift_sheet.png`; the car gift plays `cars/car_start.mp3`, the plane gift plays `plane/avion_start.mp3`, the boat gift plays `boat/bote_start.mp3`, the train gift plays `train/tren_start.mp3`. `common/racing_menu.ogg` loops on this screen at `0.75` volume.
- All screens use `StretchViewport(1376×768)`; the desktop window is fullscreen via LWJGL 2.
- `GameScreen` phases: `INTRO` (`car_horn`, static) → `DRIVING` (scrolling mosaic, cones every 4 s, `car_drive` loop, 75 s clock) → `ENDED` (freeze; `car_horn` once, then `car_drift` once, then `car_drive` loop; `car_ending` overlay; then back to `VehicleSelectScreen`).
- `AirplaneScreen` phases: `DRIVING` (scrolling sky, sky obstacles every 4 s, `avion_drive` loop, 75 s clock) → `ENDED` (stop drive, `avion_drift` once, then `avion_drive` loop; `avion_ending` overlay for ~10 s; then back to `VehicleSelectScreen`). It has no intro phase and no horn.
- `BoatScreen` phases: `INTRO` (`bote_horn`, static) → `DRIVING` (scrolling sea, `cono_mar` every 4 s, `bote_drive` loop, 75 s clock) → `ENDED` (`bote_horn` once, `bote_drift` once, then `bote_drive` loop; `bote_ending` overlay; then back to `VehicleSelectScreen`).
- `TrainScreen` phases: `INTRO` (`tren_horn`, static) → `DRIVING` (scrolling mosaic, `cono_tierra` every 4 s, `tren_drive_loop` loop, 75 s clock) → `ENDED` (`tren_horn` once, `tren_drift` once, then `tren_drive_loop`; `tren_ending` overlay; then back to `VehicleSelectScreen`).
- Input map: main menu arrows + Enter select Jugar/Opciones, ESC exits the app; Options arrows + Enter activate the selected action (Métricas opens the generated report; Reiniciar is two-step), ESC returns to MainMenu; vehicle select arrows + Enter, ESC returns to MainMenu. Gameplay uses **Enter** for lane changes and **ESC** for pause; there is no mouse input. ESC pauses only during `DRIVING`; `PauseMenu` defaults to Resumir (Enter resumes), Menu returns to `VehicleSelectScreen`.
- The car is confined to 4 lanes computed from `cars/road.jpg`; the plane uses 4 lanes computed from `plane/road_cielo.jpg`; the boat uses 4 lanes computed from `boat/road_mar.jpg`; the train uses 4 lanes from `train/road_tierra.jpg`. Obstacles spawn lane-centered and are inserted before the player in the Scene2D root so they render under it; each ending image is added after the player (on top).
- The `Illegal reflective access … ClassLoader.findLibrary` warning is expected from LWJGL 2 on JDK 11; it is not a bug.

## README

- `README.md` is current for Punch It and is written in Spanish. Keep it in sync with user-facing changes.

## Current state / next steps

- Car, plane, boat, and train modes are implemented and wired end-to-end. Train mode (`TrainScreen`) is implemented (`VehicleType.TRAIN`, `GiftScreen` train start sound and explicit `TRAIN` branches, `PunchItGame` `TrainScreen`/`trainGiftScreen` and train asset loads/dispose).
- `PauseMenu` is implemented for all four game modes: ESC during `DRIVING` opens it, Resumir is selected by default, Menu returns to `VehicleSelectScreen`, and ESC on `VehicleSelectScreen` returns to `MainMenuScreen`.
- `OptionsScreen` is implemented: Opciones shows Acerca/Reiniciar/Métricas, Métricas opens the generated accessibility report, Reiniciar requires two Enter presses, and ESC returns to `MainMenuScreen`. ESC on the main menu exits the application.
- The accessibility metrics system is implemented: `GestorMetricas` uses `Preferences`, `MetricasInputListener` records ENTER presses/holds, and all four game screens record threats, dodges/hits, streaks, anticipation, and driving time. Reports use the templates in `assets/artifacts/` and are written to the local working directory.
- Volumes: `journey_awaits` `0.5`, `racing_menu` `0.75`, `car_horn`/`bote_horn`/`tren_horn` `0.5`, `car_drive`/`avion_drive`/`bote_drive`/`tren_drive_loop` `0.3`, `avion_drift`/`bote_drift`/`tren_drift` `0.4`.
- Boat and train passed a clean compile; both also passed automated shortened-run smoke tests. The pause flow passed an automated keyboard smoke test. Options navigation and the main-menu ESC exit passed an automated smoke test. The metrics reset and HTML generation smoke test passed, and the Métricas button produced a nonzero report with replaced placeholders. Train/pause/options visuals, gameplay metric values, and browser opening still need human verification.
- The latest commit is the line-ending configuration; the train/pause/options/metrics implementations, `assets/train/`, `assets/artifacts/`, and the button assets are uncommitted. Generated `punch_it_metricas.html`/`punch_it_guia.html` files are ignored via `.gitignore`. `assets/boat/bote_ending.gif` is deleted in the working tree but is not needed at runtime.
