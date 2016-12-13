package de.doccrazy.ld37.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.world.GameState;

public class HealthLabel extends Label {
	private GameWorld world;

	public HealthLabel(GameWorld world) {
		super("", new LabelStyle(Resource.FONT.retroSmall, new Color(1f, 1f, 1f, 0.7f)));
		this.world = world;

		setAlignment(Align.left);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
        setVisible(world.getGameState() == GameState.GAME || world.isGameFinished());

		setText("Health: " + Math.max(0, (int)(world.getPlayer().getHealth())));
	}

}
