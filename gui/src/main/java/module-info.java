module mscalc.gui {
    requires mscalc.engine;

    requires javafx.controls;
    requires javafx.fxml;

    exports mscalc.gui;
    exports mscalc.gui.mainwindow;

    // Allow @FXML injection to private fields.
    opens mscalc.gui.mainwindow;
}