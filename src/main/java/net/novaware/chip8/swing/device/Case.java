package net.novaware.chip8.swing.device;

import net.novaware.chip8.swing.status.StatusBarViewImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

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

    //TODO: figure out how to handle status and menu events
    public Consumer<Integer> statusConsumer;
    public Consumer<Boolean> pauseConsumer;
    public boolean autoPauseEnabled = true;
    public Runnable resetConsumer;
    public final JPanel statusPanel;
    public final StatusBarViewImpl statusBar;

    public Case() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(50, 50);
        setFocusable(true); //TODO: input focus should be probably on something more specific?
        requestFocusInWindow();

        setLayout(new BorderLayout());

        statusBar = new StatusBarViewImpl();
        statusBar.initialize();
        statusPanel = statusBar.getComponent();
        add(statusPanel, BorderLayout.SOUTH);

        statusConsumer = fps -> {
            statusBar.setFps(fps);};

        setupAutoPause();
    }

    void setupAutoPause() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (autoPauseEnabled && pauseConsumer != null) {
                    pauseConsumer.accept(false);
                    statusBar.setPowerOn(true);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (autoPauseEnabled && pauseConsumer != null) {
                    pauseConsumer.accept(true);
                    statusBar.setPowerOn(false);
                }
            }
        });
    }
}
