package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.gpu.ViewPort;
import net.novaware.chip8.core.port.DisplayPort;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Display device
 */
public class Screen extends JComponent {

    private Lock modelLock = new ReentrantLock();

    public Consumer<Integer> fpsConsumer;

    private static final int DEFAULT_SCALE = 10;
    private static final int COLUMNS = 64;
    private static final int ROWS = 32;

    //TODO: sync & @GuardedBy missing
    private boolean[][] model = new boolean[ROWS][COLUMNS]; // [y][x]
    private boolean[][] modelPrev = new boolean[ROWS][COLUMNS]; // [y][x]

    private static final boolean REDRAW_HEURISTIC = !false;
    private static final boolean REDRAW_HEURISTIC2 = false;

    private Integer prevChange = DisplayPort.GC_DRAW;
    private Integer lastChange = DisplayPort.GC_DRAW; //TODO: improve and use timers which repaint even earlier if next erase happens long after last draw (like game over screen)

    private long lastPaint;
    private int fps; //calculate average from 3 frames?

    private ViewPort viewPort = new ViewPort();
    private ViewPort.Bit bit = new ViewPort.Bit();
    private ViewPort.Index idx = new ViewPort.Index();

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

        modelLock.lock();

        for(int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                boolean pixel = model[y][x];
                if (REDRAW_HEURISTIC2) {
                    pixel |= modelPrev[y][x];
                }

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

    public void setModelValue(int x, int y, boolean value) {
        modelPrev[y][x] = model[y][x];
        model[y][x] = value;
    }

    public void draw(Integer currentChange, byte[] data) {
        if (REDRAW_HEURISTIC) {
            //if (prevChange != uint(GC_ERASE) && lastChange != uint(GC_ERASE) && currentChange == uint(GC_ERASE)) { //TODO: catch cases on the graph where there is multiple deletions interrupted by single draw, they should be all treated as erase and no repaint should happen?
            if ((lastChange != DisplayPort.GC_ERASE && currentChange == DisplayPort.GC_ERASE) || currentChange == DisplayPort.GC_MIX) { //this works for games, above works nice for invaders menu screen
                //System.out.println("-----");
                calculateFps();
                SwingUtilities.invokeLater(this::repaint); //FIXME deferred in the future so even though called before updateModel may execute after...
            } else {
                updateModel(data);
            }

            //System.out.println(padLeft("|", currentChange));

            prevChange = lastChange;
            lastChange = currentChange;
        } else {
            updateModel(data);
            calculateFps();
            SwingUtilities.invokeLater(this::repaint);
        }
    }


    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void updateModel(byte[] data) {
        modelLock.lock();

        try {
            for (int yCoord  = 0; yCoord < 32; ++yCoord) {
                for (int xCoord  = 0; xCoord < 64; ++xCoord) {
                    bit.x = xCoord;
                    bit.y = yCoord;

                    viewPort.toIndex(bit, idx, false);

                    byte frame = data[idx.arrayByte];
                    int mask = 0x1 << 7 - idx.byteBit;
                    int pixel = (uint(frame) & mask) >>> 7 - idx.byteBit;

                    setModelValue(xCoord, yCoord, pixel != 0);
                }
            }
        } finally {
            modelLock.unlock();
        }
    }
}
