package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.WorldActor;

public class ExplosionActor extends WorldActor<GameWorld> {
    private static final float RADIUS = 1f;
    private final Weapon weapon;
    private final Vector2 spawn;

    public ExplosionActor(GameWorld world, Weapon weapon, Vector2 spawn) {
        super(world);
        this.weapon = weapon;
        this.spawn = spawn;
        for (Body body : world.allBodiesAt(spawn, RADIUS)) {
            if (body.getUserData() instanceof Damageable) {
                ((Damageable) body.getUserData()).damage(100f, weapon);
                body.applyLinearImpulse(body.getPosition().cpy().sub(spawn).nor().scl(body.getMass()*2), body.getWorldCenter(), true);
            }
        }
        setOrigin(RADIUS, RADIUS);
        setSize(RADIUS*2, RADIUS*2);
        setPosition(spawn.x - RADIUS, spawn.y - RADIUS);
    }

    @Override
    protected void doAct(float delta) {
        float scale = 1;
        if (stateTime < 0.1f) {
            scale = Math.max(0.1f, stateTime/0.1f);
        } else if (stateTime > 0.3f) {
            scale = Math.max(0.1f, (0.3f - stateTime)/0.1f + 1f);
            if (stateTime > 0.4f) {
                kill();
            }
        }
        setScale(scale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //batch.setColor(new Color(1f, 0.9f, 0.6f, 1f));
        drawRegion(batch, Resource.GFX.explosion);
        //batch.setColor(Color.WHITE);
    }
}
