package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.BoardConfig;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
import net.novaware.chip8.swing.device.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Random;

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

        // TODO: create a ROM library with game profiles instead
        if (title.equals("INVADERS")) {
            config.setCpuFrequency(1500);
            config.setLegacyShift(false);
        }

        if (title.equals("BRIX")) {
            config.setCpuFrequency(700);
            config.setEnforceMemoryRoRwState(false);
        }

        if (title.equals("BLINKY")) {
            config.setEnforceMemoryRoRwState(false);
            config.setLegacyLoadStore(false);
            config.setLegacyShift(false);
        }

        if (title.equals("UFO")) {
            config.setCpuFrequency(700);
        }

        if (title.equals("TANK")) {
            config.setCpuFrequency(1200);
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
        k.init(board.getKeyPort(), aCase);

        aCase.pauseConsumer = paused -> { if (paused) board.pause(); else board.resume(); };

        board.runOnScheduler(Integer.MAX_VALUE);
    }
}
