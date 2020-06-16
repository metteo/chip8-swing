package net.novaware.chip8.swing.ui;

import com.jhlabs.image.BoxBlurFilter;
import net.novaware.chip8.core.util.FrequencyCounter;
import net.novaware.chip8.swing.ui.JDisplay.Style;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * First attempt at glowing effect. Using additional buffers in such manner slowed down regular rendering a lot.
 */
public class JDisplay2 extends JComponent {

    private DisplayModel model;

    private int scale = 1;

    private Style style = Style.SOLID;

    private Color background = Color.GRAY;
    private Color foreground = Color.WHITE;
    private Color ghost = Color.BLACK;

    private BufferedImage backgroundBuffer;
    private BufferedImage foregroundBuffer;

    private BufferedImage pixelOn;
    private BufferedImage pixelOff;

    private BoxBlurFilter boxBlur = new BoxBlurFilter();

    private FrequencyCounter fpsCounter = new FrequencyCounter(20, 0.1);

    public JDisplay2(DisplayModel model) {
        this.model = model;

        setBackground(Color.GRAY);
        setForeground(Color.WHITE);

        updateImageBuffers();

        model.addDataUpdateListener(pce -> {
            SwingUtilities.invokeLater(this::repaint);
        });

        boxBlur.setRadius(2f);
        boxBlur.setIterations(2);
        boxBlur.setPremultiplyAlpha(true);

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
        final int width = scale * model.getColumnCount();
        final int height = scale * model.getRowCount();

        //FIXME: these buffers made solid mode a lot slower in full screen ~120fps -> ~30fps
        backgroundBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        foregroundBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        pixelOn = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_ARGB);
        pixelOff = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        fpsCounter.takeASample();

        maybeUpdateScale();
        updatePixelOn();
        updatePixelOff();
        fillBuffers();

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.drawImage(backgroundBuffer, paddingLeft, paddingTop, null);

        if (style == Style.GLOW) {
            g2d.drawImage(foregroundBuffer, boxBlur, paddingLeft, paddingTop);
        }

        g2d.drawImage(foregroundBuffer, paddingLeft, paddingTop, null);

        fpsCounter.maybePublish();
    }

    private void fillBuffers() {
        final Graphics2D bbg = (Graphics2D) backgroundBuffer.getGraphics();
        clearGraphics2D(bbg, backgroundBuffer);

        final Graphics2D fbg = (Graphics2D) foregroundBuffer.getGraphics();
        clearGraphics2D(fbg, foregroundBuffer);

        for (int row = 0; row < model.getRowCount(); ++row) {
            for (int col = 0; col < model.getColumnCount(); ++col) {
                boolean isPixelOn = model.isPixelOn(col, row);
                Image pixel = isPixelOn ? pixelOn : pixelOff;
                Graphics bufferGraphics = isPixelOn ? fbg : bbg;

                bufferGraphics.drawImage(pixel, scale * col, scale * row, null);
            }
        }

        bbg.dispose();
        fbg.dispose();
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
