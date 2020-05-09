package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.port.DisplayPort;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Display device
 */
public class Screen extends JComponent implements Accessible { //TODO: make accessible

    private Lock modelLock = new ReentrantLock();

    public Consumer<Integer> fpsConsumer;

    private static final int DEFAULT_SCALE = 10;
    private static final int COLUMNS = 64;
    private static final int ROWS = 32;

    //TODO: sync & @GuardedBy missing
    private boolean[][] model = new boolean[ROWS][COLUMNS]; // [y][x]

    private long lastPaint;
    private int fps; //calculate average from 3 frames?

    private static final Color BG = new Color(0xADBBAD);
    private static final Color GHOST = new Color(0xA9B4A7); //TODO: implement motion blur
    private static final Color FG = Color.BLACK;

    public Screen() {
        setPreferredSize(new Dimension(COLUMNS * DEFAULT_SCALE, ROWS * DEFAULT_SCALE));
    }

    @Override
    public void paint(Graphics g) {
        final int width = getWidth();
        final int height = getHeight();

        final int maxColumns = width / COLUMNS;
        final int maxRows = height / ROWS;

        final int scale = Math.min(maxColumns, maxRows);
        final int border = Math.max(1, scale / 10);

        final int renderingWidth = COLUMNS * scale;
        final int renderingHeight = ROWS * scale;

        final int pw = (width - renderingWidth) / 2;
        final int ph = (height - renderingHeight) / 2;

        g.setColor(BG);
        g.fillRect(0, 0, width, height);

        calculateFps();

        modelLock.lock();

        for(int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                boolean pixel = model[y][x];

                Color fg;
                if (pixel) {
                    fg = FG;
                } else {
                    fg = GHOST;
                }

                //TODO: figure out the scaling params for borders, padding, etc, these are experimental
                //Imitate Brick Game handheld console LCD
                g.setColor(fg);
                g.fillRect(x * scale + pw + border, y * scale + ph + border, scale - border, scale - border);

                g.setColor(BG);
                g.fillRect(x * scale + pw + 2 * border, y * scale + ph + 2 * border, scale - 3 * border, scale - 3 * border);

                g.setColor(fg);
                g.fillRect(x * scale + pw + 3 * border, y * scale + ph + 3 * border, scale - 5 * border, scale - 5 * border);
            }
        }

        modelLock.unlock(); //FIXME: should be in finally
    }

    private void calculateFps() {
        long now = System.nanoTime();
        fps = (int)(1e9 / (now - lastPaint));
        lastPaint = now;

        if (fpsConsumer != null) {
            fpsConsumer.accept(fps);
        }
    }

    public void draw(DisplayPort.Packet packet) {
        modelLock.lock();

        try {
            for (int y = 0; y < packet.getRowCount(); ++y) {
                for (int x = 0; x < packet.getColumnCount(); ++x) {
                    model[y][x] = packet.getPixel(x, y);
                }
            }
        } finally {
            modelLock.unlock();
        }

        SwingUtilities.invokeLater(this::repaint);
    }
}
