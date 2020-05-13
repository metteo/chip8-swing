package net.novaware.chip8.swing.mvp;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public abstract class AbstractPresenter<V extends View> implements Presenter<V> {

    protected final V view;

    protected AbstractPresenter(V view) {
        assertArgument(view != null, "view must not be null");

        this.view = view;
    }

    @Override
    public V getView() {
        return view;
    }

    @Override
    public void initialize() {
        //empty, subclasses are free to override
    }
}
