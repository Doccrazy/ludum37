package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class KillboxActor extends ShapeActor<GameWorld> implements CollisionListener {
    private final Vector2[] poly;
    private final boolean lava;

    public KillboxActor(GameWorld world, Vector2[] poly, boolean lava) {
        super(world, Vector2.Zero, false);
        this.poly = poly;
        this.lava = lava;
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        Vector2 center = new Vector2();
        return BodyBuilder.forStatic(poly[0])
                .fixShape(ShapeBuilder.polyRel(poly))
                .fixSensor();
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getUserData() == world.getPlayer()) {
            if (lava) {
                world.getPlayer().lavaDeath();
            } else {
                for (int i = 0; i < 10; i++) {
                    world.getPlayer().damage(100f, null);
                }
            }
        } else if (other.getUserData() instanceof Damageable) {
            ((Damageable) other.getUserData()).damage(1000f, null);
        }
        return false;
    }

    @Override
    public void endContact(Body other) {
    }

    @Override
    public void hit(float force) {
    }

    public void offsetY(float offs) {
        body.setTransform(body.getPosition().x, body.getPosition().y + offs, body.getAngle());
    }
}
