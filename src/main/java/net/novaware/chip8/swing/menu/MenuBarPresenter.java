package net.novaware.chip8.swing.menu;

import net.novaware.chip8.swing.mvp.Presenter;

public interface MenuBarPresenter extends Presenter<MenuBarView> {
    void onFocusChange(boolean gained);

    void forcePause();
}
