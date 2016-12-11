package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.scenes.scene2d.Action;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.actions.BurningAction;
import de.doccrazy.ld37.game.actions.DrawingAction;
import de.doccrazy.ld37.game.weapons.Flamethrower;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.SpriterActor;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class BatActor extends SpriterActor<GameWorld> implements Damageable {
    private Vector2 dir = new Vector2();
    private float nextDirChange = 0.5f;
    private boolean burning;
    private float hp = 50f;

    public BatActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false, Resource.SPRITER.bat, Resource.SPRITER::getDrawer);
        player.setScale(0.0018f);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .gravityScale(0).damping(10f, 0f)
                .fixShape(ShapeBuilder.circle(0.1f));
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        setScale(Math.min(1f, stateTime + 0.1f));
        if (stateTime > nextDirChange) {
            nextDirChange += 0.5f;
            Vector2 toPlayer = world.getPlayer().getBody().getPosition().sub(body.getPosition()).nor();
            if (Math.random() < 0.5f) {
                dir.set(toPlayer.rotate(MathUtils.random(-30, 30)));
            } else {
                dir.setToRandomDirection();
            }
        }
        if (!burning) {
            body.applyForceToCenter(dir.cpy().scl(0.5f), true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        for (Action action : getActions()) {
            if (action instanceof DrawingAction) {
                ((DrawingAction) action).draw(batch, parentAlpha);
            }
        }
    }

    @Override
    public void damage(float amount, Weapon cause) {
        if (burning) {
            return;
        }
        hp -= amount;
        if (hp > 0) {
            return;
        }
        if (cause instanceof Flamethrower) {
            burning = true;
            addAction(new BurningAction());
            body.setGravityScale(1f);
            body.setLinearDamping(1f);
            Filter filter = new Filter();
            filter.maskBits = ~CollCategory.PLAYER_BODY;
            body.getFixtureList().get(0).setFilterData(filter);
            player.setAnimation("die");
        } else {
            kill();
        }
    }
}
