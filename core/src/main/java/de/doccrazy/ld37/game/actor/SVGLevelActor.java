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
import com.badlogic.gdx.utils.XmlReader;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.GameRules;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.RandomEvent;
import de.doccrazy.shared.game.svg.SVGLayer;

import java.util.ArrayList;
import java.util.List;

public class SVGLevelActor extends Level {
    public static final String LAYER_PHYSICS = "Physics";
    public static final String LAYER_META = "Meta";
    public static final String LABEL_SCREEN = "screen";
    public static final String LABEL_SPAWN = "spawn";
    public static final String PREFIX_PARTICLE = "part:";

    private final Rectangle dimensions, cameraBounds;
    private final Vector2 spawn;
    private final TextureRegion levelTexture;
    private final List<Body> bodies = new ArrayList<>();
    private final List<ParticleEffectPool.PooledEffect> particles = new ArrayList<>();
    private List<Circle> batSpawners = new ArrayList<>();
    private List<RandomEvent> batSpawnTimers = new ArrayList<>();
    private List<LavaRect> lavaRects = new ArrayList<>();

    public SVGLevelActor(GameWorld world, XmlReader.Element levelElement, TextureRegion levelTexture) {
        super(world);
        this.levelTexture = levelTexture;

        SVGLayer rootLayer = new SVGLayer(levelElement);
        Vector2 cameraBoundsForScale = rootLayer.getLayerByLabel(LAYER_META).getRectSizeImmediate(LABEL_SCREEN);
        float scale = GameRules.LEVEL_HEIGHT / cameraBoundsForScale.y;

        rootLayer.applyScale(scale);
        dimensions = rootLayer.getDimensionsTransformed();

        SVGLayer metaLayer = rootLayer.getLayerByLabel(LAYER_META);

        spawn = metaLayer.getRectCenter(LABEL_SPAWN);
        Vector2[] boundsPoly = metaLayer.getRectAsPoly(LABEL_SCREEN);
        cameraBounds = new Rectangle(boundsPoly[0].x, boundsPoly[0].y, boundsPoly[2].x - boundsPoly[0].x, boundsPoly[2].y - boundsPoly[0].y);

        SVGLayer physicsLayer = rootLayer.getLayerByLabel(LAYER_PHYSICS);
        physicsLayer.createPhysicsBodiesRecursive(bodyBuilder -> bodies.add(bodyBuilder.build(world)));

        metaLayer.processCircleByPrefix("bat", (s, circle, color) -> {
            batSpawners.add(circle);
            batSpawnTimers.add(new RandomEvent(0.5f, 3f));
        });
        metaLayer.processRectAsPolyByPrefix("lava", (s, rect, color) -> {
            addLava(new Rectangle(Math.min(rect[0].x, rect[2].x), Math.min(rect[0].y, rect[2].y), Math.abs(rect[2].x - rect[0].x), Math.abs(rect[2].y - rect[0].y)));
        });

        /*metaLayer.processRectCenterByPrefix(PREFIX_PARTICLE, (type, center, color) -> {
            ParticleEffectPool.PooledEffect particle = Resource.GFX.particles.get(type).obtain();
            particle.setPosition(center.x, center.y);
            particles.add(particle);
        });
        metaLayer.processRectAsPolyByPrefix("kill", (s, rect, color) -> {
            world.addActor(new KillboxActor(world, rect));
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
            light.setXray(true);
            lights.add(light);
        });*/
    }

    private void addLava(Rectangle rect) {
        boolean firstRow = true;
        for (float y = rect.y + rect.height; y > rect.y; y -= 0.15f) {
            Color color = new Color(MathUtils.random(0.5f, 1f), MathUtils.random(0, 0.3f), 0, 1f);
            float xOffs = MathUtils.random(-0.5f, 0.5f);
            float t = (float)Math.random();
            for (float x = rect.x - 1f; x < rect.x + rect.width + 1f; x += 4) {
                LavaRect e = new LavaRect(new Rectangle(x + xOffs, y, 4, 0.5f), color, t);
                if (firstRow) {
                    e.particle = Resource.GFX.partLavasparks.obtain();
                }
                lavaRects.add(e);
                System.out.println(lavaRects.get(lavaRects.size()-1).rect);
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
        for (int i = 0; i < batSpawners.size(); i++) {
            Circle spawner = batSpawners.get(i);
            RandomEvent timer = batSpawnTimers.get(i);
            if (timer.apply(delta)) {
                Vector2 spawn = new Vector2(spawner.x, spawner.y).add(new Vector2(1, 1).rotate(MathUtils.random(0, 360)).scl(spawner.radius));
                world.addActor(new BatActor(world, spawn));
            }
        }
        for (LavaRect lavaRect : lavaRects) {
            if (lavaRect.particle != null) {
                lavaRect.particle.setPosition(lavaRect.rect.x + lavaRect.rect.width/2f, lavaRect.rect.y + lavaRect.rect.height/2f);
                lavaRect.particle.update(delta);
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (LavaRect lavaRect : lavaRects) {
            batch.setColor(lavaRect.color);
            batch.draw(Resource.GFX.lava, lavaRect.rect.x + (float)Math.sin((stateTime + lavaRect.t)*Math.PI)*0.2f, lavaRect.rect.y, lavaRect.rect.width, lavaRect.rect.height);
            batch.setColor(Color.WHITE);
            if (lavaRect.particle != null) {
                lavaRect.particle.draw(batch);
            }
        }
        batch.draw(levelTexture, 0, 0, dimensions.width, dimensions.height);
        for (ParticleEffectPool.PooledEffect particle : particles) {
            drawParticle(batch, particle);
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
        super.doRemove();
    }
}

class LavaRect {
    Rectangle rect;
    Color color;
    float t;
    public ParticleEffectPool.PooledEffect particle;

    public LavaRect(Rectangle rect, Color color, float t) {
        this.rect = rect;
        this.color = color;
        this.t = t;
    }
}