package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class RocketActor extends ShapeActor<GameWorld> implements CollisionListener {
    private final Vector2 dir;
    private final Weapon weapon;
    private final ParticleEffectPool.PooledEffect firePart;
    private boolean exploded;

    public RocketActor(GameWorld world, Vector2 spawn, Vector2 dir, Weapon weapon) {
        super(world, spawn, false);
        this.dir = dir;
        this.weapon = weapon;
        this.firePart = Resource.GFX.partSmallFire.obtain();
        task.in(5, this::kill);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .velocity(dir.cpy().scl(7f))
                .gravityScale(0)
                .fixShape(ShapeBuilder.box(0.18f, 0.07f)).fixSensor().fixFilter(CollCategory.BULLET, ((short)(~CollCategory.BULLET)));
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        setRotation(body.getLinearVelocity().angle());
        Vector2 tail = localToParentCoordinates(new Vector2(-0.09f, 0f));
        firePart.setPosition(tail.x, tail.y);
        firePart.update(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!exploded) {
            drawRegion(batch, Resource.GFX.rocket);
        }
        firePart.draw(batch);
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getFixtureList().get(0).isSensor()) {
            return true;
        }
        task.in(0, () -> {
            body.setActive(false);
            firePart.allowCompletion();
            world.addActor(new ExplosionActor(world, weapon, body.getPosition()));
            exploded = true;
        });
        task.in(2, this::kill);
        return true;
    }

    @Override
    public void endContact(Body other) {

    }

    @Override
    public void hit(float force) {

    }
}
