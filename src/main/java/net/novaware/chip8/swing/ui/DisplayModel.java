package net.novaware.chip8.swing.ui;

import java.beans.PropertyChangeListener;

public interface DisplayModel {

    void addDataUpdateListener(PropertyChangeListener listener);

    int getColumnCount();

    int getRowCount();

    boolean isPixelOn(int col, int row);
}
