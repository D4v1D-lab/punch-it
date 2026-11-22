package com.mygdx.game.desktop;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.PunchItGame;

public class DesktopLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        DisplayMode desktopMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
        config.setFromDisplayMode(desktopMode);
        config.fullscreen = true;
        config.vSyncEnabled = true;
        config.title = "Punch It";
        new LwjglApplication(new PunchItGame(), config);
    }
}
