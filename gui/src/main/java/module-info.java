module mscalc.gui {
    requires mscalc.engine;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.apache.logging.log4j;
    requires java.xml; // for log4j2

    exports mscalc.gui;
    exports mscalc.gui.mainwindow;
    exports mscalc.gui.views;
    exports mscalc.gui.views.basic;
    exports mscalc.gui.views.scientific;
    exports mscalc.gui.statisticsbox;
    exports mscalc.gui.about;

    // Allow @FXML injection to private fields.
    opens mscalc.gui.mainwindow;
    opens mscalc.gui.views;
    opens mscalc.gui.views.basic;
    opens mscalc.gui.views.scientific;
    opens mscalc.gui.statisticsbox;
    opens mscalc.gui.about;

    // CSS Tool
    exports csstool;
    opens csstool;
    requires org.reflections;
    requires javafx.web;
    requires vaadin.sass.compiler;
    requires sac;
    requires jsr305;


}