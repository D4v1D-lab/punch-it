package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class VehicleSelectScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float ICON_HEIGHT = 160f;
    private static final float ICON_Y = 30f;
    private static final float ICON_CENTER_SPACING = 240f;
    private static final float SELECTED_SCALE = 1.05f;
    private static final float DIMMED_ALPHA = 0.55f;
    private static final int CAR_INDEX = 0;
    private static final int PLANE_INDEX = 1;
    private static final int BOAT_INDEX = 2;

    private final Stage stage;
    private final Music music;
    private final Sound optionChange;
    private final Image car;
    private final Image plane;
    private final Image boat;
    private int selectedIndex = CAR_INDEX;

    public VehicleSelectScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        music = game.getManager().get("common/racing_menu.ogg", Music.class);
        optionChange = game.getManager().get("common/option_change.wav", Sound.class);

        Image background = new Image(game.getManager().get("common/main_menu.jpg", Texture.class));
        background.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.addActor(background);

        Texture carTexture = game.getManager().get("cars/carro.png", Texture.class);
        TextureRegion carRegion = new TextureRegion(carTexture, 190, 134, 120, 227);
        car = createIcon(carRegion, centerX(CAR_INDEX));

        Texture planeTexture = game.getManager().get("plane/avion.png", Texture.class);
        TextureRegion planeRegion = new TextureRegion(planeTexture, 256, 287, 512, 490);
        plane = createIcon(planeRegion, centerX(PLANE_INDEX));

        Texture boatTexture = game.getManager().get("boat/bote.png", Texture.class);
        TextureRegion boatRegion = new TextureRegion(boatTexture, 179, 80, 142, 341);
        boat = createIcon(boatRegion, centerX(BOAT_INDEX));

        stage.addActor(car);
        stage.addActor(plane);
        stage.addActor(boat);
        updateSelection();
    }

    private float centerX(int index) {
        return WORLD_WIDTH / 2f + (index - 1) * ICON_CENTER_SPACING;
    }

    private Image createIcon(TextureRegion region, float centerX) {
        Image icon = new Image(new TextureRegionDrawable(region));
        float width = ICON_HEIGHT * region.getRegionWidth() / region.getRegionHeight();
        icon.setBounds(centerX - width / 2f, ICON_Y, width, ICON_HEIGHT);
        icon.setOrigin(Align.center);
        return icon;
    }

    private void updateSelection() {
        updateIcon(car, CAR_INDEX);
        updateIcon(plane, PLANE_INDEX);
        updateIcon(boat, BOAT_INDEX);
    }

    private void updateIcon(Image icon, int index) {
        icon.setScale(selectedIndex == index ? SELECTED_SCALE : 1f);
        icon.getColor().a = selectedIndex == index ? 1f : DIMMED_ALPHA;
    }

    private void select(int index) {
        if (selectedIndex == index) {
            return;
        }
        selectedIndex = index;
        optionChange.play();
        updateSelection();
    }

    private void activateSelection() {
        if (selectedIndex == CAR_INDEX) {
            game.setScreen(game.getCarGiftScreen());
        } else if (selectedIndex == PLANE_INDEX) {
            game.setScreen(game.getPlaneGiftScreen());
        } else {
            game.setScreen(game.getBoatGiftScreen());
        }
    }

    @Override
    public void show() {
        music.setLooping(true);
        music.setVolume(0.75f);
        if (!music.isPlaying()) {
            music.play();
        }
        updateSelection();
    }

    @Override
    public void hide() {
        music.stop();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            select(Math.max(CAR_INDEX, selectedIndex - 1));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            select(Math.min(BOAT_INDEX, selectedIndex + 1));
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
        stage.dispose();
    }
}
