package mscalc.gui.statisticsbox;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StatisticsBox extends VBox implements Initializable {
    public static void showModal(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    StatisticsBox.class.getResource("StatisticsBox.fxml"));

            var childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.NONE);
            childWindow.initStyle(StageStyle.UTILITY);
            childWindow.setTitle("About...");
            childWindow.setResizable(false);
            childWindow.setScene(new Scene(loader.load()));

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
