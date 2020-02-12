module net.novaware.chip8.swing {
    requires java.desktop;
    requires net.novaware.chip8.core;

    requires org.apache.logging.log4j;
    requires java.sql; //TODO: jackson yaml support requires it (report it)

    exports net.novaware.chip8.swing;
}