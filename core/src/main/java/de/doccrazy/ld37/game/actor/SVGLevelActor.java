package de.doccrazy.ld37.game.actor;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.XmlReader;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.GameRules;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.HelpEvent;
import de.doccrazy.ld37.game.world.RandomEvent;
import de.doccrazy.shared.game.svg.SVGLayer;

import java.util.ArrayList;
import java.util.List;

public class SVGLevelActor extends Level {
    public static final String LAYER_PHYSICS = "Physics";
    public static final String LAYER_META = "Meta";
    public static final String LABEL_SCREEN = "screen";
    public static final String LABEL_SPAWN = "spawn";
    public static final String LABEL_SPAWN2 = "spawn2";
    public static final String PREFIX_PARTICLE = "part:";
    public static final String PREFIX_PARTICLE_BG = "partBg:";

    private final Rectangle dimensions, cameraBounds;
    private final Vector2 spawn;
    private final TextureRegion levelTexture, levelBgTexture;
    private final List<Body> bodies = new ArrayList<>();
    private final List<ParticleEffectPool.PooledEffect> particles = new ArrayList<>();
    private final List<ParticleEffectPool.PooledEffect> particlesBg = new ArrayList<>();
    private KillboxActor endKillbox;
    private List<LavaRect> lavaRects = new ArrayList<>();
    private List<LavaRect> endLavaRects = new ArrayList<>();
    private float lavaMax;

    public SVGLevelActor(GameWorld world, XmlReader.Element levelElement, TextureRegion levelTexture, TextureRegion levelBgTexture) {
        super(world);
        this.levelTexture = levelTexture;
        this.levelBgTexture = levelBgTexture;
        setzOrder(-1);

        SVGLayer rootLayer = new SVGLayer(levelElement);
        Vector2 cameraBoundsForScale = rootLayer.getLayerByLabel(LAYER_META).getRectSizeImmediate(LABEL_SCREEN);
        float scale = GameRules.LEVEL_HEIGHT / cameraBoundsForScale.y;

        rootLayer.applyScale(scale);
        dimensions = rootLayer.getDimensionsTransformed();

        SVGLayer metaLayer = rootLayer.getLayerByLabel(LAYER_META);

        spawn = metaLayer.getRectCenter(world.isSecondSpawn() ? LABEL_SPAWN2 : LABEL_SPAWN);
        Vector2[] boundsPoly = metaLayer.getRectAsPoly(LABEL_SCREEN);
        cameraBounds = new Rectangle(boundsPoly[0].x, boundsPoly[0].y, boundsPoly[2].x - boundsPoly[0].x, boundsPoly[2].y - boundsPoly[0].y);

        SVGLayer physicsLayer = rootLayer.getLayerByLabel(LAYER_PHYSICS);
        physicsLayer.createPhysicsBodiesRecursive(bodyBuilder -> bodies.add(bodyBuilder.build(world)));

        metaLayer.processCircleByPrefix("bat", (s, circle, color) -> {
            world.addActor(new BatCaveActor(world, new Vector2(circle.x, circle.y), circle.radius));
        });
        metaLayer.processRectAsPolyByPrefix("lava", (s, rect, color) -> {
            boolean end = ":end".equals(s);
            addLava(new Rectangle(Math.min(rect[0].x, rect[2].x), Math.min(rect[0].y, rect[2].y), Math.abs(rect[2].x - rect[0].x), Math.abs(rect[2].y - rect[0].y)), end);
            KillboxActor kb = new KillboxActor(world, rect, true);
            world.addActor(kb);
            if (end) {
                endKillbox = kb;
            }
        });
        metaLayer.processRectCenterByPrefix("crackyPlatform", (type, center, color) -> {
            world.addActor(new CrackyPlatformActor(world, center));
        });
        metaLayer.processRectAsPolyByPrefix("fog", (s, rect, color) -> {
            for (int i = 0; i < Math.abs((rect[0].x - rect[2].x) * rect[0].y - rect[2].y)/4; i++) {
                Vector2 spawn = new Vector2(MathUtils.random(rect[0].x, rect[2].x), MathUtils.random(rect[0].y, rect[2].y));
                world.addActor(new FogActor(world, spawn));
            }
        });
        metaLayer.processRectAsPolyByPrefix("spikeDrop", (s, rect, color) -> {
            world.addActor(new SpikeDropperActor(world, new Rectangle(Math.min(rect[0].x, rect[2].x), Math.min(rect[0].y, rect[2].y), Math.abs(rect[2].x - rect[0].x), Math.abs(rect[2].y - rect[0].y))));
        });
        metaLayer.processCircleByPrefix("grail", (s, circle, color) -> {
            world.addActor(new GrailActor(world, new Vector2(circle.x, circle.y)));
        });
        metaLayer.processCircleByPrefix("lavaMax", (s, circle, color) -> {
            lavaMax = circle.y;
        });

        metaLayer.processRectCenterByPrefix(PREFIX_PARTICLE, (type, center, color) -> {
            ParticleEffectPool.PooledEffect particle = Resource.GFX.particles.get(type).obtain();
            particle.setPosition(center.x, center.y);
            particles.add(particle);
        });
        metaLayer.processRectCenterByPrefix(PREFIX_PARTICLE_BG, (type, center, color) -> {
            ParticleEffectPool.PooledEffect particle = Resource.GFX.particles.get(type).obtain();
            particle.setPosition(center.x, center.y);
            particlesBg.add(particle);
        });
        metaLayer.processRectAsPolyByPrefix("kill", (s, rect, color) -> {
            world.addActor(new KillboxActor(world, rect, false));
        });
        metaLayer.processRectAsPolyByPrefix("win", (s, rect, color) -> {
            world.addActor(new WinboxActor(world, rect));
        });
        metaLayer.processCircleByPrefix("light", (s, circle, color) -> {
            PointLight light = new PointLight(world.rayHandler, 10, color, circle.radius*2f, circle.x, circle.y);
            light.setXray(true);
            lights.add(light);
        });
        metaLayer.processArcByPrefix("conelight", (s, arc, color) -> {
            ConeLight light = new ConeLight(world.rayHandler, 10, color, arc.r*7f, arc.x, arc.y, MathUtils.radDeg * (arc.a2 + arc.a1)/2f, MathUtils.radDeg * Math.abs(arc.a2 - arc.a1)/2f);
            light.setXray(false);
            lights.add(light);
        });

        if (!world.isSecondSpawn()) {
            task.in(0.5f, () -> world.postEvent(new HelpEvent("Hint: Press 1 / 2 / 3 to switch weapon / tool")));
            task.in(8, () -> world.postEvent(new HelpEvent("They just keep coming!!\n\nI might have something more useful in my 3rd pocket...")));
            task.in(20, () -> world.postEvent(new HelpEvent("That looks dangerously far away...\n\nI better use my whip [1] to swing across.")));
        }
    }

