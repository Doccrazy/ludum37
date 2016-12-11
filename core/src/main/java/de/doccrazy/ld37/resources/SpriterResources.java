package de.doccrazy.ld37.resources;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.brashmonkey.spriter.Entity;
import de.doccrazy.shared.spriter.ResourcesBaseSpriter;

public class SpriterResources extends ResourcesBaseSpriter {

    public SpriterResources(TextureAtlas atlas) {
        super("Game.scml", atlas);
    }

    public Entity player = entity("player");
    public Entity bat = entity("bat");
}
