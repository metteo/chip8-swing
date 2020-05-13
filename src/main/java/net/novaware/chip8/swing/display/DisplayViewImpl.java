package net.novaware.chip8.swing.display;

import net.novaware.chip8.swing.ui.DefaultDisplayModel;
import net.novaware.chip8.swing.ui.DisplayModel;
import net.novaware.chip8.swing.ui.JDisplay;

import java.awt.*;
import java.util.function.IntConsumer;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class DisplayViewImpl implements DisplayView {

    private DisplayModel model;
    private JDisplay component;

    public DisplayViewImpl() {
        model = new DefaultDisplayModel();
        component = new JDisplay(model);
    }

    @Override
    public void initialize() {
        component.setPreferredScale(10);
    }

    @Override
    public JDisplay getComponent() {
        return component;
    }

    @Override
    public DisplayModel getModel() {
        return model;
    }

    @Override
    public void addFpsConsumer(IntConsumer fpsConsumer) {
        assertArgument(fpsConsumer != null, "fpsConsumer must not be null");

        component.addPropertyChangeListener("fps", pce -> fpsConsumer.accept((Integer) pce.getNewValue()));
    }

    @Override
    public JDisplay.Style getStyle() {
        return component.getStyle();
    }

    @Override
    public void setStyle(JDisplay.Style style) {
        component.setStyle(style);

        switch(style) {
            case SOLID:
            case BORDERED:
                component.setBackground(Color.GRAY);
                component.setForeground(Color.WHITE);
                component.setGhost(Color.BLACK);
                break;
            case BRICKED:
                component.setBackground(new Color(0xADBBAD));
                component.setForeground(Color.BLACK);
                component.setGhost(new Color(0xA9B4A7));
                break;
        }

        component.repaint();
    }

    @Override
    public int getScale() {
        return component.getScale();
    }

    @Override
    public void setScale(int scale) {
        component.setPreferredScale(scale);
        component.repaint();
    }
}
