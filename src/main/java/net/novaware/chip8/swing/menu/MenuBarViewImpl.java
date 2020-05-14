package net.novaware.chip8.swing.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static net.novaware.chip8.core.util.AssertUtil.assertArgument;

public class MenuBarViewImpl implements MenuBarView {

    private Consumer<File> recentHandler;

    private JMenuBar component;

    private JFileChooser fileChooser;

    private JMenu fileMenu;
    private JMenuItem open;
    private JMenu openRecent;
    private JMenuItem close;
    private JMenuItem exit;

    private JMenu editMenu;
    private JMenuItem reset;
    private JMenuItem softReset;
    private JMenuItem pause;
    private JMenuItem resume;
    private JCheckBoxMenuItem autoPause;

    private JCheckBoxMenuItem mute;

    private JMenu compatibility;
    private JCheckBoxMenuItem legacyShift;
    private JCheckBoxMenuItem legacyLoadStore;
    private JCheckBoxMenuItem legacyAddressSum;

    private JMenu frequency;
    private JMenuItem increaseFrequency;
    private JMenuItem currentFrequency;
    private JMenuItem decreaseFrequency;

    private JCheckBoxMenuItem memoryProtection;

    private JMenu viewMenu;
    private JMenu theme;
    private JRadioButtonMenuItem cosmac;
    private JRadioButtonMenuItem border;
    private JRadioButtonMenuItem brick;

    private JMenu scaling;
    private JMenuItem increaseScale;
    private JMenuItem currentScale;
    private JMenuItem decreaseScale;

    private JMenu processing;
    private JRadioButtonMenuItem noProcessing;
    private JRadioButtonMenuItem merge;
    private JRadioButtonMenuItem fallingEdge;

    private JMenu laf;
    private JCheckBoxMenuItem decoration;
    private JCheckBoxMenuItem distraction;
    private JCheckBoxMenuItem fullScreen;

    private JMenu windowMenu;
    private JCheckBoxMenuItem primaryDisplay;
    private JCheckBoxMenuItem secondaryDisplay;

    private JMenu helpMenu;
    private JMenuItem helpPage;
    private JMenuItem keys;
    private JMenuItem about;

    public void initialize() {
        final JMenuBar menuBar = new JMenuBar();

        initFileMenu();
        menuBar.add(fileMenu);

        initEditMenu();
        menuBar.add(editMenu);

        initViewMenu();
        menuBar.add(viewMenu);

        initWindowMenu();
        menuBar.add(windowMenu);

        initHelpMenu();
        menuBar.add(helpMenu);

        component = menuBar;
    }

    @Override
    public JMenuBar getComponent() {
        return component;
    }

    @Override
    public Consumer<ActionListener> getOpen() {
        return open::addActionListener;
    }

    @Override
    public void showOpenDialog(Consumer<File> fileConsumer) {
        assertArgument(fileConsumer != null, "fileConsumer must not be null");

        final int result = fileChooser.showOpenDialog(open);
        switch(result) {
            case JFileChooser.ERROR_OPTION:
                //TODO: handle the error?
                System.err.println("Error while opening file chooser dialog");
                break;
            case JFileChooser.APPROVE_OPTION:
                final File selectedFile = fileChooser.getSelectedFile();
                fileConsumer.accept(selectedFile);
                break;
            case JFileChooser.CANCEL_OPTION:
                //noop
                break;
        }
    }

    @Override
    public void setRecentOpens(List<File> recents) {
        openRecent.removeAll();

        recents.stream().map(f -> {
            JMenuItem recent = new JMenuItem(f.getName());
            recent.setToolTipText(f.toString());
            recent.getAccessibleContext().setAccessibleDescription("ROM file at " + f.toString());
            recent.addActionListener(ae -> {
                if (recentHandler != null) {
                    recentHandler.accept(f);
                }
            });
            return recent;
        }).forEachOrdered(openRecent::add);
    }

