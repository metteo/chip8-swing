package net.novaware.chip8.swing.ui;

import net.novaware.chip8.core.port.DisplayPort;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class DefaultDisplayModel implements DisplayModel {

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private DisplayPort.Packet packet;

    public void updateWith(DisplayPort.Packet packet) {
        //FIXME: serious threading issue!
        this.packet = packet;

        propertyChangeSupport.firePropertyChange("data", 0, -1);
    }

    @Override
    public void addDataUpdateListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener("data", listener);
    }

    @Override
    public int getColumnCount() {
        if (packet != null) {
            return packet.getColumnCount();
        } else {
            return 64;
        }
    }

    @Override
    public int getRowCount() {
        if (packet != null) {
            return packet.getRowCount();
        } else {
            return 32;
        }
    }

    @Override
    public boolean isPixelOn(int col, int row) {
        if (packet != null) {
            return packet.getPixel(col, row);
        } else {
            return false;
        }
    }
}
