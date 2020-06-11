@SuppressWarnings("module")
module net.novaware.chip8.swing {
    requires transitive java.desktop;
    requires transitive net.novaware.chip8.core;

    requires org.apache.logging.log4j;

    requires com.formdev.flatlaf;

    exports net.novaware.chip8.swing;
}