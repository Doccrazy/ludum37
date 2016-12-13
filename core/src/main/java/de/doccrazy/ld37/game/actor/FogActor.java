package de.doccrazy.ld37.game.actor;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.actor.Tasker;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

import java.util.HashSet;
import java.util.Set;

public class FogActor extends ShapeActor<GameWorld> implements Damageable, CollisionListener {
    private static final float DPS = 25f;

    private Tasker.OnceTaskDef expireTask;
    private float alpha = 1f;
    private Set<Damageable> contacts = new HashSet<>();

    public FogActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false);
        PointLight light = new PointLight(world.rayHandler, 10, Color.GREEN, 1f, 0, 0);
        light.setXray(true);
        lights.add(light);
        task.every(0.1f, this::applyDamage);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .gravityScale(0).damping(0, 0)
                .rotation(MathUtils.random(0, (float) Math.PI*2))
                .fixShape(ShapeBuilder.circle(0.5f)).fixSensor();
    }

    @Override
    protected void init() {
        super.init();
        lights.get(0).attachToBody(body);
        body.setAngularVelocity((float) (Math.random() - 0.5f));
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(new Color(0.5f, 1f, 0.5f, 0.5f * alpha));
        lights.get(0).setColor(0, alpha, 0, 1);
        drawRegion(batch, Resource.GFX.fog);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void damage(float amount, Weapon cause) {
        task.during(0.5f, t -> alpha = (0.5f - t)/0.5f).then(0, this::kill);
    }

    private void applyDamage() {
        for (Damageable contact : contacts) {
            contact.damage(DPS/10f, null);
        }
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof Damageable && !(other.getUserData() instanceof FogActor)) {
            contacts.add((Damageable) other.getUserData());
        }
        return true;
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
