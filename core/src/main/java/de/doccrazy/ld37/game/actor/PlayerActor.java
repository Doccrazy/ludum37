package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.brashmonkey.spriter.Entity;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.CollCategory;
import de.doccrazy.ld37.game.actions.BurningAction;
import de.doccrazy.ld37.game.weapons.Flamethrower;
import de.doccrazy.ld37.game.weapons.RPG;
import de.doccrazy.ld37.game.weapons.Rope;
import de.doccrazy.ld37.game.weapons.Weapon;
import de.doccrazy.ld37.game.world.FloatingTextEvent;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.GroundContactAction;
import de.doccrazy.shared.game.actor.SpriterActor;
import de.doccrazy.shared.game.base.KeyboardMovementListener;
import de.doccrazy.shared.game.base.MovementInputListener;
import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.GameState;
import de.doccrazy.shared.game.world.ShapeBuilder;

public class PlayerActor extends SpriterActor<GameWorld> implements Damageable {
    public static final float RADIUS = 0.5f;
    private static final float VELOCITY = 5f;
    private static final float TORQUE = 2f;
    private static final float JUMP_IMPULSE = 4f;
    private static final float AIR_CONTROL = 3f;
    public static final float V_MAX_AIRCONTROL = 2.5f;
    public static final float V_MAX_GLIDE_DROP = -2f;
    public static final float V_MAX_ROLL = 40f;
    public static final float GLIDE_V_SCALE = 0.01f;
    public static final float MAX_WALL_ANGLE = MathUtils.cosDeg(15);
    public static final int CLIMB_SPEED = 5;
    public static final float MAX_HEALTH = 500f;

    private MovementInputListener movement;
    private final GroundContactAction groundContact;
    private boolean moving;
    private float orientation = 1;
    private float lastJump = 0;
    private RopeAnchor currentAnchor;
    private boolean pinned;
    private float pinDirection;
    private Weapon weapon;
    private float health = MAX_HEALTH;
    private boolean drowning;
    private float slowEndTime = 0;

