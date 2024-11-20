package mscalc.gui.views.scientific;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mscalc.engine.RadixType;
import mscalc.gui.App;
import mscalc.gui.viewmodel.ScientificCalculatorViewModel;
import mscalc.gui.views.CalculatorView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ScientificView extends VBox implements CalculatorView {
    private static final Logger logger = LogManager.getLogger(ScientificView.class);

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
    private ToggleGroup radixToggleGroup;
    @FXML
    private RadioButton radioRadixHex;
    @FXML
    private RadioButton radioRadixDec;
    @FXML
    private RadioButton radioRadixOct;
    @FXML
    private RadioButton radioRadixBin;

    @FXML
    private Button bDigit0;

    @FXML
    private Button bDigit1;

    @FXML
    private Button bDigit2;

    @FXML
    private Button bDigit3;

    @FXML
    private Button bDigit4;

    @FXML
    private Button bDigit5;

    @FXML
    private Button bDigit6;

    @FXML
    private Button bDigit7;

    @FXML
    private Button bDigit8;

    @FXML
    private Button bDigit9;

    @FXML
    private Button bDigitA;

    @FXML
    private Button bDigitB;

    @FXML
    private Button bDigitC;

    @FXML
    private Button bDigitD;

    @FXML
    private Button bDigitE;

    @FXML
    private Button bDigitF;

    @FXML
    private Button bPiNumber;

    @FXML
    private Button bSine;

    public void install(Scene scene) {
        display.textProperty().bind(viewModel.displayProperty);

        radixToggleGroup.selectToggle(radioRadixDec);
        viewModel.radixProperty.addListener((observable, oldValue, newValue) -> {
            logger.info("Selected radix from ViewModel: {}", newValue);
            radixToggleGroup.selectToggle(switch (newValue) {
                case Hex -> radioRadixHex;
                case Decimal -> radioRadixDec;
                case Octal -> radioRadixOct;
                case Binary -> radioRadixBin;
                default -> null;
            });
        });
        radixToggleGroup.selectedToggleProperty().addListener((observableValue, oldValue, newValue) -> {
            RadixType radix =
                            (newValue == radioRadixHex) ? RadixType.Hex :
                            (newValue == radioRadixDec) ? RadixType.Decimal :
                            (newValue == radioRadixOct) ? RadixType.Octal :
                            (newValue == radioRadixBin) ? RadixType.Binary :
                            null;
            logger.info("Selected radix from UI: {}", radix);
            viewModel.radixProperty.set(radix);
        });

        bindButton(bDigit0, viewModel.digit0Button);
        bindButton(bDigit1, viewModel.digit1Button);
        bindButton(bDigit2, viewModel.digit2Button);
        bindButton(bDigit3, viewModel.digit3Button);
        bindButton(bDigit4, viewModel.digit4Button);
        bindButton(bDigit5, viewModel.digit5Button);
        bindButton(bDigit6, viewModel.digit6Button);
        bindButton(bDigit7, viewModel.digit7Button);
        bindButton(bDigit8, viewModel.digit8Button);
        bindButton(bDigit9, viewModel.digit9Button);
        bindButton(bDigitA, viewModel.digitAButton);
        bindButton(bDigitB, viewModel.digitBButton);
        bindButton(bDigitC, viewModel.digitCButton);
        bindButton(bDigitD, viewModel.digitDButton);
        bindButton(bDigitE, viewModel.digitEButton);
        bindButton(bDigitF, viewModel.digitFButton);
        bindButton(bPiNumber, viewModel.piButton);


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
