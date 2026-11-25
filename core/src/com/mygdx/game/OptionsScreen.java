package com.mygdx.game;

import java.io.File;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class OptionsScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float BUTTON_WIDTH = 360f;
    private static final float BUTTON_HEIGHT = 160f;
    private static final float METRICAS_Y = 494f;
    private static final float REINICIAR_Y = 304f;
    private static final float ACERCA_Y = 114f;
    private static final float SELECTED_SCALE = 1.05f;
    private static final float DIMMED_ALPHA = 0.55f;
    private static final int BUTTON_COUNT = 3;
    private static final int METRICAS_INDEX = 0;
    private static final int REINICIAR_INDEX = 1;
    private static final int ACERCA_INDEX = 2;
    private static final float PROMPT_Y = 680f;
    private static final float PROMPT_HEIGHT = 60f;
    private static final float FONT_SCALE = 2f;
    private static final float READY_DURATION = 1.2f;

    private final Stage stage;
    private final Music music;
    private final Sound optionChange;
    private final Image[] buttons;
    private final BitmapFont font;
    private final Label statusLabel;
    private int selectedIndex;
    private boolean resetPending;
    private boolean readyShowing;
    private float readyTimer;

    public OptionsScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        music = game.getManager().get("common/racing_menu.ogg", Music.class);
        optionChange = game.getManager().get("common/option_change.wav", Sound.class);

        Image background = new Image(texture("common/main_menu.jpg"));
        background.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.addActor(background);

        buttons = new Image[BUTTON_COUNT];
        buttons[ACERCA_INDEX] = createButton(texture("common/acerca.jpg"), ACERCA_Y);
        buttons[REINICIAR_INDEX] = createButton(texture("common/reinicio.jpg"), REINICIAR_Y);
        buttons[METRICAS_INDEX] = createButton(texture("common/metricas.jpg"), METRICAS_Y);

        for (int i = 0; i < BUTTON_COUNT; i++) {
            stage.addActor(buttons[i]);
        }

        font = new BitmapFont();
        statusLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        statusLabel.setAlignment(Align.center);
        statusLabel.setBounds(0, PROMPT_Y, WORLD_WIDTH, PROMPT_HEIGHT);
        statusLabel.setFontScale(FONT_SCALE);
        statusLabel.setVisible(false);
        stage.addActor(statusLabel);

        updateSelection();
    }

    private Texture texture(String path) {
        return game.getManager().get(path, Texture.class);
    }

    private Image createButton(Texture texture, float y) {
        Image button = new Image(new TextureRegionDrawable(texture));
        button.setBounds((WORLD_WIDTH - BUTTON_WIDTH) / 2f, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setOrigin(Align.center);
        return button;
    }

    private void updateSelection() {
        for (int i = 0; i < BUTTON_COUNT; i++) {
            buttons[i].setScale(i == selectedIndex ? SELECTED_SCALE : 1f);
            buttons[i].getColor().set(1f, 1f, 1f, i == selectedIndex ? 1f : DIMMED_ALPHA);
        }
        if (resetPending) {
            buttons[REINICIAR_INDEX].getColor().set(1f, 0.45f, 0.2f, 1f);
        }
    }

    private void moveSelection(int direction) {
        selectedIndex = (selectedIndex + direction + BUTTON_COUNT) % BUTTON_COUNT;
        optionChange.play(0.75f);
        cancelPending();
        updateSelection();
    }

    private void cancelPending() {
        if (resetPending) {
            resetPending = false;
            statusLabel.setVisible(false);
        }
    }

    private void activateSelection() {
        if (selectedIndex == METRICAS_INDEX) {
            optionChange.play(0.75f);
            GestorMetricas.abrirPaginaMetricas();
            Gdx.app.exit();
        } else if (selectedIndex == ACERCA_INDEX) {
            optionChange.play(0.75f);
            openHtml("artifacts/acerca.html");
            closeAfterOpening();
        } else {
            activateReset();
        }
    }

    private void activateReset() {
        if (!resetPending) {
            resetPending = true;
            readyShowing = false;
            readyTimer = 0f;
            optionChange.play(0.75f);
            statusLabel.setText("SEGURO? ENTER OTRA VEZ");
            statusLabel.setVisible(true);
            updateSelection();
        } else {
            GestorMetricas.reiniciarMetricas();
            resetPending = false;
            readyShowing = true;
            readyTimer = READY_DURATION;
            optionChange.play(0.75f);
            statusLabel.setText("LISTO");
            statusLabel.setVisible(true);
            updateSelection();
        }
    }

    private void openHtml(String path) {
        File file = materializeFile(path);
        Gdx.net.openURI(file.toURI().toString());
    }

    @Override
    public void show() {
        selectedIndex = METRICAS_INDEX;
        resetPending = false;
        readyShowing = false;
        readyTimer = 0f;
        statusLabel.setText("");
        statusLabel.setVisible(false);
        music.setLooping(true);
        music.setVolume(0.75f);
        if (!music.isPlaying()) {
            music.play();
        }
        updateSelection();
    }

    @Override
    public void hide() {
        optionChange.stop();
        music.stop();
    }

    @Override
    public void render(float delta) {
        if (readyShowing) {
            readyTimer -= delta;
            if (readyTimer <= 0f) {
                readyShowing = false;
                readyTimer = 0f;
                if (!resetPending) {
                    statusLabel.setVisible(false);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (resetPending) {
                cancelPending();
                updateSelection();
            } else {
                game.setScreen(game.getMainMenuScreen());
                return;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            moveSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            moveSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            activateSelection();
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        hide();
        font.dispose();
        stage.dispose();
    }

    /**
     * Devuelve una copia en disco del recurso indicado. Cuando el juego se ejecuta
     * desde el jar, el archivo solo existe dentro del jar: se extrae junto al
     * ejecutable para que el navegador pueda abrirlo.
     */
    private File materializeFile(String path) {
        FileHandle local = Gdx.files.local(path);
        if (local.exists()) {
            return local.file().getAbsoluteFile();
        }

        FileHandle internal = Gdx.files.internal(path);
        try {
            FileHandle sourceDir = internal.parent();
            FileHandle targetDir = Gdx.files.local(sourceDir.path());
            if (!targetDir.exists()) {
                targetDir.mkdirs();
                if (!targetDir.exists()) {
                    Gdx.app.error("OptionsScreen", "No se pudo crear " + targetDir.path());
                }
            }
            for (FileHandle child : sourceDir.list()) {
                FileHandle target = targetDir.child(child.name());
                if (!target.exists()) {
                    child.copyTo(target);
                }
            }
        } catch (RuntimeException e) {
            Gdx.app.error("OptionsScreen", "No se pudo extraer " + path, e);
        }

        FileHandle copied = Gdx.files.local(path);
        return copied.exists() ? copied.file().getAbsoluteFile() : internal.file().getAbsoluteFile();
    }

    /**
     * Cierra el juego unos segundos después de abrir el HTML: si se llama a
     * Gdx.app.exit() de inmediato, el proceso muere antes de que Windows llegue
     * a lanzar el navegador y la pagina nunca aparece.
     */
    private void closeAfterOpening() {
        Thread closer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Gdx.app.exit();
            }
        }, "close-after-html");
        closer.setDaemon(true);
        closer.start();
    }
}
