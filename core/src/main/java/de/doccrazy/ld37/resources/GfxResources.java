package de.doccrazy.ld37.resources;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader;
import de.doccrazy.shared.core.ResourcesBase;

public class GfxResources extends ResourcesBase {

    public GfxResources() {
        super("game.atlas");
    }

    public XmlReader.Element testlevel = xml("levels/test.svg");
    public TextureRegion testlevelBg = new TextureRegion(textureFilterLinear("levels/test.png"));

    public Texture rope = textureWrap("rope.png");
    public Texture lava = textureWrapX("lava.png");
    //public Sprite player = getAtlas().createSprite("player");
    //public Sprite flamethrower = getAtlas().createSprite("flamethrower");
    public Sprite fireSprite = getAtlas().createSprite("fire");

    public ParticleEffectPool partFire = particle("fire.p", 0.01f);
    public ParticleEffectPool partSmallFire = particle("fire.p", 0.005f);
    public ParticleEffectPool partLavasparks = particle("lavasparks.p", 0.01f);

    protected Texture textureWrapX(String filename) {
        Texture tex = texture(filename);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }
}
