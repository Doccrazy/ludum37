package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.WorldActor;

public class BloodActor extends WorldActor<GameWorld> {
    private final Vector2 normal;

    public BloodActor(GameWorld world, Vector2 spawn, Vector2 normal) {
        super(world);
        this.normal = normal;
        setPosition(spawn.x, spawn.y);
        setSize(0.6f, 0.2f);
        setOrigin(0.3f, 0f);
        setzOrder(15);
        setRotation(normal.angle() - 90);
    }

    @Override
    protected void doAct(float delta) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //batch.setColor(0.8f, 0, 0, 0.8f);
        batch.draw(Resource.GFX.blood, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        //batch.setColor(1, 1, 1, 1);
    }
}
