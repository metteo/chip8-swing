package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.core.config.MutableConfig;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.swing.device.Buzzer;
import net.novaware.chip8.swing.device.Cardridge;
import net.novaware.chip8.swing.device.Case;
import net.novaware.chip8.swing.device.Keyboard;
import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import net.novaware.chip8.swing.ui.DefaultDisplayModel;
import net.novaware.chip8.swing.ui.JDisplay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.awt.event.KeyEvent.*;
import static java.lang.System.exit;
import static java.lang.System.out;
import static net.novaware.chip8.core.BoardFactory.newBoardFactory;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;

public class Chip8Backup {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        //https://stackoverflow.com/questions/41001623/java-animation-programs-running-jerky-in-linux/41002553#41002553
        System.setProperty("sun.java2d.opengl", "true"); //https://stackoverflow.com/questions/57948299/why-does-my-custom-swing-component-repaint-faster-when-i-move-the-mouse-java
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Path romPath = null;
        String title = null;

        if (args.length > 1) {
            LOG.error("usage: chip8 [<pathToRom>]");
            exit(1);
        } else if (args.length == 1) {
            romPath = Path.of(args[0]);
            title = romPath.getName(romPath.getNameCount() - 1).toString();
        } else {
            title = "Boot-128";
        }

        final URL icon = Chip8Backup.class.getResource("/c8.png");

        DefaultDisplayModel primaryDisplayModel = new DefaultDisplayModel();
        JDisplay primaryScreen = new JDisplay(primaryDisplayModel);
        primaryScreen.setPreferredScale(10);

        DefaultDisplayModel secondaryDisplayModel = new DefaultDisplayModel();
        JDisplay secondaryScreen = new JDisplay(secondaryDisplayModel);
        secondaryScreen.setPreferredScale(6);

        Case aCase = new Case();
        aCase.setIconImage(ImageIO.read(icon));
        aCase.setTitle(title + " - Chip8 Emulgator");
        aCase.add(primaryScreen);

        //TODO: refactor
        final MenuBarViewImpl menuBar = new MenuBarViewImpl();
        menuBar.initialize();

        aCase.setJMenuBar(menuBar.getComponent());

        aCase.pack();

        primaryScreen.addPropertyChangeListener("fps", pce -> aCase.statusConsumer.accept((Integer)pce.getNewValue()));

        SwingUtilities.invokeLater(() -> aCase.setVisible(true));

        Cardridge cardridge = new Cardridge(romPath);

        Buzzer buzzer = new Buzzer();
        buzzer.init();

        MutableConfig config = new MutableConfig();

        Function<KeyEvent, Integer> mapper = Keyboard::normalizeKeyCode;
        DisplayPort.Mode mode = DisplayPort.Mode.MERGE_FRAME;

        // TODO: create a ROM library with game profiles instead
        if (title.equals("INVADERS")) {
            config.setCpuFrequency(1500);
            config.setLegacyShift(false);
            mode = DisplayPort.Mode.FALLING_EDGE;

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    case VK_SPACE: return 5;
                    case VK_LEFT: return 4;
                    case VK_RIGHT: return 6;
                }

