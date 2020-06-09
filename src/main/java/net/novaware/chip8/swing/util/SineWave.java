package net.novaware.chip8.swing.util;

/**
 * https://en.wikipedia.org/wiki/Sine_wave
 */
public class SineWave extends AbstractWave {

    public SineWave(int amplitude, double frequency, int phase) {
        super(amplitude, frequency, phase);
    }

    public SineWave(int amplitude, double frequency) {
        super(amplitude, frequency);
    }

    @Override
    public int getSample(double time) {
        return (int) (amplitude * Math.sin(2 * Math.PI * frequency * time + phase));
    }

}
