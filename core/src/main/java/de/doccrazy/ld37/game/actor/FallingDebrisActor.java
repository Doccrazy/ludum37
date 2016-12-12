package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class FallingDebrisActor extends ShapeActor<GameWorld> {
    private final Sprite sprite;
    private float alpha = 1f;

    public FallingDebrisActor(GameWorld world, Vector2 spawn, Sprite sprite, float lifeSecs) {
        super(world, spawn, false);
        this.sprite = sprite;
        task.during(lifeSecs, t -> alpha = 1 - t/lifeSecs).then(this::kill);
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .velocity(new Vector2(MathUtils.random(-1.5f, 1.5f), MathUtils.random(0, -0.5f)))
                .fixShape(ShapeBuilder.circle(0.1f)).fixSensor();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(1, 1, 1, alpha);
        drawRegion(batch, sprite);
        batch.setColor(1, 1, 1, 1);
    }
}
