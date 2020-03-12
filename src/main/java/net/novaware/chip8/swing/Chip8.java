package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.BoardConfig;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.swing.device.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Function;

import static java.awt.event.KeyEvent.*;
import static java.lang.System.exit;
import static net.novaware.chip8.core.BoardFactory.newBoardFactory;

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

        Screen primaryScreen = new Screen();
        Screen secondaryScreen = new Screen();

        JPanel screens = new JPanel(new GridLayout(2,1));
        screens.add(primaryScreen);
        screens.add(secondaryScreen);

        Case aCase = new Case();
        aCase.setTitle(title);
        aCase.add(screens);
        aCase.pack();

        primaryScreen.fpsConsumer = aCase.statusConsumer;

        SwingUtilities.invokeLater(() -> aCase.setVisible(true));

        Tape tape = new Tape(romPath);

        Buzzer buzzer = new Buzzer();
        buzzer.init();

        BoardConfig config = new BoardConfig();

        Function<KeyEvent, Integer> mapper = Keyboard::normalizeKeyCode;
        DisplayPort.Mode mode = DisplayPort.Mode.DIRECT;

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
            mode = DisplayPort.Mode.MERGE_FRAME;

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
        board.getStoragePort().attachSource(tape::load);

        Keyboard k = new Keyboard();
        k.mapper = mapper;
        k.init(board.getKeyPort(), aCase);

        k.resetHandler = board::hardReset;

        aCase.pauseConsumer = paused -> { if (paused) board.pause(); else board.resume(); };

        board.initialize();
        board.runOnScheduler(Integer.MAX_VALUE);
    }
}
