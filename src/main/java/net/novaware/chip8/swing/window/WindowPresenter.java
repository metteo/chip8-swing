package net.novaware.chip8.swing.window;

import net.novaware.chip8.swing.mvp.Presenter;

public interface WindowPresenter extends Presenter<WindowView> {
    void start();

    void setOtherWindow(WindowPresenter windowPresenter);
}
