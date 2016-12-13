package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.brashmonkey.spriter.Player;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class CrackyPlatformActor extends ShapeActor<GameWorld> implements CollisionListener {
    private Vector2 shake = new Vector2();

    public CrackyPlatformActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forStatic(spawn)
                .fixShape(ShapeBuilder.box(0.6f, 0.1f)).fixProps(3f, 0f, 1f);
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        setPosition(body.getPosition().x - getOriginX() + shake.x, body.getPosition().y - getOriginY() + shake.y);
        body.getFixtureList().get(0).setSensor(world.getPlayer().isDead() || world.getPlayer().getBody().getPosition().y - PlayerActor.RADIUS < body.getPosition().y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.crackyPlatform);
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof PlayerActor && other.getPosition().y - PlayerActor.RADIUS > body.getPosition().y) {
            task.during(1f, t -> {
                shake.x = MathUtils.random(-0.1f, 0.1f);
                shake.y = MathUtils.random(-0.02f, 0.02f);
            }).then(0, () -> {
                for (int i = 0; i < 10; i++) {
                    world.addActor(new FallingDebrisActor(world, body.getPosition(), Resource.GFX.rocks[MathUtils.random(Resource.GFX.rocks.length - 1)], 0.5f));
                }
                kill();
            });
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
