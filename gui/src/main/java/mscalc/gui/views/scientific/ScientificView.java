package mscalc.gui.views.scientific;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mscalc.gui.viewmodel.ScientificCalculatorViewModel;
import mscalc.gui.views.CalculatorView;

import java.io.IOException;

public class ScientificView extends VBox implements CalculatorView {
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

    public void install(Scene scene) {
        display.textProperty().bind(viewModel.displayProperty);

        bindButton(bDigit1, viewModel.digitOneButton);
        bindButton(bSine, viewModel.sineButton);

        try {
            scene.setOnKeyPressed(this::onKeyPressed);
            scene.setOnKeyReleased(this::onKeyReleased);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void uninstall() {
        throw new RuntimeException("TODO");
    }

    private void bindButton(Button button, ScientificCalculatorViewModel.InputViewModel operation) {
        button.textProperty().bind(operation.textProperty());
        button.disableProperty().bind(operation.enabledProperty().not());
        button.setOnAction(e -> operation.execute());
    }

    private void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case KeyCode.S -> {
                bSine.arm();
            }

            default -> {
                return;
            }
        }

        e.consume();
    }

    private void onKeyReleased(KeyEvent e) {
        switch (e.getCode()) {
            case KeyCode.S -> {
                bSine.disarm();
                bSine.fire();
            }

            default -> {
                return;
            }
        }

        e.consume();
    }

}
