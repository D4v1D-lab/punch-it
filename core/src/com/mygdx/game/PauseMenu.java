package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class PauseMenu {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_RESUME = 1;
    public static final int ACTION_MENU = 2;

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float BUTTON_WIDTH = 360f;
    private static final float BUTTON_HEIGHT = 160f;
    private static final float RESUME_Y = 430f;
    private static final float MENU_Y = 170f;
    private static final float SELECTED_SCALE = 1.05f;
    private static final float DIMMED_ALPHA = 0.55f;
    private static final float SCRIM_ALPHA = 0.6f;

    private final Stage stage;
    private final Texture scrimTexture;
    private final Sound optionChange;
    private final Image resumeButton;
    private final Image menuButton;
    private int selectedIndex;

    public PauseMenu(PunchItGame game) {
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        optionChange = game.getManager().get("common/option_change.wav", Sound.class);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        scrimTexture = new Texture(pixmap);
        pixmap.dispose();

        Image scrim = new Image(new TextureRegionDrawable(new TextureRegion(scrimTexture)));
        scrim.setBounds(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        scrim.setColor(0f, 0f, 0f, SCRIM_ALPHA);
        stage.addActor(scrim);

        Texture resumeTexture = game.getManager().get("common/resumir.png", Texture.class);
        TextureRegion resumeRegion = new TextureRegion(resumeTexture, 39, 155, 422, 190);
        resumeButton = createButton(resumeRegion, RESUME_Y);

        Texture menuTexture = game.getManager().get("common/menu.png", Texture.class);
        TextureRegion menuRegion = new TextureRegion(menuTexture, 79, 155, 342, 191);
        menuButton = createButton(menuRegion, MENU_Y);

        stage.addActor(resumeButton);
        stage.addActor(menuButton);
        updateSelection();
    }

    private Image createButton(TextureRegion region, float y) {
        Image button = new Image(new TextureRegionDrawable(region));
        button.setBounds((WORLD_WIDTH - BUTTON_WIDTH) / 2f, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setOrigin(Align.center);
        return button;
    }

    public void show() {
        selectedIndex = 0;
        updateSelection();
    }

    public void hide() {
        optionChange.stop();
    }

    public int update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            moveSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            moveSelection(1);
        }
        stage.act(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return selectedIndex == 0 ? ACTION_RESUME : ACTION_MENU;
        }
        return ACTION_NONE;
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    private void moveSelection(int direction) {
        selectedIndex = (selectedIndex + direction + 2) % 2;
        optionChange.play(0.75f);
        updateSelection();
    }

    private void updateSelection() {
        resumeButton.setScale(selectedIndex == 0 ? SELECTED_SCALE : 1f);
        menuButton.setScale(selectedIndex == 1 ? SELECTED_SCALE : 1f);
        resumeButton.getColor().a = selectedIndex == 0 ? 1f : DIMMED_ALPHA;
        menuButton.getColor().a = selectedIndex == 1 ? 1f : DIMMED_ALPHA;
    }

    public void dispose() {
        stage.dispose();
        scrimTexture.dispose();
    }
}
