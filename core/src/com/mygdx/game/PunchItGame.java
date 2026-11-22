package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class PunchItGame extends Game {

    private AssetManager manager;
    private MainMenuScreen mainMenuScreen;
    private VehicleSelectScreen vehicleSelectScreen;
    private GiftScreen giftScreen;
    private GiftScreen planeGiftScreen;
    private GameScreen gameScreen;
    private AirplaneScreen airplaneScreen;

    public AssetManager getManager() {
        return manager;
    }

    public MainMenuScreen getMainMenuScreen() {
        return mainMenuScreen;
    }

    public VehicleSelectScreen getVehicleSelectScreen() {
        return vehicleSelectScreen;
    }

    public GiftScreen getGiftScreen() {
        return giftScreen;
    }

    public GiftScreen getPlaneGiftScreen() {
        return planeGiftScreen;
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    public AirplaneScreen getAirplaneScreen() {
        return airplaneScreen;
    }

    @Override
    public void create() {
        manager = new AssetManager();
        manager.load("common/main_menu.jpg", Texture.class);
        manager.load("common/jugar_hover.jpg", Texture.class);
        manager.load("common/opciones.jpg", Texture.class);
        manager.load("common/journey_awaits.mp3", Music.class);
        manager.load("common/option_change.wav", Sound.class);
        manager.load("common/racing_menu.ogg", Music.class);
        manager.load("common/load_gift_sheet.png", Texture.class);
        manager.load("cars/car_start.mp3", Sound.class);
        manager.load("cars/carro.png", Texture.class);
        manager.load("cars/mosaico_cesped.jpg", Texture.class);
        manager.load("cars/road.jpg", Texture.class);
        manager.load("cars/car_horn.mp3", Music.class);
        manager.load("cars/car_drive.wav", Sound.class);
        manager.load("cars/car_drift.mp3", Sound.class);
        manager.load("cars/car_ending_sheet.png", Texture.class);
        manager.load("cars/cono.png", Texture.class);
        manager.load("plane/avion.png", Texture.class);
        manager.load("plane/cono_cielo.png", Texture.class);
        manager.load("plane/road_cielo.jpg", Texture.class);
        manager.load("plane/mosaico_cielo.jpg", Texture.class);
        manager.load("plane/avion_start.mp3", Sound.class);
        manager.load("plane/avion_drive.mp3", Sound.class);
        manager.load("plane/avion_drift.mp3", Sound.class);
        manager.load("plane/avion_ending_sheet.png", Texture.class);
        manager.finishLoading();

        mainMenuScreen = new MainMenuScreen(this);
        vehicleSelectScreen = new VehicleSelectScreen(this);
        gameScreen = new GameScreen(this);
        airplaneScreen = new AirplaneScreen(this);
        giftScreen = new GiftScreen(this, false);
        planeGiftScreen = new GiftScreen(this, true);

        setScreen(mainMenuScreen);
    }

    @Override
    public void dispose() {
        mainMenuScreen.dispose();
        vehicleSelectScreen.dispose();
        giftScreen.dispose();
        planeGiftScreen.dispose();
        gameScreen.dispose();
        airplaneScreen.dispose();
        if (manager != null) {
            manager.dispose();
        }
    }
}
