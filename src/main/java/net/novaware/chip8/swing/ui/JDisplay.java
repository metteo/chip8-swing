package net.novaware.chip8.swing.ui;

import net.novaware.chip8.core.util.FrequencyCounter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JDisplay extends JComponent {

    public enum Style {
        SOLID,
        GLOW,
        BORDERED,
        BRICKED
    }
    private DisplayModel model;

    private int scale = 1;

    private Style style = Style.SOLID;

    private Color background = Color.GRAY;
    private Color foreground = Color.WHITE;
    private Color ghost = Color.BLACK;

    private BufferedImage pixelOn;
    private BufferedImage pixelOff;

    private FrequencyCounter fpsCounter = new FrequencyCounter(20, 0.1);

    public JDisplay(DisplayModel model) {
        this.model = model;

        setBackground(Color.GRAY);
        setForeground(Color.WHITE);

        updateImageBuffers();

        model.addDataUpdateListener(pce -> {
            SwingUtilities.invokeLater(this::repaint);
        });

        fpsCounter.initialize();
        fpsCounter.subscribe(fc -> {
            firePropertyChange("fps", 0, fc.getFrequency());
        });
    }

    public void setStyle(Style style) {
        this.style = style;
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
        }
    }

    private void updateImageBuffers() {
        pixelOn = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);
        pixelOff = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        fpsCounter.takeASample();

        maybeUpdateScale();
        updatePixelOn();
        updatePixelOff();

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int row = 0; row < model.getRowCount(); ++row) {
            for (int col = 0; col < model.getColumnCount(); ++col) {
                Image pixel = model.isPixelOn(col, row) ? pixelOn : pixelOff;

                g.drawImage(pixel, paddingLeft + scale * col, paddingTop + scale * row, null);
            }
        }

        fpsCounter.maybePublish();
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
