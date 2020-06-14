@SuppressWarnings("module")
module net.novaware.chip8.swing {
    requires transitive java.desktop;
    requires transitive net.novaware.chip8.core;

    requires org.apache.logging.log4j;

    //TODO: should be transitive of jiconfont.swing
    requires jiconfont;
    requires jiconfont.swing;
    requires jiconfont.google.material.design.icons;

    //TODO: make optional and install only if available
    requires com.formdev.flatlaf;

    //TODO: make optional and install only if available
    requires io.github.vincenzopalazzo.materialuiswing;

    exports net.novaware.chip8.swing;
}