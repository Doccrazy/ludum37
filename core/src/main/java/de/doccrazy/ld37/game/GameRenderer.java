package de.doccrazy.ld37.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.data.GameRules;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.ScreenShakeEvent;
import de.doccrazy.shared.game.BaseGameRenderer;
import de.doccrazy.shared.game.world.GameState;
import net.dermetfan.gdx.math.GeometryUtils;

public class GameRenderer extends BaseGameRenderer<GameWorld> {
	private static final float CAM_PPS = 5f;

    private Scaling bgScaling = Scaling.fill;
	private float zoom = 1;
	private float zoomDelta = 0;
	private float camY;
    private boolean animateCamera;
	private float shakeAmount = 0;
	private float time;

    public GameRenderer(GameWorld world) {
        super(world, new Vector2(GameRules.LEVEL_WIDTH, GameRules.LEVEL_HEIGHT));
    }

    @Override
    protected void init() {
        world.rayHandler.setAmbientLight(new Color(0.6f, 0.5f, 0.5f, 1f));
    }

    @Override
	protected void drawBackground(SpriteBatch batch) {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Vector2 bgSize = bgScaling.apply(gameViewport.x, gameViewport.y, world.stage.getWidth(), world.stage.getHeight());
        //batch.draw(Resource.GFX.backgroundHigh, world.stage.getWidth() / 2 - bgSize.x / 2, 0, bgSize.x, bgSize.y);
        //batch.draw(Resource.GFX.backgroundLow, world.stage.getWidth() / 2 - bgSize.x / 2, -bgSize.y + 0.1f, bgSize.x, bgSize.y);
    }

	@Override
	protected void beforeRender() {
        shakeAmount = shakeAmount * 0.91f;
        world.pollEvents(ScreenShakeEvent.class, screenShakeEvent -> shakeAmount += 0.1f);
        if (world.isEndSequence()) {
            shakeAmount = 0.05f;
        }

	    //zoom = MathUtils.clamp(zoom + zoomDelta*0.02f, 1f, 2f);

        if (world.getGameState() != GameState.INIT) {
            Vector2 cameraCenter = GeometryUtils.keepWithin(new Vector2(world.getPlayer().getX() - GameRules.LEVEL_WIDTH / 2f, world.getPlayer().getY() - GameRules.LEVEL_HEIGHT / 2f),
                    GameRules.LEVEL_WIDTH, GameRules.LEVEL_HEIGHT,
                    0, 0, world.getLevel().getBoundingBox().width, world.getLevel().getBoundingBox().height);
            camera.position.x = cameraCenter.x + GameRules.LEVEL_WIDTH / 2f + MathUtils.random(-shakeAmount, shakeAmount);
            camera.position.y = cameraCenter.y + GameRules.LEVEL_HEIGHT / 2f + MathUtils.random(-shakeAmount, shakeAmount);
        }

	}

    @Override
    protected void renderFramebufferToScreen(SpriteBatch batch, FrameBuffer frameBuffer) {
        float dt = Gdx.graphics.getDeltaTime();
        time += dt;
        float angle = time * (2 * MathUtils.PI);
        if (angle > (2 * MathUtils.PI))
            angle -= (2 * MathUtils.PI);

        Resource.GFX.heatShader.begin();
        Resource.GFX.heatShader.setUniformf("timedelta", -angle);
        Resource.GFX.heatShader.end();

        batch.setShader(Resource.GFX.heatShader);
        super.renderFramebufferToScreen(batch, frameBuffer);
    }
}
