package net.novaware.chip8.swing.ui;

import javax.swing.*;

public class UiDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace(); //TODO: handle
            }

            Window w = new Window();
            w.getFrame().setVisible(true);
        });
    }
}
