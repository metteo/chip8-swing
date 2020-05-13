package net.novaware.chip8.swing.window;

import net.novaware.chip8.swing.mvp.Presenter;

import java.awt.image.BufferedImage;

public interface WindowPresenter extends Presenter<WindowView> {
    void setPath(String path);

    void setOtherWindow(WindowPresenter windowPresenter);

    void setIcon(BufferedImage bufferedImage);

    void start();
}
