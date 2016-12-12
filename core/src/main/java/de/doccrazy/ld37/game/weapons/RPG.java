package de.doccrazy.ld37.game.weapons;

import com.badlogic.gdx.math.Vector2;
import de.doccrazy.ld37.game.actor.RocketActor;

public class RPG extends Weapon {
    public RPG() {
        fireRate = 1.5f;
        muzzleName = "muzzle_lg";
    }

    @Override
    protected void spawnShot(Vector2 spawn, Vector2 dir) {
        player.getWorld().addActor(new RocketActor(player.getWorld(), spawn, dir, this));
    }

    @Override
    public String getPlayerAnim() {
        return "rpg";
    }
}
