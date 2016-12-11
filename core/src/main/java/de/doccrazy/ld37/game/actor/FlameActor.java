package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

import java.util.HashSet;
import java.util.Set;

public class FlameActor extends ShapeActor<GameWorld> implements CollisionListener {
    private static final float RADIUS = 0.5f;
    private static final float LIFETIME = 1f;
    private static final float MAX_SCALE = 2f;
    private static final float DPS = 50f;

    private final Vector2 dir;
    private final Weapon weapon;
    private final ParticleEmitter.GradientColorValue tint;
    private Set<Damageable> contacts = new HashSet<>();

    public FlameActor(GameWorld world, Vector2 spawn, Vector2 dir, Weapon weapon) {
        super(world, spawn, false);
        this.dir = dir;
        this.weapon = weapon;
        this.tint = Resource.GFX.partFire.obtain().getEmitters().get(0).getTint();
        task.every(0.1f, this::applyDamage);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .velocity(dir.cpy().scl(5f)).rotation(MathUtils.random(0, (float) Math.PI))
                .gravityScale(0)
                .fixShape(ShapeBuilder.circle(0.1f)).fixSensor().fixFilter(CollCategory.BULLET, ((short)(~CollCategory.BULLET)));
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        float radius = Math.min(RADIUS, RADIUS * stateTime/LIFETIME * 1.33f + 0.1f);
        body.getFixtureList().get(0).getShape().setRadius(radius);
        setSize(radius * 2f, radius * 2f);
        //setScale(Math.min(MAX_SCALE, MAX_SCALE * stateTime/LIFETIME * 1.33f + 0.1f));
        if (stateTime > LIFETIME) {
            kill();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float[] col = tint.getColor(Math.min(1f, stateTime/LIFETIME));
        batch.setColor(col[0], col[1], col[2], 1f);
        drawRegion(batch, Resource.GFX.fireSprite);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void applyDamage(Void x) {
        for (Damageable contact : contacts) {
            contact.damage(DPS/10f, weapon);
        }
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getType() == BodyDef.BodyType.StaticBody) {
            kill();
        }
        if (other.getUserData() instanceof Damageable) {
            contacts.add((Damageable) other.getUserData());
        }
        return false;
    }

    @Override
    public void endContact(Body other) {
        if (other.getUserData() instanceof Damageable) {
            contacts.remove(other.getUserData());
        }
    }

    @Override
    public void hit(float force) {

    }
}
