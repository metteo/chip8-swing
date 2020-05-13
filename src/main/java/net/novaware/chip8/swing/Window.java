package net.novaware.chip8.swing;

import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import net.novaware.chip8.swing.status.StatusBarViewImpl;
import net.novaware.chip8.swing.ui.JDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

public class Window {

    private JFrame frame;

    private MenuBarViewImpl menuBar;

    private int scale = 1;
    private JComponent display;

    private StatusBarViewImpl statusBar;

    public Window() {
        initialize();
    }

    public void initialize() {
        frame = initFrame();

        menuBar = new MenuBarViewImpl();
        menuBar.initialize();

        menuBar.getIncreaseScale().accept(ae -> {
            scale++;
            display.repaint();
        });

        menuBar.getDecreaseScale().accept(ae -> {
            scale--;
            display.repaint();
        });

        frame.setJMenuBar(menuBar.getComponent());

        display = new JComponent() {

            @Override
            public void paint(Graphics g) {
                BufferedImage bi = new BufferedImage(6 * scale, 6 * scale, BufferedImage.TYPE_INT_ARGB);
                Graphics big = bi.getGraphics();

                Graphics2D g2d = (Graphics2D) g;

                big.setColor(Color.BLACK); //TODO: background color of the pixel should be combined RGB
                big.fillRect(0, 0, 6 * scale, 6 * scale);
                g.fillRect(0, 0, getWidth(), getHeight());

                big.setColor(Color.RED);
//                big.setColor(new Color(100, 0, 0));
                big.fillRoundRect(0, 0, 1 * scale, 5 * scale, scale, scale);
                //big.drawLine(0, 0, 0, 4 * scale);
                big.setColor(Color.GREEN);
                big.fillRoundRect(2 * scale, 0, 1 * scale, 5 * scale, scale, scale);
//                big.drawLine(2 * scale, 0, 2 * scale, 4 * scale);
                big.setColor(Color.BLUE);
                //big.drawLine(4 * scale, 0, 4 * scale, 4 * scale);
                big.fillRoundRect(4 * scale, 0, 1 * scale, 5 * scale, scale, scale);

                for (int x = 0; x < getWidth() / (6 * scale) +1; x++) {
                    for (int y = 0; y < getHeight() / (6 * scale); y++) {
                        g.drawImage(bi, x * (6 * scale), y * (6 * scale) + (x % 2 * (6 * scale) / 2), null);
                    }
                }

                BufferedImage mask = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D maskG2d = (Graphics2D) mask.getGraphics();

                maskG2d.setColor(Color.BLACK);
                maskG2d.fillRect(0, 0, getWidth(), getHeight());

                maskG2d.setComposite(AlphaComposite.Clear);
                maskG2d.fillRoundRect(3, 3 * scale, getWidth() - 6, getHeight() - 8 * scale, 80, 80);
                maskG2d.dispose();

                g.drawImage(mask, 0, 0, null);

                super.paint(g);
            }
        };
        display.setPreferredSize(new Dimension(640, 480));
        frame.setContentPane(display);

        statusBar = new StatusBarViewImpl();
        statusBar.initialize();
        frame.add(statusBar.getComponent(), BorderLayout.SOUTH);
        frame.pack();
    }

    private JFrame initFrame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //TODO: change to dispose
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same
        //frame.setSize(400, 200);
        frame.setFocusable(true); //TODO: input focus should be probably on something more specific?
        frame.requestFocusInWindow();
        return frame;
    }

    public JFrame getFrame() {
        return frame;
    }

    public static void main(String[] args) {
        Window w = new Window();
        w.initialize();
        w.getFrame().setVisible(true);
    }
}
