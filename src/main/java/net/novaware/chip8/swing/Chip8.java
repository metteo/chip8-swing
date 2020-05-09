package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.core.config.MutableConfig;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.swing.device.*;
import net.novaware.chip8.swing.menu.MenuBarViewImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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

public class Chip8 {

    private static final Logger LOG = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
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

        final URL icon = Chip8.class.getResource("/c8.png");

        Screen primaryScreen = new Screen();
        Screen secondaryScreen = new Screen();

        Case aCase = new Case();
        aCase.setIconImage(ImageIO.read(icon));
        aCase.setTitle(title);
        aCase.add(primaryScreen);

        //TODO: refactor
        final MenuBarViewImpl menuBar = new MenuBarViewImpl();
        menuBar.initialize();

        aCase.setJMenuBar(menuBar.getComponent());

        aCase.pack();

        primaryScreen.fpsConsumer = aCase.statusConsumer;

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

        board.getDisplayPort(DisplayPort.Type.PRIMARY).connect(primaryScreen::draw);
        board.getDisplayPort(DisplayPort.Type.PRIMARY).setMode(mode);

        board.getDisplayPort(DisplayPort.Type.SECONDARY).connect(secondaryScreen::draw);

        board.getAudioPort().connect(buzzer);
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
            out.println("cosmac");
        });

        menuBar.getBrick().accept(ae -> out.println("brick"));

        AtomicInteger scale = new AtomicInteger(2);

        menuBar.getIncreaseScale().accept(ae -> {
            int s = scale.incrementAndGet();

            menuBar.setIncreaseScale(s + 1);
            menuBar.setCurrentScale(s);
            menuBar.setDecreaseScale(s - 1);
        });

        menuBar.getDecreaseScale().accept(ae -> {
            int s = scale.decrementAndGet(); //TODO: handle x0, negatives and max scale for given screen

            menuBar.setIncreaseScale(s + 1);
            menuBar.setCurrentScale(s);
            menuBar.setDecreaseScale(s - 1);
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

        menuBar.getPrimaryDisplay().accept(ae -> out.println("primary"));
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

        board.powerOn();
    }
}
