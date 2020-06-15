package net.novaware.chip8.swing.prefs;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.novaware.chip8.swing.util.spring.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class PreferencesViewImpl implements PreferencesView {

    private JFrame frame;

    private JPanel listPanel;

    private JPanel detailPanel;

    private JPanel bottomPanel;

    public PreferencesViewImpl() {
        frame = initFrame();

        listPanel = new JPanel(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(145, 340));
        listPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        frame.add(listPanel, BorderLayout.WEST);

        JList<String> list = new JList<>(new String[]{"General", "Emulator", "Keyboard", "Audio", "Video",
            "Multiplayer"});
        list.setSelectedIndex(2);

        listPanel.add(list);

        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setPreferredSize(new Dimension(425, 340));
//        detailPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        frame.add(detailPanel, BorderLayout.CENTER);

        final SpringLayout grid = new SpringLayout();
        JPanel keyboardPanel = new JPanel(grid);
        detailPanel.add(new JScrollPane(keyboardPanel));

        JLabel l1 = new JLabel("Action");
        l1.setHorizontalAlignment(JLabel.CENTER);
        keyboardPanel.add(l1);
        JLabel l2 = new JLabel("Application");
        l2.setHorizontalAlignment(JLabel.CENTER);
        keyboardPanel.add(l2);
        JLabel l3 = new JLabel("Primary");
        l3.setHorizontalAlignment(JLabel.CENTER);
        keyboardPanel.add(l3);
        JLabel l4 = new JLabel("Secondary");
        l4.setHorizontalAlignment(JLabel.CENTER);
        keyboardPanel.add(l4);

        for (int i = 0; i < 16; i++) {
            final String hex = Integer.toHexString(i).toUpperCase();
            JLabel l = new JLabel("Key '" + hex + "'");
            keyboardPanel.add(l);

            JTextField tf = new JTextField();
            keyboardPanel.add(tf);

            JTextField tf2 = new JTextField();
            tf2.setText(hex);
            tf2.setHorizontalAlignment(JTextField.CENTER);
            keyboardPanel.add(tf2);

            JTextField tf3 = new JTextField();
            keyboardPanel.add(tf3);
        }

        SpringUtilities.makeGrid(keyboardPanel, 17, 4, 5, 5, 2, 4);

        bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(570, 50));
        bottomPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        final BoxLayout boxLayout = new BoxLayout(bottomPanel, BoxLayout.X_AXIS);
        bottomPanel.setLayout(boxLayout);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        bottomPanel.add(Box.createVerticalStrut(1));

        JButton ok = new JButton("OK");
        bottomPanel.add(ok);

        bottomPanel.add(Box.createHorizontalStrut(8));

        JButton cancel = new JButton("Cancel");
        bottomPanel.add(cancel);

        bottomPanel.add(Box.createHorizontalStrut(8));

        JButton apply = new JButton("Apply");
        bottomPanel.add(apply);

        bottomPanel.add(Box.createHorizontalStrut(8));

        frame.pack();
    }

    private JFrame initFrame() {
        JFrame frame = new JFrame("Preferences");
        frame.setLayout(new BorderLayout());
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same?
        return frame;
    }

    @Override
    public JComponent getComponent() {
        return frame.getRootPane();
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarculaLaf());

        PreferencesViewImpl pv = new PreferencesViewImpl();
        pv.setVisible(true);
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}
