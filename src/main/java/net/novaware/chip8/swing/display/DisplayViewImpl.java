package net.novaware.chip8.swing.display;

import net.novaware.chip8.swing.ui.DefaultDisplayModel;
import net.novaware.chip8.swing.ui.DisplayModel;
import net.novaware.chip8.swing.ui.JDisplay;
import net.novaware.chip8.swing.ui.JDisplay2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.IntConsumer;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class DisplayViewImpl implements DisplayView {

    private static Logger LOG = LogManager.getLogger();

    private DisplayModel model;
    private JDisplay component;

    public DisplayViewImpl() {
        model = new DefaultDisplayModel();
        component = new JDisplay(model);

        //TODO: move trigger to window, and invoke a method on displayview
        //TODO: use swingworker for saving the file
        final KeyStroke keyStroke = KeyStroke.getKeyStroke("released F5");
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "screenshot");

        component.getActionMap().put("screenshot", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D imageGfx = image.createGraphics();
                component.paint(imageGfx);

                File f = new File(System.getProperty("user.home") + File.separator + "screenshot.png");

                try {
                    ImageIO.write(image, "png", f);
                    LOG.info("File saved as " + f.getAbsolutePath());
                } catch (IOException ex) {
                    LOG.warn("Unable to save " + f + ": ", ex);
                }
            }
        });
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
        switch(style) {
            case SOLID:
            case BORDERED:
            case GLOW:
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

        component.setStyle(style);

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
