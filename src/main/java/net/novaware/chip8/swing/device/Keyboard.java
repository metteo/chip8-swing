package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.port.KeyPort;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte;

/**
 * Key device
 */
public class Keyboard extends KeyAdapter {
    //16 keys

    private short keyState;

    private Consumer<KeyPort.InputPacket> keyReceiver;
    public Runnable resetHandler;

    public Function<KeyEvent, Integer> mapper = Keyboard::normalizeKeyCode;

    @Override
    public void keyPressed(KeyEvent e) {
        //displayInfo(e, "KEY PRESSED: ");

        if (e.getKeyCode() == KeyEvent.VK_R) {
            if (resetHandler != null) {
                resetHandler.run();
            }
            return;
        }

        int keyIdx = mapper.apply(e);
        if (keyIdx >= 0x0 && keyIdx <= 0xF) {
            keyReceiver.accept(new InPacket(KeyPort.Direction.DOWN, ubyte(keyIdx)));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { //TODO: add heuristic that doesn't clear the keys until they are checked for presence (configurable ofc)
        //displayInfo(e, "KEY RELEASED: ");

        int keyIdx = mapper.apply(e);
        if (keyIdx >= 0x0 && keyIdx <= 0xF) {
            keyReceiver.accept(new InPacket(KeyPort.Direction.UP, ubyte(keyIdx)));
        }
    }

    public void init(Consumer<KeyPort.InputPacket> keyReceiver, Component c) {
        this.keyReceiver = keyReceiver;

        c.addKeyListener(this);
    }

    public static int normalizeKeyCode(KeyEvent e) {
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
