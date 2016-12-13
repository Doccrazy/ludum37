package de.doccrazy.ld37.game.world;

import de.doccrazy.shared.game.event.Event;

public class HelpEvent extends Event {
    private final String text;

    public HelpEvent(String text) {
        super(0, 0);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
