package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.BoardConfig;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.swing.device.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
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
        if (args.length != 1) {
            LOG.error("usage: chip8 <pathToRom>");
            exit(1);
        }

        final Path romPath = Path.of(args[0]);
        final String title = romPath.getName(romPath.getNameCount() - 1).toString();

        Screen screen = new Screen();

        Case aCase = new Case();
        aCase.setTitle(title);
        aCase.add(screen);
        aCase.pack();

        screen.fpsConsumer = aCase.statusConsumer;

        SwingUtilities.invokeLater(() -> aCase.setVisible(true));

        Tape tape = new Tape(romPath);

        Buzzer buzzer = new Buzzer();
        buzzer.init();

        BoardConfig config = new BoardConfig();

        Function<KeyEvent, Integer> mapper = Keyboard::normalizeKeyCode;

        // TODO: create a ROM library with game profiles instead
        if (title.equals("INVADERS")) {
            config.setCpuFrequency(1500);
            config.setLegacyShift(false);

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
        }

        if (title.equals("BLINKY")) {
            config.setEnforceMemoryRoRwState(false);
            config.setLegacyLoadStore(false);
            config.setLegacyShift(false);

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

        if (title.equals("UFO")) {
            config.setCpuFrequency(700);
        }

        if (title.equals("TANK")) {
            config.setCpuFrequency(1200);
            config.setEnforceMemoryRoRwState(false);

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

        ClockGenerator clock = new ClockGeneratorJvmImpl("Swing");

        Board board = newBoardFactory(config, clock, new Random()::nextInt)
                .newBoard();
        board.init();

        board.getDisplayPort().attach(screen::draw);
        board.getAudioPort().attach(buzzer);
        board.getStoragePort().load(tape.load());

        Keyboard k = new Keyboard();
        k.mapper = mapper;
        k.init(board.getKeyPort(), aCase);

        k.resetHandler = board::reset;

        aCase.pauseConsumer = paused -> { if (paused) board.pause(); else board.resume(); };

        board.runOnScheduler(Integer.MAX_VALUE);
    }
}
