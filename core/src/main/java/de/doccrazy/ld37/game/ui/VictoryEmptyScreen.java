package de.doccrazy.ld37.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.shared.game.world.GameState;

public class VictoryEmptyScreen extends Widget {
    private final UiRoot uiRoot;
    private float stateTime;

    public VictoryEmptyScreen(UiRoot uiRoot) {
        this.uiRoot = uiRoot;
        setFillParent(true);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean vis = uiRoot.getWorld().getGameState() == GameState.VICTORY && !uiRoot.getWorld().isEndSequence();
        setVisible(vis);
        if (vis) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(Resource.GFX.victoryEmpty, getX(), getY(), getWidth(), getHeight());
        /*TextureRegion sprite = uiRoot.getWorld().isGameOver() ? Resource.GFX.thanksTx : Resource.GFX.continueTx;
        if (stateTime >= 1f) {
            float scale = Interpolation.bounceOut.apply(6f, 1f, Math.max(stateTime - 1f, 0));
            batch.draw(sprite, getX() + 200, getY() + 40,
                    sprite.getRegionWidth() / 2f, sprite.getRegionHeight() / 2f,
                    sprite.getRegionWidth(), sprite.getRegionHeight(), scale, scale, 0);
        }*/
    }
}
