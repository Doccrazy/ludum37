package de.doccrazy.ld37.game.actions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.shared.game.actor.Box2dActor;
import de.doccrazy.shared.game.actor.DrawingAction;
import de.doccrazy.shared.game.actor.WorldActor;

public class BurningAction extends DrawingAction {
    private final ParticleEffectPool fireParticle;
    private ParticleEffectPool.PooledEffect fire;
    private float stateTime;

    public BurningAction() {
        fireParticle = Resource.GFX.partSmallFire;
    }

    public BurningAction(ParticleEffectPool fireParticle) {
        this.fireParticle = fireParticle;
    }

    @Override
    public void setActor(Actor actor) {
        if (actor != null) {
            fire = fireParticle.obtain();
            actor.setColor(0.5f, 0.4f, 0.3f, 1f);
            stateTime = 0f;
        } else if (getActor() != null) {
            fire.free();
            getActor().setColor(1f, 1f, 1f, 1f);
        }
        super.setActor(actor);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (fire != null) {
            fire.draw(batch);
        }
    }

    @Override
    public boolean act(float delta) {
        if (fire == null) {
            return false;
        }
        stateTime += delta;
        if (stateTime >= 3f) {
            ((WorldActor)getActor()).kill();
        }
        Vector2 p = ((Box2dActor) getActor()).getBody().getPosition();
        fire.setPosition(p.x, p.y);
        fire.update(delta);
        return false;
    }
}
