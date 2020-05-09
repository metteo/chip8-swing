package net.novaware.chip8.swing.status;

import net.novaware.chip8.swing.mvp.HasComponent;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar implements HasComponent {

    public JPanel getComponent() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));

        JPanel statusSection1 = new JPanel();
        statusSection1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusSection1.setPreferredSize(new Dimension(statusBar.getWidth(), 20)); //TODO: figure it out dynamically
        statusBar.add(statusSection1);

        JPanel statusSection2 = new JPanel();
        statusSection2.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusSection2.setPreferredSize(new Dimension(statusBar.getWidth(), 20));
        statusBar.add(statusSection2);

        return statusBar;
    }
}
