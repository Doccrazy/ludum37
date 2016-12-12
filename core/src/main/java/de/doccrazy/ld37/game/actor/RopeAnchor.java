package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.CollisionListener;
import de.doccrazy.shared.game.base.PolyLineRenderer;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.ShapeBuilder;
import net.dermetfan.gdx.physics.box2d.Chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RopeAnchor extends ShapeActor<GameWorld> implements CollisionListener {
    public static final float RADIUS = 0.07f;
    public static final float SPEED = 10f;
    public static final float MIN_LENGTH = 1.5f;
    public static final float MAX_FLY_TIME = 0.75f;
    public static final float MOVE_LEN_PER_S = 2f;

    private final Vector2 dir;
    private List<Chain> chains = new ArrayList<>();
    private Body attachBody;
    private Body endBody;
    private float moveDir = 0;
    private DistanceJoint distJoint;

    public RopeAnchor(GameWorld world, Vector2 spawn, Vector2 dir) {
        super(world, spawn, false);
        this.dir = dir;
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn).velocity(dir.cpy().scl(SPEED))
                .gravityScale(0)
                .fixShape(ShapeBuilder.circle(RADIUS)).fixSensor();
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        if (attachBody != null) {
            attach(attachBody, body.getPosition().add(dir.cpy().scl(RADIUS*2f)));
            attachBody = null;
        }
        if (!isAttached() && stateTime > MAX_FLY_TIME) {
            kill();
        }
        if (isAttached()) {
            distJoint.setLength(distJoint.getLength() + moveDir * delta * MOVE_LEN_PER_S);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        if (!isAttached()) {
            PolyLineRenderer.drawLine(Arrays.asList(world.getPlayer().getBody().getPosition(), body.getPosition()), RADIUS, batch.getProjectionMatrix(), Resource.GFX.rope);
        } else {
            PolyLineRenderer.drawLine(Arrays.asList(world.getPlayer().getBody().getPosition(), endBody.getPosition()), RADIUS, batch.getProjectionMatrix(), Resource.GFX.rope);
            for (Chain chain: chains) {
                if (chain.length() < 2) {
                    continue;
                }
                List<Vector2> points = new ArrayList<>(chain.length());

                for (Body body : chain.getSegments()) {
                    points.add(body.getPosition());
                }
                PolyLineRenderer.drawLine(points, RADIUS, batch.getProjectionMatrix(), Resource.GFX.rope);
            }
        }
        batch.begin();
    }

    private void attach(Body bodyA, Body bodyB, Vector2 joinPoint) {
        if (bodyA != null && bodyB != null) {
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.initialize(bodyA, bodyB, joinPoint);
            world.box2dWorld.createJoint(jointDef);
        }
    }

    private void createChain(Body startBody, Body endBody, float radius, float spacing) {
        Vector2 start = startBody.getPosition();
        Vector2 end = endBody.getPosition();
        float len = start.dst(end);
        if (len < MIN_LENGTH) {
            len = MIN_LENGTH;
        }
        float dist = radius*2;
        List<Body> chainBodies = new ArrayList<>();
        while (dist < len - radius*2) {
            Vector2 pos = end.cpy().sub(start).nor().scl(dist).add(start);
            chainBodies.add(createLinkBody(pos).build(world));
            dist += radius*2 + spacing;
        }
        DistanceJointDef jointDef = new DistanceJointDef();
        jointDef.localAnchorA.x = 0;
        jointDef.localAnchorB.x = 0;
        jointDef.length = radius*2 + spacing/4f;
        jointDef.frequencyHz = 100;
        jointDef.dampingRatio = 0.9f;
        Chain.DefBuilder builder = new Chain.DefBuilder(world.box2dWorld, null, null, jointDef);
        Chain chain = new Chain(builder);
        chain.add(startBody);
        chain.add(chainBodies.toArray(new Body[]{}));
        chain.add(endBody);
        chains.add(chain);
    }

    private BodyBuilder createLinkBody(Vector2 pos) {
        return BodyBuilder.forDynamic(pos).damping(2f, 2f).noSleep().gravityScale(0.25f).userData(this)
                .fixShape(ShapeBuilder.circle(RADIUS)).fixSensor().fixProps(3f, 0.1f, 0.1f);
    }

    private void attach(Body attachEnd, Vector2 contactPoint) {
        endBody = createLinkBody(contactPoint).build(world);
        createRope(world.getPlayer().getBody(), endBody, RADIUS, RADIUS);
        //createChain(world.getPlayer().getBody(), endBody, RADIUS, RADIUS);
        attach(endBody, attachEnd, contactPoint);
    }

    private void createRope(Body startBody, Body endBody, float radius, float spacing) {
        DistanceJointDef jointDef = new DistanceJointDef();
        jointDef.bodyA = startBody;
        jointDef.bodyB = endBody;
        jointDef.localAnchorA.x = 0;
        jointDef.localAnchorB.x = 0;
        jointDef.length = startBody.getPosition().dst(endBody.getPosition());
        jointDef.frequencyHz = 60;
        jointDef.dampingRatio = 1f;
        distJoint = (DistanceJoint) world.box2dWorld.createJoint(jointDef);
    }

    public boolean isAttached() {
        //return !chains.isEmpty();
        return endBody != null;
    }

    public Vector2 attachAngle() {
//        return chains.isEmpty() ? body.getPosition().cpy().sub(world.getPlayer().getBody().getPosition()).nor() :
//                chains.get(0).getSegment(1).getPosition().cpy().sub(chains.get(0).getSegment(0).getPosition()).nor();
        return !isAttached() ? body.getPosition().cpy().sub(world.getPlayer().getBody().getPosition()).nor() :
                endBody.getPosition().cpy().sub(world.getPlayer().getBody().getPosition()).nor();
    }

    @Override
    protected void doRemove() {
        super.doRemove();
        for (Chain chain : chains) {
            chain.destroy(1, chain.length() - 1);
        }
        if (isAttached()) {
            world.box2dWorld.destroyBody(endBody);
        }
    }

    @Override
    public boolean beginContact(Body me, Body other, Vector2 normal, Vector2 contactPoint) {
        if (other.getType() == BodyDef.BodyType.StaticBody && !other.getFixtureList().get(0).isSensor() && !isAttached()) {
            attachBody = other;
        }
        return false;
    }

    @Override
    public void endContact(Body other) {

    }

    @Override
    public void hit(float force) {

    }

    public void setMoveDir(float moveDir) {
        this.moveDir = moveDir;
    }
}
