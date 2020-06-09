package net.novaware.chip8.swing.util;

/**
 * https://en.wikipedia.org/wiki/Waveform
 * https://pudding.cool/2018/02/waveforms/
 */
public interface Waveform {

    /**
     *
     */
    int getAmplitude();


    int getPhase();

    /**
     * Inverse of period. Number of cycles in a second.
     */
    double getFrequency();

    /**
     * The distance sound travels during one period. lambda = speed of sound * period
     */
    default int getWaveLength() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * The time it takes to complete a cycle of a sound wave.
     */
    default int getPeriod() {
        throw new UnsupportedOperationException("not implemented");
    }

    int getSample(double time);
}
