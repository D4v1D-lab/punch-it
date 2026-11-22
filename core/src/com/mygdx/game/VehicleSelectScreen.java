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
    private static final float ICON_HEIGHT = 200f;
    private static final float ICON_Y = 50f;
    private static final float ICON_OFFSET_X = 130f;
    private static final float SELECTED_SCALE = 1.05f;
    private static final float DIMMED_ALPHA = 0.55f;
    private static final int CAR_INDEX = 0;
    private static final int PLANE_INDEX = 1;

    private final Stage stage;
    private final Music music;
    private final Sound optionChange;
    private final Image car;
    private final Image plane;
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
        car = createIcon(carRegion, WORLD_WIDTH / 2f - ICON_OFFSET_X);

        Texture planeTexture = game.getManager().get("plane/avion.png", Texture.class);
        TextureRegion planeRegion = new TextureRegion(planeTexture, 256, 287, 512, 490);
        plane = createIcon(planeRegion, WORLD_WIDTH / 2f + ICON_OFFSET_X);

        stage.addActor(car);
        stage.addActor(plane);
        updateSelection();
    }

    private Image createIcon(TextureRegion region, float centerX) {
        Image icon = new Image(new TextureRegionDrawable(region));
        float width = ICON_HEIGHT * region.getRegionWidth() / region.getRegionHeight();
        icon.setBounds(centerX - width / 2f, ICON_Y, width, ICON_HEIGHT);
        icon.setOrigin(Align.center);
        return icon;
    }

    private void updateSelection() {
        car.setScale(selectedIndex == CAR_INDEX ? SELECTED_SCALE : 1f);
        plane.setScale(selectedIndex == PLANE_INDEX ? SELECTED_SCALE : 1f);
        car.getColor().a = selectedIndex == CAR_INDEX ? 1f : DIMMED_ALPHA;
        plane.getColor().a = selectedIndex == PLANE_INDEX ? 1f : DIMMED_ALPHA;
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
            game.setScreen(game.getGiftScreen());
        } else {
            game.setScreen(game.getPlaneGiftScreen());
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
            select(CAR_INDEX);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            select(PLANE_INDEX);
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