                return Keyboard.normalizeKeyCode(keyEvent);
            };
        }

        if (title.equals("VERS")) {
            config.setCpuFrequency(500);
            mode = DisplayPort.Mode.DIRECT;

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    //player 1
                    case VK_UP: return 0x7;
                    case VK_DOWN: return 0xA;
                    case VK_LEFT: return 1;
                    case VK_RIGHT: return 2;

                    //player 2
                    case VK_I: return 0xC;
                    case VK_K: return 0xD;
                    case VK_L: return 0xF;
                    case VK_J: return 0xB;
                }

                return Keyboard.normalizeKeyCode(keyEvent);
            };
        }

        if (title.equals("BRIX")) {
            config.setCpuFrequency(700);
            config.setEnforceMemoryRoRwState(false);
            mode = DisplayPort.Mode.MERGE_FRAME;
        }

        if (title.equals("BLINKY")) {
            config.setEnforceMemoryRoRwState(false);
            config.setLegacyLoadStore(false);
            config.setLegacyShift(false);
            mode = DisplayPort.Mode.MERGE_FRAME;

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    case VK_UP: return 3;
                    case VK_DOWN: return 6;
                    case VK_LEFT: return 7;
                    case VK_RIGHT: return 8;
                }

                return Keyboard.normalizeKeyCode(keyEvent);
            };
        }

        if (title.equals("PONG2")) {
            config.setEnforceMemoryRoRwState(false);
            mode = DisplayPort.Mode.FALLING_EDGE;

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    case VK_W: return 1;
                    case VK_S: return 4;
                    case VK_UP: return 0xC;
                    case VK_DOWN: return 0xD;
                }

                return Keyboard.normalizeKeyCode(keyEvent);
            };
        }

        if (title.equals("UFO")) {
            config.setCpuFrequency(700);
            mode = DisplayPort.Mode.MERGE_FRAME;
        }

        if (title.equals("TANK")) {
            config.setCpuFrequency(1200);
            config.setEnforceMemoryRoRwState(false);
            mode = DisplayPort.Mode.MERGE_FRAME;

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    case VK_UP: return 8;
                    case VK_DOWN: return 2;
                    case VK_LEFT: return 4;
                    case VK_RIGHT: return 6;
                    case VK_SPACE: return 5;
                }

                return Keyboard.normalizeKeyCode(keyEvent);
            };
        }

        if (title.contains("Lunar")) {
            config.setCpuFrequency(500);
            config.setEnforceMemoryRoRwState(false);
        }

        if (title.equals("Boot-128")) {
            config.setCpuFrequency(100);
            config.setEnforceMemoryRoRwState(false);
            config.setLegacyShift(false);
            config.setLegacyLoadStore(false);
            config.setLegacyAddressSum(false);
        }

        ClockGenerator clock = new ClockGeneratorJvmImpl("Swing");

        Board board = newBoardFactory(config, clock, new Random()::nextInt)
                .newBoard();

