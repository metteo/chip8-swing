package net.novaware.chip8.swing.window;

import net.novaware.chip8.swing.display.DisplayView;
import net.novaware.chip8.swing.menu.MenuBarView;
import net.novaware.chip8.swing.mvp.View;
import net.novaware.chip8.swing.status.StatusBarView;

import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

public interface WindowView extends View {

    void setVisible(boolean visible);
    void exit();

    MenuBarView getMenuBar();
    DisplayView getDisplay();
    StatusBarView getStatusBar();

    Consumer<FocusListener> getFocusRegistry();
    Consumer<KeyListener> getKeyRegistry();

    void setIcon(Image icon);
    void setTitle(String title);

    void updateSize();

    void setDecorated(boolean decorationSelected);
    boolean isDecorated();

    void setFullScreen(boolean fullScreen);

    void requestFocus();

    void setCloseOnExit(boolean b);
}
