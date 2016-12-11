package de.doccrazy.ld37.game.actions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;

public abstract class DrawingAction extends Action {
    public abstract void draw(Batch batch, float parentAlpha);
}
