package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class BoatScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float WATER_HEIGHT = 768f;
    private static final float SCROLL_SPEED = 90f;
    private static final float BOAT_WIDTH = 80f;
    private static final float BOAT_Y = 45f;
    private static final int LANE_COUNT = 4;
    private static final int START_LANE = 1;
    private static final float LANE_SLIDE_SPEED = 700f;
    private static final float OBSTACLE_WIDTH = 60f;
    private static final float OBSTACLE_SPAWN_INTERVAL = 4f;
    private static final float LANE_CLEAR_MARGIN = 100f;
    private static final float RUN_DURATION = 75f;
    private static final float BLINK_DURATION = 0.5f;
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float HORN_TIMEOUT = 2.5f;
    private static final float DRIFT_DURATION = 1.75f;
    private static final float BOTE_DRIFT_VOLUME = 0.4f;
    private static final float ROAD_BORDER_TEX_PX = 20f;
    private static final float ROAD_ASPHALT_TEX_PX = 454f;
    private static final int ENDING_FRAME_COLS = 15;
    private static final int ENDING_FRAME_ROWS = 10;
    private static final int ENDING_FRAME_COUNT = 150;
    private static final float ENDING_FRAME_DURATION = 1f / 15f;

    private enum Phase {
        INTRO, DRIVING, ENDED
    }

    private static final class Obstacle {
        final Image image;
        final int lane;
        boolean alreadyHit;

        Obstacle(Image image, int lane) {
            this.image = image;
            this.lane = lane;
        }
    }

    private final Stage stage;
    private final Image waterA;
    private final Image waterB;
    private final Image boat;
    private final Music boatHorn;
    private final Sound boatDrive;
    private final Sound boatDrift;
    private final TextureRegionDrawable obstacleDrawable;
    private final Image endingImage;
    private final TextureRegionDrawable endingDrawable;
    private final Animation<TextureRegion> endingAnimation;
    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private final Rectangle boatRect = new Rectangle();
    private final Rectangle obstacleRect = new Rectangle();
    private final float asphaltLeft;
    private final float laneWidth;

    private Phase phase = Phase.INTRO;
    private float scrollOffset;
    private float boatX;
    private float targetX;
    private int laneIndex;
    private float spawnTimer;
    private float runTimer;
    private float introElapsed;
    private boolean hornStarted;
    private boolean blinking;
    private float blinkTimer;
    private float blinkToggleTimer;
    private float endingElapsed;
    private boolean endingHornStarted;
    private boolean endingDriftStarted;
    private boolean endingDriveStarted;
    private float endingDriveStartAt;

    public BoatScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        Texture waterTexture = game.getManager().get("boat/mosaico_mar.jpg", Texture.class);
        waterA = new Image(waterTexture);
        waterA.setBounds(0, 0, WORLD_WIDTH, WATER_HEIGHT);
        waterB = new Image(waterTexture);
        waterB.setBounds(0, 0, WORLD_WIDTH, WATER_HEIGHT);
        stage.addActor(waterA);
        stage.addActor(waterB);

        Texture roadTexture = game.getManager().get("boat/road_mar.jpg", Texture.class);
        TextureRegion roadRegion = new TextureRegion(roadTexture);
        Image road = new Image(new TextureRegionDrawable(roadRegion));
        float roadWidth = WORLD_HEIGHT * roadTexture.getWidth() / roadTexture.getHeight();
        float roadLeft = (WORLD_WIDTH - roadWidth) / 2f;
        road.setBounds(roadLeft, 0, roadWidth, WORLD_HEIGHT);
        stage.addActor(road);

        float textureScale = roadWidth / roadTexture.getWidth();
        asphaltLeft = roadLeft + ROAD_BORDER_TEX_PX * textureScale;
        laneWidth = ROAD_ASPHALT_TEX_PX * textureScale / LANE_COUNT;

        Texture boteTexture = game.getManager().get("boat/bote.png", Texture.class);
        TextureRegion boteRegion = new TextureRegion(boteTexture, 179, 80, 142, 341);
        boat = new Image(new TextureRegionDrawable(boteRegion));
        float boatHeight = BOAT_WIDTH * boteRegion.getRegionHeight() / boteRegion.getRegionWidth();
        boat.setBounds(0, BOAT_Y, BOAT_WIDTH, boatHeight);
        stage.addActor(boat);

        Texture conoTexture = game.getManager().get("boat/cono_mar.png", Texture.class);
        obstacleDrawable = new TextureRegionDrawable(new TextureRegion(conoTexture, 199, 144, 102, 212));

        Texture endingSheet = game.getManager().get("boat/bote_ending_sheet.png", Texture.class);
        int endingFrameWidth = endingSheet.getWidth() / ENDING_FRAME_COLS;
        int endingFrameHeight = endingSheet.getHeight() / ENDING_FRAME_ROWS;
        TextureRegion[][] endingGrid = TextureRegion.split(endingSheet, endingFrameWidth, endingFrameHeight);
        TextureRegion[] endingFrames = new TextureRegion[ENDING_FRAME_COUNT];
        int endingIndex = 0;
        for (int row = 0; row < ENDING_FRAME_ROWS; row++) {
            for (int col = 0; col < ENDING_FRAME_COLS; col++) {
                if (endingIndex < ENDING_FRAME_COUNT) {
                    endingFrames[endingIndex++] = endingGrid[row][col];
                }
            }
        }
        endingAnimation = new Animation<TextureRegion>(ENDING_FRAME_DURATION, endingFrames);
        endingAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        endingDrawable = new TextureRegionDrawable(endingFrames[0]);
        endingImage = new Image(endingDrawable);
        float endingWidth = WORLD_HEIGHT * endingFrameWidth / endingFrameHeight;
        endingImage.setBounds((WORLD_WIDTH - endingWidth) / 2f, 0f, endingWidth, WORLD_HEIGHT);
        endingImage.setVisible(false);
        stage.addActor(endingImage);

        boatHorn = game.getManager().get("boat/bote_horn.mp3", Music.class);
        boatDrive = game.getManager().get("boat/bote_drive.mp3", Sound.class);
        boatDrift = game.getManager().get("boat/bote_drift.mp3", Sound.class);
        boatHorn.setLooping(false);
        boatHorn.setVolume(0.5f);

        reset();
    }

    private void reset() {
        boatHorn.stop();
        boatDrive.stop();
        boatDrift.stop();

        for (int i = 0; i < obstacles.size; i++) {
            obstacles.get(i).image.remove();
        }
        obstacles.clear();

        scrollOffset = 0f;
        updateWaterPositions();

        laneIndex = START_LANE;
        boatX = laneCenterX(laneIndex) - BOAT_WIDTH / 2f;
        targetX = boatX;
        boat.setX(boatX);
        boat.setVisible(true);

        spawnTimer = 0f;
        runTimer = 0f;
        introElapsed = 0f;
        hornStarted = false;
        blinking = false;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        endingElapsed = 0f;
        endingHornStarted = false;
        endingDriftStarted = false;
        endingDriveStarted = false;
        endingDriveStartAt = 0f;
        endingImage.setVisible(false);
        phase = Phase.INTRO;
    }

    @Override
    public void show() {
        reset();
        boatHorn.play();
    }

    @Override
    public void hide() {
        boatHorn.stop();
        boatDrive.stop();
        boatDrift.stop();
    }

    @Override
    public void render(float delta) {
        if (phase != Phase.ENDED) {
            handleInput();
            updateBoatSlide(delta);
        }

        if (phase == Phase.INTRO) {
            updateIntro(delta);
        } else if (phase == Phase.DRIVING) {
            updateDriving(delta);
        } else if (phase == Phase.ENDED && updateEnding(delta)) {
            return;
        }

        if (blinking && phase != Phase.ENDED) {
            updateBlink(delta);
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            moveToSafeLane();
        }
    }

    private void moveToLane(int lane) {
        laneIndex = lane;
        targetX = laneCenterX(lane) - BOAT_WIDTH / 2f;
        boatDrift.stop();
        boatDrift.play(BOTE_DRIFT_VOLUME);
    }

    private void moveToSafeLane() {
        if (laneIndex == 0) {
            moveToLane(1);
            return;
        }
        if (laneIndex == LANE_COUNT - 1) {
            moveToLane(LANE_COUNT - 2);
            return;
        }

        int leftLane = laneIndex - 1;
        int rightLane = laneIndex + 1;
        boolean canLeft = isLaneClear(leftLane);
        boolean canRight = isLaneClear(rightLane);
        if (canLeft && canRight) {
            moveToLane(MathUtils.randomBoolean() ? rightLane : leftLane);
        } else if (canLeft) {
            moveToLane(leftLane);
        } else if (canRight) {
            moveToLane(rightLane);
        }
    }

    private boolean isLaneClear(int lane) {
        float boatBottom = boat.getY() - LANE_CLEAR_MARGIN;
        float boatTop = boat.getY() + boat.getHeight() + LANE_CLEAR_MARGIN;
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.lane != lane) {
                continue;
            }
            float obstacleBottom = obstacle.image.getY();
            float obstacleTop = obstacleBottom + obstacle.image.getHeight();
            if (obstacleTop > boatBottom && obstacleBottom < boatTop) {
                return false;
            }
        }
        return true;
    }

    private void updateBoatSlide(float delta) {
        float step = LANE_SLIDE_SPEED * delta;
        if (boatX < targetX) {
            boatX = Math.min(boatX + step, targetX);
        } else if (boatX > targetX) {
            boatX = Math.max(boatX - step, targetX);
        }
        boat.setX(boatX);
    }

    private float laneCenterX(int lane) {
        return asphaltLeft + laneWidth * (lane + 0.5f);
    }

    private void updateWaterPositions() {
        waterA.setY(scrollOffset);
        waterB.setY(scrollOffset + WATER_HEIGHT);
    }

    private void updateIntro(float delta) {
        introElapsed += delta;
        if (boatHorn.isPlaying()) {
            hornStarted = true;
        }
        if ((hornStarted && !boatHorn.isPlaying()) || introElapsed >= HORN_TIMEOUT) {
            startDriving();
        }
    }

    private void startDriving() {
        phase = Phase.DRIVING;
        runTimer = 0f;
        spawnTimer = 0f;
        boatHorn.stop();
        boatDrive.loop(0.3f);
    }

    private void updateDriving(float delta) {
        runTimer += delta;

        scrollOffset -= SCROLL_SPEED * delta;
        if (scrollOffset <= -WATER_HEIGHT) {
            scrollOffset += WATER_HEIGHT;
        }
        updateWaterPositions();

        spawnTimer += delta;
        while (spawnTimer >= OBSTACLE_SPAWN_INTERVAL) {
            spawnTimer -= OBSTACLE_SPAWN_INTERVAL;
            spawnObstacle();
        }

        boatRect.set(boat.getX(), boat.getY(), boat.getWidth(), boat.getHeight());
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.image.setY(obstacle.image.getY() - SCROLL_SPEED * delta);

            if (obstacle.image.getY() + obstacle.image.getHeight() < 0f) {
                obstacle.image.remove();
                obstacles.removeIndex(i);
                continue;
            }

            obstacleRect.set(obstacle.image.getX(), obstacle.image.getY(), obstacle.image.getWidth(), obstacle.image.getHeight());
            if (!obstacle.alreadyHit && Intersector.overlaps(boatRect, obstacleRect)) {
                obstacle.alreadyHit = true;
                if (!blinking) {
                    startBlink();
                }
            }
        }

        if (runTimer >= RUN_DURATION) {
            phase = Phase.ENDED;
            boatDrive.stop();
            blinking = false;
            boat.setVisible(true);
            startEnding();
        }
    }

    private void startEnding() {
        boatHorn.stop();
        boatHorn.play();
        endingElapsed = 0f;
        endingHornStarted = false;
        endingDriftStarted = false;
        endingDriveStarted = false;
        endingDriveStartAt = 0f;
        endingDrawable.setRegion(endingAnimation.getKeyFrame(0f));
        endingImage.setVisible(true);
    }

    private boolean updateEnding(float delta) {
        endingElapsed += delta;
        endingDrawable.setRegion(endingAnimation.getKeyFrame(endingElapsed, false));

        if (boatHorn.isPlaying()) {
            endingHornStarted = true;
        }
        if (!endingDriftStarted
                && ((endingHornStarted && !boatHorn.isPlaying()) || endingElapsed >= HORN_TIMEOUT)) {
            boatDrift.stop();
            boatDrift.play(BOTE_DRIFT_VOLUME);
            endingDriftStarted = true;
            endingDriveStartAt = endingElapsed + DRIFT_DURATION;
        }
        if (endingDriftStarted && !endingDriveStarted && endingElapsed >= endingDriveStartAt) {
            boatDrive.loop(0.3f);
            endingDriveStarted = true;
        }

        if (endingElapsed >= endingAnimation.getAnimationDuration()) {
            boatDrive.stop();
            boatDrift.stop();
            boatHorn.stop();
            game.setScreen(game.getVehicleSelectScreen());
            return true;
        }
        return false;
    }

    private void spawnObstacle() {
        int lane = MathUtils.random(0, LANE_COUNT - 1);
        Image image = new Image(obstacleDrawable);
        float obstacleHeight = OBSTACLE_WIDTH * obstacleDrawable.getRegion().getRegionHeight() / obstacleDrawable.getRegion().getRegionWidth();
        image.setBounds(laneCenterX(lane) - OBSTACLE_WIDTH / 2f, WORLD_HEIGHT + 10f, OBSTACLE_WIDTH, obstacleHeight);
        stage.getRoot().addActorBefore(boat, image);
        obstacles.add(new Obstacle(image, lane));
    }

    private void startBlink() {
        blinking = true;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        boat.setVisible(false);
    }

    private void updateBlink(float delta) {
        blinkTimer += delta;
        blinkToggleTimer += delta;
        if (blinkToggleTimer >= BLINK_INTERVAL) {
            blinkToggleTimer -= BLINK_INTERVAL;
            boat.setVisible(!boat.isVisible());
        }
        if (blinkTimer >= BLINK_DURATION) {
            blinking = false;
            boat.setVisible(true);
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
