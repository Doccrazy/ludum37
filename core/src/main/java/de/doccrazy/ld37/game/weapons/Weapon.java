package de.doccrazy.ld37.game.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.doccrazy.ld37.game.actor.PlayerActor;

public abstract class Weapon extends Action {
    protected PlayerActor player;
    protected boolean firing;
    protected float stateTime, lastShot = 0f;
    protected float fireRate, delay, fireVariation = 0f;
    protected Vector2 aim;

    @Override
    public void setActor(Actor actor) {
        super.setActor(actor);
        this.player = (PlayerActor)actor;
    }

    @Override
    public boolean act(float delta) {
        stateTime += delta;
        while (firing && lastShot + (fireRate == 0f ? 0f : 1f/fireRate) <= stateTime) {
            spawnShot();
            if (fireRate == 0f) {
                lastShot = 999999999f;
            } else {
                lastShot += 1f / fireRate + MathUtils.random(-fireVariation, fireVariation);
            }
        }
        return false;
    }

    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
        if (firing) {
            lastShot = Math.max(lastShot, stateTime + delay - (fireRate == 0f ? 0f : 1f/fireRate));
        }
    }

    public void setAim(Vector2 aim) {
        this.aim = aim;
    }

    protected abstract void spawnShot();

    public abstract String getPlayerAnim();
}
