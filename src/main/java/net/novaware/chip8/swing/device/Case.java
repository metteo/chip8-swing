package net.novaware.chip8.swing.device;

import javax.swing.*;
import javax.swing.border.BevelBorder;
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

    public Case() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(50, 50);
        setFocusable(true); //TODO: input focus should be probably on something more specific?
        requestFocusInWindow();

        setLayout(new BorderLayout());

        statusPanel = getStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        JLabel statusLabel = getStatusLabel();
        statusPanel.add(statusLabel);
        statusConsumer = fps -> statusLabel.setText("FPS: " + fps);

        setupAutoPause();
    }

    //TODO: RES/RUN switch status, PWR light, Q light (sound)
    private JLabel getStatusLabel() {
        JLabel statusLabel = new JLabel("FPS: ");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        return statusLabel;
    }

    private JPanel getStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        //statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        return statusPanel;
    }



    void setupAutoPause() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (autoPauseEnabled && pauseConsumer != null) {
                    pauseConsumer.accept(false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (autoPauseEnabled && pauseConsumer != null) {
                    pauseConsumer.accept(true);
                }
            }
        });
    }
}
