package net.novaware.chip8.swing.device;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Storage device
 */
public class Tape {
    private Path romPath;

    public Tape(Path romPath) {
        this.romPath = romPath;
    }
    //TODO: implement loading of roms
    //create a DB of rom hashes and key mapping for each rom, maybe some basic metadata with screenshot

    public byte[] load() throws IOException {
        final InputStream binary = Files.newInputStream(romPath);

        final byte[] bytes;
        try (binary) {
            bytes = binary.readAllBytes(); //TODO handle exception
        }

        return bytes;
    }
}
