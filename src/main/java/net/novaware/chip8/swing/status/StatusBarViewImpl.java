package net.novaware.chip8.swing.status;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBarViewImpl implements StatusBarView {

    private static final int DEFAULT_HEIGHT = 20;

    private SpringLayout layout;
    private JPanel component;

    private JPanel infoPanel;
    private JLabel infoLabel;

    private JPanel fpsPanel;
    private JLabel fps;

    private JPanel frequencyPanel;
    private JLabel frequency;

    private JPanel delayPanel;
    private JProgressBar delay;

    private JPanel soundPanel;
    private JProgressBar sound;

    private JPanel powerPanel;
    private JLabel power;

    private JPanel soundOnPanel;
    private JLabel soundOn;

    public StatusBarViewImpl() {
        layout = new SpringLayout();

        component = new JPanel(layout);
        component.setPreferredSize(new Dimension(1, DEFAULT_HEIGHT));
    }

    public void initialize() {
        initInfo();
        initFps();
        initFrequency();
        initDelay();
        initSound();
        initPower();
        initSoundOn();

        initConstraints();
    }

    public JPanel getComponent() {
        return component;
    }

    @Override
    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    @Override
    public void setFps(int fps) {
        this.fps.setText(fps + " FPS");
    }

    @Override
    public void setFrequency(int frequency) {
        this.frequency.setText(frequency + " Hz");
    }

    @Override
    public void setDelay(int delay) {
        this.delay.setValue(delay);
    }

    @Override
    public void setSound(int sound) {
        this.sound.setValue(sound);
    }

    @Override
    public void setSoundOn(boolean on) {
        if (on) {
            soundOn.setText("Q \uD83D\uDD0A");
        } else {
            soundOn.setText("Q \uD83D\uDD08");
        }
    }

    @Override
    public void setPowerOn(boolean on) {
        if (on) {
            power.setText("RUN ⏵️");
        } else {
            power.setText("RUN ⏸️");
        }
    }

    private void initSoundOn() {
        soundOnPanel = new JPanel(new BorderLayout());
        soundOnPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        soundOnPanel.setPreferredSize(new Dimension(30, DEFAULT_HEIGHT));
        component.add(soundOnPanel);

        soundOn = new JLabel("Q ");
        soundOnPanel.add(soundOn, BorderLayout.CENTER);
    }

    private void initPower() {
        powerPanel = new JPanel(new BorderLayout());
        powerPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        powerPanel.setPreferredSize(new Dimension(50, DEFAULT_HEIGHT));
        component.add(powerPanel);

        power = new JLabel("RUN ️");
        powerPanel.add(power, BorderLayout.CENTER);
    }

    private void initSound() {
        soundPanel = new JPanel(new BorderLayout());
        soundPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        soundPanel.setPreferredSize(new Dimension(80, DEFAULT_HEIGHT));
        component.add(soundPanel);

        JLabel st = new JLabel("ST ");
        soundPanel.add(st, BorderLayout.WEST);

        sound = new JProgressBar();
        soundPanel.add(sound);
        sound.setMaximum(20);
    }

    private void initDelay() {
        delayPanel = new JPanel(new BorderLayout());
        delayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        delayPanel.setPreferredSize(new Dimension(80, DEFAULT_HEIGHT));
        component.add(delayPanel);

        JLabel dt = new JLabel("DT ");
        delayPanel.add(dt, BorderLayout.WEST);

        delay = new JProgressBar();
        delayPanel.add(delay);
        delay.setMaximum(60);
    }

    private void initFrequency() {
        frequencyPanel = new JPanel(new BorderLayout());
        frequencyPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        frequencyPanel.setPreferredSize(new Dimension(70, DEFAULT_HEIGHT));
        component.add(frequencyPanel);

        frequency = new JLabel("? Hz");
        frequency.setHorizontalAlignment(SwingConstants.RIGHT);
        frequencyPanel.add(frequency, BorderLayout.CENTER);
    }

    private void initFps() {
        fpsPanel = new JPanel(new BorderLayout());
        fpsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        fpsPanel.setPreferredSize(new Dimension(60, DEFAULT_HEIGHT));
        component.add(fpsPanel);

        fps = new JLabel("? FPS");
        fps.setHorizontalAlignment(SwingConstants.RIGHT);
        fpsPanel.add(fps, BorderLayout.CENTER);
    }

    private void initInfo() {
        infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        component.add(infoPanel);

        infoLabel = new JLabel(" ");
        infoLabel.setToolTipText(" ");
        infoPanel.add(infoLabel, BorderLayout.CENTER);
    }

    private void initConstraints() {
        layout.putConstraint(SpringLayout.WEST, infoPanel, 0, SpringLayout.WEST, component);
        layout.putConstraint(SpringLayout.NORTH, infoPanel, 0, SpringLayout.NORTH, component);
        layout.putConstraint(SpringLayout.SOUTH, infoPanel, 0, SpringLayout.SOUTH, component);

        layout.putConstraint(SpringLayout.EAST, infoPanel, 0, SpringLayout.WEST, fpsPanel);
        layout.putConstraint(SpringLayout.EAST, fpsPanel, 0, SpringLayout.WEST, frequencyPanel);
        layout.putConstraint(SpringLayout.EAST, frequencyPanel, 0, SpringLayout.WEST, delayPanel);
        layout.putConstraint(SpringLayout.EAST, delayPanel, 0, SpringLayout.WEST, soundPanel);
        layout.putConstraint(SpringLayout.EAST, soundPanel, 0, SpringLayout.WEST, powerPanel);
        layout.putConstraint(SpringLayout.EAST, powerPanel, 0, SpringLayout.WEST, soundOnPanel);
        layout.putConstraint(SpringLayout.EAST, soundOnPanel, 0, SpringLayout.EAST, component);
    }
}
