package de.doccrazy.ld37.game.world;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import de.doccrazy.ld37.game.ui.UiRoot;
import de.doccrazy.shared.game.world.GameState;

public class GameInputListener extends InputListener {
    private GameWorld world;
    private UiRoot root;

    public GameInputListener(UiRoot root) {
        this.root = root;
        this.world = root.getWorld();
        reset();
    }

    @Override
    public boolean keyTyped(InputEvent event, char character) {
        if (world.getGameState() != GameState.GAME) {
            return false;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(InputEvent event, float x, float y) {
        world.setMouseTarget(new Vector2(x, y));
        return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
        world.setMouseTarget(new Vector2(x, y));
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (world.getGameState() != GameState.GAME) {
            return false;
        }
        if (button == 0) {
            world.getPlayer().startFire();
            return true;
            //start = world.createAttachedPoint(new Vector2(x, y), 0.02f);
        }
        if (button == 2) {
            //world.createFly(new Vector2(x, y));
        }
        return false;
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (world.getGameState() != GameState.GAME) {
            return;
        }
        if (button == 0) {
            world.getPlayer().stopFire();
        }
    }

    public void reset() {
    }
}
