# Punch It

Juego 2D de esquiva y carreras hecho con [libGDX](https://libgdx.com/). Eliges entre un coche y un avión, cambias de carril para esquivar obstáculos y completas una carrera de 75 segundos. Cada vehículo tiene su propio escenario y su animación final.

## Requisitos

| Qué necesitas | Detalle |
|---|---|
| Sistema | **Windows** (el proyecto está configurado para escritorio) |
| Java | **JDK 8 a 15**. La combinación probada es **Temurin 11** |
| Gradle | No hace falta instalarlo; el repo incluye el wrapper |
| Git | Para clonar el repositorio |

> No uses Java 16 o superior. El proyecto fija Gradle 6.7.1 y libGDX 1.10.0; el build falla deliberadamente con versiones de Java no soportadas.

## Cómo ejecutar

Desde PowerShell, en la raíz del proyecto:

```powershell
scripts\run.bat
```

El lanzador busca automáticamente un JDK compatible (8–15) y abre el juego en pantalla completa. Para cerrarlo, usa `Alt+F4`.

Alternativas:

```powershell
# Ejecutar directamente con Gradle
.\gradlew.bat desktop:run

# Comprobar que compila sin abrir el juego
.\gradlew.bat :desktop:compileJava
```

La primera ejecución puede tardar mientras Gradle descarga las dependencias.

Si el juego ya está abierto, ciérralo antes de lanzar cualquier comando de Gradle. Un proceso `desktop:run` activo puede dejar el daemon ocupado y hacer que el siguiente build se quede esperando.

## Controles

- **Menú principal:** flechas arriba/abajo o izquierda/derecha para mover la selección; `Enter` para confirmar.
- **Selección de vehículo:** izquierda/arriba selecciona el coche; derecha/abajo selecciona el avión; `Enter` empieza.
- **Durante la partida:** `Enter` cambia de carril.
  - El cambio prefiere un carril adyacente libre.
  - En los carriles extremos siempre te mueve hacia dentro.
  - No hay control con ratón.
- Si chocas con un obstáculo, el vehículo parpadea brevemente; no hay game over y la partida continúa.

## Modos de juego

### Coche

- Escenario de carretera con césped en movimiento.
- Conos que aparecen en carriles aleatorios cada 4 segundos.
- Introducción con bocina, música de motor en bucle durante la carrera.
- Al terminar los 75 segundos se reproduce la animación final del coche y se vuelve al menú de vehículos.

### Avión

- Escenario de cielo con nubes en movimiento.
- Obstáculos aéreos que aparecen cada 4 segundos.
- La carrera empieza directamente después de la pantalla de carga; no tiene introducción propia ni bocina.
- Al terminar los 75 segundos se reproduce la animación final del avión y se vuelve al menú de vehículos.

## Estructura del proyecto

```text
assets/
├── cars/       # Recursos exclusivos del coche
├── common/     # Menú, pantalla de carga y recursos compartidos
├── plane/      # Recursos exclusivos del avión
└── boat/       # Reservado; vacío por ahora

core/src/com/mygdx/game/
├── PunchItGame.java        # AssetManager y creación de pantallas
├── MainMenuScreen.java     # Menú principal
├── VehicleSelectScreen.java# Selección coche/avión
├── GiftScreen.java         # Transición compartida de carga
├── GameScreen.java         # Modo coche
├── AirplaneScreen.java     # Modo avión
└── BaseScreen.java         # Base común de pantallas

desktop/src/com/mygdx/game/desktop/
└── DesktopLauncher.java    # Arranque de escritorio (LWJGL 2)

scripts/
└── run.bat                 # Lanzador para Windows
```

## Recursos y animaciones

libGDX no puede cargar GIFs directamente, así que las animaciones se guardan como hojas PNG:

- `assets/common/load_gift_sheet.png`: transición de carga compartida.
- `assets/cars/car_ending_sheet.png`: final del coche.
- `assets/plane/avion_ending_sheet.png`: final del avión.
- `assets/plane/avion_ending.gif`: GIF original del final del avión; no se carga en el juego.

Los fondos, sprites y audios de cada modo viven en su carpeta correspondiente. Las claves del `AssetManager` deben incluir siempre el prefijo (`cars/`, `common/` o `plane/`).

## Notas técnicas

- **libGDX 1.10.0**, **Gradle 6.7.1** y **LWJGL 2**.
- Ventana en pantalla completa con `StretchViewport(1376×768)`.
- Código compatible con Java 7 (`sourceCompatibility = 1.7`).
- Solo existen los módulos `:core` y `:desktop`; no hay objetivos Android ni de otras plataformas.
- Box2D está incluido como dependencia, pero actualmente no se usa.
- No hay tests, lint ni CI. La verificación es compilar y abrir el juego.
- En JDK 11 es normal ver una advertencia de *illegal reflective access* de LWJGL 2; no impide jugar.

## Problemas frecuentes

- **`Unsupported class file major version` o error de Gradle al arrancar:** estás usando Java 16 o superior. Instala Temurin 11 y vuelve a ejecutar `scripts\run.bat`.
- **El build se queda colgado:** cierra el juego y cualquier proceso `DesktopLauncher` antes de volver a compilar.
- **No se oye audio:** comprueba la salida de audio del sistema; el juego no silencia los sonidos de forma intencionada.

## Licencia

Este repositorio no incluye un archivo de licencia. Si vas a reutilizar el código o los recursos, consulta antes con el autor.
