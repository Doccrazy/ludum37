package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.actions.BurningAction;
import de.doccrazy.ld37.game.weapons.Flamethrower;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.SpriterActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class BatActor extends SpriterActor<GameWorld> implements Damageable, CollisionListener {
    private Vector2 dir = new Vector2();
    private float nextDirChange = 0.5f;
    private boolean dying;
    private float hp = 50f;
    private Vector2 pushNormal;

    public BatActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false, Resource.SPRITER.bat, Resource.SPRITER::getDrawer);
        player.setScale(0.0018f);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .gravityScale(0).damping(10f, 0f).noRotate()
                .fixShape(ShapeBuilder.circle(0.1f)).fixFilter(CollCategory.ENEMY, (short) ~(CollCategory.BG | CollCategory.ENEMY));
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        setScale(Math.min(1f, stateTime + 0.1f));
        if (stateTime > nextDirChange) {
            nextDirChange += 0.5f;
            if (Math.random() < 0.5f && !world.getPlayer().isDead()) {
                Vector2 toPlayer = world.getPlayer().getBody().getPosition().sub(body.getPosition()).nor();
                dir.set(toPlayer.rotate(MathUtils.random(-30, 30)));
            } else {
                dir.setToRandomDirection();
            }
        }
        if (!dying) {
            body.applyForceToCenter(dir.cpy().scl(0.5f), true);
        }
        if (pushNormal != null) {
            body.applyLinearImpulse(pushNormal.scl(0.5f), body.getWorldCenter(), true);
            pushNormal = null;
        }
    }

    @Override
    public void damage(float amount, Weapon cause) {
        if (dying) {
            return;
        }
        hp -= amount;
        if (hp > 0) {
            return;
        }
        dying = true;
        if (cause instanceof Flamethrower) {
            addAction(new BurningAction());
        } else {
            task.in(2, this::kill);
        }
        body.setGravityScale(1f);
        body.setLinearDamping(0f);
        Filter filter = new Filter();
        filter.maskBits = ~CollCategory.PLAYER;
        body.getFixtureList().get(0).setFilterData(filter);
        player.setAnimation("die");
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof PlayerActor && !dying) {
            ((PlayerActor) other.getUserData()).damage(10f, null);
            pushNormal = body.getPosition().cpy().sub(other.getPosition()).nor();
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
