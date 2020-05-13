package net.novaware.chip8.swing.display;

import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.swing.mvp.AbstractPresenter;
import net.novaware.chip8.swing.ui.DefaultDisplayModel;

public class DisplayPresenterImpl extends AbstractPresenter<DisplayView> implements DisplayPresenter {

    private final DisplayPort displayPort;

    public DisplayPresenterImpl(DisplayView view, DisplayPort displayPort) {
        super(view);
        this.displayPort = displayPort;
    }

    @Override
    public void initialize() {
        //TODO: presenter should set the model (MVC doesn't like MVP...)
        displayPort.connect(((DefaultDisplayModel)view.getModel())::updateWith);
    }
}