//        board.getDisplayPort(DisplayPort.Type.PRIMARY).connect(primaryScreen::draw);
        board.getDisplayPort(DisplayPort.Type.PRIMARY).connect(primaryDisplayModel::updateWith);
        board.getDisplayPort(DisplayPort.Type.PRIMARY).setMode(mode);

        board.getDisplayPort(DisplayPort.Type.SECONDARY).connect(secondaryDisplayModel::updateWith);

        board.getStoragePort().connect(cardridge::toPacket);

        Keyboard k = new Keyboard();
        k.mapper = mapper;
        k.init(board.getKeyPort().connect(op -> {
            for (int i = 0; i < 0x10; ++i) {
                if (op.isKeyUsed(ubyte(i))) {
                    System.out.println("Key used: " + toHexString(ubyte(i)));
                }
            }
        }), aCase);

        aCase.resetConsumer = board::hardReset;
        aCase.pauseConsumer = paused -> { if (paused) board.pause(); else board.resume(); };


        menuBar.getOpen().accept(ae -> menuBar.showOpenDialog(f -> {
            board.getStoragePort().disconnect();
            board.getStoragePort().connect(() -> new Cardridge(f.toPath()).toPacket());
            board.hardReset();
        }));

        menuBar.setRecentOpens(List.of(new File("/home/metteo/Repos/chip8-roms/INVADERS")));
        menuBar.setRecentHandler(f -> {
            board.getStoragePort().disconnect();
            board.getStoragePort().connect(() -> new Cardridge(f.toPath()).toPacket());
            board.hardReset();
        });

        menuBar.getClose().accept(ae -> {
            board.getStoragePort().disconnect();
            board.getStoragePort().connect(() -> new Cardridge(null).toPacket());
            board.hardReset();
        });

        menuBar.getExit().accept(ae -> aCase.dispatchEvent(new WindowEvent(aCase, WindowEvent.WINDOW_CLOSING)));

        menuBar.getReset().accept(ae -> board.hardReset());
        menuBar.getSoftReset().accept(ae -> board.softReset());


        //FIXME: progress here: ---------------------------------------->


        menuBar.getPause().accept(ae -> {
            menuBar.setResumeEnabled(true);
            menuBar.setPauseEnabled(false);
            board.pause();
        });

        menuBar.getResume().accept(ae -> {
            menuBar.setResumeEnabled(false);
            menuBar.setPauseEnabled(true);
            board.resume(); //TODO: make sure it works with autopause
        });

        menuBar.getAutoPause().accept(ae -> aCase.autoPauseEnabled = menuBar.isAutoPauseSelected());
        menuBar.setAutoPauseSelected(aCase.autoPauseEnabled);

        menuBar.getLegacyShift().accept(ae -> config.setLegacyShift(menuBar.isLegacyShiftSelected()));
        menuBar.setLegacyShiftSelected(config.isLegacyShift());

        menuBar.getLegacyLoadStore().accept(ae -> config.setLegacyLoadStore(menuBar.isLegacyLoadStoreSelected()));
        menuBar.setLegacyLoadStoreSelected(config.isLegacyLoadStore());

        menuBar.getLegacyAddressSum().accept(ae -> config.setLegacyAddressSum(menuBar.isLegacyAddressSumSelected()));
        menuBar.setLegacyAddressSumSelected(config.isLegacyAddressSum());

        menuBar.getIncreaseFrequency().accept(ae -> {
            int f = config.getCpuFrequency();
            f+=100;
            config.setCpuFrequency(f);
            menuBar.setFrequency(f);
        });
        menuBar.getDecreaseFrequency().accept(ae -> {
            int f = config.getCpuFrequency();

            if (f <= 100) {
                return;
            }

            f-=100;
            config.setCpuFrequency(f);
            menuBar.setFrequency(f);
        });
        menuBar.setFrequency(config.getCpuFrequency());

        menuBar.getMemoryProtection().accept(ae -> config.setEnforceMemoryRoRwState(menuBar.isMemoryProtectionSelected()));
        menuBar.setMemoryProtectionSelected(config.isEnforceMemoryRoRwState());

        //TODO: maybe use https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html to handle hidden menu case?

        menuBar.getFullScreen().accept(ae -> {
            boolean fs = menuBar.isFullScreenSelected();

            //TODO: handle the case when the window is pulled from fullscreen and the checkbox is not updated
            if (fs) {
                aCase.getGraphicsConfiguration().getDevice().setFullScreenWindow(aCase);
            } else {
                aCase.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
            }
        });

        menuBar.getDecoration().accept(ae -> {
            boolean dec = menuBar.isDecorationSelected();

            if (dec) {
                aCase.dispose();
                aCase.setUndecorated(true); //TODO: make it an option
                aCase.setVisible(true);
            } else {
                aCase.dispose();
                aCase.setUndecorated(false);
                aCase.setVisible(true);
            }
        });

        menuBar.getDistraction().accept(ae -> {
            boolean distraction = menuBar.isDistractionSelected();

            if (distraction) {
                menuBar.getComponent().setVisible(false); //FIXME: doing that disables menu shortcuts...
                aCase.statusPanel.setVisible(false);
            } else {
                menuBar.getComponent().setVisible(true);
                aCase.statusPanel.setVisible(true);
            }
        });

        menuBar.getCosmac().accept(ae -> {
            primaryScreen.setBackground(Color.GRAY);
            primaryScreen.setForeground(Color.WHITE);
            primaryScreen.setGhost(Color.BLACK);
            primaryScreen.setStyle(JDisplay.Style.SOLID);
            primaryScreen.repaint();
        });

        menuBar.getBorder().accept(ae -> {
            primaryScreen.setBackground(Color.GRAY);
            primaryScreen.setForeground(Color.WHITE);
            primaryScreen.setGhost(Color.BLACK);
            primaryScreen.setStyle(JDisplay.Style.BORDERED);
            primaryScreen.repaint();
        });

        menuBar.getBrick().accept(ae -> {
            primaryScreen.setBackground(new Color(0xADBBAD));
            primaryScreen.setForeground(Color.BLACK);
            primaryScreen.setGhost(new Color(0xA9B4A7));
            primaryScreen.setStyle(JDisplay.Style.BRICKED);
            primaryScreen.repaint();
        });

