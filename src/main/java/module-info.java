module kisu {
    requires java.compiler;

    requires javafx.controls;
    requires org.slf4j;

    requires pinej;
    requires kisu.plugins;

    opens kisu to javafx.graphics;
    opens kisu.exceptions to javafx.graphics;
    opens kisu.plugin to javafx.graphics;
}