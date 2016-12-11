package de.doccrazy.ld37.game.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.actor.SVGLevelActor;
import de.doccrazy.shared.core.Debug;
import de.doccrazy.shared.game.world.GameState;

public class UiInputListener extends InputListener {
	private UiRoot root;

    public UiInputListener(UiRoot root) {
        this.root = root;
	}

	@Override
    public boolean keyDown(InputEvent event, int keycode) {
		if (keycode == Keys.F12) {
            root.getRenderer().setRenderBox2dDebug(!root.getRenderer().isRenderBox2dDebug());
        } else if (keycode == Keys.ENTER) {
			root.getWorld().setLevel(world -> new SVGLevelActor(world, Resource.GFX.testlevel, Resource.GFX.testlevelBg));
			root.getWorld().resetAll();
		} else if (keycode == Keys.ESCAPE) {
			root.getWorld().transition(GameState.INIT);
		}/* else {
			if ((root.getWorld().isGameFinished() && !root.getWorld().isGameOver() && root.getWorld().getStateTime() > 1f)
                    || root.getWorld().getGameState() == GameState.INIT) {
				root.getWorld().transition(GameState.INIT);
				root.getWorld().transition(GameState.PRE_GAME);
			}
		}*/
		if (Debug.ON) {
			/*if (keycode == Keys.Z) {
				root.getRenderer().setZoomDelta(1f);
			}*/
		}
		return false;
	}

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
        if (Debug.ON) {
            /*if (keycode == Keys.Z) {
                root.getRenderer().setZoomDelta(-2f);
            }*/
        }
        return false;
	}
}
