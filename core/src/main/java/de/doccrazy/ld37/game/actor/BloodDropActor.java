package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class BloodDropActor extends ShapeActor<GameWorld> implements CollisionListener {
    public BloodDropActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .velocity(Vector2.Y.cpy().scl(MathUtils.random(3, 6)).rotate(MathUtils.random(-70, 70)))
                .fixShape(ShapeBuilder.circle(0.1f)).fixSensor();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.bloodDrop);
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getType() == BodyDef.BodyType.StaticBody && !other.getFixtureList().get(0).isSensor()) {
            task.in(0, () -> {
                world.addActor(new BloodActor(world, me.getPosition().add(me.getLinearVelocity().nor().scl(0.4f)), Vector2.Y));
                kill();
            });
            return false;
        }
        return true;
    }

    @Override
    public void endContact(Body other) {

    }

    @Override
    public void hit(float force) {

    }
}