    @Override
    public void setRecentHandler(Consumer<File> handler) {
        this.recentHandler = handler;
    }

    @Override
    public Consumer<ActionListener> getClose() {
        return close::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getExit() {
        return exit::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getReset() {
        return reset::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getSoftReset() {
        return softReset::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getPause() {
        return pause::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getResume() {
        return resume::addActionListener;
    }

    @Override
    public void setPauseEnabled(boolean enabled) {
        pause.setEnabled(enabled);
    }

    @Override
    public void setResumeEnabled(boolean enabled) {
        resume.setEnabled(enabled);
    }

    @Override
    public Consumer<ActionListener> getAutoPause() {
        return autoPause::addActionListener;
    }

    @Override
    public void setAutoPauseSelected(boolean selected) {
        autoPause.setState(selected);
    }

    @Override
    public boolean isAutoPauseSelected() {
        return autoPause.getState();
    }

    @Override
    public Consumer<ActionListener> getLegacyShift() {
        return legacyShift::addActionListener;
    }

    @Override
    public void setLegacyShiftSelected(boolean selected) {
        legacyShift.setState(selected);
    }

    @Override
    public boolean isLegacyShiftSelected() {
        return legacyShift.getState();
    }

    @Override
    public Consumer<ActionListener> getLegacyLoadStore() {
        return legacyLoadStore::addActionListener;
    }

    @Override
    public void setLegacyLoadStoreSelected(boolean selected) {
        legacyLoadStore.setState(selected);
    }

    @Override
    public boolean isLegacyLoadStoreSelected() {
        return legacyLoadStore.getState();
    }

    @Override
    public Consumer<ActionListener> getLegacyAddressSum() {
        return legacyAddressSum::addActionListener;
    }

    @Override
    public void setLegacyAddressSumSelected(boolean selected) {
        legacyAddressSum.setState(selected);
    }

    @Override
    public boolean isLegacyAddressSumSelected() {
        return legacyAddressSum.getState();
    }

    @Override
    public Consumer<ActionListener> getIncreaseFrequency() {
        return increaseFrequency::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getDecreaseFrequency() {
        return decreaseFrequency::addActionListener;
    }

    @Override
    public void setDecreaseFrequencyEnabled(boolean enabled) {
        decreaseFrequency.setEnabled(enabled);
    }

    @Override
    public void setFrequency(int frequency) {
        currentFrequency.setText("  " + frequency + "Hz");
    }

    @Override
    public Consumer<ActionListener> getMemoryProtection() {
        return memoryProtection::addActionListener;
    }

    @Override
    public boolean isMemoryProtectionSelected() {
        return memoryProtection.getState();
    }

    @Override
    public void setMemoryProtectionSelected(boolean selected) {
        memoryProtection.setState(selected);
    }

    @Override
    public Consumer<ActionListener> getHelpPage() {
        return helpPage::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getKeys() {
        return keys::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getAbout() {
        return about::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getCosmac() {
        return cosmac::addActionListener;
    }

    @Override
    public void setCosmacSelected(boolean selected) {
        cosmac.setSelected(selected);
    }

    @Override
    public boolean isCosmacSelected() {
        return cosmac.isSelected();
    }

    @Override
    public Consumer<ActionListener> getBorder() {
        return border::addActionListener;
    }

    @Override
    public void setBorderSelected(boolean selected) {
        border.setSelected(selected);
    }

    @Override
    public boolean isBorderSelected() {
        return border.isSelected();
    }

    @Override
    public Consumer<ActionListener> getBrick() {
        return brick::addActionListener;
    }

    @Override
    public void setBrickSelected(boolean selected) {
        brick.setSelected(selected);
    }

    @Override
    public boolean isBrickSelected() {
        return brick.isSelected();
    }

    @Override
    public Consumer<ActionListener> getIncreaseScale() {
        return increaseScale::addActionListener;
    }

    @Override
    public Consumer<ActionListener> getDecreaseScale() {
        return decreaseScale::addActionListener;
    }

    @Override
    public void setIncreaseScale(int scale) {
        increaseScale.setText("x" + scale);
    }

    @Override
    public void setCurrentScale(int scale) {
        currentScale.setText("x" + scale);
    }

    @Override
    public void setDecreaseScale(int scale) {
        decreaseScale.setText("x" + scale);
    }

    @Override
    public Consumer<ActionListener> getNoProcessing() {
        return noProcessing::addActionListener;
    }

    @Override
    public void setNoProcessingSelected(boolean selected) {
        noProcessing.setSelected(selected);
    }

    @Override
    public boolean isNoProcessingSelected() {
        return noProcessing.isSelected();
    }

    @Override
    public Consumer<ActionListener> getMerge() {
        return merge::addActionListener;
    }

    @Override
    public void setMergeSelected(boolean selected) {
        merge.setSelected(selected);
    }

    @Override
    public boolean isMergeSelected() {
        return merge.isSelected();
    }

    @Override
    public Consumer<ActionListener> getFallingEdge() {
        return fallingEdge::addActionListener;
    }

    @Override
    public void setFallingEdgeSelected(boolean selected) {
        fallingEdge.setSelected(selected);
    }

    @Override
    public boolean isFallingEdgeSelected() {
        return fallingEdge.isSelected();
    }

    @Override
    public Consumer<ActionListener> getDecoration() {
        return decoration::addActionListener;
    }

    @Override
    public void setDecorationSelected(boolean selected) {
        decoration.setSelected(selected);
    }

    @Override
    public boolean isDecorationSelected() {
        return decoration.isSelected();
    }

    @Override
    public Consumer<ActionListener> getDistraction() {
        return distraction::addActionListener;
    }

    @Override
    public void setDistractionSelected(boolean selected) {
        distraction.setSelected(selected);
    }

    @Override
    public boolean isDistractionSelected() {
        return distraction.isSelected();
    }

    @Override
    public Consumer<ActionListener> getFullScreen() {
        return fullScreen::addActionListener;
    }

    @Override
    public void setFullScreenSelected(boolean selected) {
        fullScreen.setSelected(selected);
    }

    @Override
    public boolean isFullScreenSelected() {
        return fullScreen.isSelected();
    }

    @Override
    public Consumer<ActionListener> getPrimaryDisplay() {
        return primaryDisplay::addActionListener;
    }

    @Override
    public void setPrimaryDisplaySelected(boolean selected) {
        primaryDisplay.setSelected(selected);
    }

    @Override
    public boolean isPrimaryDisplaySelected() {
        return primaryDisplay.isSelected();
    }

    @Override
    public Consumer<ActionListener> getSecondaryDisplay() {
        return secondaryDisplay::addActionListener;
    }

    @Override
    public void setSecondaryDisplaySelected(boolean selected) {
        secondaryDisplay.setSelected(selected);
    }

    @Override
    public boolean isSecondaryDisplaySelected() {
        return secondaryDisplay.isSelected();
    }

    private void initFileMenu() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setToolTipText("File menu");
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu");

        open = new JMenuItem("Open...", KeyEvent.VK_O);
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
        open.getAccessibleContext().setAccessibleDescription("Open ROM file");
        fileMenu.add(open);

        openRecent = new JMenu("Open recent");
        openRecent.getAccessibleContext().setAccessibleDescription("Open recent ROM file");
        fileMenu.add(openRecent);

        close = new JMenuItem("Close", KeyEvent.VK_C);
        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
        close.getAccessibleContext().setAccessibleDescription("Close current ROM");
        fileMenu.add(close);

        fileMenu.addSeparator();

        exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
        exit.getAccessibleContext().setAccessibleDescription("Exit the program");
        fileMenu.add(exit);
    }

    private void initEditMenu() {
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("Edit menu");

        reset = new JMenuItem("Reset", KeyEvent.VK_R);
        reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        reset.getAccessibleContext().setAccessibleDescription("Reset the CPU");
        editMenu.add(reset);

        softReset = new JMenuItem("Soft reset");
        softReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        softReset.getAccessibleContext().setAccessibleDescription("Reset the CPU");
        editMenu.add(softReset);

        editMenu.addSeparator();

        pause = new JMenuItem("Pause", KeyEvent.VK_P);
        pause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
        pause.getAccessibleContext().setAccessibleDescription("Pause the CPU");
        editMenu.add(pause);

        resume = new JMenuItem("Resume");
        resume.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        resume.getAccessibleContext().setAccessibleDescription("Resume the CPU");
        resume.setEnabled(false);
        editMenu.add(resume);

        autoPause = new JCheckBoxMenuItem("Auto-pause");
        autoPause.setMnemonic(KeyEvent.VK_A);
        autoPause.setState(true);
        editMenu.add(autoPause);

        editMenu.addSeparator();

        mute = new JCheckBoxMenuItem("Mute"); //TODO: sound: sound on ST=1, mute
        mute.setEnabled(false);
        editMenu.add(mute);

        editMenu.addSeparator();

        JMenu gameplay = new JMenu("Gameplay");
        gameplay.setEnabled(false); //TODO: collision, h / v wrap etc
        gameplay.getAccessibleContext().setAccessibleDescription("Settings affecting gameplay");
        editMenu.add(gameplay);

        compatibility = new JMenu("Compatibility");
        compatibility.getAccessibleContext().setAccessibleDescription("Legacy / modern mode of operation");
        editMenu.add(compatibility);

        legacyShift = new JCheckBoxMenuItem("Legacy shift");        //TODO: tooltip
        compatibility.add(legacyShift);

        legacyLoadStore = new JCheckBoxMenuItem("Legacy load/store");        //TODO: tooltip
        compatibility.add(legacyLoadStore);

        legacyAddressSum = new JCheckBoxMenuItem("Legacy address sum");        //TODO: tooltip
        compatibility.add(legacyAddressSum);

        frequency = new JMenu("Frequency");
        editMenu.add(frequency);

        increaseFrequency = new JMenuItem("+100Hz", KeyEvent.VK_F2);
        increaseFrequency.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        frequency.add(increaseFrequency);

        currentFrequency = new JMenuItem("  500Hz");
        currentFrequency.setEnabled(false);
        frequency.add(currentFrequency);

        decreaseFrequency = new JMenuItem(" -100Hz", KeyEvent.VK_F1);
        decreaseFrequency.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        frequency.add(decreaseFrequency);

        memoryProtection = new JCheckBoxMenuItem("Memory protection");
        memoryProtection.setMnemonic(KeyEvent.VK_M);
        memoryProtection.setState(false);
        editMenu.add(memoryProtection);
    }

    private void initViewMenu() {
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.getAccessibleContext().setAccessibleDescription("View menu");

        theme = new JMenu("Theme");
        theme.getAccessibleContext().setAccessibleDescription("Different ways of rendering pixels");
        viewMenu.add(theme); //cosmac/telmac, brick game

        ButtonGroup themeGroup = new ButtonGroup();
        cosmac = new JRadioButtonMenuItem("COSMAC VIP");
        theme.add(cosmac);
        themeGroup.add(cosmac);

        border = new JRadioButtonMenuItem("COSMAC VIP*");
        border.setToolTipText("* with gray pixel borders");
        theme.add(border);
        themeGroup.add(border);

        brick = new JRadioButtonMenuItem("Brick Game");
        theme.add(brick);
        themeGroup.add(brick);

        scaling = new JMenu("Scaling");
        scaling.getAccessibleContext().setAccessibleDescription("Resize emulated pixels");
        viewMenu.add(scaling);

        increaseScale = new JMenuItem("x3", KeyEvent.VK_F4);
        increaseScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        scaling.add(increaseScale);

        currentScale = new JMenuItem("x2");
        currentScale.setEnabled(false);
        scaling.add(currentScale);

        decreaseScale = new JMenuItem("x1", KeyEvent.VK_F3);
        decreaseScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        scaling.add(decreaseScale);

        processing = new JMenu("Processing");
        processing.getAccessibleContext().setAccessibleDescription("Switch between different view processing modes");
        viewMenu.add(processing);

        ButtonGroup processingGroup = new ButtonGroup();

        noProcessing = new JRadioButtonMenuItem("None");
        processing.add(noProcessing);
        processingGroup.add(noProcessing);

        merge = new JRadioButtonMenuItem("Merge");
        processing.add(merge);
        processingGroup.add(merge);

        fallingEdge = new JRadioButtonMenuItem("Falling Down"); //great movie
        fallingEdge.setSelected(true);
        processing.add(fallingEdge);
        processingGroup.add(fallingEdge);

        viewMenu.addSeparator();

        laf = initLaFMenu();
        viewMenu.add(laf);

        decoration = new JCheckBoxMenuItem("Decorations");
        decoration.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        decoration.getAccessibleContext().setAccessibleDescription("Toggle decorations (title bar, window buttons)");
        viewMenu.add(decoration);

        distraction = new JCheckBoxMenuItem("Distractions");
        distraction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        distraction.getAccessibleContext().setAccessibleDescription("Toggle distractions (menu / status)");
        viewMenu.add(distraction);

        fullScreen = new JCheckBoxMenuItem("Full screen");
        fullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        fullScreen.getAccessibleContext().setAccessibleDescription("Toggle full screen");
        viewMenu.add(fullScreen);
    }

    private void initWindowMenu() {
        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);
        windowMenu.getAccessibleContext().setAccessibleDescription("Window menu");

        primaryDisplay = new JCheckBoxMenuItem("Primary");
        windowMenu.add(primaryDisplay);

        secondaryDisplay = new JCheckBoxMenuItem("Secondary");
        windowMenu.add(secondaryDisplay);
    }

    private void initHelpMenu() {
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu");

        helpPage = new JMenuItem("Help page");
        helpMenu.add(helpPage);

        keys = new JMenuItem("ROM keys", KeyEvent.VK_K);
        helpMenu.add(keys);

        helpMenu.addSeparator();

        about = new JMenuItem("About", KeyEvent.VK_A);
        helpMenu.add(about);
    }

    //FIXME: move to window presenter to allow sync update of both windows
    //Part of https://tips4java.wordpress.com/2008/10/09/uimanager-defaults/
    private JMenu initLaFMenu() {
        ButtonGroup lafGroup = new ButtonGroup();

        JMenu menu = new JMenu("Look & Feel");
        menu.setMnemonic('L');

        String lafId = UIManager.getLookAndFeel().getID();
        UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();

        for (int i = 0; i < lafInfo.length; i++) {
            String laf = lafInfo[i].getClassName();
            String name = lafInfo[i].getName();

            System.out.println(name + ": " + laf);

            Action action = new ChangeLookAndFeelAction(laf, name);
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(action);
            menu.add(mi);
            lafGroup.add(mi);

            if (name.startsWith(lafId)) { // GTK vs GTK+
                mi.setSelected(true);
            }
        }

        return menu;
    }

    /*
     *  Change the LAF and recreate the UIManagerDefaults so that the properties
     *  of the new LAF are correctly displayed.
     */
    class ChangeLookAndFeelAction extends AbstractAction {
        private String laf;

        protected ChangeLookAndFeelAction(String laf, String name) {
            this.laf = laf;
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                UIManager.setLookAndFeel(laf);

                //TODO: update both windows
                SwingUtilities.updateComponentTreeUI(component.getRootPane());

                //  Use custom decorations when supported by the LAF

                JFrame frame = (JFrame) SwingUtilities.windowForComponent(component.getRootPane());
                frame.dispose();
                frame.setVisible(true);
            } catch (Exception ex) {
                System.out.println("Failed loading L&F: " + laf); //TODO: handle exception
                ex.printStackTrace();
            }
        }
    }
}
