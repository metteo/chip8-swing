package net.novaware.chip8.swing.window;

import net.novaware.chip8.swing.display.DisplayView;
import net.novaware.chip8.swing.display.DisplayViewImpl;
import net.novaware.chip8.swing.menu.MenuBarView;
import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import net.novaware.chip8.swing.status.StatusBarView;
import net.novaware.chip8.swing.status.StatusBarViewImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class WindowViewImpl implements WindowView {

    private JFrame frame;

    private MenuBarViewImpl menuBar;

    private DisplayViewImpl display;

    private StatusBarViewImpl statusBar;

    public WindowViewImpl() {
        frame = initFrame();

        menuBar = new MenuBarViewImpl();
        menuBar.initialize();
        frame.setJMenuBar(menuBar.getComponent());

        display = new DisplayViewImpl();
        display.initialize();
        frame.add(display.getComponent());

        statusBar = new StatusBarViewImpl();
        statusBar.initialize();
        frame.add(statusBar.getComponent(), BorderLayout.SOUTH);

        frame.pack();
    }

    private JFrame initFrame() {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same?
        frame.setFocusable(true); //TODO: input focus should be probably on something more specific?
        frame.requestFocusInWindow();
        return frame;
    }

    @Override
    public JComponent getComponent() {
        return frame.getRootPane();
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public MenuBarView getMenuBar() {
        return menuBar;
    }

    @Override
    public DisplayView getDisplay() {
        return display;
    }

    @Override
    public StatusBarView getStatusBar() {
        return statusBar;
    }

    @Override
    public Consumer<FocusListener> getFocusRegistry() {
        return frame::addFocusListener;
    }

    @Override
    public Consumer<KeyListener> getKeyRegistry() {
        return frame::addKeyListener;
    }

    @Override
    public void setIcon(Image image) {
        frame.setIconImage(image);
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    @Override
    public void exit() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void updateSize() {
        frame.pack();
    }

    @Override
    public void setDecorated(boolean decorated) {
        frame.dispose();
        frame.setUndecorated(!decorated);
        frame.setVisible(true);
    }

    @Override
    public boolean isDecorated() {
        return !frame.isUndecorated();
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullScreen ? frame : null);
    }

    @Override
    public void requestFocus() {
        frame.requestFocus();
    }

    @Override
    public void setCloseOnExit(boolean b) {
        frame.setDefaultCloseOperation(b ? JFrame.EXIT_ON_CLOSE : JFrame.HIDE_ON_CLOSE);
    }
}
