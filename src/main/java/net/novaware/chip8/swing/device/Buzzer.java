package net.novaware.chip8.swing.device;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.function.Consumer;

/**
 * Sound device
 */
public class Buzzer implements Consumer<Boolean> {
    // A Midi device for simple tone generation
    //TODO: consider different java api for sound generation
    private Synthesizer synthesizer;

    // The Midi channel to perform playback on
    /*private*/ MidiChannel midiChannel;

    public Buzzer() {

    }

    public void init() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            throw new RuntimeException("Unable to init buzzer: ", e);
        }
    }

    public void startBuzzing() {
        midiChannel.noteOn(60, 400);
    }

    public void stopBuzzing() {
        midiChannel.noteOff(60);
    }


    public static void main(String[] args) throws InterruptedException {
        Buzzer b = new Buzzer();
        b.init();

        for (int i = 0; i < 128; ++i) {
            b.midiChannel.noteOn(i, 400);
            System.out.println(i);
            Thread.sleep(1000);
            b.midiChannel.noteOff(i);
        }
    }

    @Override
    public void accept(Boolean buzz) {
        if (buzz) {
            startBuzzing();
        } else {
            stopBuzzing();
        }
    }
}


