package net.novaware.chip8.swing.profile;

import net.novaware.chip8.core.config.MutableConfig;

import java.awt.event.KeyEvent;
import java.util.function.Function;

import static java.awt.event.KeyEvent.*;

public class ProfileStub {

    public static Function<KeyEvent, Integer> loadProfile(String title, MutableConfig config) {
        MutableConfig defaults = new MutableConfig();

        config.setLegacyShift(defaults.isLegacyShift());
        config.setLegacyLoadStore(defaults.isLegacyLoadStore());
        config.setLegacyAddressSum(defaults.isLegacyAddressSum());
        config.setCpuFrequency(defaults.getCpuFrequency());
        config.setEnforceMemoryRoRwState(defaults.isEnforceMemoryRoRwState());

        Function<KeyEvent, Integer> mapper = ke -> -1;

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

                return -1;
            };
        }

        if (title.equals("VERS")) {
            config.setCpuFrequency(500);
            config.setVerticalClipping(false);

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

                return -1;
            };
        }

        if (title.equals("BLITZ")) {
            config.setVerticalClipping(true);
        }

        if (title.equals("BRIX")) {
            config.setCpuFrequency(700);
            config.setEnforceMemoryRoRwState(false);
            config.setHorizontalClipping(true);
            config.setWrapping(false);
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

                return -1;
            };
        }

        if (title.equals("PONG2")) {
            config.setEnforceMemoryRoRwState(false);

            mapper = keyEvent -> {
                switch(keyEvent.getKeyCode()) {
                    case VK_W: return 1;
                    case VK_S: return 4;
                    case VK_UP: return 0xC;
                    case VK_DOWN: return 0xD;
                }

                return -1;
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

                return -1;
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

        return mapper;
    }
}
