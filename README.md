# 🎮 CumbiaBall

Un juego de plataformas 2D hecho con [libGDX](https://libgdx.com/): controlas un balón que salta de plataforma en plataforma, esquiva pinchos y llega a la meta mientras suena música de cumbia. Hay dos niveles y una pantalla de "game over" con reintento.

<!-- Si tienes una captura o GIF del juego, guárdalo en docs/captura.gif y descomenta la línea siguiente. -->
<!-- ![Captura de CumbiaBall](docs/captura.gif) -->

---

## ✅ Requisitos

| Qué necesitas | Detalle |
|---|---|
| **Java** | **JDK 11 — Temurin recomendado** (probado con 11.0.x) |
| Rango soportado | Java 8 a 15. Fuera de ese rango el proyecto no arranca |
| ⚠️ Evita | Java 16 o superior, y el **JDK de Microsoft** (ver solución de problemas) |
| Sistema | Windows, macOS, Linux o GitHub Codespaces |
| Gradle | No hace falta instalarlo: el repo trae el wrapper (`./gradlew`) |

> ⚠️ **No uses Java 16, 17, 21 ni 25.** El proyecto usa Gradle 6.7.1, que solo funciona hasta Java 15. Si ves `Unsupported class file major version`, es exactamente eso.

---

## ☁️ Opción A — Jugar en GitHub Codespaces (sin instalar nada)

1. Entra al repo y pulsa **Code → Codespaces → Create codespace on master**.
2. Espera a que termine de construirse el contenedor (la primera vez tarda unos minutos: instala Java 11 y las librerías gráficas).
3. Abre la pestaña **Ports** (junto a la Terminal) y busca el puerto **6080 — Escritorio noVNC**. Pulsa el icono del globo para abrirlo.
4. Ingresa la contraseña: **`vscode`**.
5. En el escritorio remoto, abre una terminal (icono de terminal en la barra o clic derecho → *Open Terminal Here*) y ejecuta:

   ```bash
   ./scripts/run.sh
   ```

   El juego aparecerá en esa misma ventana remota.

> 💡 También puedes lanzarlo desde la terminal normal de VS Code: el contenedor ya trae `DISPLAY=:1`, así que la ventana se dibuja en el escritorio de noVNC.
>
> 🔇 En Codespaces no hay tarjeta de sonido. Verás `Couldn't initialize audio` y el juego seguirá funcionando sin sonido: es lo esperado.

---

## 💻 Opción B — Jugar en tu computador

### 1. Instala Java 11 (Temurin)

**Windows**

```powershell
winget install EclipseAdoptium.Temurin.11.JDK
```

**macOS** (con Homebrew)

```bash
brew install --cask temurin@11
```

**Linux (Debian/Ubuntu)** — descarga el `.tar.gz` de [Adoptium](https://adoptium.net/temurin/releases/?version=11), descomprímelo y apunta `JAVA_HOME` a esa carpeta.

**Cualquier sistema con [SDKMAN!](https://sdkman.io/)**

```bash
sdk env install     # lee .sdkmanrc y baja Temurin 11
sdk env             # activa esa versión en la sesión actual
```

Comprueba que quedó bien:

```bash
java -version
# openjdk version "11.0.x" ...
```

### 2. Clona el repositorio

```bash
git clone https://github.com/D4v1D-lab/CumbiaBall.git
cd CumbiaBall
```

### 3. Ejecuta el juego

**Linux / macOS**

```bash
./scripts/run.sh
```

**Windows**

```bat
scripts\run.bat
```

Los scripts verifican la versión de Java, avisan si algo no cuadra y arrancan `gradlew desktop:run`. En Windows, `scripts\run.bat` además busca Temurin 11 automáticamente si `JAVA_HOME` no existe o apunta a una versión no soportada, así que no necesitas configurar nada más (ni `ANDROID_HOME`). Si prefieres lanzarlo a mano: `./gradlew desktop:run` (Linux/macOS) o `gradlew.bat desktop:run` (Windows).

---

## 🕹️ Controles

| Acción | Cómo |
|---|---|
| Saltar | **Clic** o **toque** en cualquier parte de la pantalla |
| Saltar al aterrizar | Mantén pulsado mientras caes: el salto se ejecuta al tocar el suelo |
| Iniciar partida | Clic en el botón **play** del menú |
| Reintentar tras perder | Clic en el botón de la pantalla de *game over* |
| Salir | Cierra la ventana del juego |

Objetivo: avanzar por las plataformas sin tocar los pinchos y alcanzar la bandera de meta. Al superar el primer nivel pasas al segundo, con otra música.

---

## 🔧 Solución de problemas

**`Unsupported class file major version 69`**
Estás usando Java 25 (o cualquier versión 16+). Gradle 6.7.1 no puede ejecutarse ahí. Instala Temurin 11 y asegúrate de que `java -version` lo confirme. Si usas SDKMAN, ejecuta `sdk env`.

**`libjawt.so: version 'SUNWprivate_1.1' not found`**
Estás con el JDK de Microsoft (`msopenjdk`). LWJGL 2 no es compatible con ese build. Desinstálalo o cambia `JAVA_HOME` a un Temurin 11 (o Zulu 11) y detén el daemon: `./gradlew --stop`.

**`libXcursor.so.1: cannot open shared object file`**
Faltan librerías gráficas de X11. En Debian/Ubuntu:

```bash
sudo apt-get update && sudo apt-get install -y \
  libxcursor1 libxrandr2 libxxf86vm1 libgl1-mesa-dri libopenal1 libgl1 mesa-utils
```

En Codespaces esto ya lo instala el `postCreateCommand` del devcontainer. Si acabas de cambiar el devcontainer, reconstruye el contenedor (*Command Palette → Codespaces: Rebuild Container*).

**`Could not open X display connection` / pantalla en negro en noVNC**
No hay servidor gráfico disponible. En Codespaces: confirma que el puerto **6080** está abierto, entra con la contraseña `vscode` y ejecuta el juego **dentro** del escritorio remoto. En Linux sin entorno gráfico, define `export DISPLAY=:1` solo si tienes un servidor X (Xvfb o noVNC) corriendo ahí. En macOS, el script ya añade `-XstartOnFirstThread`.

**`Couldn't initialize audio`**
Es normal en Codespaces y en equipos sin tarjeta de sonido. El juego se ejecuta igual, sin audio; no es un error del proyecto.

**Cambié de Java y sigue fallando con el Java viejo**
El daemon de Gradle conserva el JDK con el que se inició. Deténlo y vuelve a lanzar:

```bash
./gradlew --stop
./scripts/run.sh
```

**VS Code no reconoce Java 11**
El repo no fija rutas de JDK (para no romper en otras máquinas). Abre la paleta de comandos → **Java: Configure Java Runtime** y selecciona tu Temurin 11, o define `JAVA_HOME` antes de abrir VS Code. Las extensiones recomendadas se instalan solas al abrir el proyecto.

**`Invalid jdkhome specified` al abrir el proyecto en VS Code**
La variable de usuario `JAVA_HOME` quedó apuntando a un JDK que ya no existe (por ejemplo, un 17 desinstalado). Corrígela para que apunte al Temurin 11 instalado y reinicia VS Code por completo:

```powershell
[Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-11.0.33.10-hotspot', 'User')
```

Ajusta la ruta si tu versión es otra (`jdk-11.x.x-hotspot`). `scripts\run.bat` ya ignora un `JAVA_HOME` inválido, así que el juego arranca igualmente.

---

## 📁 Estructura del proyecto

```
CumbiaBall/
├── core/                 Lógica del juego (pantallas, entidades, física Box2D)
├── desktop/              Launcher de escritorio (LWJGL 2) y tarea `run`
├── android/              Módulo Android y assets del juego (android/assets)
├── assets/               Copias legacy/plantilla NO usadas por el juego
├── scripts/              run.sh (Linux/macOS/Codespaces) y run.bat (Windows)
├── .devcontainer/        Entorno reproducible de Codespaces
├── .vscode/              Extensiones y configuración recomendadas
├── build.gradle          Configuración raíz y verificación del JDK
└── gradlew, gradlew.bat  Gradle Wrapper 6.7.1
```

> ℹ️ El módulo `desktop` toma las imágenes y sonidos de `android/assets`, por eso el `workingDir` de la tarea `run` apunta ahí.

---

## 🧭 Contexto técnico completo (traspaso / reconstrucción)

> Esta sección es el documento de traspaso del proyecto. Está pensada para que otra persona (o tú mismo dentro de unos meses) pueda entender **cómo funciona el juego, de qué piezas está hecho y cómo modificarlo**, incluso si el código actual se borra y se empieza de cero. Describe el estado real del repo, incluyendo bugs y deuda técnica.

### Ficha rápida

| Dato | Valor actual |
|---|---|
| Género | Plataformas 2D de un solo botón (saltar) |
| Nombre visible | **CumbiaBall** (README) / **MyJump** (`appName` y label de Android) |
| Motor | [libGDX](https://libgdx.com/) **1.10.0** |
| Backend de escritorio | `gdx-backend-lwjgl` (**LWJGL 2**) |
| Física | Box2D (`gdx-box2d`) |
| UI / escenas | Scene2D + Scene2D.UI |
| Lenguaje | Java (fuente/nivel 1.7), JDK de ejecución **8–15**, probado **11 (Temurin)** |
| Build | **Gradle 6.7.1** fijado por el wrapper |
| Android | AGP 3.5.4, `compileSdk 30`, `buildTools 30.0.3`, `minSdk 14`, `targetSdk 30` |
| Viewport lógico | **640 × 360** (16:9), `FitViewport` |
| Escala | **45 px = 1 m** (`Constants.PIXELS_IN_METERS`) |
| Gravedad | `(0, -10) m/s²` |
| Paso de física | `world.step(delta, 6, 2)` (delta variable) |
| Niveles actuales | 2 (el nivel 2 **no tiene meta/win**) |
| Estado | Prototipo jugable con código legado y fugas de memoria |

### Stack, versiones y por qué están fijadas

Todo se define en `build.gradle` (raíz) y `gradle/wrapper/gradle-wrapper.properties`:

- `gdxVersion = '1.10.0'`, `appName = "MyJump"`.
- Módulos Gradle: `:core` (lógica), `:desktop` (launcher LWJGL 2), `:android` (launcher y assets). `settings.gradle` incluye los tres.
- `core` y `desktop` compilan con `sourceCompatibility = 1.7`.
- `gradle.properties`: `org.gradle.daemon=true`, `org.gradle.jvmargs=-Xms128m -Xmx1500m`, `org.gradle.configureondemand=false`.
- `.sdkmanrc` fija `java=11.0.33-tem`.

Restricciones que **no** son opcionales si se mantiene el stack:

- **Gradle 6.7.1 no corre en JDK 16+**. El `build.gradle` raíz falla a propósito si detecta Java > 15, para dar un mensaje claro.
- **LWJGL 2**, usado por `gdx-backend-lwjgl`, es antiguo; en Linux el JDK de Microsoft rompe con `libjawt.so: version 'SUNWprivate_1.1' not found`.
- El wrapper se actualizó por accidente una vez a Gradle 9.7.1 y el juego dejó de arrancar (`Gradle requires JVM 17 or later`). Si pasa, restaurar con:
  ```bash
  git restore gradle/wrapper/gradle-wrapper.properties
  ```
- Migrar a libGDX moderno implica, como mínimo: LWJGL 2 → LWJGL 3 (`gdx-backend-lwjgl3`), Gradle 8+, JDK 17+, y revisar APIs de Box2D/Scene2D. No es un cambio trivial.

### Árbol de archivos y responsabilidades

```
punch-it/
├── build.gradle                  # repos, versiones, dependencias por módulo y guardia de JDK
├── settings.gradle               # include 'desktop', 'android', 'core'
├── gradle.properties             # daemon, memoria, configureondemand
├── gradlew / gradlew.bat         # wrapper Gradle 6.7.1
├── gradle/wrapper/               # properties + jar del wrapper (NO actualizar)
├── core/src/com/mygdx/game/
│   ├── MainGame.java             # ApplicationListener: AssetManager y dueño de las pantallas
│   ├── BaseScreen.java           # clase base con métodos Screen vacíos
│   ├── LoadingScreen.java        # carga assets con AssetManager y muestra progreso
│   ├── MenuScreen.java           # fondo + logo + botón play
│   ├── GameScreen.java           # NIVEL 1 (Box2D + escena)
│   ├── GameScreen2.java          # NIVEL 2 (Box2D + escena, sin meta)
│   ├── GameOverScreen.java       # fondo + "game over" + botón reintentar
│   ├── Constants.java            # 45 px/m, impulso de salto, velocidades
│   ├── Box2DScreen.java          # prototipo antiguo, NO usado
│   ├── entities/
│   │   ├── PlayerEntity.java     # jugador del nivel 1 (Actor + body dinámico)
│   │   ├── PlayerEntity2.java    # jugador del nivel 2 (casi idéntico, más rápido)
│   │   ├── FloorEntity.java      # plataforma estática + borde izquierdo letal
│   │   ├── SpikeEntity.java      # pincho triangular estático
│   │   └── LevelEntity.java      # trigger de meta (fixture "finish")
│   └── scene2D/
│       ├── Scene2DScreen.java    # prototipo Scene2D puro, NO usado
│       ├── PlayerActor.java      # actor de prueba, NO usado
│       └── SpikesActor.java      # actor de prueba, NO usado
├── desktop/src/com/mygdx/game/desktop/DesktopLauncher.java
├── android/
│   ├── src/com/mygdx/game/AndroidLauncher.java
│   ├── AndroidManifest.xml       # landscape, package com.mygdx.game
│   ├── build.gradle              # app Android + tarea copyAndroidNatives + tarea run (adb)
│   ├── res/                      # strings ("MyJump"), estilos, iconos
│   └── assets/                   # ⚠️ ASSETS REALES que usa también desktop
├── assets/                       # copias sueltas/legacy (NO se usan en desktop)
├── scripts/run.sh | run.bat      # lanzadores Linux/macOS/Codespaces y Windows
├── .devcontainer/                # Java 11 + desktop-lite (noVNC) para Codespaces
├── .vscode/                      # launch.json, extensions.json, settings.json
└── .sdkmanrc                     # Temurin 11.0.33
```

Puntos clave del árbol:

- **`android/assets/` es la carpeta de assets de facto.** La tarea `desktop:run` fija `workingDir` ahí; si un recurso no está en `android/assets/`, el juego no lo encuentra aunque exista en `assets/`.
- `assets/` (raíz) contiene `badlogic.jpg` y copias de `player1/3/4/5.png`, `spikes1.png` que **no se referencian** (restos del template de gdx-setup). No las edites esperando que cambien el juego.
- `Box2DScreen`, `Scene2DScreen`, `PlayerActor` y `SpikesActor` no se instancian desde `MainGame`; son prototipos. `player5.png` solo lo usa `Scene2DScreen`.

### Flujo de pantallas (máquina de estados)

`MainGame` extiende `Game` y guarda **una instancia de cada pantalla** creada en `finishloading()`. Solo se renderiza una a la vez (`Game.setScreen`).

```
DesktopLauncher.main()
  └─ new LwjglApplication(new MainGame(), config)
       └─ MainGame.create()
            ├─ crea AssetManager y encola todos los assets
            └─ setScreen(LoadingScreen)

LoadingScreen.render()
  ├─ mientras manager.update() == false → muestra "Loading... N%"
  └─ cuando termina → MainGame.finishloading()
        ├─ crea GameScreen, GameOverScreen, GameScreen2, MenuScreen
        └─ setScreen(MenuScreen)

MenuScreen  --(botón play)-->  GameScreen (nivel 1)
GameScreen  --(toca pincho)--> GameOverScreen   [delay 2.85 s]
GameScreen  --(toca meta)----> GameScreen2       [delay 1.5 s]
GameOverScreen --(botón)-----> LoadingScreen
LoadingScreen (assets ya cargados) → finishloading() → MenuScreen
GameScreen2 --(toca pincho)--> GameOverScreen   [delay 2.85 s]
GameScreen2 --(no hay meta)---> (sigue hasta morir)
```

Consecuencias importantes:

- **Reintentar no reinicia el nivel directamente:** vuelve a `LoadingScreen`; como el `AssetManager` ya terminó, `finishloading()` **crea pantallas nuevas** y entra al menú. Es decir, reintentar = volver al menú.
- `finishloading()` no destruye las pantallas anteriores; se van acumulando (fuga de memoria).
- El nivel 2 no tiene `LevelEntity` ni rama `"finish"` en su `ContactListener`: **no se puede ganar**.

### Ciclo de vida y carga de assets

`MainGame.create()` encola en `AssetManager`:

```java
manager.load("floor.png", Texture.class);
manager.load("spikes1.png", Texture.class);
manager.load("ballgamer.png", Texture.class);
manager.load("player1.png", Texture.class);
manager.load("overfloor.png", Texture.class);
manager.load("song1.mp3", Music.class);
manager.load("song2.mp3", Music.class);
manager.load("jump.mp3", Sound.class);
manager.load("death.mp3", Sound.class);
manager.load("gameover.png", Texture.class);
manager.load("plybtn.png", Texture.class);
manager.load("bg.png", Texture.class);
manager.load("bground.png", Texture.class);
manager.load("bground2.png", Texture.class);
manager.load("intro.png", Texture.class);
manager.load("finish.png", Texture.class);
```

`LoadingScreen` crea su propio `Skin` leyendo `skin/uiskin.json`, muestra un `Label` y llama a `manager.update()` en cada frame. Cuando devuelve `true`, entra al menú.

Pero hay recursos que **no** pasan por el manager y por tanto no se liberan:

- `MenuScreen` y `GameOverScreen` hacen `new Texture("plybtn.png")` directamente.
- `GameScreen` y `GameScreen2` hacen `new SpriteBatch()` y `new Texture("bground.png"/"bground2.png")` directamente.
- `Scene2DScreen` (legado) hace `new Texture("player5.png")`.
- `MainGame` no sobrescribe `dispose()`, así que el `AssetManager` (texturas y música) tampoco se libera al cerrar.

Regla práctica: si añades un asset, cárgalo en `MainGame.create()` y consíguelo con `game.getManager().get("nombre", Tipo.class)`; si lo creas con `new Texture(...)`, acuérdate de liberarlo en `dispose()`.

### Motor de juego: Scene2D + Box2D

El patrón que usa el proyecto es **actores de Scene2D respaldados por cuerpos de Box2D**:

1. Cada pantalla de juego crea un `Stage` con `new FitViewport(640, 360)` y un `World` con gravedad `(0, -10)`.
2. La física trabaja en **metros**. El render trabaja en **píxeles**. La conversión es `1 m = 45 px`.
3. Cada entidad (`FloorEntity`, `SpikeEntity`, etc.) es un `Actor` que:
   - crea su `Body`/`Fixture` en el constructor,
   - etiqueta la fixture con `fixture.setUserData("player" | "floor" | "spike" | "finish")`,
   - en `draw()` dibuja su textura en coordenadas de pantalla,
   - expone `detach()` para destruir su cuerpo.
4. Para el jugador, el `Actor` no fija su posición “a mano”: en `draw()` copia la posición del body (`body.getPosition()`) a píxeles.
5. `GameScreen`/`GameScreen2` registran un `ContactListener` en el `World` y reaccionan según los `userData`:
   - `player` + `floor` → el jugador puede volver a saltar.
   - `player` + `spike` → muerte (incluye los **bordes izquierdos** de las plataformas, que llevan `userData="spike"`).
   - `player` + `finish` → solo en el nivel 1; transición al nivel 2.
6. Por frame: se lee input, se llama `stage.act()` (los actores ajustan velocidades), luego `world.step(delta, 6, 2)` (la física resuelve) y `stage.draw()` (se dibuja).

Composición de cada pantalla de juego:

| Elemento | `GameScreen` (nivel 1) | `GameScreen2` (nivel 2) |
|---|---|---|
| Textura del jugador | `ballgamer.png` | `player1.png` |
| Clase de jugador | `PlayerEntity` (6 m/s) | `PlayerEntity2` (10 m/s) |
| Música | `song1.mp3` | `song2.mp3` |
| Fondo | `bground.png` | `bground2.png` |
| Meta | Sí (`LevelEntity` + `"finish"`) | **No** |
| Plataformas | 4 | 8 |
| Pinchos | 11 | 9 |

Cámara:

- Es la cámara del `Stage` (`FitViewport`), inicialmente centrada en `320,180` (mundo 640×360).
- Solo se desplaza en X y solo cuando el jugador supera `x > 150 px`.
- Velocidad de scroll: `PLAYER_SPEED * delta * PIXELS_IN_METERS` (270 px/s en nivel 1) y `PLAYER_SPEED2 * …` (450 px/s en nivel 2). Está pensada para igualar la velocidad horizontal del jugador.
- No hay seguimiento vertical, límites ni suavizado.

Fondo:

- Se dibuja con un `SpriteBatch` aparte en `(2, 0)`.
- Hay un detalle frágil: dentro de `batch.begin()` se dibuja y **después** se cambia la matriz de proyección (`setToOrtho2D`) antes de `batch.end()`. El cambio afecta al frame siguiente. El fondo es estático y no acompaña a la cámara; si rehaces esto, dibuja el fondo con la cámara o usa un `Actor`/`Image` en el `Stage`.

### Entidades: contrato y detalles

#### `PlayerEntity` / `PlayerEntity2`

- `Actor` + `BodyDef.BodyType.DynamicBody`, shape `PolygonShape.setAsBox(0.5f, 0.5f)` → colisionador de **1×1 m**.
- `fixture.setUserData("player")`.
- El actor mide `45×45 px` (`setSize(PIXELS_IN_METERS, PIXELS_IN_METERS)`), así que **cualquier sprite se estira a un cuadrado**, sin importar su aspecto original.
- `act(delta)`:
  - si `mustJump` → `jump()` y lo limpia;
  - si `alive` → fija velocidad horizontal constante (`PLAYER_SPEED` = 6 en `PlayerEntity`, `PLAYER_SPEED2` = 10 en `PlayerEntity2`), conservando la velocidad vertical;
  - si `jumping` → aplica una fuerza hacia abajo `-IMPULSE_JUMP * 1.25` (efecto de salto “corto/flotante”).
- `jump()`: solo si `!jumping && alive`; aplica impulso `(0, 7)` en el centro del body y marca `jumping = true`. No hay `setLinearVelocity` vertical, ni fricción/restitución configuradas.
- `detach()`: destruye la fixture y el body.

Diferencias reales entre las dos clases: **solo la velocidad** (6 vs 10) y la textura que les pasa la pantalla. `Constants.PLAYER_SPEED2` (10) y `PlayerEntity2.PLAYER_SPEED2` (10) están duplicados: si cambias uno, el otro no se entera.

#### `FloorEntity`

Constructor: `FloorEntity(world, texturaSuelo, texturaBorde, x, width, y)`.

- Cuerpo estático centrado en `(x + width/2, y - 0.5)`, caja de semiejes `(width/2, 0.5)` → **la superficie superior queda en `y`**.
- Fixture principal con `userData="floor"`.
- **Borde izquierdo letal:** cuerpo estático en `(x, y - 0.55)` con caja `(0.02, 0.45)` y `userData="spike"`. Si el jugador choca con el lateral izquierdo de una plataforma elevada, muere.
- Actor: tamaño `(width * 45) × 45 px`, posición `(x * 45, (y - 1) * 45)`.
- `draw()`: dibuja `floor` en todo el rectángulo y `overfloor` como una franja superior del 10 % de la altura.
- `detach()`: destruye **solo** el cuerpo principal; `leftBody`/`leftFixture` quedan vivos (fuga).

#### `SpikeEntity`

Constructor: `SpikeEntity(world, textura, x, y)`.

- Cuerpo estático en `(x, y + 0.5)`.
- Triángulo manual: `(-0.5,-0.5)`, `(0.5,-0.5)`, `(0,0.5)` → base en `y`, punta en `y + 1`.
- `userData="spike"`.
- Actor en `((x - 0.5) * 45, y * 45)`, tamaño `45×45 px`.
- `detach()`: destruye fixture y body.

#### `LevelEntity`

Constructor: `LevelEntity(world, texturaMeta, x, width, y)`.

- Cuerpo estático centrado en `(x + width/2, y - 0.5)`, caja `(width/2, 0.5)` → superficie superior en `y`.
- `userData="finish"`.
- Crea un `leftBody` en `(x, y - 0.55)` **sin fixture** (el código está comentado); no se destruye en `detach()` (fuga).
- `draw()`: dibuja **solo** la franja superior (`finish` al 10 % de la altura), no un suelo completo.
- En el nivel 1, la meta visual es una `FloorEntity` con `finish.png` como `overfloor` en `x=105, width=10, y=1.5`, y el trigger es un `LevelEntity` en `x=105, width=10, y=1.6` (un poco por encima).

#### Unidades y coordenadas (chuleta)

| Concepto | Unidad / convención |
|---|---|
| Física (`Body.position`) | metros |
| Render (`Actor.setPosition`) | píxeles |
| Conversión | `px = m × 45` |
| Viewport | 640×360 px = 14.22×8 m visibles |
| `FloorEntity.y` | superficie **superior** de la plataforma |
| `SpikeEntity.y` | **base** del pincho (punta en `y+1`) |
| `LevelEntity.y` | superficie superior del trigger |
| Spawn del jugador | `(1.5, 1.5)` m; centro del body |
| Umbral de cámara | `player.getX() > 150` px |

### Datos de los niveles (extraídos del código)

Los niveles están **hardcodeados** en `GameScreen.show()` y `GameScreen2.show()`.

#### Nivel 1 — `GameScreen.java`

Spawn: `(1.5, 1.5)` m. Velocidad: 6 m/s. Cámara: 270 px/s tras `x > 150 px`.

| Tipo | x | width | y | Notas |
|---|---:|---:|---:|---|
| Suelo | 0 | 1000 | 1 | suelo continuo |
| Plataforma | 12 | 10 | 2 | |
| Plataforma | 32 | 5 | 1.5 | |
| Meta (visual) | 105 | 10 | 1.5 | `FloorEntity` con `finish.png` |
| Meta (trigger) | 105 | 10 | 1.6 | `LevelEntity` |

Pinchos `(x, y)`:

```
(6,1) (18,2) (26,1) (38,1) (47,1) (56,1) (65,1) (74,1) (83,1) (92,1) (101,1)
```

#### Nivel 2 — `GameScreen2.java`

Spawn: `(1.5, 1.5)` m. Velocidad: 10 m/s. Cámara: 450 px/s tras `x > 150 px`. **Sin meta.**

| Tipo | x | width | y | Notas |
|---|---:|---:|---:|---|
| Suelo | 2 | 1000 | 1 | empieza en x=2, no en 0 |
| Plataforma | 14 | 10 | 2 | |
| Plataforma | 30 | 5 | 1.5 | |
| Plataforma | 113 | 4 | 1.5 | |
| Plataforma | 124 | 4 | 1.5 | |
| Plataforma | 135 | 4 | 1.5 | |
| Plataforma | 148 | 4 | 1.5 | |
| Plataforma | 158 | 4 | 3 | |

Pinchos `(x, y)`:

```
(18,2) (37,1) (50,1) (56,1) (65,1) (74,1) (83,1) (92,1) (101,1)
```

> 🐛 El suelo del nivel 2 empieza en `x=2`, pero el jugador aparece en `x=1.5` (ocupa de 1.0 a 2.0). En la práctica no se cae porque en el primer frame ya se le fija velocidad horizontal de 10 m/s y entra en la plataforma; aun así, es una posición inicial inconsistente que conviene corregir si se rehace el nivel.

### Controles y reglas

- **Saltar:** clic/toque en cualquier parte (`Gdx.input.isTouched()`). No hay teclado.
- Mientras el jugador está en el aire, `jump()` no hace nada (`jumping == true`).
- Al tocar suelo, el `ContactListener` pone `jumping = false`; si el botón sigue pulsado, marca `mustJump = true` y salta al siguiente `act()`. Por eso **mantener pulsado = saltar al aterrizar**.
- El sonido de salto (`jumpSound.play()`) se ejecuta **en cada frame** que el botón está pulsado, no solo cuando el salto ocurre; mantener pulsado puede solapar sonidos.
- **Morir:** contacto con `"spike"` (pinchos o borde izquierdo de plataformas). Si `alive`: `alive=false`, `deathSound.play(0.40f)`, se detiene la música, espera **2.85 s** y pasa a `GameOverScreen`.
- **Ganar nivel 1:** contacto con `"finish"` → `alive=false`, espera **1.5 s** y pasa a `GameScreen2`.
- **Nivel 2:** no hay condición de victoria; se juega hasta morir.
- **Caer al vacío:** no se puede; el suelo mide 1000 m y no hay muerte por caída.
- No hay puntuación, vidas, temporizador, pausa ni menú de ajustes.
- Cerrar la ventana termina el proceso.

### Inventario de assets y uso

Ruta base: `android/assets/`.

| Archivo | Tipo | Tamaño | Dónde se usa | Notas |
|---|---|---:|---|---|
| `ballgamer.png` | Texture | 240×207 | Jugador nivel 1 | se estira a 45×45 px |
| `player1.png` | Texture | 240×263 | Jugador nivel 2 | se estira a 45×45 px |
| `floor.png` | Texture | 336×112 | Suelos | se estira a `width×45` px |
| `overfloor.png` | Texture | 64×10 | Franja superior de suelos | 10 % de la altura |
| `finish.png` | Texture | 64×10 | Franja de meta + suelo de meta | |
| `spikes1.png` | Texture | 248×128 | Pinchos | se estira a 45×45 px |
| `bg.png` | Texture | 640×360 | Fondo de menú y game over | |
| `intro.png` | Texture | 494×105 | Logo del menú | |
| `gameover.png` | Texture | 480×105 | Texto de game over | |
| `plybtn.png` | Texture | 104×58 | Botón play/reintentar | se crea con `new Texture` en Menu/GameOver |
| `bground.png` | Texture | 640×360 | Fondo nivel 1 | cargado en manager **y** creado con `new Texture` (duplicado) |
| `bground2.png` | Texture | 640×360 | Fondo nivel 2 | ídem |
| `song1.mp3` | Music | ~1.3 MB | Música nivel 1 | sin `setLooping(true)` → no se repite |
| `song2.mp3` | Music | ~2.8 MB | Música nivel 2 | ídem |
| `jump.mp3` | Sound | ~9.5 KB | Efecto de salto | |
| `death.mp3` | Sound | ~110 KB | Efecto de muerte | volumen 0.40 |
| `skin/uiskin.json` + `default.fnt` + `default.png` | Skin | — | Label "Loading..." | `LoadingScreen` |
| `player3.png`, `player4.png` | Texture | 73×80 / 240×277 | **No se usan** | restos |
| `player5.png` | Texture | 240×216 | Solo `Scene2DScreen` (legado) | no se usa en el juego real |
| `skin/uiskin.atlas`, `skin/uiskin.png` | Skin | — | Soporte del skin | |

Fuera de `android/assets/`: `assets/badlogic.jpg` y copias de `player1/3/4/5.png`, `spikes1.png` **no se usan**.

### Cómo modificar cada cosa (recetas)

#### Cambiar el diseño de un nivel

Edita `GameScreen.show()` o `GameScreen2.show()`:

```java
// FloorEntity(world, texturaSuelo, texturaFranja, x, width, y)
floorList.add(new FloorEntity(world, floorTexture, overfloorTexture, 12, 10, 2));

// SpikeEntity(world, textura, x, y)  -> x = centro, y = base
spikeList.add(new SpikeEntity(world, spikesTexture, 18, 2));

// LevelEntity(world, texturaMeta, x, width, y)  -> y = superficie del trigger
levelList.add(new LevelEntity(world, finishTexture, 105, 10, 1.6));
```

No olvides añadirlo al `Stage` (`stage.addActor(...)`) y equilibrar `detach()`/`dispose()` si creas listas nuevas. Las coordenadas están en metros; la cámara se mueve hacia la derecha, así que los niveles deben crecer en X.

#### Añadir meta al nivel 2

1. Añade un `LevelEntity` en `GameScreen2.show()` y su `stage.addActor(...)`.
2. Copia la rama `"finish"` del `ContactListener` de `GameScreen` a `GameScreen2`.
3. Decide a dónde ir: otro nivel, menú o una pantalla de victoria nueva.

#### Cambiar el “feeling” del salto / velocidad

- `core/src/com/mygdx/game/Constants.java`: `PIXELS_IN_METERS`, `IMPULSE_JUMP`, `PLAYER_SPEED`, `PLAYER_SPEED2`.
- Gravedad: en los constructores de `GameScreen` y `GameScreen2` (`new World(new Vector2(0, -10), true)`).
- Fuerza extra al saltar: `PlayerEntity.act()` / `PlayerEntity2.act()` (`-IMPULSE_JUMP * 1.25f`).
- Velocidad real del jugador: `PlayerEntity` usa `PLAYER_SPEED`; `PlayerEntity2` usa su propio `PLAYER_SPEED2`.
- Si cambias `PIXELS_IN_METERS`, cambias el tamaño visual de todo y el scroll de cámara, pero **no** la física (que está en metros). Ajusta la cámara en consecuencia.

#### Añadir un tipo de entidad/enemigo

1. Crea una clase en `entities/` que extienda `Actor`.
2. En el constructor crea `Body`/`Fixture` con `Shape` de Box2D y asigna `fixture.setUserData("mi_tag")`.
3. Implementa `draw()` (metros → píxeles) y `detach()` (destruye fixture y body).
4. Añade la entidad a la lista y al `Stage` en `show()`.
5. Añade una rama en el `ContactListener` de **cada pantalla** donde deba actuar.
6. Libera el cuerpo en `dispose()`/`hide()`.

#### Añadir o cambiar pantallas

- Extiende `BaseScreen` (o implementa `Screen`).
- Crea la instancia en `MainGame.finishloading()` si depende de assets; si no, en `create()`.
- Navega con `game.setScreen(...)`.
- Para UI, usa `Stage` + `Skin` + actores de Scene2D.UI; recuerda `Gdx.input.setInputProcessor(stage)` en `show()` y limpiarlo en `hide()`.
- Implementa `dispose()` y llama a `dispose()` de la pantalla anterior si la reemplazas de verdad (hoy no se hace).

#### Sustituir arte o audio

- Reemplaza el archivo en `android/assets/` conservando el nombre, o cambia las rutas en `MainGame.create()` y en los `get()`/`new Texture()` correspondientes.
- Formatos actuales: PNG para texturas, MP3 para música/efectos.
- Recuerda que el `AssetManager` cachea por nombre; si cambias un archivo en caliente, reinicia el juego.
- `desktop` lee de `android/assets` (workingDir), no de `assets/`.

#### Cambiar la ventana de escritorio

`desktop/src/com/mygdx/game/desktop/DesktopLauncher.java` usa `LwjglApplicationConfiguration` por defecto (sin título, tamaño ni vsync explícitos). Ahí puedes fijar:

```java
LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
config.title = "CumbiaBall";
config.width = 1280;
config.height = 720;
config.vSyncEnabled = true;
new LwjglApplication(new MainGame(), config);
```

El `FitViewport(640, 360)` mantiene la relación 16:9 y añade barras si la ventana no es 16:9.

#### Ajustar la cámara

En `render()` de cada pantalla:

```java
if (player.getX() > 150 && player.isAlive()) {
    stage.getCamera().translate(Constants.PLAYER_SPEED * delta * Constants.PIXELS_IN_METERS, 0, 0);
}
```

`150` es el umbral en píxeles; el segundo factor debe coincidir con la velocidad horizontal real del jugador para que no se vea “arrastrado”.

### Problemas conocidos y deuda técnica

Fugas / recursos:

- `MainGame` no implementa `dispose()`; el `AssetManager` no se libera.
- `MenuScreen` y `GameOverScreen` crean `new Texture("plybtn.png")` y no la liberan.
- `GameScreen`/`GameScreen2` crean `SpriteBatch` y `new Texture("bground*.png")` y no los liberan.
- Las pantallas anteriores no se `dispose()` al cambiar de pantalla; `finishloading()` crea pantallas nuevas cada reintento.
- `FloorEntity.detach()` no destruye `leftBody`/`leftFixture`.
- `LevelEntity` crea un `leftBody` sin fixture y no lo destruye.
- `GameScreen.dispose()` no recorre `levelList`.
- `Scene2DScreen.dispose()` solo libera una textura.

Lógica / jugabilidad:

- Nivel 2 sin meta ni victoria.
- La música no se reproduce en bucle.
- El sonido de salto se dispara por frame mientras se mantiene pulsado.
- La fuerza descendente mientras `jumping` hace el salto flotante y poco intuitivo.
- El fondo no se desplaza con la cámara y la matriz de proyección se cambia después de dibujar.
- `resize()` no está implementado en las pantallas: el `FitViewport` no se actualiza al redimensionar.
- No hay pausa, `resume`/`pause` reales, ni parada de música al minimizar.
- `world.step(delta, 6, 2)` con delta variable puede ser inestable con FPS bajos; lo habitual es acumular un paso fijo.

Duplicación / mantenibilidad:

- `PlayerEntity` y `PlayerEntity2` son casi idénticas; `PLAYER_SPEED2` está duplicado.
- Los niveles están hardcodeados y repetidos en dos pantallas.
- Los `userData` son strings mágicos; un typo desactiva una colisión sin avisar.
- Código legado (`Box2DScreen`, `scene2D/`) que no se ejecuta y confunde.
- `appName` = `MyJump`, label Android = `MyJump`, repo/README = `CumbiaBall`/`punch-it`.
- No hay tests, CI, lint ni formateo automatizado.
- `android/project.properties` (target android-19) es legado de Eclipse y no lo usa Gradle.

Build:

- El wrapper puede actualizarse por accidente (ya pasó) y romper el arranque por el JDK.
- El proyecto configura `:android` incluso para tareas de escritorio; en esta máquina `:desktop:compileJava` funcionó sin `ANDROID_HOME`, pero compilar Android sí requiere el SDK (platform 30, build-tools 30.0.3).
- `sourceCompatibility = 1.7` está obsoleto; se mantiene por compatibilidad con el stack fijado.

### Si hay que reconstruirlo o hacer otro juego

Qué merece la pena conservar:

- El patrón **Actor + Body + `userData`** es sencillo y suficiente para un plataformas 2D.
- `AssetManager` centralizado y pantalla de carga.
- La estructura de módulos `core` + `desktop` (+ `android` opcional).
- El set de assets y la carpeta `android/assets` como base.

Qué conviene rehacer:

1. **Un solo `Player`** configurable (velocidad, impulso, sprite) en lugar de dos clases.
2. **Niveles data-driven**: un JSON/TOML con `{type, x, y, width, ...}` y un cargador que cree entidades. Así se cambia el nivel sin recompilar lógica.
3. **Router de pantallas** que haga `dispose()` de la anterior y libere recursos; `MainGame.dispose()` que cierre el `AssetManager`.
4. **Fábrica de entidades + constantes** para los tags de colisión (enum o `static final`), y un único manejador de colisiones reutilizado por todos los niveles.
5. **Input explícito**: `justTouched` para salto por flanco y una regla clara para “mantener para saltar al aterrizar”.
6. **Paso de física fijo** con acumulador (`while (acc >= step) { world.step(step, 6, 2); acc -= step; }`).
7. **Cámara con seguimiento** suave y límites, no traducción manual por umbral.
8. **Audio**: música en bucle, volúmenes configurables, sonidos ligados a eventos (no por frame).
9. **Tests headless** para el cargador de niveles y las colisiones (libGDX tiene backend headless).
10. **Decisión de stack**: si se va a mantener Java 11 + Gradle 6.7.1, quedarse en libGDX 1.10.0/LWJGL 2; si se quiere modernizar, planificar el salto a Java 17+, Gradle 8+, libGDX reciente y LWJGL 3.

Preguntas que conviene responder antes de rehacer:

- ¿Solo escritorio o también Android?
- ¿Se mantiene el control de un botón o se añaden teclado/mando?
- ¿Los niveles serán ficheros de datos o código?
- ¿Se quiere editor de niveles, puntuación, checkpoints, vidas?
- ¿Se conserva el arte actual (y por tanto la escala 45 px/m y el viewport 640×360)?

### Entornos de ejecución (resumen)

| Entorno | Comando | Notas |
|---|---|---|
| Windows | `scripts\run.bat` | autodetecta JDK 8–15; ignora `JAVA_HOME` inválido; no necesita `ANDROID_HOME` para `desktop:run` |
| Linux / macOS | `./scripts/run.sh` | macOS añade `-XstartOnFirstThread`; sin `DISPLAY` usa `:1` |
| Codespaces | `./scripts/run.sh` | devcontainer con Java 11 + noVNC (puerto 6080, contraseña `vscode`) |
| Manual | `./gradlew desktop:run` / `gradlew.bat desktop:run` | requiere JDK 8–15 en `JAVA_HOME` o PATH |
| Android (opcional) | `./gradlew android:assembleDebug` | requiere SDK con platform 30 y build-tools 30.0.3; tarea `android:run` usa `adb` |

---

## 🤝 Contribuir

1. Crea una rama: `git checkout -b mi-cambio`.
2. Haz tus cambios y pruébalos con `./scripts/run.sh`.
3. Abre un Pull Request describiendo qué cambiaste y cómo lo probaste.

No actualices Gradle ni las versiones de libGDX sin discutirlo antes: el proyecto está fijado a versiones antiguas a propósito y migrarlo implica cambios mayores.

## 📄 Licencia

El repositorio no incluye un archivo de licencia. Si vas a reutilizar el código o los recursos, consulta antes con el autor.
