package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class FallingSpike extends ShapeActor<GameWorld> implements CollisionListener {
    private final float yBottom;

    public FallingSpike(GameWorld world, Vector2 spawn, float yBottom) {
        super(world, spawn, false);
        this.yBottom = yBottom;
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .fixShape(ShapeBuilder.box(0.07f, 0.18f)).fixSensor();
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        if (body.getPosition().y < yBottom) {
            body.setActive(false);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.spike);
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof PlayerActor) {
            kill();
            world.getPlayer().damage(50f, null);
            world.getPlayer().slow(0.5f);
        }
        return false;
    }

    @Override
    public void endContact(Body other) {

    }

    @Override
    public void hit(float force) {

    }
}
