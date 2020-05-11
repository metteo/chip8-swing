package net.novaware.chip8.swing.status;

import net.novaware.chip8.swing.mvp.HasComponent;

public interface StatusBarView extends HasComponent {

    void setInfo(String info);

    void setFps(int fps);

    void setFrequency(int frequency);

    void setDelay(int delay);

    void setSound(int sound);

    void setPowerOn(boolean on);

    void setSoundOn(boolean on);
}
