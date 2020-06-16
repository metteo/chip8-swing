package net.novaware.chip8.swing.ui;

import com.jhlabs.image.BoxBlurFilter;
import net.novaware.chip8.core.util.FrequencyCounter;
import net.novaware.chip8.swing.ui.JDisplay.Style;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Second attempt at glowing effect. backBuffer is used always, but for glow effect only for ON pixels.
 * Works nice on scale up to 7, full screen unplayable.
 *
 * Optimizes rendering of pixel images - happens only if needed instead of every frame.
 */
public class JDisplay3 extends JComponent {

    private DisplayModel model;

    private int scale = 1;

    private Style style = Style.SOLID;

    private Color background = Color.GRAY;
    private Color foreground = Color.WHITE;
    private Color ghost = Color.BLACK;

    private BufferedImage backBuffer;
    private Graphics2D backGraphics;

    private BufferedImage pixelOn;
    private BufferedImage pixelOff;

    private BoxBlurFilter boxBlur = new BoxBlurFilter();

    private FrequencyCounter fpsCounter = new FrequencyCounter(20, 0.1);

    public JDisplay3(DisplayModel model) {
        this.model = model;

        setBackground(Color.GRAY);
        setForeground(Color.WHITE);

        updateImageBuffers();
        updatePixelOn();
        updatePixelOff();

        model.addDataUpdateListener(pce -> {
            SwingUtilities.invokeLater(this::repaint);
        });

        boxBlur.setRadius(3f);
        boxBlur.setIterations(3);
        boxBlur.setPremultiplyAlpha(true);

        fpsCounter.initialize();
        fpsCounter.subscribe(fc -> {
            firePropertyChange("fps", 0, fc.getFrequency());
        });
    }

    public void setStyle(Style style) {
        this.style = style;

        updatePixelOn();
        updatePixelOff();
    }

    public Style getStyle() {
        return style;
    }

    public int getScale() {
        return scale;
    }

    public void setPreferredScale(int scale) {
        this.scale = scale;

        int newWidth = scale * model.getColumnCount();
        int newHeight = scale * model.getRowCount();

        setPreferredSize(new Dimension(newWidth, newHeight));

        updateImageBuffers();
        updatePixelOn();
        updatePixelOff();
    }

    private int calculateScale() {
        final int width = getWidth();
        final int height = getHeight();

        final int maxColumns = width / model.getColumnCount();
        final int maxRows = height / model.getRowCount();

        final int scale = Math.min(maxColumns, maxRows);

        return scale;
    }

    private void maybeUpdateScale() {
        final int calculatedScale = calculateScale();
        if (calculatedScale != scale) {
            if (calculatedScale < 1) {
                return;
            }

            int oldScale = scale;
            scale = calculatedScale;
            firePropertyChange("scale", oldScale, scale);

            updateImageBuffers();
            updatePixelOn();
            updatePixelOff();
        }
    }

    private void updateImageBuffers() {
        if (backGraphics != null) {
            backGraphics.dispose();
            backGraphics = null;
        }

        final GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        backBuffer = graphicsConfiguration.createCompatibleImage(
                scale * model.getColumnCount(),
                scale * model.getRowCount(),
                Transparency.TRANSLUCENT
        );

        backGraphics = (Graphics2D) backBuffer.getGraphics();

        pixelOn = graphicsConfiguration.createCompatibleImage(scale, scale);
        pixelOff = graphicsConfiguration.createCompatibleImage(scale, scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        fpsCounter.takeASample();

        maybeUpdateScale();

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        clearGraphics2D(backGraphics, backBuffer);

        for (int row = 0; row < model.getRowCount(); ++row) {
            for (int col = 0; col < model.getColumnCount(); ++col) {
                final boolean pixelOn = model.isPixelOn(col, row);
                BufferedImage pixel = pixelOn ? this.pixelOn : pixelOff;

                if(style == Style.GLOW) {
                    if (pixelOn) {
                        backGraphics.drawImage(pixel, scale * col, scale * row, null);
                    } else {
                        g2d.drawImage(pixel, paddingLeft + scale * col, paddingTop + scale * row, null);
                    }
                } else {
                    backGraphics.drawImage(pixel, scale * col, scale * row, null);
                }
            }
        }

        if (style == Style.GLOW) {
            g2d.drawImage(backBuffer, boxBlur, paddingLeft, paddingTop);
            //g2d.drawImage(backBuffer, paddingLeft, paddingTop, null);
        } else {
            g2d.drawImage(backBuffer, paddingLeft, paddingTop, null);
        }

        fpsCounter.maybePublish();
    }

    private void clearGraphics2D(Graphics2D fbg, BufferedImage foregroundBuffer) {
        fbg.setComposite(AlphaComposite.Clear);
        fbg.fillRect(0, 0, foregroundBuffer.getWidth(), foregroundBuffer.getHeight());
        fbg.setPaintMode();
    }

    private void updatePixelOff() {
        final Graphics g = pixelOff.getGraphics();

        switch(style) {
            case BORDERED:
            case GLOW:
            case SOLID:
                g.setColor(ghost);
                g.fillRect(0, 0, scale, scale);
                break;

            case BRICKED:
                g.setColor(background);
                g.fillRect(0, 0, scale, scale);

                g.setColor(ghost);
                drawBrick(g);
                break;
        }

        g.dispose();
    }

    private void updatePixelOn() {
        final Graphics g = pixelOn.getGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, scale, scale);

        g.setColor(getForeground());

        int size = scale;

        switch(style) {
            case BORDERED:
                size -= 1;
                //!break;
            case GLOW:
            case SOLID:
                g.fillRect(0, 0, size, size);
                break;

            case BRICKED:
                drawBrick(g);
                break;
        }
        g.dispose();
    }

    private void drawBrick(Graphics g) {
        if (scale < 4) { // 1, 2, 3
            g.fillRect(0, 0, scale, scale);
        } else if (scale < 7) { // 4, 5, 6
            g.fillRect(0, 0, scale - 1, scale - 1);
        } else if (scale < 9) { // 7, 8
            g.drawRect(0, 0, scale - 2, scale - 2);
            g.fillRect(2, 2, scale - 3, scale - 3);
        } else if (scale < 19) { // 9 - 18
            g.drawRect(0, 0, scale - 2, scale - 2);
            g.fillRect(2, 2, scale - 5, scale - 5);
        } else { // 19, ...
            g.drawRect(0, 0, scale - 2, scale - 2);
            g.drawRect(1, 1, scale - 4, scale - 4);
            g.fillRect(4, 4, scale - 9, scale - 9);
        }
    }

    private int getPaddingTop() {
        return (getHeight() - scale * model.getRowCount()) / 2;
    }

    private int getPaddingLeft() {
        return (getWidth() - scale * model.getColumnCount()) / 2;
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public void setBackground(Color background) {
        this.background = background;
    }

    @Override
    public Color getForeground() {
        return foreground;
    }

    @Override
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setGhost(Color ghost) {
        this.ghost = ghost;
    }
}
