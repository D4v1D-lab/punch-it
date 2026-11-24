package com.mygdx.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class MetricasInputListener extends InputAdapter {

    private boolean enterHeld;

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER && !enterHeld) {
            enterHeld = true;
            GestorMetricas.registrarInicioPulsacion();
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.ENTER && enterHeld) {
            enterHeld = false;
            GestorMetricas.registrarFinPulsacion();
        }
        return false;
    }
}
