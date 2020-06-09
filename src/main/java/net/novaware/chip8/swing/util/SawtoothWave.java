package net.novaware.chip8.swing.util;

/**
 * https://en.wikipedia.org/wiki/Sawtooth_wave
 */
public class SawtoothWave extends AbstractWave {

    public SawtoothWave(int amplitude, double frequency, int phase) {
        super(amplitude, frequency, phase);
    }

    public SawtoothWave(int amplitude, double frequency) {
        super(amplitude, frequency);
    }

    @Override
    public int getSample(double time) {
        throw new UnsupportedOperationException("not implemented!");
    }

}