//        display.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                board.pause();
//                aCase.statusBar.setPowerOn(false);
//
//                //TODO: check state before pausing, maybe it should be resume instead
//            }
//        });

        AtomicInteger scale = new AtomicInteger(primaryScreen.getScale());
        primaryScreen.addPropertyChangeListener("scale", pce -> scale.set((Integer)pce.getNewValue()));

        menuBar.getIncreaseScale().accept(ae -> {
            int s = scale.incrementAndGet();

            menuBar.setIncreaseScale(s + 1);
            menuBar.setCurrentScale(s);
            menuBar.setDecreaseScale(s - 1);

            primaryScreen.setPreferredScale(s);
            aCase.pack();
        });

        menuBar.getDecreaseScale().accept(ae -> {
            int s = scale.get();
            if (s < 2) {
                return;
            }

            s = scale.decrementAndGet(); //TODO: handle x0, negatives and max scale for given screen

            menuBar.setIncreaseScale(s + 1);
            menuBar.setCurrentScale(s);
            menuBar.setDecreaseScale(s - 1);

            primaryScreen.setPreferredScale(s);
            aCase.pack();
        });

        menuBar.getNoProcessing().accept(ae -> {
            //TODO: threading
            board.getDisplayPort(DisplayPort.Type.PRIMARY).setMode(DisplayPort.Mode.DIRECT);
        });

        menuBar.getMerge().accept(ae -> {
            //TODO: threading
            board.getDisplayPort(DisplayPort.Type.PRIMARY).setMode(DisplayPort.Mode.MERGE_FRAME);
        });

        menuBar.getFallingEdge().accept(ae -> {
            //TODO: threading
            board.getDisplayPort(DisplayPort.Type.PRIMARY).setMode(DisplayPort.Mode.FALLING_EDGE);
        });

        menuBar.getPrimaryDisplay().accept(ae -> aCase.requestFocus());
        menuBar.getSecondaryDisplay().accept(ae -> {
            Case secCase = new Case();
            secCase.add(secondaryScreen);
            secCase.pack();
            secCase.setVisible(true);
        });

        menuBar.getHelpPage().accept(ae -> {
            try {
                URI uri = new URI("https://github.com/metteo/chip8-core");
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        });

        menuBar.getKeys().accept(ae -> out.println("keys"));

        menuBar.getAbout().accept(ae -> {
            JOptionPane.showMessageDialog(aCase, "Version: 0.0.1-SNAPSHOT\nAuthor: Grzegorz Nowak",
                    "Chip8 Emulgator", JOptionPane.INFORMATION_MESSAGE);
        });

        board.setDelayTimerMonitor(delay -> SwingUtilities.invokeLater(() -> {
            aCase.statusBar.setDelay(delay);
        }));
        board.setSoundTimerMonitor(sound -> SwingUtilities.invokeLater(() -> aCase.statusBar.setSound(sound)));

        board.getAudioPort().connect(p -> {
            buzzer.accept(p);
            boolean on = p.isSoundOn();
            SwingUtilities.invokeLater(()-> {aCase.statusBar.setSoundOn(on);});
        });

        board.setCpuFrequencyMonitor(f -> aCase.statusBar.setFrequency(f));

        primaryScreen.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) { //TODO: animate?
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

                    board.getStoragePort().disconnect(); //TODO: combine it with file open
                    board.getStoragePort().connect(() -> new Cardridge(files.get(0).toPath()).toPacket());
                    board.hardReset();
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });

        aCase.statusBar.setInfo("Use File menu or drop a ROM to open it.");

        board.powerOn();
    }
}
