package net.novaware.chip8.swing.menu;

import net.novaware.chip8.swing.mvp.View;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

//TODO: maybe split into sub views: file, edit, etc
public interface MenuBarView extends View {

    Consumer<ActionListener> getOpen();

    void showOpenDialog(Consumer<File> fileConsumer);

    void setRecentOpens(List<File> recents);
    void setRecentHandler(Consumer<File> handler);

    Consumer<ActionListener> getClose();
    Consumer<ActionListener> getExit();

    Consumer<ActionListener> getReset();
    Consumer<ActionListener> getSoftReset();

    Consumer<ActionListener> getPause();
    void setPauseEnabled(boolean enabled);

    Consumer<ActionListener> getResume();
    void setResumeEnabled(boolean enabled);

    Consumer<ActionListener> getAutoPause();
    void setAutoPauseSelected(boolean selected);
    boolean isAutoPauseSelected();

    Consumer<ActionListener> getLegacyShift();
    void setLegacyShiftSelected(boolean selected);
    boolean isLegacyShiftSelected();

    Consumer<ActionListener> getLegacyLoadStore();
    void setLegacyLoadStoreSelected(boolean selected);
    boolean isLegacyLoadStoreSelected();

    Consumer<ActionListener> getLegacyAddressSum();
    void setLegacyAddressSumSelected(boolean selected);
    boolean isLegacyAddressSumSelected();

    Consumer<ActionListener> getIncreaseFrequency();
    Consumer<ActionListener> getDecreaseFrequency();

    void setFrequency(int frequency);

    Consumer<ActionListener> getMemoryProtection();
    void setMemoryProtectionSelected(boolean selected);
    boolean isMemoryProtectionSelected();

    Consumer<ActionListener> getCosmac();
    void setCosmacSelected(boolean selected);
    boolean isCosmacSelected();

    Consumer<ActionListener> getBorder();
    void setBorderSelected(boolean selected);
    boolean isBorderSelected();

    Consumer<ActionListener> getBrick();
    void setBrickSelected(boolean selected);
    boolean isBrickSelected();

    Consumer<ActionListener> getIncreaseScale();
    Consumer<ActionListener> getDecreaseScale();

    void setIncreaseScale(int scale);
    void setCurrentScale(int scale);
    void setDecreaseScale(int scale);

    Consumer<ActionListener> getNoProcessing();
    void setNoProcessingSelected(boolean selected);
    boolean isNoProcessingSelected();

    Consumer<ActionListener> getMerge();
    void setMergeSelected(boolean selected);
    boolean isMergeSelected();

    Consumer<ActionListener> getFallingEdge();
    void setFallingEdgeSelected(boolean selected);
    boolean isFallingEdgeSelected();

    Consumer<ActionListener> getDecoration();
    void setDecorationSelected(boolean selected);
    boolean isDecorationSelected();

    Consumer<ActionListener> getDistraction();
    void setDistractionSelected(boolean selected);
    boolean isDistractionSelected();

    Consumer<ActionListener> getFullScreen();
    void setFullScreenSelected(boolean selected);
    boolean isFullScreenSelected();

    Consumer<ActionListener> getPrimaryDisplay();
    void setPrimaryDisplaySelected(boolean selected);
    boolean isPrimaryDisplaySelected();

    Consumer<ActionListener> getSecondaryDisplay();
    void setSecondaryDisplaySelected(boolean selected);
    boolean isSecondaryDisplaySelected();

    Consumer<ActionListener> getHelpPage();
    Consumer<ActionListener> getKeys();
    Consumer<ActionListener> getAbout();
}