    public PlayerActor(GameWorld world, Vector2 spawn) {
        super(world, spawn, false, Resource.SPRITER.player, Resource.SPRITER::getDrawer);
        player.setScale(0.008f);
        setzOrder(50);
        addAction(groundContact = new GroundContactAction());
        //setScaleX(Resource.GFX.mower.getWidth() / Resource.GFX.mower.getHeight());
        setWeapon(new Flamethrower());
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected BodyBuilder createBody(Vector2 spawn) {
        return BodyBuilder.forDynamic(spawn)
                .fixShape(ShapeBuilder.circle(RADIUS)).fixProps(3f, 0.1f, 1f).fixFilter(CollCategory.PLAYER, (short)-1);
    }

    public void setupKeyboardControl() {
        movement = new KeyboardMovementListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.E && (groundContact.isTouchingLeftWall() || groundContact.isTouchingRightWall())) {
                    //pinned = true;
                    //pinDirection = groundContact.isTouchingLeftWall() ? -1 : 1;
                }
                if (keycode == Input.Keys.NUM_1) {
                    setWeapon(new Rope());
                }
                if (keycode == Input.Keys.NUM_2) {
                    setWeapon(new Flamethrower());
                }
                if (keycode == Input.Keys.NUM_3) {
                    setWeapon(new RPG());
                }
                return super.keyDown(event, keycode);
            }
        };
        addListener((InputListener)movement);
    }

    public void setupController(MovementInputListener movement) {
        this.movement = movement;
    }

    @Override
    protected void doAct(float delta) {
        if (!drowning) {
            if (movement != null && world.getGameState() == GameState.GAME) {
                move(delta);
            } else {
                body.setAngularVelocity(0);
            }
            if (currentAnchor != null && !currentAnchor.isDead()) {
                orientation = Math.signum(currentAnchor.attachAngle().x);
                currentAnchor.setMoveDir(-movement.getMovement().y);
            } else if (world.getMouseTarget() != null) {
                orientation = Math.signum(world.getMouseTarget().x - body.getPosition().x);
            }
        }
        if (body.getPosition().y + body.getFixtureList().get(0).getShape().getRadius() < world.getLevel().getBoundingBox().y) {
            kill();
        }
        super.doAct(delta);
        animate();
    }

    private void animate() {
        if (orientation != player.flippedX()) {
            player.flipX();
        }
        if (world.getMouseTarget() != null) {
            Vector2 aim = world.getMouseTarget().cpy().sub(body.getPosition());
            weapon.setAim(aim.cpy().nor());
            float aimAngle = currentAnchor != null && !currentAnchor.isDead() ? currentAnchor.attachAngle().scl(orientation).angle() : aim.scl(orientation).angle();
            try {
                player.setBone("weapon", aimAngle);
            } catch (NullPointerException ignore) {
            }
        }
        if (weapon instanceof Rope) {
            if (currentAnchor != null && !currentAnchor.isDead()) {
                activateCharMap("weapon_hand");
            } else {
                activateCharMap("weapon_" + weapon.getPlayerAnim());
            }
        }
    }

    private void move(float delta) {
        Vector2 mv = movement.getMovement();
        moving = Math.abs(mv.x) > 0;
        if (moving) {
            orientation = Math.signum(mv.x);
        }

        Vector2 v = body.getLinearVelocity();

        if (pinned) {
            boolean touching = pinDirection < 0 ? groundContact.isTouchingLeftWall() : groundContact.isTouchingRightWall();
            RayListener wallTop = new RayListener();
            RayListener wallBottom = new RayListener();
            world.box2dWorld.rayCast(wallTop, body.getPosition().cpy(), body.getPosition().add(pinDirection*RADIUS*1.5f, RADIUS*0.5f));
            world.box2dWorld.rayCast(wallBottom, body.getPosition().cpy(), body.getPosition().add(pinDirection*RADIUS*1.5f, -RADIUS*0.5f));
            body.setLinearVelocity(pinDirection, (wallTop.hit() && touching && mv.y > 0
                    || wallBottom.hit() && touching && mv.y < 0) ? mv.y*CLIMB_SPEED : 0);
        } else if (mv.x == 0 || Math.signum(mv.x) == Math.signum(orientation)) {
            if (groundContact.isTouchingFloor()) {
                body.setAngularVelocity(-mv.x*VELOCITY * (slowEndTime > stateTime ? 0.5f : 1f));
            } else {
                if (Math.abs(v.x) < V_MAX_AIRCONTROL) {
                    body.applyForceToCenter(mv.x * AIR_CONTROL, 0f, true);
                }
            }
        }
        boolean jump = movement.pollJump();
        if (stateTime - lastJump > GroundContactAction.FLOOR_CONTACT_TTL && jump) {
            if (groundContact.isTouchingFloor() || (currentAnchor != null && currentAnchor.isAttached()) || pinned) {
                addImpulse(0f, (currentAnchor == null || currentAnchor.isDead()) && slowEndTime < stateTime ? JUMP_IMPULSE : JUMP_IMPULSE/2f);
                lastJump = stateTime;
                unattach();
                //Resource.jump.play();
            }/* else if (groundContact.isTouchingLeftWall() && mv.x > 0) {
                body.applyLinearImpulse(JUMP_IMPULSE/3f, JUMP_IMPULSE, body.getPosition().x, body.getPosition().y, true);
                lastJump = stateTime;
            } else if (groundContact.isTouchingRightWall() && mv.x < 0) {
                body.applyLinearImpulse(-JUMP_IMPULSE/3f, JUMP_IMPULSE, body.getPosition().x, body.getPosition().y, true);
                lastJump = stateTime;
            }*/
        }
    }

    private void addImpulse(float impulseX, float impulseY) {
        body.applyLinearImpulse(impulseX, impulseY, body.getPosition().x, body.getPosition().y, true);
        //floorContacts.clear();
    }

    /*@Override
    public void draw(Batch batch, float parentAlpha) {
        drawRegion(batch, Resource.GFX.player);
    }*/

    private void drawParticle(Batch batch, ParticleEffectPool.PooledEffect effect, Vector2 attach, float rotation) {
        Vector2 center = new Vector2(getX() + getOriginX(), getY() + getOriginY());
        float r = getRotation() + rotation;
        Vector2 p = attach.rotate(r).add(center);
        effect.setPosition(p.x, p.y);
        effect.getEmitters().first().getAngle().setHigh(190 + r, 170 + r);
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch);
    }

    @Override
    public void damage(float amount, Weapon weapon) {
        world.postEvent(new FloatingTextEvent(body.getPosition().x + MathUtils.random(-RADIUS/2, RADIUS/2), body.getPosition().y + RADIUS, Float.toString((int)amount), false, false));
        health -= amount;
        if (health < 0) {
            kill();
        }
        //world.postEvent(new ParticleEvent(body.getPosition().x, body.getPosition().y, Resource.GFX.particles.get("explosion")));
        //kill();
        task.in(0, () -> {
            unattach();
            for (int i = 0; i < MathUtils.random(2, 4); i++) {
                world.addActor(new BloodDropActor(world, body.getPosition()));
            }
        });
    }

    public void fireRope() {
        unattach();
        currentAnchor = new RopeAnchor(world, body.getPosition(), world.getMouseTarget().sub(body.getPosition()).nor());
        world.addActor(currentAnchor);
    }

    private void unattach() {
        if (currentAnchor != null) {
            currentAnchor.kill();
            currentAnchor = null;
        }
        pinned = false;
    }

    public void setWeapon(Weapon weapon) {
        if ((currentAnchor != null && !currentAnchor.isDead()) || drowning || isDead()) {
            return;
        }
        boolean firing = false;
        if (this.weapon != null) {
            firing = this.weapon.isFiring();
            this.weapon.setFiring(false);
            removeAction(this.weapon);
        }
        this.weapon = weapon;
        addAction(weapon);

        activateCharMap("weapon_" + weapon.getPlayerAnim());
    }

    public void activateCharMap(String anim) {
        Entity.CharacterMap charMap = player.getEntity().getCharacterMap(anim);
        player.characterMaps = new Entity.CharacterMap[]{charMap};
    }

    public void startFire() {
        if (drowning || isDead()) {
            return;
        }
        weapon.setFiring(true);
    }

    public void stopFire() {
        weapon.setFiring(false);
    }

    public void lavaDeath() {
        drowning = true;
        unattach();
        player.setAnimation("drown");
        //body.setLinearVelocity(0, 0);
        body.setLinearDamping(30f);
        addAction(new BurningAction(Resource.GFX.partFire));
        setzOrder(-10);
        task.in(1.5f, () -> player.setAnimation("drown2"));
        task.in(5f, this::kill);
    }

    public Vector2 getPoint(String name) {
        try {
            return new Vector2(player.getObject(name).position.x + getX() + getOriginX(), player.getObject(name).position.y + getY() + getOriginY());
        } catch (NullPointerException e) {
            return body.getPosition();
        }
    }

    public void slow(float secs) {
        slowEndTime = stateTime + secs;
    }

    public void heal() {
        health = MAX_HEALTH;
    }

    public float getHealth() {
        return health;
    }
}

class RayListener implements RayCastCallback {
    public Vector2 normal;

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        this.normal = normal;
        return 0;
    }

    public boolean hit() {
        return normal != null;
    }
}