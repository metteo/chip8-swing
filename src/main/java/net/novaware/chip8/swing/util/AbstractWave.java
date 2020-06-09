package net.novaware.chip8.swing.util;

public abstract class AbstractWave implements Waveform {

    protected int amplitude;
    protected double frequency;
    protected int phase;

    public AbstractWave(int amplitude, double frequency, int phase) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
    }

    public AbstractWave(int amplitude, double frequency) {
        this(amplitude, frequency, 0);
    }

    @Override
    public int getAmplitude() {
        return amplitude;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    @Override
    public int getPhase() {
        return phase;
    }

    public abstract int getSample(double time);
}
