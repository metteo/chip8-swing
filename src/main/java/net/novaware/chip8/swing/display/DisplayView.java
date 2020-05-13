package net.novaware.chip8.swing.display;

import net.novaware.chip8.swing.mvp.View;
import net.novaware.chip8.swing.ui.DisplayModel;
import net.novaware.chip8.swing.ui.JDisplay;

import java.util.function.IntConsumer;

public interface DisplayView extends View {

    JDisplay getComponent();

    DisplayModel getModel();

    void addFpsConsumer(IntConsumer fpsConsumer);

    JDisplay.Style getStyle();

    void setStyle(JDisplay.Style style);

    int getScale();

    void setScale(int scale);

    void initialize();
}
