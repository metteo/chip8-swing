package net.novaware.chip8.swing.device;

import net.novaware.chip8.core.port.AudioPort;
import net.novaware.chip8.swing.util.WaveGenerator;

import java.util.function.Consumer;

/**
 * Sound device
 */
public class Buzzer implements Consumer<AudioPort.Packet> {

    private WaveGenerator waveGenerator;

    public Buzzer() {
        waveGenerator = new WaveGenerator();
    }

    public void init() {
        waveGenerator.init();
    }

    @Override
    public void accept(AudioPort.Packet buzz) {
        if (buzz.isSoundOn()) {
            waveGenerator.play();
        } else {
            waveGenerator.stop();
        }
    }

    public void close() {
        waveGenerator.close();
    }
}
