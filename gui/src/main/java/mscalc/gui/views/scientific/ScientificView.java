package mscalc.gui.views.scientific;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import mscalc.engine.RadixType;
import mscalc.gui.viewmodel.ScientificCalculatorViewModel;
import mscalc.gui.viewmodel.ScientificCalculatorViewModel.KeyboardCode;
import mscalc.gui.views.CalculatorView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public class ScientificView extends VBox implements CalculatorView {
    private static final Logger logger = LogManager.getLogger(ScientificView.class);

    private final ScientificCalculatorViewModel viewModel = new ScientificCalculatorViewModel();
    private final HashMap<KeyboardCode, ButtonBase> keyboardMapping = new HashMap<>();

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
    private CheckBox cbInvert;

    @FXML
    private CheckBox cbHyperbolic;

    @FXML
    private Button bBackspace;
    @FXML
    private Button bClearEntry;
    @FXML
    private Button bClear;

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
    private Button bDecimalPoint;

    @FXML
    private Button bChangeSign;

    // Memory block

    @FXML
    private Button bMemoryClear;

    @FXML
    private Button bMemoryRecall;

    @FXML
    private Button bMemorySet;

    @FXML
    private Button bMemoryAdd;

    // Arithmetic block

    @FXML
    private Button bDivide;

    @FXML
    private Button bMultiply;

    @FXML
    private Button bSubtract;

    @FXML
    private Button bAdd;

    @FXML
    private Button bModulo;

    @FXML
    private Button bBitOr;

    @FXML
    private Button bLeftShift;

    @FXML
    private Button bEquals;

    @FXML
    private Button bBitAnd;

    @FXML
    private Button bBitXor;

    @FXML
    private Button bBitNot;

    @FXML
    private Button bIntegralPart;

    // Scientific block

    @FXML
    private Button bScientificNotation;

    @FXML
    private Button bDegreesMinutesSeconds;

    @FXML
    private Button bSine;

    @FXML
    private Button bCosine;

    @FXML
    private Button bTangent;

    // --

    @FXML
    private Button bOpenBracket;

    @FXML
    private Button bEnterScientific;

    @FXML
    private Button bPower;

    @FXML
    private Button bCube;

    @FXML
    private Button bSquare;

    // --

    @FXML
    private Button bCloseBracket;

    @FXML
    private Button bNaturalLog;

    @FXML
    private Button b10Logarithm;

    @FXML
    private Button bFactorial;

    @FXML
    private Button bOneOverX;

    public void install(Scene scene) {
        display.textProperty().bind(viewModel.displayProperty);

        bindButton(cbInvert, viewModel.invertButton);
        bindButton(cbHyperbolic, viewModel.hyperbolicButton);

        radixToggleGroup.selectToggle(radioRadixDec);
        bindButton(radioRadixHex, viewModel.radixHexButton);
        bindButton(radioRadixDec, viewModel.radixDecButton);
        bindButton(radioRadixOct, viewModel.radixOctButton);
        bindButton(radioRadixBin, viewModel.radixBinButton);
        /*
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
        });*/

        bindButton(bClear, viewModel.clearButton);
        bindButton(bClearEntry, viewModel.clearEntryButton);
        bindButton(bBackspace, viewModel.backspaceButton);

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

        bindButton(bChangeSign, viewModel.signButton);
        bindButton(bDecimalPoint, viewModel.decimalPointButton);

        bindButton(bDivide, viewModel.divideButton);
        bindButton(bMultiply, viewModel.multiplyButton);
        bindButton(bSubtract, viewModel.subtractButton);
        bindButton(bAdd, viewModel.addButton);

        bindButton(bModulo, viewModel.modButton);
        bindButton(bBitOr, viewModel.bitOrButton);
        bindButton(bLeftShift, viewModel.lshButton);
        bindButton(bEquals, viewModel.equalsButton);

        bindButton(bBitAnd, viewModel.bitAndButton);
        bindButton(bBitXor, viewModel.bitXorButton);
        bindButton(bBitNot, viewModel.bitNotButton);
        bindButton(bIntegralPart, viewModel.integerButton);

        bindButton(bMemoryClear, viewModel.memoryClearButton);
        bindButton(bMemoryRecall, viewModel.memoryRecallButton);
        bindButton(bMemorySet, viewModel.memoryStoreButton);
        bindButton(bMemoryAdd, viewModel.memoryAddButton);

        bindButton(bScientificNotation, viewModel.scientificNotationOnOffButton);
        bindButton(bDegreesMinutesSeconds, viewModel.dmsButton);
        bindButton(bSine, viewModel.sineButton);
        bindButton(bCosine, viewModel.cosineButton);
        bindButton(bTangent, viewModel.tangentButton);

        bindButton(bOpenBracket, viewModel.openBracketButton);
        bindButton(bEnterScientific, viewModel.enterExponentButton);
        bindButton(bPower, viewModel.powerButton);
        bindButton(bCube, viewModel.cubeButton);
        bindButton(bSquare, viewModel.squareButton);

        bindButton(bCloseBracket, viewModel.closeBracketButton);
        bindButton(bNaturalLog, viewModel.lnButton);
        bindButton(b10Logarithm, viewModel.logButton);
        bindButton(bFactorial, viewModel.factorialButton);
        bindButton(bOneOverX, viewModel.reciprocalButton);

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

    private void bindButton(ButtonBase button, ScientificCalculatorViewModel.InputViewModel operation) {
        button.textProperty().bind(operation.textProperty());

        // Pre-create the tooltip object
        Tooltip t = new Tooltip();
        t.textProperty().bind(operation.tooltipProperty());

        button.tooltipProperty().bind(operation.tooltipProperty().map(tooltipText -> {
            if (tooltipText == null || tooltipText.isBlank()) {
                return null;
            }

            return t;
        }));

        button.disableProperty().bind(operation.enabledProperty().not());
        button.setOnAction(e -> operation.execute());

        button.setUserData(operation);

        operation.keyboardShortcut().ifPresent(ks -> {
            var conflict = keyboardMapping.put(ks, button);
            if (conflict != null) {
                logger.error("Conflicting keyboard mapping: {}", ks);
            }
        });
    }

    private void onKeyPressed(KeyEvent e) {
        KeyboardCode kc = new KeyboardCode(e.getCode(), e.isControlDown(), e.isShiftDown());

        if (keyboardMapping.containsKey(kc)) {
            keyboardMapping.get(kc).arm();
            e.consume();
        }
    }

    private void onKeyReleased(KeyEvent e) {
        KeyboardCode kc = new KeyboardCode(e.getCode(), e.isControlDown(), e.isShiftDown());

        if (keyboardMapping.containsKey(kc)) {
            var button = keyboardMapping.get(kc);
            button.disarm();
            button.fire();
            e.consume();
        }
    }

}
