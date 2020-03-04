package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.gpu.ViewPort;
import net.novaware.chip8.core.port.DisplayPort;

import javax.swing.*;
import java.awt.*;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Display device
 */
public class Screen extends JComponent {

    //TODO: sync & @GuardedBy missing
    private boolean[][] model = new boolean[32][64]; // [y][x]
    private boolean[][] modelPrev = new boolean[32][64]; // [y][x]

    private static final boolean REDRAW_HEURISTIC = !false;
    private static final boolean REDRAW_HEURISTIC2 = false;

    private Integer prevChange = DisplayPort.GC_DRAW;
    private Integer lastChange = DisplayPort.GC_DRAW; //TODO: improve and use timers which repaint even earlier if next erase happens long after last draw (like game over screen)

    private long lastPaint;
    private int fps; //calculate average from 3 frames?

    private ViewPort viewPort = new ViewPort();
    private ViewPort.Bit bit = new ViewPort.Bit();
    private ViewPort.Index idx = new ViewPort.Index();

    public Screen() {
        setPreferredSize(new Dimension(640, 320));
    }

    @Override
    public void paint(Graphics g) {
        for(int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                boolean pixel = model[y][x];
                if (REDRAW_HEURISTIC2) {
                    pixel |= modelPrev[y][x];
                }

                if (pixel) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(x * 10, y * 10, 10, 10);
            }
        }

        char[] s = ("" + fps).toCharArray(); //TODO: very inefficient

        g.setColor(Color.RED);
        g.drawChars(s, 0, s.length, 4, 12);
    }

    private void calculateFps() {
        long now = System.nanoTime();
        fps = (int)(1e9 / (now - lastPaint));
        lastPaint = now;
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
    }
}
