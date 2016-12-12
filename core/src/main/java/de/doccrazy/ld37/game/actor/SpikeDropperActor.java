package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.RandomEvent;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class SpikeDropperActor extends ShapeActor<GameWorld> implements CollisionListener {
    private final Rectangle rect;
    private boolean playerIn;
    private RandomEvent spawnEvent = new RandomEvent(0.25f, 0.5f);

    public SpikeDropperActor(GameWorld world, Rectangle rect) {
        super(world, rect.getCenter(new Vector2()), false);
        this.rect = rect;
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forStatic(spawn)
                .fixShape(ShapeBuilder.box(rect.width/2, rect.height/2)).fixSensor();
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        if (spawnEvent.apply(delta) && playerIn) {
            Vector2 spawn = new Vector2(world.getPlayer().getBody().getPosition().x, rect.y + rect.height);
            world.addActor(new FallingSpike(world, spawn, rect.y));
        }
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof PlayerActor) {
            playerIn = true;
        }
        return false;
    }

    @Override
    public void endContact(Body other) {
        if (other.getUserData() instanceof PlayerActor) {
            playerIn = false;
        }
    }

    @Override
    public void hit(float force) {

    }
}
