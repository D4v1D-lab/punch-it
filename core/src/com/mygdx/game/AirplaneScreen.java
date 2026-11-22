package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class AirplaneScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float SKY_HEIGHT = 768f;
    private static final float SKY_SPEED = 90f;
    private static final float AVION_WIDTH = 100f;
    private static final float AVION_Y = 45f;
    private static final int LANE_COUNT = 4;
    private static final int START_LANE = 1;
    private static final float LANE_SLIDE_SPEED = 700f;
    private static final float OBSTACLE_WIDTH = 70f;
    private static final float OBSTACLE_SPAWN_INTERVAL = 4f;
    private static final float LANE_CLEAR_MARGIN = 100f;
    private static final float RUN_DURATION = 75f;
    private static final float BLINK_DURATION = 0.5f;
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float AVION_DRIFT_DURATION = 1.85f;
    private static final float AVION_DRIFT_VOLUME = 0.2f;
    private static final float ROAD_BORDER_TEX_PX = 10f;
    private static final float ROAD_ASPHALT_TEX_PX = 348f;
    private static final int ENDING_FRAME_COLS = 15;
    private static final int ENDING_FRAME_ROWS = 10;
    private static final int ENDING_FRAME_COUNT = 150;
    private static final float ENDING_FRAME_DURATION = 1f / 15f;

    private enum Phase {
        DRIVING, ENDED
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
    private final Image skyA;
    private final Image skyB;
    private final Image avion;
    private final Sound avionDrive;
    private final Sound avionDrift;
    private final TextureRegionDrawable obstacleDrawable;
    private final Image endingImage;
    private final TextureRegionDrawable endingDrawable;
    private final Animation<TextureRegion> endingAnimation;
    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private final Rectangle avionRect = new Rectangle();
    private final Rectangle obstacleRect = new Rectangle();
    private final float asphaltLeft;
    private final float laneWidth;

    private Phase phase = Phase.DRIVING;
    private float scrollOffset;
    private float avionX;
    private float targetX;
    private int laneIndex;
    private float spawnTimer;
    private float runTimer;
    private boolean blinking;
    private float blinkTimer;
    private float blinkToggleTimer;
    private float endingElapsed;
    private boolean endingDriveStarted;

    public AirplaneScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        Texture skyTexture = game.getManager().get("plane/mosaico_cielo.jpg", Texture.class);
        skyA = new Image(skyTexture);
        skyA.setBounds(0, 0, WORLD_WIDTH, SKY_HEIGHT);
        skyB = new Image(skyTexture);
        skyB.setBounds(0, 0, WORLD_WIDTH, SKY_HEIGHT);
        stage.addActor(skyA);
        stage.addActor(skyB);

        Texture roadTexture = game.getManager().get("plane/road_cielo.jpg", Texture.class);
        Image road = new Image(roadTexture);
        float roadWidth = WORLD_HEIGHT * roadTexture.getWidth() / roadTexture.getHeight();
        float roadLeft = (WORLD_WIDTH - roadWidth) / 2f;
        road.setBounds(roadLeft, 0, roadWidth, WORLD_HEIGHT);
        stage.addActor(road);

        float textureScale = roadWidth / roadTexture.getWidth();
        asphaltLeft = roadLeft + ROAD_BORDER_TEX_PX * textureScale;
        laneWidth = ROAD_ASPHALT_TEX_PX * textureScale / LANE_COUNT;

        Texture avionTexture = game.getManager().get("plane/avion.png", Texture.class);
        TextureRegion avionRegion = new TextureRegion(avionTexture, 256, 287, 512, 490);
        avion = new Image(new TextureRegionDrawable(avionRegion));
        float avionHeight = AVION_WIDTH * avionRegion.getRegionHeight() / avionRegion.getRegionWidth();
        avion.setBounds(0, AVION_Y, AVION_WIDTH, avionHeight);
        stage.addActor(avion);

        Texture obstacleTexture = game.getManager().get("plane/cono_cielo.png", Texture.class);
        obstacleDrawable = new TextureRegionDrawable(new TextureRegion(obstacleTexture, 159, 194, 182, 112));

        Texture endingSheet = game.getManager().get("plane/avion_ending_sheet.png", Texture.class);
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

        avionDrive = game.getManager().get("plane/avion_drive.mp3", Sound.class);
        avionDrift = game.getManager().get("plane/avion_drift.mp3", Sound.class);

        reset();
    }

    private void reset() {
        avionDrive.stop();
        avionDrift.stop();

        for (int i = 0; i < obstacles.size; i++) {
            obstacles.get(i).image.remove();
        }
        obstacles.clear();

        scrollOffset = 0f;
        updateSkyPositions();

        laneIndex = START_LANE;
        avionX = laneCenterX(laneIndex) - AVION_WIDTH / 2f;
        targetX = avionX;
        avion.setX(avionX);
        avion.setVisible(true);

        spawnTimer = 0f;
        runTimer = 0f;
        blinking = false;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        endingElapsed = 0f;
        endingDriveStarted = false;
        endingImage.setVisible(false);
        phase = Phase.DRIVING;
    }

    @Override
    public void show() {
        reset();
        avionDrive.loop(0.3f);
    }

    @Override
    public void hide() {
        avionDrive.stop();
        avionDrift.stop();
    }

    @Override
    public void render(float delta) {
        if (phase != Phase.ENDED) {
            handleInput();
            updateAvionSlide(delta);
        }

        if (phase == Phase.DRIVING) {
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
        targetX = laneCenterX(lane) - AVION_WIDTH / 2f;
        avionDrift.stop();
        avionDrift.play(AVION_DRIFT_VOLUME);
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
        float avionBottom = avion.getY() - LANE_CLEAR_MARGIN;
        float avionTop = avion.getY() + avion.getHeight() + LANE_CLEAR_MARGIN;
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.lane != lane) {
                continue;
            }
            float obstacleBottom = obstacle.image.getY();
            float obstacleTop = obstacleBottom + obstacle.image.getHeight();
            if (obstacleTop > avionBottom && obstacleBottom < avionTop) {
                return false;
            }
        }
        return true;
    }

    private void updateAvionSlide(float delta) {
        float step = LANE_SLIDE_SPEED * delta;
        if (avionX < targetX) {
            avionX = Math.min(avionX + step, targetX);
        } else if (avionX > targetX) {
            avionX = Math.max(avionX - step, targetX);
        }
        avion.setX(avionX);
    }

    private float laneCenterX(int lane) {
        return asphaltLeft + laneWidth * (lane + 0.5f);
    }

    private void updateSkyPositions() {
        skyA.setY(scrollOffset);
        skyB.setY(scrollOffset + SKY_HEIGHT);
    }

    private void updateDriving(float delta) {
        runTimer += delta;

        scrollOffset -= SKY_SPEED * delta;
        if (scrollOffset <= -SKY_HEIGHT) {
            scrollOffset += SKY_HEIGHT;
        }
        updateSkyPositions();

        spawnTimer += delta;
        while (spawnTimer >= OBSTACLE_SPAWN_INTERVAL) {
            spawnTimer -= OBSTACLE_SPAWN_INTERVAL;
            spawnObstacle();
        }

        avionRect.set(avion.getX(), avion.getY(), avion.getWidth(), avion.getHeight());
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.image.setY(obstacle.image.getY() - SKY_SPEED * delta);

            if (obstacle.image.getY() + obstacle.image.getHeight() < 0f) {
                obstacle.image.remove();
                obstacles.removeIndex(i);
                continue;
            }

            obstacleRect.set(obstacle.image.getX(), obstacle.image.getY(), obstacle.image.getWidth(), obstacle.image.getHeight());
            if (!obstacle.alreadyHit && Intersector.overlaps(avionRect, obstacleRect)) {
                obstacle.alreadyHit = true;
                if (!blinking) {
                    startBlink();
                }
            }
        }

        if (runTimer >= RUN_DURATION) {
            phase = Phase.ENDED;
            avionDrive.stop();
            blinking = false;
            avion.setVisible(true);
            startEnding();
        }
    }

    private void startEnding() {
        endingElapsed = 0f;
        endingDriveStarted = false;
        endingDrawable.setRegion(endingAnimation.getKeyFrame(0f));
        endingImage.setVisible(true);
        avionDrift.stop();
        avionDrift.play(AVION_DRIFT_VOLUME);
    }

    private boolean updateEnding(float delta) {
        endingElapsed += delta;
        endingDrawable.setRegion(endingAnimation.getKeyFrame(endingElapsed, false));

        if (!endingDriveStarted && endingElapsed >= AVION_DRIFT_DURATION) {
            avionDrive.loop(0.3f);
            endingDriveStarted = true;
        }

        if (endingElapsed >= endingAnimation.getAnimationDuration()) {
            avionDrive.stop();
            avionDrift.stop();
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
        stage.getRoot().addActorBefore(avion, image);
        obstacles.add(new Obstacle(image, lane));
    }

    private void startBlink() {
        blinking = true;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        avion.setVisible(false);
    }

    private void updateBlink(float delta) {
        blinkTimer += delta;
        blinkToggleTimer += delta;
        if (blinkToggleTimer >= BLINK_INTERVAL) {
            blinkToggleTimer -= BLINK_INTERVAL;
            avion.setVisible(!avion.isVisible());
        }
        if (blinkTimer >= BLINK_DURATION) {
            blinking = false;
            avion.setVisible(true);
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
