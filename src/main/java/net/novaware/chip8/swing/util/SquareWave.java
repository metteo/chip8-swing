package net.novaware.chip8.swing.util;

/**
 * https://en.wikipedia.org/wiki/Square_wave
 */
public class SquareWave extends AbstractWave {

    public SquareWave(int amplitude, double frequency, int phase) {
        super(amplitude, frequency, phase);
    }

    public SquareWave(int amplitude, double frequency) {
        super(amplitude, frequency);
    }

    @Override
    public int getSample(double time) {
        return (int) (amplitude * Math.ceil(Math.signum(Math.sin(2f * Math.PI * frequency * time))));
    }

}
