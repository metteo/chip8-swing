package net.novaware.chip8.swing.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaveGenerator implements AutoCloseable, Runnable {

    private static final int BYTE = 8;
    private static final int SAMPLE_RATE = 44100;

    //writing happens only from emulator thread
    private volatile boolean playing = false;

    private Waveform waveform = new SquareWave(16, 150);

    private AudioFormat audioFormat;
    private SourceDataLine sourceDataLine;

    //owned by executor below
    private byte[] sourceDataBuffer = new byte[256];
    private int sampleIndex = 1;

    private ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "WaveGenerator");
        t.setDaemon(true);
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    });

    public void init() {
        audioFormat = new AudioFormat(44100f, 1 * BYTE, 1, true, false);
        try {
            sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceDataLine.open(audioFormat, sourceDataBuffer.length);
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Unable to initialize source data line: " + e);
        }
    }

    public void play() {
        if (playing) {
            return;
        }

        try {
            sourceDataLine.open(audioFormat, sourceDataBuffer.length * 2);
            playing = true;
            executor.submit(this);
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Unable to open source data line: ", e);
        }
    }

    @Override
    public void run() {
        generateSamples(); // pre-generate before starting the loop
        sourceDataLine.write(sourceDataBuffer, 0, sourceDataBuffer.length);
        sourceDataLine.start();

        while (playing) {
            generateSamples();
            sourceDataLine.write(sourceDataBuffer, 0, sourceDataBuffer.length);
        }
    }

    private void generateSamples() {
        for (int i = 0; i < sourceDataBuffer.length; i++, sampleIndex++) {
            sourceDataBuffer[i] = (byte) waveform.getSample((double) sampleIndex / SAMPLE_RATE);
        }
    }

    public void stop() {
        if (playing) {
            playing = false;
            sourceDataLine.stop();
            sourceDataLine.flush();
        }
    }

    public void close() {
        stop();
        executor.shutdown();
        sourceDataLine.close();
    }

    public static void main(String[] args) throws InterruptedException {
        WaveGenerator wg = new WaveGenerator();
        wg.init();
        wg.play();

        Thread.sleep(2000);

        wg.stop();

        wg.close();
    }
}
