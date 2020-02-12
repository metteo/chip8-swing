package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.swing.device.*;

import javax.swing.*;

import java.nio.file.Path;

import static java.lang.System.err;
import static java.lang.System.exit;
import static net.novaware.chip8.core.BoardFactory.newBoardFactory;

public class Chip8 {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            err.println("usage: chip8 <pathToRom>");
            exit(1);
        }

        final Path romPath = Path.of(args[0]);
        final String title = romPath.getName(romPath.getNameCount() - 1).toString();

        Screen screen = new Screen();

        Case aCase = new Case();
        aCase.setTitle(title);
        aCase.add(screen);
        aCase.pack();

        SwingUtilities.invokeLater(() -> aCase.setVisible(true));

        Tape tape = new Tape(romPath);

        Buzzer buzzer = new Buzzer();
        buzzer.init();

        Board board = newBoardFactory().newBoard();
        board.init();

        board.getDisplayPort().attach(screen::draw);
        board.getAudioPort().attach(buzzer);
        board.getStoragePort().load(tape.load());

        Keyboard k = new Keyboard();
        k.init(board.getKeyPort(), aCase);

        board.run(Integer.MAX_VALUE);
    }
}