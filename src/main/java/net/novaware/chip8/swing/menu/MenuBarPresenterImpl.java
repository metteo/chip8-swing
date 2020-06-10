package net.novaware.chip8.swing.menu;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.config.MutableConfig;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.port.StoragePort;
import net.novaware.chip8.swing.mvp.AbstractPresenter;
import net.novaware.chip8.swing.ui.JDisplay;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static javax.swing.JOptionPane.showMessageDialog;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;

public class MenuBarPresenterImpl extends AbstractPresenter<MenuBarView> implements MenuBarPresenter {

    public static final int FREQUENCY_STEP = 100;
    public static final int FREQUENCY_MIN = 100;

    private final MutableConfig config;
    private final Board board;
    private final DisplayPort.Type dpType;
    private final String path; //TODO: app scope (not window)
    private final Consumer<String> titleConsumer; //TODO: app scope (not window)
    private final Runnable exitRun; //TODO: app scope (not window)
    private final Supplier<JDisplay.Style> styleGetter;
    private final Consumer<JDisplay.Style> styleSetter;
    private final IntSupplier scaleGetter;
    private final IntConsumer scaleSetter;
    private final Consumer<Boolean> pauseIndicator; //TODO: app scope (not window)

    private SwingWorker<StoragePort.Packet, Void> openWorker; //TODO: app scope (not window)

    private LinkedList<File> recentFiles = new LinkedList<>(); //TODO: app scope (not window)

    private boolean autoPause = true; //TODO: app scope (not window)
    private boolean paused = false; //TODO: app scope (not window)

    public MenuBarPresenterImpl(
            MenuBarView view,
            MutableConfig config,
            Board board,
            DisplayPort.Type dpType,
            String path,

            Consumer<String> titleConsumer,
            Runnable exitRun,
            Supplier<JDisplay.Style> styleGetter,
            Consumer<JDisplay.Style> styleSetter,
            IntSupplier scaleGetter,
            IntConsumer scaleSetter,
            Consumer<Boolean> pauseIndicator
    ) {
        super(view);

        this.config = config;
        this.board = board;
        this.dpType = dpType;
        this.path = path;
        this.titleConsumer = titleConsumer;
        this.exitRun = exitRun;
        this.styleGetter = styleGetter;
        this.styleSetter = styleSetter;
        this.scaleGetter = scaleGetter;
        this.scaleSetter = scaleSetter;
        this.pauseIndicator = pauseIndicator;
    }

    @Override
    public void initialize() {
        view.getOpen().accept(ae -> view.showOpenDialog(this::onOpen));
        view.setRecentHandler(this::onOpen);
        view.getClose().accept(ae -> onClose());
        view.getExit().accept(ae -> exitRun.run());
        view.getReset().accept(ae -> board.hardReset());
        view.getSoftReset().accept(ae -> board.softReset());
        view.getPause().accept(ae -> onPause());
        view.getResume().accept(ae -> onResume());
        view.getAutoPause().accept(ae -> onAutoPause());
        view.getLegacyShift().accept(ae -> onLegacyShift());
        view.getLegacyLoadStore().accept(ae -> onLegacyLoadStore());
        view.getLegacyAddressSum().accept(ae -> onLegacyAddressSum());
        view.getIncreaseFrequency().accept(ae -> onIncreaseFrequency());
        view.getDecreaseFrequency().accept(ae -> onDecreaseFrequency());
        view.getMemoryProtection().accept(ae -> onMemoryProtection());
        view.getCosmac().accept(ae -> onStyle(JDisplay.Style.SOLID));
        view.getBorder().accept(ae -> onStyle(JDisplay.Style.BORDERED));
        view.getBrick().accept(ae -> onStyle(JDisplay.Style.BRICKED));
        view.getIncreaseScale().accept(ae -> onIncreaseScale());
        view.getDecreaseScale().accept(ae -> onDecreaseScale());
        view.getNoProcessing().accept(ae -> onProcessing(DisplayPort.Mode.DIRECT));
        view.getFallingEdge().accept(ae -> onProcessing(DisplayPort.Mode.FALLING_EDGE));
        view.getMerge().accept(ae -> onProcessing(DisplayPort.Mode.MERGE_FRAME));
        view.getHelpPage().accept(ae -> onHelpPage());
        view.getKeys().accept(ae -> onKeys());
        view.getAbout().accept(ae -> onAbout());

        if (dpType == DisplayPort.Type.PRIMARY) {
            setDefaultStorage();
            scheduleFileOpen();
        }

        updatePauseMenus();
        updateCompatibilityMenus();
        updateFrequencyMenus();
        updateMemoryProtectionMenu();
        updateThemeMenus();
        updateScaleMenus();
        updateProcessingMenus();
    }

