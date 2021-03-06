package net.novaware.chip8.swing.window;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.config.MutableConfig;
import net.novaware.chip8.core.port.DebugPort;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.KeyPort;
import net.novaware.chip8.swing.device.Buzzer;
import net.novaware.chip8.swing.display.DisplayPresenter;
import net.novaware.chip8.swing.display.DisplayPresenterImpl;
import net.novaware.chip8.swing.menu.MenuBarPresenter;
import net.novaware.chip8.swing.menu.MenuBarPresenterImpl;
import net.novaware.chip8.swing.mvp.AbstractPresenter;
import net.novaware.chip8.swing.profile.ProfileStub;
import net.novaware.chip8.swing.status.StatusBarPresenter;
import net.novaware.chip8.swing.status.StatusBarPresenterImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static javax.swing.JOptionPane.showMessageDialog;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;

public class WindowPresenterImpl extends AbstractPresenter<WindowView> implements WindowPresenter {

    private static final Logger LOG = LogManager.getLogger();

    private final MutableConfig config;
    private final ClockGenerator clock;
    private final Board board;
    private final DisplayPort.Type displayPortType;

    private MenuBarPresenter menuBarPresenter;
    private DisplayPresenter displayPresenter;
    private StatusBarPresenter statusBarPresenter;

    private Buzzer buzzer = new Buzzer();

    private WindowPresenter otherWindow;

    private String path; //TODO: app scope (not window)
    private BufferedImage icon; //TODO: app scope (not window)

    private Function<KeyEvent, Integer> customKeyMapper = ke -> -1;

    public WindowPresenterImpl(
            WindowView view,
            MutableConfig config,
            ClockGenerator clock,
            Board board,
            DisplayPort.Type displayPortType
    ) {
        super(view);

        this.config = config;
        this.clock = clock;
        this.board = board;
        this.displayPortType = displayPortType;
    }

    @Override
    public void initialize() {
        view.setIcon(icon);

        menuBarPresenter = new MenuBarPresenterImpl(
                view.getMenuBar(),
                config,
                board,
                displayPortType,
                path,
                //TODO: these should be app wide events on the bus...
                this::setAppTitle,
                view::exit,
                view.getDisplay()::getStyle,
                view.getDisplay()::setStyle,
                view.getDisplay()::getScale,
                scale -> {
                    view.getDisplay().setScale(scale);
                    view.updateSize();
                },
                paused -> view.getStatusBar().setPowerOn(!paused)
        );

        menuBarPresenter.initialize();

        displayPresenter = new DisplayPresenterImpl(view.getDisplay(), board.getDisplayPort(displayPortType));
        displayPresenter.initialize();

        statusBarPresenter = new StatusBarPresenterImpl(view.getStatusBar());
        statusBarPresenter.initialize();

        registerFocusListener();
        registerKeyListener();
        registerFpsConsumer();
        updateDistractionMenu();
        updateDecorationMenu();
        registerDnD();

        //TODO: window takes over some menubar responsibility (easier from here)
        view.getMenuBar().getDecoration().accept(ae -> onDecoration());
        view.getMenuBar().getDistraction().accept(ae -> onDistraction());
        view.getMenuBar().getFullScreen().accept(ae -> onFullScreen());

        //This is tricky but doable
        view.getMenuBar().getPrimaryDisplay().accept(ae -> onPrimaryDisplay());
        view.getMenuBar().getSecondaryDisplay().accept(ae -> onSecondaryDisplay());

        updateWindowMenus();

        if (isPrimary()) {
            registerMonitoring();

            buzzer.init();
            board.getAudioPort().connect(buzzer);
        }

        view.getStatusBar().setInfo("Ready.");
    }

