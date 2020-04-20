package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.port.StoragePort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.novaware.chip8.core.util.UnsignedUtil.uint;

/**
 * Storage device
 */
public class Tape {

    private static final Logger LOG = LogManager.getLogger();

    private Path romPath;

    public Tape(Path romPath) {
        this.romPath = romPath;
    }
    //TODO: implement loading of roms
    //create a DB of rom hashes and key mapping for each rom, maybe some basic metadata with screenshot

    public byte[] load() {
        if (romPath == null) {
            return new byte[] { 0x11, 0x00 }; // GO 100 (jump to Boot-128)
        }

        final byte[] bytes;
        try (var binary = Files.newInputStream(romPath)) {
            bytes = binary.readAllBytes(); //TODO handle exception
        } catch (IOException e) {
            LOG.error("Exc while loading ROM", e);
            return new byte[0];
        }

        return bytes;
    }

    public StoragePort.Packet toPacket() {
        byte[] bytes = load();

        return new StoragePort.Packet() {
            @Override
            public int getSize() {
                return bytes.length;
            }

            @Override
            public byte getByte(short i) {
                return bytes[uint(i)];
            }
        };
    }
}
