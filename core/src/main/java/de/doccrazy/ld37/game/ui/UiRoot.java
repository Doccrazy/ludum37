package de.doccrazy.ld37.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.doccrazy.ld37.game.GameRenderer;
import de.doccrazy.ld37.game.world.FloatingTextEvent;
import de.doccrazy.ld37.game.world.GameInputListener;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.shared.game.ui.UiBase;

public class UiRoot extends UiBase<GameWorld, GameRenderer, GameInputListener> {
    //private Toolbar toolbar;

    public UiRoot(Stage stage, GameWorld world, GameRenderer renderer) {
        super(stage, world, renderer);

        /*toolbar = new Toolbar(this);
        toolbar.setVisible(false);
        left().add(toolbar);*/

        //add(new ScoreLabel(world)).pad(5);
        row().expandY();
        add(new HelpLabel(world)).expandX().center();
        row().expandY();
        add(new HealthLabel(world)).expandX().bottom().left();

        stage.addActor(new IntroScreen(this));
        stage.addActor(new DefeatScreen(this));
        stage.addActor(new VictoryScreen(this));
        stage.addActor(new VictoryEmptyScreen(this));

        /*getStage().addActor(new DeathLabel(getWorld()));
        getStage().addActor(new DeathLabel2(getWorld()));*/
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        getWorld().pollEvents(FloatingTextEvent.class, event -> new FloatingTextLabel(this, event));
    }

    @Override
    protected InputListener createUiInput() {
        return new UiInputListener(this);
    }

    @Override
    protected GameInputListener createGameInput() {
        return new GameInputListener(this);
    }
}
