package net.novaware.chip8.swing.status;

import net.novaware.chip8.swing.mvp.AbstractPresenter;

public class StatusBarPresenterImpl extends AbstractPresenter<StatusBarView> implements StatusBarPresenter {

    public StatusBarPresenterImpl(StatusBarView view) {
        super(view);
    }
}
