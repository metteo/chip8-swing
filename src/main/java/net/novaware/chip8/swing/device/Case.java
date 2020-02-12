package net.novaware.chip8.swing.device;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Computer Case a.k.a. Enclosure
 *
 * Contains:
 * Screen
 * Keyboard
 * Buzzer
 *
 * Tape is an attachment
 */
public class Case extends JFrame {

    public Case() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(50, 50);
        setFocusable(true); //TODO: input focus should be probably on something more specific?
        requestFocusInWindow();
    }

    void setupAutoPause() { //TODO: reimplement it
        //TODO: configurable, default true, gaining focus should request confirmation for unpause
        final AtomicBoolean paused = new AtomicBoolean();

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //console.setTitle(title);
                paused.set(false);
            }

            @Override
            public void focusLost(FocusEvent e) {
                //console.setTitle(title + " - Paused");
                paused.set(true);
            }
        });
    }
}
