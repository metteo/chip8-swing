package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.port.KeyPort;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Key device
 */
public class Keyboard extends KeyAdapter {
    //16 keys

    private short keyState;
    private byte lastKey;

    private KeyPort keyPort;

    @Override
    public void keyPressed(KeyEvent e) {
        //displayInfo(e, "KEY PRESSED: ");

        if (e.getKeyCode() == KeyEvent.VK_R) {
            keyPort.reset();
            return;
        }

        int keyIdx = normalizeKeyCode(e);
        if (keyIdx >= 0x0 && keyIdx <= 0xF) {
            //System.out.println("+" + keyIdx);

            lastKey = (byte) keyIdx;
            keyPort.keyPressed(lastKey);

            //TODO: registers.getKeyWait().set((byte)0x0);

            final short currentKeyState = keyState;

            int keyMask = 1 << keyIdx;
            boolean alreadyPressed = (Short.toUnsignedInt(currentKeyState) & keyMask) > 0;

            if (!alreadyPressed) {
                final short newKeyState = (short)(keyMask | Short.toUnsignedInt(currentKeyState));

                keyState = newKeyState;
                keyPort.updateKeyState(keyState);
            }

            //System.out.println(String.format("%16s", Integer.toBinaryString(registers.getKeyState().get())).replace(' ', '0'));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { //TODO: add heuristic that doesn't clear the keys until they are checked for presence (configurable ofc)
        //displayInfo(e, "KEY RELEASED: ");

        int keyIdx = normalizeKeyCode(e);
        if (keyIdx >= 0x0 && keyIdx <= 0xF) {
            //System.out.println("-" + keyIdx);

            final short currentKeyState = keyState;

            int keyMask = 1 << keyIdx;
            boolean alreadyReleased = (Short.toUnsignedInt(currentKeyState) & keyMask) == 0;

            if (!alreadyReleased) {
                final short newKeyState = (short)(~keyMask & Short.toUnsignedInt(currentKeyState));

                keyState = newKeyState;
                keyPort.updateKeyState(keyState);
            }

            //System.out.println(String.format("%16s", Integer.toBinaryString(registers.getKeyState().get())).replace(' ', '0'));
        }
    }

    public void init(KeyPort keyPort, Component c) {
        this.keyPort = keyPort;

        c.addKeyListener(this);
    }

    private static int normalizeKeyCode(KeyEvent e) {
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
}
