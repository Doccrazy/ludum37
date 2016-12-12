package de.doccrazy.ld37.game.actor;

import box2dLight.ConeLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class GrailActor extends ShapeActor<GameWorld> implements CollisionListener {

    public GrailActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, true);
        Vector2 org = new Vector2(spawn.x - 5, spawn.y + 10);
        ConeLight light = new ConeLight(world.rayHandler, 10, new Color(1, 1, 0.3f, 1), 30f, org.x, org.y, spawn.cpy().sub(org).angle(), 2);
        lights.add(light);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forStatic(spawn)
                .fixShape(ShapeBuilder.box(0.13f, 0.2f)).fixSensor();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.grail);
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() instanceof PlayerActor) {
            world.getPlayer().heal();
            world.startEndSequence();
            kill();
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
