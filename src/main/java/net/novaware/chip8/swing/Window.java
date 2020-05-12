package net.novaware.chip8.swing;

import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import net.novaware.chip8.swing.status.StatusBarViewImpl;
import net.novaware.chip8.swing.ui.JDisplay;

import javax.swing.*;
import java.awt.*;

public class Window {

    private JFrame frame;

    private MenuBarViewImpl menuBar;

    private JDisplay display;

    private StatusBarViewImpl statusBar;

    public Window() {
        initialize();
    }

    public void initialize() {
        frame = initFrame();

        menuBar = new MenuBarViewImpl();
        menuBar.initialize();

        frame.setJMenuBar(menuBar.getComponent());

        //display = new JDisplay();
        //frame.add(display.getComponent());

        statusBar = new StatusBarViewImpl();
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
