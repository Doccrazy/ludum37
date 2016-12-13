package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.weapons.RPG;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.RandomEvent;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class BatCaveActor extends ShapeActor<GameWorld> implements Damageable {
    private static final float MAX_DIST = 10f;

    private final float spawnRadius;
    private final RandomEvent spawnTimer = new RandomEvent(1f, 5f);
    private float health = 250f;

    public BatCaveActor(GameWorld world, Vector2 spawn, float spawnRadius) {
        super(world, spawn, false);
        this.spawnRadius = spawnRadius;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            spawnBat();
        }
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forStatic(spawn)
                .fixShape(ShapeBuilder.circle(0.4f)).fixFilter(CollCategory.BG, CollCategory.BULLET);
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        if (spawnTimer.apply(delta) && !world.getPlayer().isDead() && world.getPlayer().getBody().getPosition().dst(body.getPosition()) < MAX_DIST) {
            spawnBat();
        }
    }

    private void spawnBat() {
        Vector2 spawn = new Vector2(body.getPosition().x, body.getPosition().y).add(new Vector2().setToRandomDirection().scl(spawnRadius));
        world.addActor(new BatActor(world, spawn));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.batCave);
    }

    @Override
    public void damage(float amount, Weapon cause) {
        if (cause instanceof RPG) {
            health -= amount;
        }
        if (health <= 0) {
            kill();
        }
    }
}
