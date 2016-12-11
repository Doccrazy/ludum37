package de.doccrazy.ld37.game.weapons;

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
    protected void spawnShot() {
        player.fireRope();
    }

    @Override
    public String getPlayerAnim() {
        return "whip";
    }
}
