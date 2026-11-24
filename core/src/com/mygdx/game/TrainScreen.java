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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class TrainScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float MOSAIC_HEIGHT = 2284.16f;
    private static final float SCROLL_SPEED = 90f;
    private static final float TRAIN_WIDTH = 60f;
    private static final float TRAIN_Y = 45f;
    private static final int LANE_COUNT = 4;
    private static final int START_LANE = 1;
    private static final float LANE_SLIDE_SPEED = 700f;
    private static final float OBSTACLE_WIDTH = 55f;
    private static final float OBSTACLE_SPAWN_INTERVAL = 4f;
    private static final float LANE_CLEAR_MARGIN = 100f;
    private static final float RUN_DURATION = 75f;
    private static final float BLINK_DURATION = 0.5f;
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float HORN_TIMEOUT = 3.2f;
    private static final float TRAIN_DRIFT_DURATION = 2.4f;
    private static final float TRAIN_DRIFT_VOLUME = 0.4f;
    private static final float ROAD_WIDTH = 330f;
    private static final float ROAD_BORDER_TEX_PX = 20f;
    private static final float ROAD_ASPHALT_TEX_PX = 127f;
    private static final int ENDING_FRAME_COLS = 15;
    private static final int ENDING_FRAME_ROWS = 10;
    private static final int ENDING_FRAME_COUNT = 150;
    private static final float ENDING_FRAME_DURATION = 1f / 15f;
    private static final float DANGER_ZONE_DISTANCE = 250f;

    private enum Phase {
        INTRO, DRIVING, ENDED
    }

    private static final class Obstacle {
        final Image image;
        final int lane;
        boolean alreadyHit;
        boolean resolved;
        long dangerEnteredAt;
        boolean anticipacionRegistrada;
        boolean amenaza;

        Obstacle(Image image, int lane) {
            this.image = image;
            this.lane = lane;
        }
    }

    private final Stage stage;
    private final PauseMenu pauseMenu;
    private boolean paused;
    private final Image mosaicA;
    private final Image mosaicB;
    private final Image train;
    private final Music trainHorn;
    private final Sound trainDrive;
    private final Sound trainDrift;
    private final TextureRegionDrawable obstacleDrawable;
    private final Image endingImage;
    private final TextureRegionDrawable endingDrawable;
    private final Animation<TextureRegion> endingAnimation;
    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private final Rectangle trainRect = new Rectangle();
    private final Rectangle obstacleRect = new Rectangle();
    private final float asphaltLeft;
    private final float laneWidth;

    private Phase phase = Phase.INTRO;
    private float scrollOffset;
    private float trainX;
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

    public TrainScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        Texture mosaicTexture = game.getManager().get("train/mosaico_tren.jpg", Texture.class);
        mosaicA = new Image(mosaicTexture);
        mosaicA.setBounds(0, 0, WORLD_WIDTH, MOSAIC_HEIGHT);
        mosaicB = new Image(mosaicTexture);
        mosaicB.setBounds(0, 0, WORLD_WIDTH, MOSAIC_HEIGHT);
        stage.addActor(mosaicA);
        stage.addActor(mosaicB);

        Texture roadTexture = game.getManager().get("train/road_tierra.jpg", Texture.class);
        TextureRegion roadRegion = new TextureRegion(roadTexture);
        Image road = new Image(new TextureRegionDrawable(roadRegion));
        float roadLeft = (WORLD_WIDTH - ROAD_WIDTH) / 2f;
        road.setBounds(roadLeft, 0, ROAD_WIDTH, WORLD_HEIGHT);
        stage.addActor(road);

        float textureScale = ROAD_WIDTH / roadTexture.getWidth();
        asphaltLeft = roadLeft + ROAD_BORDER_TEX_PX * textureScale;
        laneWidth = ROAD_ASPHALT_TEX_PX * textureScale / LANE_COUNT;

        Texture trenTexture = game.getManager().get("train/tren.png", Texture.class);
        TextureRegion trenRegion = new TextureRegion(trenTexture, 209, 47, 82, 391);
        train = new Image(new TextureRegionDrawable(trenRegion));
        float trainHeight = TRAIN_WIDTH * trenRegion.getRegionHeight() / trenRegion.getRegionWidth();
        train.setBounds(0, TRAIN_Y, TRAIN_WIDTH, trainHeight);
        stage.addActor(train);

        Texture conoTexture = game.getManager().get("train/cono_tierra.png", Texture.class);
        obstacleDrawable = new TextureRegionDrawable(new TextureRegion(conoTexture, 145, 89, 206, 262));

        Texture endingSheet = game.getManager().get("train/tren_ending_sheet.png", Texture.class);
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

        trainHorn = game.getManager().get("train/tren_horn.mp3", Music.class);
        trainDrive = game.getManager().get("train/tren_drive_loop.mp3", Sound.class);
        trainDrift = game.getManager().get("train/tren_drift.mp3", Sound.class);
        trainHorn.setLooping(false);
        trainHorn.setVolume(0.5f);
        pauseMenu = new PauseMenu(game);

        reset();
    }

    private void reset() {
        paused = false;
        pauseMenu.hide();
        trainHorn.stop();
        trainDrive.stop();
        trainDrift.stop();

        for (int i = 0; i < obstacles.size; i++) {
            obstacles.get(i).image.remove();
        }
        obstacles.clear();

        scrollOffset = 0f;
        updateMosaicPositions();

        laneIndex = START_LANE;
        trainX = laneCenterX(laneIndex) - TRAIN_WIDTH / 2f;
        targetX = trainX;
        train.setX(trainX);
        train.setVisible(true);

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
        trainHorn.play();
    }

    @Override
    public void hide() {
        paused = false;
        pauseMenu.hide();
        trainHorn.stop();
        trainDrive.stop();
        trainDrift.stop();
        GestorMetricas.guardarMetricas();
    }

    @Override
    public void render(float delta) {
        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                resumeGame();
                return;
            }
            int action = pauseMenu.update(delta);
            if (action == PauseMenu.ACTION_RESUME) {
                resumeGame();
                return;
            }
            if (action == PauseMenu.ACTION_MENU) {
                game.setScreen(game.getVehicleSelectScreen());
                return;
            }
            drawFrame();
            pauseMenu.draw();
            return;
        }

        if (phase == Phase.DRIVING && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
            drawFrame();
            pauseMenu.draw();
            return;
        }

        if (phase != Phase.ENDED) {
            handleInput();
            updateTrainSlide(delta);
        }

        if (phase == Phase.DRIVING) {
            GestorMetricas.registrarTiempoJuego("tren", delta);
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

        stage.act(delta);
        drawFrame();
    }

    private void pauseGame() {
        paused = true;
        pauseMenu.show();
        trainDrive.stop();
        trainDrift.stop();
    }

    private void resumeGame() {
        paused = false;
        pauseMenu.hide();
        trainDrive.loop(0.3f);
    }

    private void drawFrame() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            moveToSafeLane();
        }
    }

    private void moveToLane(int lane) {
        registrarAnticipacion();
        laneIndex = lane;
        targetX = laneCenterX(lane) - TRAIN_WIDTH / 2f;
        trainDrift.stop();
        trainDrift.play(TRAIN_DRIFT_VOLUME);
    }

    private void registrarAnticipacion() {
        long now = TimeUtils.millis();
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.lane != laneIndex) {
                continue;
            }
            if (obstacle.resolved || obstacle.anticipacionRegistrada || obstacle.dangerEnteredAt == 0) {
                continue;
            }
            if (obstacle.image.getY() + obstacle.image.getHeight() < train.getY()) {
                continue;
            }
            long elapsed = now - obstacle.dangerEnteredAt;
            if (elapsed > 0 && elapsed <= 10000) {
                obstacle.anticipacionRegistrada = true;
                GestorMetricas.registrarAnticipacion(elapsed);
                break;
            }
        }
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
        float trainBottom = train.getY() - LANE_CLEAR_MARGIN;
        float trainTop = train.getY() + train.getHeight() + LANE_CLEAR_MARGIN;
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.lane != lane) {
                continue;
            }
            float obstacleBottom = obstacle.image.getY();
            float obstacleTop = obstacleBottom + obstacle.image.getHeight();
            if (obstacleTop > trainBottom && obstacleBottom < trainTop) {
                return false;
            }
        }
        return true;
    }

    private void updateTrainSlide(float delta) {
        float step = LANE_SLIDE_SPEED * delta;
        if (trainX < targetX) {
            trainX = Math.min(trainX + step, targetX);
        } else if (trainX > targetX) {
            trainX = Math.max(trainX - step, targetX);
        }
        train.setX(trainX);
    }

    private float laneCenterX(int lane) {
        return asphaltLeft + laneWidth * (lane + 0.5f);
    }

    private void updateMosaicPositions() {
        mosaicA.setY(scrollOffset);
        mosaicB.setY(scrollOffset + MOSAIC_HEIGHT);
    }

    private void updateIntro(float delta) {
        introElapsed += delta;
        if (trainHorn.isPlaying()) {
            hornStarted = true;
        }
        if ((hornStarted && !trainHorn.isPlaying()) || introElapsed >= HORN_TIMEOUT) {
            startDriving();
        }
    }

    private void startDriving() {
        phase = Phase.DRIVING;
        runTimer = 0f;
        spawnTimer = 0f;
        trainHorn.stop();
        trainDrive.loop(0.3f);
    }

    private void updateDriving(float delta) {
        runTimer += delta;

        scrollOffset -= SCROLL_SPEED * delta;
        if (scrollOffset <= -MOSAIC_HEIGHT) {
            scrollOffset += MOSAIC_HEIGHT;
        }
        updateMosaicPositions();

        spawnTimer += delta;
        while (spawnTimer >= OBSTACLE_SPAWN_INTERVAL) {
            spawnTimer -= OBSTACLE_SPAWN_INTERVAL;
            spawnObstacle();
        }

        trainRect.set(train.getX(), train.getY(), train.getWidth(), train.getHeight());
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.image.setY(obstacle.image.getY() - SCROLL_SPEED * delta);

            if (obstacle.image.getY() + obstacle.image.getHeight() < 0f) {
                obstacle.image.remove();
                obstacles.removeIndex(i);
                continue;
            }

            if (obstacle.dangerEnteredAt == 0
                    && obstacle.image.getY() <= train.getY() + train.getHeight() + DANGER_ZONE_DISTANCE) {
                obstacle.dangerEnteredAt = TimeUtils.millis();
            }
            if (obstacle.lane == laneIndex
                    && obstacle.image.getY() + obstacle.image.getHeight() >= train.getY()
                    && obstacle.image.getY() <= train.getY() + train.getHeight() + DANGER_ZONE_DISTANCE) {
                obstacle.amenaza = true;
            }

            obstacleRect.set(obstacle.image.getX(), obstacle.image.getY(), obstacle.image.getWidth(), obstacle.image.getHeight());
            if (!obstacle.alreadyHit && Intersector.overlaps(trainRect, obstacleRect)) {
                obstacle.alreadyHit = true;
                obstacle.resolved = true;
                GestorMetricas.registrarChoque();
                if (!blinking) {
                    startBlink();
                }
            }

            if (!obstacle.resolved && obstacle.image.getY() + obstacle.image.getHeight() < train.getY()) {
                obstacle.resolved = true;
                if (obstacle.amenaza) {
                    GestorMetricas.registrarObstaculoEsquivado();
                }
            }
        }

        if (runTimer >= RUN_DURATION) {
            phase = Phase.ENDED;
            trainDrive.stop();
            blinking = false;
            train.setVisible(true);
            startEnding();
        }
    }

    private void startEnding() {
        trainHorn.stop();
        trainHorn.play();
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

        if (trainHorn.isPlaying()) {
            endingHornStarted = true;
        }
        if (!endingDriftStarted
                && ((endingHornStarted && !trainHorn.isPlaying()) || endingElapsed >= HORN_TIMEOUT)) {
            trainDrift.stop();
            trainDrift.play(TRAIN_DRIFT_VOLUME);
            endingDriftStarted = true;
            endingDriveStartAt = endingElapsed + TRAIN_DRIFT_DURATION;
        }
        if (endingDriftStarted && !endingDriveStarted && endingElapsed >= endingDriveStartAt) {
            trainDrive.loop(0.3f);
            endingDriveStarted = true;
        }

        if (endingElapsed >= endingAnimation.getAnimationDuration()) {
            trainDrive.stop();
            trainDrift.stop();
            trainHorn.stop();
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
        stage.getRoot().addActorBefore(train, image);
        obstacles.add(new Obstacle(image, lane));
    }

    private void startBlink() {
        blinking = true;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        train.setVisible(false);
    }

    private void updateBlink(float delta) {
        blinkTimer += delta;
        blinkToggleTimer += delta;
        if (blinkToggleTimer >= BLINK_INTERVAL) {
            blinkToggleTimer -= BLINK_INTERVAL;
            train.setVisible(!train.isVisible());
        }
        if (blinkTimer >= BLINK_DURATION) {
            blinking = false;
            train.setVisible(true);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        pauseMenu.resize(width, height);
    }

    @Override
    public void dispose() {
        hide();
        pauseMenu.dispose();
        stage.dispose();
    }
}
