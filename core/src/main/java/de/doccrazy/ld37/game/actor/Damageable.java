package de.doccrazy.ld37.game.actor;

import de.doccrazy.ld37.game.weapons.Weapon;

public interface Damageable {
    void damage(float amount, Weapon cause);
}
