package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.GroundContactAction;
import de.doccrazy.shared.game.actor.ShapeActor;
import de.doccrazy.shared.game.base.KeyboardMovementListener;
import de.doccrazy.shared.game.base.MovementInputListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.GameState;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class PlayerActor extends ShapeActor<GameWorld> {
    private static final float RADIUS = 0.5f;
    private static final float VELOCITY = 5f;
    private static final float TORQUE = 2f;
    private static final float JUMP_IMPULSE = 4f;
    private static final float AIR_CONTROL = 3f;
    public static final float V_MAX_AIRCONTROL = 2.5f;
    public static final float V_MAX_GLIDE_DROP = -2f;
    public static final float V_MAX_ROLL = 40f;
    public static final float GLIDE_V_SCALE = 0.01f;

    private MovementInputListener movement;
    private final GroundContactAction groundContact;
    private boolean moving;
    private float orientation = 1;
    private float lastJump = 0;

    public PlayerActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false);
        setzOrder(50);
        addAction(groundContact = new GroundContactAction());
        //setScaleX(Resource.GFX.mower.getWidth() / Resource.GFX.mower.getHeight());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .fixShape(ShapeBuilder.circle(RADIUS)).fixProps(3f, 0.1f, 1f);
    }

    public void setupKeyboardControl() {
        movement = new KeyboardMovementListener();
        addListener((InputListener)movement);
    }

    public void setupController(MovementInputListener movement) {
        this.movement = movement;
    }

    @Override
    protected void doAct(float delta) {
        if (movement != null && world.getGameState() == GameState.GAME) {
            move(delta);
        } else {
            body.setAngularVelocity(0);
        }
        if (body.getPosition().y + body.getFixtureList().get(0).getShape().getRadius() < world.getLevel().getBoundingBox().y) {
            kill();
        }
        super.doAct(delta);
    }

    private void move(float delta) {
        Vector2 mv = movement.getMovement();
        moving = Math.abs(mv.x) > 0;
        if (moving) {
            orientation = Math.signum(mv.x);
        }

        Vector2 v = body.getLinearVelocity();

        if (mv.x == 0 || Math.signum(mv.x) == Math.signum(orientation)) {
            //System.out.println(touchingFloor());
            if (groundContact.isTouchingFloor()) {
                body.setAngularVelocity(-mv.x*VELOCITY);
            } else {
                if (Math.abs(v.x) < V_MAX_AIRCONTROL) {
                    body.applyForceToCenter(mv.x * AIR_CONTROL, 0f, true);
                }
            }
        }
        boolean jump = movement.pollJump();
        if (stateTime - lastJump > GroundContactAction.FLOOR_CONTACT_TTL && jump) {
            if (groundContact.isTouchingFloor()) {
                addImpulse(0f, JUMP_IMPULSE);
                lastJump = stateTime;
                //Resource.jump.play();
            } else if (groundContact.isTouchingLeftWall() && mv.x > 0) {
                body.applyLinearImpulse(JUMP_IMPULSE/3f, JUMP_IMPULSE, body.getPosition().x, body.getPosition().y, true);
                lastJump = stateTime;
            } else if (groundContact.isTouchingRightWall() && mv.x < 0) {
                body.applyLinearImpulse(-JUMP_IMPULSE/3f, JUMP_IMPULSE, body.getPosition().x, body.getPosition().y, true);
                lastJump = stateTime;
            }
        }
    }

    private void addImpulse(float impulseX, float impulseY) {
        body.applyLinearImpulse(impulseX, impulseY, body.getPosition().x, body.getPosition().y, true);
        //floorContacts.clear();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //drawRegion(batch, Resource.GFX.player[shapeState]);
    }

    private void drawParticle(Batch batch, ParticleEffectPool.PooledEffect effect, Vector2 attach, float rotation) {
        Vector2 center = new Vector2(getX() + getOriginX(), getY() + getOriginY());
        float r = getRotation() + rotation;
        Vector2 p = attach.rotate(r).add(center);
        effect.setPosition(p.x, p.y);
        effect.getEmitters().first().getAngle().setHigh(190 + r, 170 + r);
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch);
    }

    public void damage(float amount) {
        //world.postEvent(new ParticleEvent(body.getPosition().x, body.getPosition().y, Resource.GFX.particles.get("explosion")));
        kill();
    }
}
