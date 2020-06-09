package net.novaware.chip8.swing.util;

/**
 * https://en.wikipedia.org/wiki/Triangle_wave
 */
public class TriangleWave extends AbstractWave {

    public TriangleWave(int amplitude, double frequency, int phase) {
        super(amplitude, frequency, phase);
    }

    public TriangleWave(int amplitude, double frequency) {
        super(amplitude, frequency);
    }

    @Override
    public int getSample(double time) {
        throw new UnsupportedOperationException("not implemented!");
    }

}
