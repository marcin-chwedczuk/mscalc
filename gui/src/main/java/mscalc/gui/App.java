package mscalc.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import mscalc.gui.mainwindow.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);


    @Override
    public void start(Stage stage) {
        MainWindow.showOn(stage);
    }

    public static void main(String[] args) {
        logger.info("Application is starting with args: {}", (Object)args);

        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                // TODO: Working but needs polishing
                System.out.println("PROTOCOL setURLStreamHandlerFactory: " + protocol);
                if ("fxcss".equals(protocol)) {
                    return new csstool.FxCssURLStreamHandler();
                }

                System.out.println("NULL FOR: " + protocol);
                return null;
            }
        });

        launch();
    }
}