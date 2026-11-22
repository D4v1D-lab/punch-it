package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class GiftScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float DURATION = 5f;
    private static final int FRAME_COLS = 8;
    private static final int FRAME_ROWS = 10;
    private static final int FRAME_COUNT = 75;
    private static final float FRAME_DURATION = 1f / 15f;

    private final Stage stage;
    private final TextureRegionDrawable drawable;
    private final Animation<TextureRegion> animation;
    private final Sound carStart;
    private final Sound avionStart;
    private final boolean plane;
    private float elapsed;

    public GiftScreen(PunchItGame game, boolean plane) {
        super(game);
        this.plane = plane;
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        carStart = game.getManager().get("cars/car_start.mp3", Sound.class);
        avionStart = game.getManager().get("plane/avion_start.mp3", Sound.class);

        Texture sheet = game.getManager().get("common/load_gift_sheet.png", Texture.class);
        int frameWidth = sheet.getWidth() / FRAME_COLS;
        int frameHeight = sheet.getHeight() / FRAME_ROWS;
        TextureRegion[][] grid = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        int index = 0;
        for (int row = 0; row < FRAME_ROWS; row++) {
            for (int col = 0; col < FRAME_COLS; col++) {
                if (index < FRAME_COUNT) {
                    frames[index++] = grid[row][col];
                }
            }
        }

        animation = new Animation<TextureRegion>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        drawable = new TextureRegionDrawable(frames[0]);
        Image image = new Image(drawable);
        image.setBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        stage.addActor(image);
    }

    @Override
    public void show() {
        elapsed = 0f;
        drawable.setRegion(animation.getKeyFrame(0f));
        carStart.stop();
        avionStart.stop();
        if (plane) {
            avionStart.play();
        } else {
            carStart.play();
        }
    }

    @Override
    public void hide() {
        carStart.stop();
        avionStart.stop();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        drawable.setRegion(animation.getKeyFrame(elapsed));

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (elapsed >= DURATION) {
            carStart.stop();
            avionStart.stop();
            game.setScreen(plane ? game.getAirplaneScreen() : game.getGameScreen());
        }
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
