package net.novaware.chip8.swing.device;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public Case() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(50, 50);
        setFocusable(true); //TODO: input focus should be probably on something more specific?
        requestFocusInWindow();

        setLayout(new BorderLayout());

        JPanel statusPanel = getStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        JLabel statusLabel = getStatusLabel();
        statusPanel.add(statusLabel);
        statusConsumer = fps -> statusLabel.setText("FPS: " + fps);

        setJMenuBar(getMenuBar0());

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

    private JMenuBar getMenuBar0() {
        final JMenuBar menuBar = new JMenuBar();

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu");
        menuBar.add(fileMenu);

        JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
        open.getAccessibleContext().setAccessibleDescription("Open ROM file");
        open.addActionListener(ae -> fileChooser.showOpenDialog(Case.this)); //TODO: handle return value
        fileMenu.add(open);

        JMenuItem close = new JMenuItem("Close", KeyEvent.VK_C);
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        close.getAccessibleContext().setAccessibleDescription("Close current ROM");
        fileMenu.add(close);

        fileMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
        exit.getAccessibleContext().setAccessibleDescription("Exit the program");
        exit.addActionListener(ae -> Case.this.dispatchEvent(new WindowEvent(Case.this, WindowEvent.WINDOW_CLOSING)));
        fileMenu.add(exit);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("Edit menu");
        menuBar.add(editMenu);

        JMenuItem reset = new JMenuItem("Reset", KeyEvent.VK_R);
        reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        reset.getAccessibleContext().setAccessibleDescription("Reset the CPU");
        reset.addActionListener(ae -> { if (resetConsumer != null) {resetConsumer.run();}});
        editMenu.add(reset);

        //TODO: add resume item, disable one or the other when un/paused
        JMenuItem pause = new JMenuItem("Pause", KeyEvent.VK_P);
        pause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
        pause.getAccessibleContext().setAccessibleDescription("Pause the CPU");
        pause.addActionListener(ae -> { if (pauseConsumer != null) {pauseConsumer.accept(true);}});
        editMenu.add(pause);

        editMenu.addSeparator();

        final JCheckBoxMenuItem autoPause = new JCheckBoxMenuItem("Auto-pause");
        autoPause.setMnemonic(KeyEvent.VK_A);
        autoPause.setState(autoPauseEnabled);
        autoPause.addActionListener(ae -> {
            autoPauseEnabled = autoPause.getState();
            pauseConsumer.accept(false);
        });
        editMenu.add(autoPause);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.getAccessibleContext().setAccessibleDescription("View menu");
        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu");
        menuBar.add(helpMenu);

        return menuBar;
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
