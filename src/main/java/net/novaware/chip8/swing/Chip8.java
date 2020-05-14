package net.novaware.chip8.swing;

import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl;
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

        ClockGenerator clock = new ClockGeneratorJvmImpl("Swing");

        Board board = newBoardFactory(config, clock, new Random()::nextInt)
                .newBoard();

        SwingUtilities.invokeLater(() -> {
            try { //TODO: allow runtime change from menu
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e
            ) {
                LOG.error("Unable to set L&F: ", e);
            }

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

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true"); //TODO: set from script

        String path = args.length == 1 ? args[0] : null;

        new Chip8(path).start();
    }

}
