package net.novaware.chip8.swing.proto;

import io.grpc.stub.StreamObserver;
import net.novaware.chip8.core.port.AudioPort;

public class Chip8ServiceImpl extends Chip8ServiceGrpc.Chip8ServiceImplBase {

    private volatile boolean sound = true;

    private AudioPort audioPort;

    public Chip8ServiceImpl(AudioPort audioPort) {
        this.audioPort = audioPort;
    }

    @Override
    public void getSound(SoundRequest request, StreamObserver<SoundState> responseObserver) {
        audioPort.connect(p -> {
            responseObserver.onNext(SoundState.newBuilder().setOn(p.isSoundOn()).build());
        });

        while(sound) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        audioPort.disconnect();
        responseObserver.onCompleted();
    }
}
