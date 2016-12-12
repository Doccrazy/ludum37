package de.doccrazy.ld37.game.weapons;

import com.badlogic.gdx.math.Vector2;

public class Rope extends Weapon {
    public Rope() {
        fireRate = 0f;
    }

    @Override
    public void setFiring(boolean firing) {
        super.setFiring(firing);
        lastShot = 0f;
    }

    @Override
    protected void spawnShot(Vector2 spawn, Vector2 dir) {
        player.fireRope();
    }

    @Override
    public String getPlayerAnim() {
        return "whip";
    }
}