    private void addLava(Rectangle rect, boolean end) {
        boolean firstRow = true;
        for (float y = rect.y + rect.height; y > rect.y; y -= 0.15f) {
            Color color = new Color(MathUtils.random(0.5f, 1f), MathUtils.random(0, 0.3f), 0, 1f);
            float t = (float)Math.random();
            float xOffs = MathUtils.random(-0.5f, 0.5f);
            LavaRect e = new LavaRect(rect, new Rectangle(rect.x, y - 0.5f, rect.width, 0.5f), color, t, xOffs);
            if (firstRow) {
                for (float x = 1f; x < rect.width - 1; x += 1) {
                    e.particles.add(Resource.GFX.partLavasparks.obtain());
                    e.particleOffsets.add(x);
                }
            }
            lavaRects.add(e);
            if (end) {
                endLavaRects.add(e);
            }
            firstRow = false;
        }
    }

    @Override
    public Rectangle getBoundingBox() {
        return dimensions;
    }

    @Override
    public Rectangle getViewportBox() {
        return cameraBounds;
    }

    @Override
    public Vector2 getSpawn() {
        return spawn;
    }

    @Override
    public int getScoreGoal() {
        return 10000;
    }

    @Override
    public float getTime() {
        return 300;
    }

    @Override
    protected void doAct(float delta) {
        if (world.isEndSequence2()) {
            if (endLavaRects.get(0).rect.y < lavaMax) {
                float yOffs = delta * 0.75f;
                for (LavaRect lavaRect : endLavaRects) {
                    lavaRect.rect.setY(lavaRect.rect.y + yOffs);
                }
                endKillbox.offsetY(yOffs);
            }
        }
        for (LavaRect lavaRect : lavaRects) {
            List<ParticleEffectPool.PooledEffect> particles1 = lavaRect.particles;
            for (int i = 0; i < particles1.size(); i++) {
                ParticleEffectPool.PooledEffect particle = particles1.get(i);
                particle.setPosition(lavaRect.rect.x + lavaRect.particleOffsets.get(i), lavaRect.rect.y + lavaRect.rect.height / 2f);
                particle.update(delta);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (levelBgTexture != null) {
            batch.draw(levelBgTexture, 0, 0, dimensions.width, dimensions.height);
        }
        for (ParticleEffectPool.PooledEffect particle : particlesBg) {
            drawParticle(batch, particle);
        }
        batch.draw(levelTexture, 0, 0, dimensions.width, dimensions.height);
        for (ParticleEffectPool.PooledEffect particle : particles) {
            drawParticle(batch, particle);
        }
        for (LavaRect lavaRect : lavaRects) {
            batch.setColor(lavaRect.color);
            float offs = (float) Math.sin((stateTime + lavaRect.t) * Math.PI) * 0.05f;
            float arSrc = Resource.GFX.lava.getWidth() / (float)Resource.GFX.lava.getHeight();
            float arDest = lavaRect.rect.width/lavaRect.rect.height;
            batch.draw(Resource.GFX.lava, lavaRect.rect.x, lavaRect.rect.y, lavaRect.rect.width, lavaRect.rect.height, offs + lavaRect.offs, 0, arDest/arSrc + offs + lavaRect.offs, 1);
            batch.setColor(Color.WHITE);
            for (ParticleEffectPool.PooledEffect particle : lavaRect.particles) {
                particle.draw(batch);
            }
        }
    }

    private void drawParticle(Batch batch, ParticleEffectPool.PooledEffect effect) {
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch);
    }

    @Override
    protected void doRemove() {
        for (Body b : bodies) {
            world.box2dWorld.destroyBody(b);
        }
        for (ParticleEffectPool.PooledEffect p : particles) {
            p.free();
        }
        for (ParticleEffectPool.PooledEffect p : particlesBg) {
            p.free();
        }
        super.doRemove();
    }
}

class LavaRect {
    Rectangle clip, rect;
    Color color;
    float t, offs;
    public List<ParticleEffectPool.PooledEffect> particles = new ArrayList<>();
    public List<Float> particleOffsets = new ArrayList<>();

    public LavaRect(Rectangle clip, Rectangle rect, Color color, float t, float offs) {
        this.clip = clip;
        this.rect = rect;
        this.color = color;
        this.t = t;
        this.offs = offs;
    }
}