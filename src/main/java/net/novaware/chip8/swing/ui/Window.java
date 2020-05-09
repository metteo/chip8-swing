package net.novaware.chip8.swing.ui;

import net.novaware.chip8.swing.display.Display;
import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import net.novaware.chip8.swing.status.StatusBar;

import javax.swing.*;
import java.awt.*;

public class Window {

    private JFrame frame;

    private MenuBarViewImpl menuBar;

    private Display display;

    private StatusBar statusBar;

    public Window() {
        initialize();
    }

    public void initialize() {
        frame = initFrame();

        menuBar = new MenuBarViewImpl();
        menuBar.initialize();

        frame.setJMenuBar(menuBar.getComponent());

        display = new Display();
        frame.add(display.getComponent());

        statusBar = new StatusBar();
        frame.add(statusBar.getComponent(), BorderLayout.SOUTH);
    }

    private JFrame initFrame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //TODO: change to dispose
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same
        frame.setSize(400, 200);
        frame.setFocusable(true); //TODO: input focus should be probably on something more specific?
        frame.requestFocusInWindow();
        return frame;
    }

    public JFrame getFrame() {
        return frame;
    }
}
