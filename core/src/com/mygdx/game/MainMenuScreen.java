package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class MainMenuScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float BUTTON_WIDTH = 360f;
    private static final float PLAY_Y = 320f;
    private static final float OPTIONS_Y = 130f;
    private static final float SELECTED_SCALE = 1.05f;
    private static final float DIMMED_ALPHA = 0.55f;

    private final Stage stage;
    private final Music music;
    private final Sound optionChange;
    private final Image playButton;
    private final Image optionsButton;
    private int selectedIndex = 0;

    public MainMenuScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        music = game.getManager().get("common/journey_awaits.mp3", Music.class);
        optionChange = game.getManager().get("common/option_change.wav", Sound.class);

        Image background = new Image(game.getManager().get("common/main_menu.jpg", Texture.class));
        background.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.addActor(background);

        playButton = createButton(game.getManager().get("common/jugar_hover.jpg", Texture.class));
        optionsButton = createButton(game.getManager().get("common/opciones.jpg", Texture.class));

        float buttonX = (WORLD_WIDTH - BUTTON_WIDTH) / 2f;
        playButton.setPosition(buttonX, PLAY_Y);
        optionsButton.setPosition(buttonX, OPTIONS_Y);

        stage.addActor(playButton);
        stage.addActor(optionsButton);

        updateSelection();
    }

    private Image createButton(Texture texture) {
        Image button = new Image(new TextureRegionDrawable(texture));
        float height = BUTTON_WIDTH * texture.getHeight() / texture.getWidth();
        button.setSize(BUTTON_WIDTH, height);
        button.setOrigin(Align.center);
        return button;
    }

    private void updateSelection() {
        playButton.setScale(selectedIndex == 0 ? SELECTED_SCALE : 1f);
        optionsButton.setScale(selectedIndex == 1 ? SELECTED_SCALE : 1f);
        playButton.getColor().a = selectedIndex == 0 ? 1f : DIMMED_ALPHA;
        optionsButton.getColor().a = selectedIndex == 1 ? 1f : DIMMED_ALPHA;
    }

    private void moveSelection(int direction) {
        selectedIndex = (selectedIndex + direction + 2) % 2;
        optionChange.play();
        updateSelection();
    }

    private void activateSelection() {
        if (selectedIndex == 0) {
            music.stop();
            game.setScreen(game.getVehicleSelectScreen());
        }
    }

    @Override
    public void show() {
        music.setLooping(true);
        music.setVolume(0.5f);
        if (!music.isPlaying()) {
            music.play();
        }
    }

    @Override
    public void hide() {
        music.stop();
    }

    @Override
    public void render(float delta) {
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
        stage.dispose();
    }
}
