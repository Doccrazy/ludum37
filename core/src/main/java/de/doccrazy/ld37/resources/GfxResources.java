package de.doccrazy.ld37.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.XmlReader;
import de.doccrazy.shared.core.ResourcesBase;

import java.util.HashMap;
import java.util.Map;

public class GfxResources extends ResourcesBase {

    public GfxResources() {
        super("game.atlas");
    }

    public XmlReader.Element testlevel = xml("levels/test.svg");
    public TextureRegion testlevelTex = new TextureRegion(textureFilterLinear("levels/test.png"));
    public TextureRegion victory = new TextureRegion(texture("victory.png"));
    public TextureRegion victoryEmpty = new TextureRegion(texture("victoryEmpty.png"));
    public TextureRegion defeat = new TextureRegion(texture("defeat.png"));

    public Texture rope = textureWrap("rope.png");
    public Texture lava = textureWrapX("lava.png");
    //public Sprite player = getAtlas().createSprite("player");
    //public Sprite flamethrower = getAtlas().createSprite("flamethrower");
    public Sprite fireSprite = getAtlas().createSprite("fire");
    public Sprite bloodDrop = getAtlas().createSprite("bloodDrop");
    public Sprite blood = getAtlas().createSprite("blood");
    public Sprite crackyPlatform = getAtlas().createSprite("cracky_platform");
    public Sprite rocket = getAtlas().createSprite("rocket");
    public Sprite explosion = getAtlas().createSprite("explosion");
    public Sprite fog = getAtlas().createSprite("fog");
    public Sprite batCave = getAtlas().createSprite("batCave");
    public Sprite[] rocks = new Sprite[]{getAtlas().createSprite("rock0"), getAtlas().createSprite("rock1"),
            getAtlas().createSprite("rock2"), getAtlas().createSprite("rock3")};
    public Sprite spike = getAtlas().createSprite("spike");
    public Sprite grail = getAtlas().createSprite("grail");

    public Pixmap crosshair = new Pixmap(Gdx.files.internal("crosshair.png"));

    public Map<String, ParticleEffectPool> particles = new HashMap<String, ParticleEffectPool>() {{
        put("fire", particle("fire.p", 0.01f));
        put("smoke", particle("smoke.p", 0.01f));
        put("explosion", particle("explosion.p", 0.02f));
    }};
    public ParticleEffectPool partFire = particle("fire.p", 0.01f);
    public ParticleEffectPool partSmallFire = particle("fire.p", 0.005f);
    public ParticleEffectPool partLavasparks = particle("lavasparks.p", 0.01f);

    public ShaderProgram heatShader = shader("shaders/idtVertexShader.glsl", "shaders/heatFragmentShader.glsl");

    protected Texture textureWrapX(String filename) {
        Texture tex = texture(filename);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }
}
