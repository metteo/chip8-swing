package net.novaware.chip8.swing.mvp;

public interface Presenter<V extends View> {

    V getView();

    void initialize();
}
