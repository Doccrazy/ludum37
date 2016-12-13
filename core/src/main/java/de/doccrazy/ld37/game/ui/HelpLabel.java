package de.doccrazy.ld37.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import de.doccrazy.ld37.core.Resource;
import de.doccrazy.ld37.game.world.GameWorld;
import de.doccrazy.ld37.game.world.HelpEvent;

import java.util.ArrayList;
import java.util.List;

public class HelpLabel extends Label {
    private GameWorld world;
    private List<String> helpTexts = new ArrayList<>();
    private float stateTime;

    public HelpLabel(GameWorld world) {
        super("", new LabelStyle(new BitmapFont(), new Color(0.5f, 1f, 1f, 0.75f)));
        this.world = world;

        setAlignment(Align.center);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        world.pollEvents(HelpEvent.class, helpEvent -> {
            if (helpTexts.size() == 0) {
                stateTime = 0;
            }
            helpTexts.add(helpEvent.getText());
        });

        //setVisible(helpTexts.size() > 0);

        setText(helpTexts.size() > 0 ? helpTexts.get(0) : "");

        if (stateTime > 5f && helpTexts.size() > 0) {
            helpTexts.remove(0);
            stateTime = 0f;
        }
    }
}
