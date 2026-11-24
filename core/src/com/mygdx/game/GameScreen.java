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

public class GameScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float GRASS_SIZE = 1376f;
    private static final float GRASS_SPEED = 90f;
    private static final float CAR_WIDTH = 90f;
    private static final float CAR_Y = 45f;
    private static final int LANE_COUNT = 4;
    private static final int START_LANE = 1;
    private static final float LANE_SLIDE_SPEED = 700f;
    private static final float CONE_WIDTH = 60f;
    private static final float CONE_SPAWN_INTERVAL = 4f;
    private static final float LANE_CLEAR_MARGIN = 100f;
    private static final float RUN_DURATION = 75f;
    private static final float BLINK_DURATION = 0.5f;
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float HORN_TIMEOUT = 2.5f;
    private static final float CAR_DRIFT_DURATION = 0.86f;
    private static final float ROAD_BORDER_TEX_PX = 10f;
    private static final float ROAD_ASPHALT_TEX_PX = 398f;
    private static final int ENDING_FRAME_COLS = 15;
    private static final int ENDING_FRAME_ROWS = 10;
    private static final int ENDING_FRAME_COUNT = 150;
    private static final float ENDING_FRAME_DURATION = 1f / 15f;
    private static final float DANGER_ZONE_DISTANCE = 250f;

    private enum Phase {
        INTRO, DRIVING, ENDED
    }

    private static final class Cone {
        final Image image;
        final int lane;
        boolean alreadyHit;
        boolean resolved;
        long dangerEnteredAt;
        boolean anticipacionRegistrada;
        boolean amenaza;

        Cone(Image image, int lane) {
            this.image = image;
            this.lane = lane;
        }
    }

    private final Stage stage;
    private final PauseMenu pauseMenu;
    private boolean paused;
    private final Image grassA;
    private final Image grassB;
    private final Image car;
    private final Music carHorn;
    private final Sound carDrive;
    private final Sound carDrift;
    private final TextureRegionDrawable coneDrawable;
    private final Image endingImage;
    private final TextureRegionDrawable endingDrawable;
    private final Animation<TextureRegion> endingAnimation;
    private final Array<Cone> cones = new Array<Cone>();
    private final Rectangle carRect = new Rectangle();
    private final Rectangle coneRect = new Rectangle();
    private final float asphaltLeft;
    private final float laneWidth;

    private Phase phase = Phase.INTRO;
    private float scrollOffset;
    private float carX;
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

    public GameScreen(PunchItGame game) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        Texture grassTexture = game.getManager().get("cars/mosaico_cesped.jpg", Texture.class);
        grassA = new Image(grassTexture);
        grassA.setBounds(0, 0, GRASS_SIZE, GRASS_SIZE);
        grassB = new Image(grassTexture);
        grassB.setBounds(0, 0, GRASS_SIZE, GRASS_SIZE);
        stage.addActor(grassA);
        stage.addActor(grassB);

        Texture roadTexture = game.getManager().get("cars/road.jpg", Texture.class);
        Image road = new Image(roadTexture);
        float roadWidth = WORLD_HEIGHT * roadTexture.getWidth() / roadTexture.getHeight();
        float roadLeft = (WORLD_WIDTH - roadWidth) / 2f;
        road.setBounds(roadLeft, 0, roadWidth, WORLD_HEIGHT);
        stage.addActor(road);

        float textureScale = roadWidth / roadTexture.getWidth();
        asphaltLeft = roadLeft + ROAD_BORDER_TEX_PX * textureScale;
        laneWidth = ROAD_ASPHALT_TEX_PX * textureScale / LANE_COUNT;

        Texture carTexture = game.getManager().get("cars/carro.png", Texture.class);
        TextureRegion carRegion = new TextureRegion(carTexture, 190, 134, 120, 227);
        car = new Image(new TextureRegionDrawable(carRegion));
        float carHeight = CAR_WIDTH * carRegion.getRegionHeight() / carRegion.getRegionWidth();
        car.setBounds(0, CAR_Y, CAR_WIDTH, carHeight);
        stage.addActor(car);

        Texture coneTexture = game.getManager().get("cars/cono.png", Texture.class);
        coneDrawable = new TextureRegionDrawable(new TextureRegion(coneTexture, 192, 169, 116, 162));

        Texture endingSheet = game.getManager().get("cars/car_ending_sheet.png", Texture.class);
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

        carHorn = game.getManager().get("cars/car_horn.mp3", Music.class);
        carDrive = game.getManager().get("cars/car_drive.wav", Sound.class);
        carDrift = game.getManager().get("cars/car_drift.mp3", Sound.class);
        carHorn.setLooping(false);
        carHorn.setVolume(0.5f);
        pauseMenu = new PauseMenu(game);

        reset();
    }

    private void reset() {
        paused = false;
        pauseMenu.hide();
        carHorn.stop();
        carDrive.stop();
        carDrift.stop();

        for (int i = 0; i < cones.size; i++) {
            cones.get(i).image.remove();
        }
        cones.clear();

        scrollOffset = 0f;
        updateGrassPositions();

        laneIndex = START_LANE;
        carX = laneCenterX(laneIndex) - CAR_WIDTH / 2f;
        targetX = carX;
        car.setX(carX);
        car.setVisible(true);

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
        carHorn.play();
    }

    @Override
    public void hide() {
        paused = false;
        pauseMenu.hide();
        carHorn.stop();
        carDrive.stop();
        carDrift.stop();
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
            updateCarSlide(delta);
        }

        if (phase == Phase.DRIVING) {
            GestorMetricas.registrarTiempoJuego("auto", delta);
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
        carHorn.stop();
        carDrive.stop();
        carDrift.stop();
    }

    private void resumeGame() {
        paused = false;
        pauseMenu.hide();
        carDrive.loop(0.3f);
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
        targetX = laneCenterX(lane) - CAR_WIDTH / 2f;
        carDrift.stop();
        carDrift.play();
    }

    private void registrarAnticipacion() {
        long now = TimeUtils.millis();
        for (int i = 0; i < cones.size; i++) {
            Cone cone = cones.get(i);
            if (cone.lane != laneIndex) {
                continue;
            }
            if (cone.resolved || cone.anticipacionRegistrada || cone.dangerEnteredAt == 0) {
                continue;
            }
            if (cone.image.getY() + cone.image.getHeight() < car.getY()) {
                continue;
            }
            long elapsed = now - cone.dangerEnteredAt;
            if (elapsed > 0 && elapsed <= 10000) {
                cone.anticipacionRegistrada = true;
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
        float carBottom = car.getY() - LANE_CLEAR_MARGIN;
        float carTop = car.getY() + car.getHeight() + LANE_CLEAR_MARGIN;
        for (int i = 0; i < cones.size; i++) {
            Cone cone = cones.get(i);
            if (cone.lane != lane) {
                continue;
            }
            float coneBottom = cone.image.getY();
            float coneTop = coneBottom + cone.image.getHeight();
            if (coneTop > carBottom && coneBottom < carTop) {
                return false;
            }
        }
        return true;
    }

    private void updateCarSlide(float delta) {
        float step = LANE_SLIDE_SPEED * delta;
        if (carX < targetX) {
            carX = Math.min(carX + step, targetX);
        } else if (carX > targetX) {
            carX = Math.max(carX - step, targetX);
        }
        car.setX(carX);
    }

    private float laneCenterX(int lane) {
        return asphaltLeft + laneWidth * (lane + 0.5f);
    }

    private void updateGrassPositions() {
        grassA.setY(scrollOffset);
        grassB.setY(scrollOffset + GRASS_SIZE);
    }

    private void updateIntro(float delta) {
        introElapsed += delta;
        if (carHorn.isPlaying()) {
            hornStarted = true;
        }
        if ((hornStarted && !carHorn.isPlaying()) || introElapsed >= HORN_TIMEOUT) {
            startDriving();
        }
    }

    private void startDriving() {
        phase = Phase.DRIVING;
        runTimer = 0f;
        spawnTimer = 0f;
        carHorn.stop();
        carDrive.loop(0.3f);
    }

    private void updateDriving(float delta) {
        runTimer += delta;

        scrollOffset -= GRASS_SPEED * delta;
        if (scrollOffset <= -GRASS_SIZE) {
            scrollOffset += GRASS_SIZE;
        }
        updateGrassPositions();

        spawnTimer += delta;
        while (spawnTimer >= CONE_SPAWN_INTERVAL) {
            spawnTimer -= CONE_SPAWN_INTERVAL;
            spawnCone();
        }

        carRect.set(car.getX(), car.getY(), car.getWidth(), car.getHeight());
        for (int i = cones.size - 1; i >= 0; i--) {
            Cone cone = cones.get(i);
            cone.image.setY(cone.image.getY() - GRASS_SPEED * delta);

            if (cone.image.getY() + cone.image.getHeight() < 0f) {
                cone.image.remove();
                cones.removeIndex(i);
                continue;
            }

            if (cone.dangerEnteredAt == 0
                    && cone.image.getY() <= car.getY() + car.getHeight() + DANGER_ZONE_DISTANCE) {
                cone.dangerEnteredAt = TimeUtils.millis();
            }
            if (cone.lane == laneIndex
                    && cone.image.getY() + cone.image.getHeight() >= car.getY()
                    && cone.image.getY() <= car.getY() + car.getHeight() + DANGER_ZONE_DISTANCE) {
                cone.amenaza = true;
            }

            coneRect.set(cone.image.getX(), cone.image.getY(), cone.image.getWidth(), cone.image.getHeight());
            if (!cone.alreadyHit && Intersector.overlaps(carRect, coneRect)) {
                cone.alreadyHit = true;
                cone.resolved = true;
                GestorMetricas.registrarChoque();
                if (!blinking) {
                    startBlink();
                }
            }

            if (!cone.resolved && cone.image.getY() + cone.image.getHeight() < car.getY()) {
                cone.resolved = true;
                if (cone.amenaza) {
                    GestorMetricas.registrarObstaculoEsquivado();
                }
            }
        }

        if (runTimer >= RUN_DURATION) {
            phase = Phase.ENDED;
            carDrive.stop();
            blinking = false;
            car.setVisible(true);
            startEnding();
        }
    }

    private void startEnding() {
        carHorn.stop();
        carHorn.play();
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

        if (carHorn.isPlaying()) {
            endingHornStarted = true;
        }
        if (!endingDriftStarted
                && ((endingHornStarted && !carHorn.isPlaying()) || endingElapsed >= HORN_TIMEOUT)) {
            carDrift.stop();
            carDrift.play();
            endingDriftStarted = true;
            endingDriveStartAt = endingElapsed + CAR_DRIFT_DURATION;
        }
        if (endingDriftStarted && !endingDriveStarted && endingElapsed >= endingDriveStartAt) {
            carDrive.loop(0.3f);
            endingDriveStarted = true;
        }

        if (endingElapsed >= endingAnimation.getAnimationDuration()) {
            carDrive.stop();
            carDrift.stop();
            game.setScreen(game.getVehicleSelectScreen());
            return true;
        }
        return false;
    }

    private void spawnCone() {
        int lane = MathUtils.random(0, LANE_COUNT - 1);
        Image image = new Image(coneDrawable);
        float coneHeight = CONE_WIDTH * coneDrawable.getRegion().getRegionHeight() / coneDrawable.getRegion().getRegionWidth();
        image.setBounds(laneCenterX(lane) - CONE_WIDTH / 2f, WORLD_HEIGHT + 10f, CONE_WIDTH, coneHeight);
        stage.getRoot().addActorBefore(car, image);
        cones.add(new Cone(image, lane));
    }

    private void startBlink() {
        blinking = true;
        blinkTimer = 0f;
        blinkToggleTimer = 0f;
        car.setVisible(false);
    }

    private void updateBlink(float delta) {
        blinkTimer += delta;
        blinkToggleTimer += delta;
        if (blinkToggleTimer >= BLINK_INTERVAL) {
            blinkToggleTimer -= BLINK_INTERVAL;
            car.setVisible(!car.isVisible());
        }
        if (blinkTimer >= BLINK_DURATION) {
            blinking = false;
            car.setVisible(true);
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
