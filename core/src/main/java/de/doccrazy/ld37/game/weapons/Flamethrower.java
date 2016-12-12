package de.doccrazy.ld37.game.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.doccrazy.ld37.game.actor.FlameActor;
import de.doccrazy.ld37.game.actor.PlayerActor;

public class Flamethrower extends Weapon {
    public Flamethrower() {
        delay = 0f;
        fireRate = 10f;
        fireVariation = 0.02f;
    }

    @Override
    protected void spawnShot(Vector2 spawn, Vector2 dir) {
        player.getWorld().addActor(new FlameActor(player.getWorld(), spawn, dir.rotate(MathUtils.random(-5f, 5f)), this));
    }

    @Override
    public String getPlayerAnim() {
        return "flamethrower";
    }
}
