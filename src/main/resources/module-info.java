@SuppressWarnings("module")
module net.novaware.chip8.swing {
    requires transitive jdk.compiler;
    requires transitive java.desktop;
    requires transitive net.novaware.chip8.core;

    requires org.apache.logging.log4j;

    requires filters; //com.jhlabs.filters

    requires grpc.api;
    requires grpc.stub;
    requires grpc.protobuf;
    requires com.google.protobuf;
    requires com.google.common;

    requires jsr305;
//    requires annotations.api; //patched into jsr305

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