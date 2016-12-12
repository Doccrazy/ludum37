package de.doccrazy.ld37.game.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.actor.Box2dActor;

public abstract class Level extends Box2dActor<GameWorld> {

    public Level(GameWorld world) {
        super(world);
    }

    public abstract Rectangle getBoundingBox();

    public abstract Rectangle getViewportBox();

    public abstract Vector2 getSpawn();

    public abstract int getScoreGoal();

    public abstract float getTime();

    /**
     * Gets a random point <b>inside</b> the level (i.e. the playable area)
     * @param avoidPlayer true to keep some distance away from the player character
     */
    public Vector2 getRandomPoint(boolean avoidPlayer) {
        Vector2 spawn;
        do {
            spawn = new Vector2(MathUtils.random(getBoundingBox().getX(), getBoundingBox().getWidth() + getBoundingBox().getX()),
                    MathUtils.random(getBoundingBox().getY(), getBoundingBox().getHeight() + getBoundingBox().getY()));
        } while ((avoidPlayer && spawn.dst(getSpawn()) < 2f));
        return spawn;
    }

    /**
     * Gets a random points <b>on the level borders</b>, i.e. outside the playable area
     */
    /*public Vector2 getRandomBorderPoint() {
        Vector2 spawn;
        do {
            spawn = new Vector2(MathUtils.random(getBoundingBox().getX(), getBoundingBox().getWidth() + getBoundingBox().getX()),
                    MathUtils.random(getBoundingBox().getY(), getBoundingBox().getHeight() + getBoundingBox().getY()));
        } while (getGrassBox().contains(spawn));
        return spawn;
    }

    protected boolean excluded(Vector2 pos) {
        return false;
    }

    protected void spawnRandomObject(BiFunction<GameWorld, Vector2, WorldActor<GameWorld>> factory, boolean avoidPlayer) {
        world.addActor(factory.apply(world, getRandomPoint(avoidPlayer)));
    }*/

}
