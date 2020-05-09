package net.novaware.chip8.swing.display;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;

public class Display extends JComponent implements Accessible {

    public Display getComponent() {
        setPreferredSize(new Dimension(400, 200));
        setBackground(Color.WHITE);

        return this;
    }

    @Override
    public void paint(Graphics g) {






        super.paint(g); //should be last so children show up on top
    }
}
