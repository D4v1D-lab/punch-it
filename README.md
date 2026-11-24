# Punch It

Juego 2D de esquiva y carreras hecho con [libGDX](https://libgdx.com/). Eliges entre un coche, un avión, un bote o un tren, cambias de carril para esquivar obstáculos y completas una carrera de 75 segundos. Cada vehículo tiene su propio escenario y su animación final.

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

- **Menú principal:** flechas arriba/abajo o izquierda/derecha para mover la selección; `Enter` para confirmar. `Esc` cierra el juego.
- **Opciones:** flechas para elegir Métricas, Reiniciar o Acerca de; `Enter` activa la opción. Métricas abre el informe generado en el navegador, Reiniciar pide confirmación con un segundo `Enter` y Acerca abre la dedicatoria; `Esc` vuelve al menú principal.
- **Selección de vehículo:** izquierda/arriba selecciona el vehículo anterior; derecha/abajo el siguiente. El orden es coche, avión, bote y tren; `Enter` empieza. `Esc` vuelve al menú principal.
- **Durante la partida:** `Enter` cambia de carril.
  - El cambio prefiere un carril adyacente libre.
  - En los carriles extremos siempre te mueve hacia dentro.
  - No hay control con ratón.
- **Pausa:** `Esc` durante la carrera abre el menú de pausa. **Resumir** está seleccionado por defecto; `Enter` continúa. Con **Menú** vuelves a la selección de vehículo.
- Si chocas con un obstáculo, el vehículo parpadea brevemente; no hay game over y la partida continúa.

## Métricas de accesibilidad

Punch It guarda automáticamente métricas entre sesiones mediante `Preferences`: pulsaciones de ENTER, tiempo de retención, obstáculos esquivados y choques, racha máxima, anticipación media y tiempo jugado por vehículo.

Desde **Opciones → Métricas** se genera un informe local y se abre en el navegador. El informe y su guía se escriben como `punch_it_metricas.html` y `punch_it_guia.html` en el directorio de ejecución, usando enlaces relativos entre ambos. **Reiniciar** borra las métricas con una confirmación de dos pasos.

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

### Bote

- Escenario de mar con olas en movimiento.
- Obstáculos marinos que aparecen cada 4 segundos.
- Introducción con bocina de barco y música de motor en bucle durante la carrera.
- Al terminar los 75 segundos se reproduce la animación final del bote y se vuelve al menú de vehículos.

### Tren

- Escenario de vía terrestre con fondo en movimiento.
- Obstáculos que aparecen cada 4 segundos.
- Introducción con bocina de tren y sonido de motor en bucle durante la carrera.
- Al terminar los 75 segundos se reproduce la animación final del tren y se vuelve al menú de vehículos.

## Estructura del proyecto

```text
assets/
├── cars/       # Recursos exclusivos del coche
├── common/     # Menú, pantalla de carga y recursos compartidos
├── artifacts/  # Plantillas HTML del informe de métricas
├── plane/      # Recursos exclusivos del avión
├── boat/       # Recursos exclusivos del bote
└── train/      # Recursos exclusivos del tren

core/src/com/mygdx/game/
├── PunchItGame.java        # AssetManager y creación de pantallas
├── MainMenuScreen.java     # Menú principal
├── OptionsScreen.java      # Acerca de / Reiniciar / Métricas
├── GestorMetricas.java     # Métricas persistentes y generación HTML
├── MetricasInputListener.java # Pulsaciones de ENTER para métricas
├── VehicleSelectScreen.java# Selección coche/avión/bote/tren
├── VehicleType.java        # Enumeración de vehículos
├── GiftScreen.java         # Transición compartida de carga
├── PauseMenu.java          # Menú de pausa (Resumir / Menú)
├── GameScreen.java         # Modo coche
├── AirplaneScreen.java     # Modo avión
├── BoatScreen.java         # Modo bote
├── TrainScreen.java        # Modo tren
└── BaseScreen.java         # Base común de pantallas

desktop/src/com/mygdx/game/desktop/
└── DesktopLauncher.java    # Arranque de escritorio (LWJGL 2)

scripts/
└── run.bat                 # Lanzador para Windows
```

## Recursos y animaciones

libGDX no puede cargar GIFs directamente, así que las animaciones se guardan como hojas PNG:

- `assets/common/load_gift_sheet.png`: transición de carga compartida.
- `assets/common/resumir.png` y `assets/common/menu.png`: botones del menú de pausa.
- `assets/common/acerca.jpg`, `assets/common/metricas.jpg`, `assets/common/reinicio.jpg`: botones de Opciones.
- `assets/artifacts/acerca.html`: página de dedicatoria de Acerca de.
- `assets/artifacts/punch_it_metricas.html` y `assets/artifacts/punch_it_guia.html`: plantillas del informe de métricas. El juego escribe `punch_it_metricas.html` y `punch_it_guia.html` en el directorio local de ejecución y abre el primero en el navegador.
- `assets/cars/car_ending_sheet.png`: final del coche.
- `assets/plane/avion_ending_sheet.png`: final del avión.
- `assets/boat/bote_ending_sheet.png`: final del bote.
- `assets/train/tren_ending_sheet.png`: final del tren (generado a partir de `tren_ending.gif`, que no se carga en el juego).

Los fondos, sprites y audios de cada modo viven en su carpeta correspondiente. Las claves del `AssetManager` deben incluir siempre el prefijo (`cars/`, `common/`, `plane/`, `boat/` o `train/`).

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
