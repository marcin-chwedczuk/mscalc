package mscalc.gui.views.scientific;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import mscalc.gui.views.CalculatorView;

import java.io.IOException;

public class ScientificView extends VBox implements CalculatorView {

    public ScientificView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ScientificView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        // Ugly hack!
        // https://stackoverflow.com/questions/24016229/cant-import-custom-components-with-custom-cell-factories/24039826#24039826
        // fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Cannot load view: " + exception);
        }
    }
}
