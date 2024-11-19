package mscalc.gui.views.scientific;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mscalc.gui.viewmodel.ScientificCalculatorViewModel;
import mscalc.gui.views.CalculatorView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ScientificView extends VBox implements CalculatorView, Initializable {
    private final ScientificCalculatorViewModel viewModel = new ScientificCalculatorViewModel();

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

    @FXML
    private TextField display;

    @FXML
    private Button bDigit1;

    @FXML
    private Button bSine;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        display.textProperty().bind(viewModel.displayProperty);

        bindButton(bDigit1, viewModel.digitOneButton);
        bindButton(bSine, viewModel.sineButton);
    }

    private void bindButton(Button button, ScientificCalculatorViewModel.InputViewModel operation) {
        button.textProperty().bind(operation.textProperty());
        button.disableProperty().bind(operation.enabledProperty().not());
        button.setOnAction(e -> operation.execute());
    }
}