    private void registerDnD() { //TODO: make part of display?
        view.getDisplay().getComponent().setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) { //TODO: animate?, make optional in menu so users can figure it out
                for (DataFlavor flavor : support.getDataFlavors()) {
                    if (flavor.isFlavorJavaFileListType()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    final List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() != 1) {
                        return false; // single files are supported
                    }

                    menuBarPresenter.open(files.get(0));
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    /**
     * Primary window only!
     */
    private void registerMonitoring() {
        board.getDebugPort().connect(new DebugPort.Receiver() {
            @Override
            public void onException(Exception e) {
                //TODO: report to MenuBarPresenter that file open failed?
                LOG.error("Exception from the Board: ", e);

                SwingUtilities.invokeLater(() -> {
                    showMessageDialog(view.getComponent().getTopLevelAncestor(), e.toString(), "Error",
                            JOptionPane.ERROR_MESSAGE);

                    menuBarPresenter.forcePause(); //to prevent dialogs showing up one after the other
                });
            }

            @Override
            public void onDelayTimerChange(int dt) {
                SwingUtilities.invokeLater(() -> {
                    view.getStatusBar().setDelay(dt);
                    if (otherWindow != null) {
                        otherWindow.getView().getStatusBar().setDelay(dt);
                    }
                });
            }

            @Override
            public void onSoundTimerChange(int st) {
                SwingUtilities.invokeLater(() -> {
                    view.getStatusBar().setSound(st);
                    view.getStatusBar().setSoundOn(st > 0); // q line blinked even for st = 1

                    if (otherWindow != null) {
                        otherWindow.getView().getStatusBar().setSound(st);
                        otherWindow.getView().getStatusBar().setSoundOn(st > 0);
                    }
                });
            }

            @Override
            public void onCpuFrequencyChange(int f) {
                SwingUtilities.invokeLater(()-> {
                    view.getStatusBar().setFrequency(f);
                    if (otherWindow != null) {
                        otherWindow.getView().getStatusBar().setFrequency(f);
                    }
                });
            }

            @Override
            public void onStateChange(boolean paused) {
                //TODO: move pause state handling here (from menu presenter)
            }
        });
    }

    private void updateWindowMenus() {
        view.getMenuBar().setPrimaryDisplaySelected(isPrimary());
        view.getMenuBar().setSecondaryDisplaySelected(!isPrimary());
    }

    private void onSecondaryDisplay() {
        updateWindowMenus();

        switch(displayPortType) {
            case PRIMARY:
                openOrFocusSecondaryWindow();
                break;
            case SECONDARY:
                view.requestFocus();
                break;
        }
    }

    private void openOrFocusSecondaryWindow() {
        if (otherWindow != null) {
            otherWindow.getView().setVisible(true);
            otherWindow.getView().requestFocus();
            return;
        }

        WindowView otherView = new WindowViewImpl();

        otherWindow = new WindowPresenterImpl(
                otherView,
                config,
                clock,
                board,
                DisplayPort.Type.SECONDARY
        );
        otherWindow.setIcon(icon);
        otherWindow.initialize();
        otherWindow.setOtherWindow(this);

        otherWindow.start();
    }

    @Override
    public void setOtherWindow(WindowPresenter windowPresenter) {
        otherWindow = windowPresenter;
    }

    @Override
    public void setIcon(BufferedImage icon) {
        this.icon = icon;
    }

    private void onPrimaryDisplay() {
        updateWindowMenus();

        switch (displayPortType) {
            case PRIMARY:
                view.requestFocus();
                break;
            case SECONDARY:
                focusOtherWindow();
                break;
        }
    }

    private void focusOtherWindow() {
        if (otherWindow != null) {
            otherWindow.getView().requestFocus();
        }
    }

    private void onFullScreen() {
        boolean fullScreen = view.getMenuBar().isFullScreenSelected();

        view.setFullScreen(fullScreen);
    }

    private void updateDecorationMenu() {
        boolean decorated = view.isDecorated();

        view.getMenuBar().setDecorationSelected(decorated);
    }

    private void onDecoration() {
        boolean decorationSelected = view.getMenuBar().isDecorationSelected();

        view.setDecorated(decorationSelected);
        view.updateSize();
    }

    private void onDistraction() {
        boolean distractionSelected = view.getMenuBar().isDistractionSelected();

        view.getStatusBar().getComponent().setVisible(distractionSelected);
        updateDistractionMenu();
        view.updateSize();
    }

    private void updateDistractionMenu() {
        final boolean statusVisible = view.getStatusBar().getComponent().isVisible();
        view.getMenuBar().setDistractionSelected(statusVisible);
    }

    private void registerFpsConsumer() {
        view.getDisplay().addFpsConsumer(fps -> view.getStatusBar().setFps(fps));
    }

    private void setAppTitle(String appName) { //TODO: other window too
        String title = "Chip8 Emulgator";
        if (appName != null && !appName.isEmpty()) {
            title = appName + " - " + title;
        }
        view.setTitle(title);

        customKeyMapper = ProfileStub.loadProfile(appName, config);
        menuBarPresenter.updateCompatMenus();
    }

    private void registerFocusListener() {
        view.getFocusRegistry().accept(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                onFocusChange(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                onFocusChange(false);
            }
        });
    }

    private void registerKeyListener() {
        //TODO: collect, reverse map and display the keys checked by the game
        final Consumer<KeyPort.InputPacket> keyReceiver = board.getKeyPort().connect(op -> {
            for (int i = 0; i < 0x10; ++i) {
                if (op.isKeyUsed(ubyte(i))) {
                    System.out.println("Key used: " + toHexString(ubyte(i)));
                }
            }
        });

        view.getKeyRegistry().accept(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyIdx = mapKey(e);
                if (keyIdx >= 0x0 && keyIdx <= 0xF) {
                    keyReceiver.accept(new InPacket(KeyPort.Direction.DOWN, ubyte(keyIdx)));
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyIdx = mapKey(e);
                if (keyIdx >= 0x0 && keyIdx <= 0xF) {
                    keyReceiver.accept(new InPacket(KeyPort.Direction.UP, ubyte(keyIdx)));
                    e.consume();
                }
            }
        });
    }

    //TODO: change to use common mapping for chip8 emulators
    private int mapKey(KeyEvent e) {
        final int keyOverride = customKeyMapper.apply(e);
        if (keyOverride != -1) {
            return keyOverride;
        }

        final int keyCode = e.getKeyCode();
        int keyIdx = -1;
        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            keyIdx = keyCode - KeyEvent.VK_0; // normalize to 0 based indexing
        }

        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_F) {
            keyIdx = keyCode - KeyEvent.VK_A + 10; // as above but after 9
        }

        return keyIdx;
    }

    private void onFocusChange(boolean gained) {
        menuBarPresenter.onFocusChange(gained);
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void start() {
        getView().setVisible(true);

        if (isPrimary()) {
            getView().setCloseOnExit(true);
            board.powerOn();
        } else {
            getView().setCloseOnExit(false);
        }
    }

    private boolean isPrimary() {
        return displayPortType == DisplayPort.Type.PRIMARY;
    }

    static class InPacket implements KeyPort.InputPacket {

        KeyPort.Direction direction;
        byte keyCode;

        InPacket(KeyPort.Direction direction, byte keyCode) {
            this.direction = direction;
            this.keyCode = keyCode;
        }

        @Override
        public KeyPort.Direction getDirection() {
            return direction;
        }

        @Override
        public byte getKeyCode() {
            return keyCode;
        }
    }
}
