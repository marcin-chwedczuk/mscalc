package mscalc.gui.mainwindow;

import csstool.CssTool;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import mscalc.gui.about.AboutDialog;
import mscalc.gui.statisticsbox.StatisticsBox;
import mscalc.gui.views.scientific.ScientificView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow {
    public static MainWindow showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            Scene scene = new Scene(loader.load());
            MainWindow controller = (MainWindow) loader.getController();

            window.getIcons().add(new Image(MainWindow.class.getResourceAsStream("/calc-icon-48.png")));
            window.setTitle("Calculator");
            window.setScene(scene);
            window.setResizable(false);

            controller.init();

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private BorderPane container;

    public void init() {
        // We cannot use initialize here, as Scene is not available in initialize()
        var view = new ScientificView();
        container.setCenter(view);
        view.install(container.getScene());
    }

    public void showCssTool(ActionEvent event) {
        Stage secondStage = new Stage();
        secondStage.initModality(Modality.NONE);

        CssTool.showOn(secondStage);
    }

    @FXML
    public void showAboutDialog() {
        AboutDialog.showModal(getCurrentWindow());
        // StatisticsBox.showUtility(getCurrentWindow());
    }

    private Window getCurrentWindow() {
        return container.getScene().getWindow();
    }
}
