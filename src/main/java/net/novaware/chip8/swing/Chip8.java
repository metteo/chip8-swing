package net.novaware.chip8.swing;

import com.formdev.flatlaf.FlatDarculaLaf;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialLiteTheme;
import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ScheduledClockGenerator;
import net.novaware.chip8.core.config.MutableConfig;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.swing.window.WindowPresenter;
import net.novaware.chip8.swing.window.WindowPresenterImpl;
import net.novaware.chip8.swing.window.WindowView;
import net.novaware.chip8.swing.window.WindowViewImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

import static net.novaware.chip8.core.BoardFactory.newBoardFactory;

public class Chip8 {

    private static final Logger LOG = LogManager.getLogger();

    private String path;
    private WindowPresenter primaryWindow;

    public Chip8(String path) {
        this.path = path;
    }

    void start() {
        //TODO: update config with settings from preferences?
        MutableConfig config = new MutableConfig();

        ClockGenerator clock = new ScheduledClockGenerator("Swing");

        Board board = newBoardFactory(config, clock, new Random()::nextInt)
                .newBoard();

        SwingUtilities.invokeLater(() -> {
            install(new FlatDarculaLaf());
            new MaterialLookAndFeel(new MaterialLiteTheme()); // constructor installs the laf & theme

            WindowView primaryWindowView = new WindowViewImpl();

            primaryWindow = new WindowPresenterImpl(
                    primaryWindowView,
                    config,
                    clock,
                    board,
                    DisplayPort.Type.PRIMARY
            );

            primaryWindow.setPath(path);
            getIcon().ifPresent(primaryWindow::setIcon);
            primaryWindow.initialize();
            primaryWindow.start();
        });
    }

    private static Optional<BufferedImage> getIcon() {
        final URL iconUrl = Chip8.class.getResource("/c8.png");

        try {
            return Optional.of(ImageIO.read(iconUrl));
        } catch (IOException e) {
            LOG.warn("Unable to load custom app icon, falling back to default. ", e);
            return Optional.empty();
        }
    }

    private static void install(LookAndFeel laf) {
        UIManager.installLookAndFeel(laf.getName(), laf.getClass().getName());
    }

    public static void main(String[] args) {
        String path = args.length == 1 ? args[0] : null;

        new Chip8(path).start();
    }
}