    private void onKeys() {
        //TODO: should call view, which delegates to this static method
        showMessageDialog(getView().getComponent().getTopLevelAncestor(),
                "Feature not supported, yet.", "Keys",
                JOptionPane.WARNING_MESSAGE);
    }

    private void onAbout() {
        //TODO: should call view, which delegates to this static method
        showMessageDialog(getView().getComponent().getTopLevelAncestor(),
                "Version: 0.0.1-SNAPSHOT\nAuthor: Grzegorz Nowak",
                "Chip8 Emulgator", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onHelpPage() {
        try {
            URI uri = new URI("https://github.com/metteo/chip8-core"); //TODO: extract from POM ProjectInfo
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            //TODO: show dialog with url to copy
        }
    }

    private void updateProcessingMenus() {
        final DisplayPort.Mode mode = board.getDisplayPort(dpType).getMode();
        switch(mode) {
            case MERGE_FRAME:
                view.setMergeSelected(true);
                break;
            case FALLING_EDGE:
                view.setFallingEdgeSelected(true);
                break;
            case DIRECT:
            default:
                view.setNoProcessingSelected(true);
        }
    }

    private void onProcessing(DisplayPort.Mode mode) {
        board.getDisplayPort(dpType).setMode(mode);
    }

    private void updateScaleMenus() {
        int scale = scaleGetter.getAsInt();

        view.setIncreaseScale(scale + 1);
        view.setCurrentScale(scale);
        view.setDecreaseScale(scale - 1);
    }

    private void onDecreaseScale() {
        final int scale = scaleGetter.getAsInt() - 1;

        if (scale > 0) {
            scaleSetter.accept(scale);
            updateScaleMenus();
        }
    }

    private void onIncreaseScale() {
        final int scale = scaleGetter.getAsInt() + 1;
        scaleSetter.accept(scale);

        updateScaleMenus();
    }

    private void onStyle(JDisplay.Style style) {
        styleSetter.accept(style);

        updateThemeMenus();
    }

    private void updateThemeMenus() {
        final JDisplay.Style style = styleGetter.get();

        switch (style) {
            case SOLID:
                view.setCosmacSelected(true);
                break;
            case BORDERED:
                view.setBorderSelected(true);
                break;
            case BRICKED:
                view.setBrickSelected(true);
                break;
        }
    }

    private void updateMemoryProtectionMenu() {
        boolean protect = config.isEnforceMemoryRoRwState();
        view.setMemoryProtectionSelected(protect);
    }

    private void onMemoryProtection() {
        boolean protect = view.isMemoryProtectionSelected();

        config.setEnforceMemoryRoRwState(protect);

        updateMemoryProtectionMenu();
    }

    private void onIncreaseFrequency() {
        int frequency = config.getCpuFrequency() + FREQUENCY_STEP;

        config.setCpuFrequency(frequency);
        updateFrequencyMenus();
    }

    private void onDecreaseFrequency() {
        int frequency = config.getCpuFrequency() - FREQUENCY_STEP;

        if (frequency < FREQUENCY_MIN) {
            return;
        }

        config.setCpuFrequency(frequency);
        updateFrequencyMenus();
    }

    private void updateFrequencyMenus() {
        int frequency = config.getCpuFrequency();
        view.setFrequency(frequency);

        boolean minimalFrequency = frequency <= FREQUENCY_MIN;
        view.setDecreaseFrequencyEnabled(!minimalFrequency);
    }

    private void onLegacyAddressSum() {
        config.setLegacyAddressSum(view.isLegacyAddressSumSelected());

        updateCompatibilityMenus();
    }

    private void onLegacyLoadStore() {
        config.setLegacyLoadStore(view.isLegacyLoadStoreSelected());

        updateCompatibilityMenus();
    }

    private void onLegacyShift() {
        config.setLegacyShift(view.isLegacyShiftSelected());

        updateCompatibilityMenus();
    }

    private void updateCompatibilityMenus() {
        view.setLegacyShiftSelected(config.isLegacyShift());
        view.setLegacyLoadStoreSelected(config.isLegacyLoadStore());
        view.setLegacyAddressSumSelected(config.isLegacyAddressSum());
    }

    private void onAutoPause() {
        if (view.isAutoPauseSelected()) {
            autoPause = true;
        } else {
            autoPause = false;
            resume();
            updatePauseMenus();
        }
    }

    private void onResume() {
        resume();
        updatePauseMenus();
    }

    private void onPause() {
        pause();
        updatePauseMenus();
    }

    private void pause() {
        paused = true;
        board.pause();
    }

    @Override
    public void onFocusChange(boolean gained) {
        if (autoPause) {
            if (gained) {
                resume();
            } else {
                pause();
            }
            updatePauseMenus();
        }
    }

    @Override
    public void forcePause() {
        autoPause = false;
        pause();
        updatePauseMenus();
    }

    private void resume() {
        paused = false;
        board.resume();
    }

    private void updatePauseMenus() {
        view.setAutoPauseSelected(autoPause);
        view.setPauseEnabled(!paused);
        view.setResumeEnabled(paused);

        pauseIndicator.accept(paused); //TODO: won't work correctly with 2 windows, add Board.isPaused
    }

    private void onClose() {
        titleConsumer.accept("");
        setDefaultStorage();
        board.hardReset();
    }

    @Override
    public void open(File file) {
        onOpen(file);
    }

    @Override
    public void updateCompatMenus() {
        updateCompatibilityMenus();
        updateFrequencyMenus();
        updateMemoryProtectionMenu();
    }

    private void onOpen(File file) {
        if (openWorker != null) {
            //TODO: show dialog that the file is loading
            return;
        }

        openWorker = new SwingWorker<>() {

            @Override
            protected StoragePort.Packet doInBackground() throws Exception {
                byte[] bytes;
                try (var binary = Files.newInputStream(file.toPath())) {
                    bytes = binary.readAllBytes();
                }

                return new StoragePort.Packet() {
                    @Override public int getSize() {
                        return bytes.length;
                    }
                    @Override public byte getByte(short i) {
                        return bytes[uint(i)];
                    }
                };
            }

            @Override
            protected void done() {
                openWorker = null;

                try {
                    StoragePort.Packet packet = get();
                    board.getStoragePort().disconnect();
                    board.getStoragePort().connect(() -> packet);
                    board.hardReset();

                    recentFiles.remove(file); // remove to add on the top
                    recentFiles.addFirst(file);
                    view.setRecentOpens(recentFiles);

                    titleConsumer.accept(file.getName());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    //FIXME: show dialog that file open failed
                }
            }
        };

        openWorker.execute();
    }

    private void scheduleFileOpen() {
        if (path != null) {
            File f = new File(path);

            if (f.exists()) {
                //this is async action, board already starts and this will trigger hard reset when finished
                //the reason why cmd line argument causes 2 beeps at the start.
                onOpen(f);
            }
        }
    }

    private void setDefaultStorage() {
        board.getStoragePort().disconnect();
        board.getStoragePort().connect(() -> new StoragePort.Packet(){
            private byte[] jump = new byte[] { 0x11, 0x00 };

            @Override
            public int getSize() {
                return jump.length;
            }

            @Override
            public byte getByte(short i) {
                return jump[uint(i)];
            }
        });

        titleConsumer.accept("Boot-128");
    }


}
