package mscalc.gui.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import mscalc.engine.*;
import mscalc.engine.commands.Command;
import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.resource.JavaBundleResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class ScientificCalculatorViewModel {
    private static final Logger logger = LogManager.getLogger(ScientificCalculatorViewModel.class);

    private final CalculatorManager calculatorManager = new CalculatorManager(
            new ThisViewModelCalculatorDisplay(),
            new JavaBundleResourceProvider());

    public final BooleanProperty invertedModeProperty = new SimpleBooleanProperty(false);
    public final BooleanProperty hyperbolicModeProperty = new SimpleBooleanProperty(false);

    public final BooleanProperty degreeGroupingProperty = new SimpleBooleanProperty(true);

    public final ObjectProperty<RadixType> radixProperty = new SimpleObjectProperty<>(RadixType.Decimal);
    public final ObjectProperty<NumberWidth> integerNumberWidthProperty = new SimpleObjectProperty<>(NumberWidth.QWORD_WIDTH);
    public final ObjectProperty<DegreeType> degreeTypeProperty = new SimpleObjectProperty<>(DegreeType.Degrees);

    // TODO: Model enum as 4 buttons with InputViewModel

    public final StringProperty displayProperty = new SimpleStringProperty("");

    // --- RADIX SELECT ---
    public final InputViewModel radixHexButton = newInputViewModel()
            .withText("Hex")
            .withCommand(Command.CommandHex)
            .withKeyboardShortcut(KeyCode.F5)
            .build();

    public final InputViewModel radixDecButton = newInputViewModel()
            .withText("Dec")
            .withCommand(Command.CommandDec)
            .withKeyboardShortcut(KeyCode.F6)
            .build();

    public final InputViewModel radixOctButton = newInputViewModel()
            .withText("Oct")
            .withCommand(Command.CommandOct)
            .withKeyboardShortcut(KeyCode.F7)
            .build();

    public final InputViewModel radixBinButton = newInputViewModel()
            .withText("Bin")
            .withCommand(Command.CommandBin)
            .withKeyboardShortcut(KeyCode.F8)
            .build();

    // -- ANGLE TYPE SELECT --
    public final InputViewModel angleDegreesButton = newInputViewModel()
            .withText("Degrees")
            .withCommand(Command.CommandDEG)
            .withKeyboardShortcut(KeyCode.F2)
            .onlyActiveWhen(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel angleRadiansButton = newInputViewModel()
            .withText("Radians")
            .withCommand(Command.CommandRAD)
            .withKeyboardShortcut(KeyCode.F3)
            .onlyActiveWhen(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel angleGradiansButton = newInputViewModel()
            .withText("Gradians")
            .withCommand(Command.CommandGRAD)
            .withKeyboardShortcut(KeyCode.F4)
            .onlyActiveWhen(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    // --- WORD WIDTH SELECT ---
    public final InputViewModel radixWordWidthQWord = newInputViewModel()
            .withText("QWord")
            .withCommand(Command.CommandQword)
            .withKeyboardShortcut(KeyCode.F12)
            .onlyActiveWhen(radixProperty.isNotEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel radixWordWidthDWord = newInputViewModel()
            .withText("DWord")
            .withCommand(Command.CommandDword)
            .withKeyboardShortcut(KeyCode.F2)
            .onlyActiveWhen(radixProperty.isNotEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel radixWordWidthWord = newInputViewModel()
            .withText("Word")
            .withCommand(Command.CommandWord)
            .withKeyboardShortcut(KeyCode.F3)
            .onlyActiveWhen(radixProperty.isNotEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel radixWordWidthByte = newInputViewModel()
            .withText("Byte")
            .withCommand(Command.CommandByte)
            .withKeyboardShortcut(KeyCode.F4)
            .onlyActiveWhen(radixProperty.isNotEqualTo(RadixType.Decimal))
            .build();

    // -- INVERT & HYP BUTTONS ---
    public final InputViewModel invertButton = newInputViewModel()
            .withText("Inverse")
            .withCommand(Command.CommandINV)
            .withKeyboardShortcut(KeyCode.I)
            .build();

    public final InputViewModel hyperbolicButton = newInputViewModel()
            .withText("Hyper")
            .withCommand(Command.CommandHYP)
            .withKeyboardShortcut(KeyCode.H)
            .build();

    // --- CONTROL BUTTONS ---
    public final InputViewModel clearButton = newInputViewModel()
            .withText("C")
            .withCommand(Command.CommandCLEAR)
            .withKeyboardShortcut(KeyCode.ESCAPE)
            .build();

    public final InputViewModel clearEntryButton = newInputViewModel()
            .withText("CE")
            .withCommand(Command.CommandCENTR)
            .withKeyboardShortcut(KeyCode.DELETE)
            .build();

    public final InputViewModel backspaceButton = newInputViewModel()
            .withText("Backspace")
            .withCommand(Command.CommandBACK)
            .withKeyboardShortcut(KeyCode.BACK_SPACE)
            .build();

    // --- INPUT DIGITS & CONSTANTS ----
    public final InputViewModel digit0Button = newInputViewModel()
            .withText("0")
            .withCommand(Command.Command0)
            .withKeyboardShortcut(KeyCode.DIGIT0)
            .build();

    public final InputViewModel digit1Button = newInputViewModel()
            .withText("1")
            .withCommand(Command.Command1)
            .withKeyboardShortcut(KeyCode.DIGIT1)
            .build();

    public final InputViewModel digit2Button = newInputViewModel()
            .withText("2")
            .withCommand(Command.Command2)
            .withKeyboardShortcut(KeyCode.DIGIT2)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(2)))
            .build();

    public final InputViewModel digit3Button = newInputViewModel()
            .withText("3")
            .withCommand(Command.Command3)
            .withKeyboardShortcut(KeyCode.DIGIT3)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(3)))
            .build();

    public final InputViewModel digit4Button = newInputViewModel()
            .withText("4")
            .withCommand(Command.Command4)
            .withKeyboardShortcut(KeyCode.DIGIT4)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(4)))
            .build();

    public final InputViewModel digit5Button = newInputViewModel()
            .withText("5")
            .withCommand(Command.Command5)
            .withKeyboardShortcut(KeyCode.DIGIT5)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(5)))
            .build();

    public final InputViewModel digit6Button = newInputViewModel()
            .withText("6")
            .withCommand(Command.Command6)
            .withKeyboardShortcut(KeyCode.DIGIT6)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(6)))
            .build();

    public final InputViewModel digit7Button = newInputViewModel()
            .withText("7")
            .withCommand(Command.Command7)
            .withKeyboardShortcut(KeyCode.DIGIT7)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(7)))
            .build();

    public final InputViewModel digit8Button = newInputViewModel()
            .withText("8")
            .withCommand(Command.Command8)
            .withKeyboardShortcut(KeyCode.DIGIT8)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(8)))
            .build();

    public final InputViewModel digit9Button = newInputViewModel()
            .withText("9")
            .withCommand(Command.Command9)
            .withKeyboardShortcut(KeyCode.DIGIT9)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(9)))
            .build();

    public final InputViewModel digitAButton = newInputViewModel()
            .withText("A")
            .withCommand(Command.CommandA)
            .withKeyboardShortcut(KeyCode.A)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xA)))
            .build();

    public final InputViewModel digitBButton = newInputViewModel()
            .withText("B")
            .withCommand(Command.CommandB)
            .withKeyboardShortcut(KeyCode.B)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xB)))
            .build();

    public final InputViewModel digitCButton = newInputViewModel()
            .withText("C")
            .withCommand(Command.CommandC)
            .withKeyboardShortcut(KeyCode.C)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xC)))
            .build();

    public final InputViewModel digitDButton = newInputViewModel()
            .withText("D")
            .withCommand(Command.CommandD)
            .withKeyboardShortcut(KeyCode.D)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xD)))
            .build();

    public final InputViewModel digitEButton = newInputViewModel()
            .withText("E")
            .withCommand(Command.CommandE)
            .withKeyboardShortcut(KeyCode.E)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xE)))
            .build();

    public final InputViewModel digitFButton = newInputViewModel()
            .withText("F")
            .withCommand(Command.CommandF)
            .withKeyboardShortcut(KeyCode.F)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xF)))
            .build();

    public final InputViewModel piButton = newInputViewModel()
            .withModeText("π", "2π")
            // TODO: Implement 2xPI
            .withModeCommand(Command.CommandPI, Command.CommandPI)
            .withKeyboardShortcut(KeyCode.P)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    // --- +/- & DECIMAL POINT ----

    public final InputViewModel signButton = newInputViewModel()
            .withText("±")
            .withKeyboardShortcut(KeyCode.F9)
            .withCommand(Command.CommandSIGN)
            .build();

    public final InputViewModel decimalPointButton = newInputViewModel()
            .withText(".")
            .withCommand(Command.CommandPNT)
            .withKeyboardShortcut(KeyCode.PERIOD) // TODO: Also supports comma
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    // -- ARITHMETIC FUNCTIONS ---

    public final InputViewModel divideButton = newInputViewModel()
            .withText("÷")
            .withKeyboardShortcut(KeyCode.SLASH)
            .withCommand(Command.CommandDIV)
            .build();

    public final InputViewModel multiplyButton = newInputViewModel()
            .withText("×")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT8) // TODO: What about multiply on numeric keyboard?
            .withCommand(Command.CommandMUL)
            .build();

    public final InputViewModel subtractButton = newInputViewModel()
            .withText("-")
            .withKeyboardShortcut(KeyCode.MINUS)
            .withCommand(Command.CommandSUB)
            .build();

    public final InputViewModel addButton = newInputViewModel()
            .withText("+")
            .withKeyboardShortcutShiftAnd(KeyCode.EQUALS)
            .withCommand(Command.CommandADD)
            .build();

    public final InputViewModel modButton = newInputViewModel()
            .withText("Mod")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT5)
            .withCommand(Command.CommandMOD)
            .build();

    public final InputViewModel bitOrButton = newInputViewModel()
            .withText("Or")
            .withKeyboardShortcutShiftAnd(KeyCode.BACK_SLASH)
            .withCommand(Command.CommandOR)
            .build();

    public final InputViewModel lshButton = newInputViewModel()
            .withModeText("Lsh", "Rsh")
            .withKeyboardShortcutShiftAnd(KeyCode.PERIOD)
            .withModeCommand(Command.CommandLSHF, Command.CommandRSHF)
            .build();

    public final InputViewModel equalsButton = newInputViewModel()
            .withText("=")
            .withKeyboardShortcut(KeyCode.ENTER)
            .withCommand(Command.CommandEQU)
            .build();

    public final InputViewModel bitAndButton = newInputViewModel()
            .withText("And")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT7)
            .withCommand(Command.CommandAnd)
            .build();

    public final InputViewModel bitXorButton = newInputViewModel()
            .withText("Xor")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT6)
            .withCommand(Command.CommandXor)
            .build();

    public final InputViewModel bitNotButton = newInputViewModel()
            .withText("Not")
            .withKeyboardShortcutShiftAnd(KeyCode.BACK_QUOTE)
            .withCommand(Command.CommandNot)
            .build();

    public final InputViewModel integerButton = newInputViewModel()
            .withModeText("Int", "Fra")
            .withKeyboardShortcut(KeyCode.SEMICOLON)
            .withCommand(Command.CommandCHOP) // TODO: Bug Invert not working
            .build();

    // -- MEMORY FUNCTIONS ---

    public final InputViewModel memoryClearButton = newInputViewModel()
            .withText("MC")
            .withKeyboardShortcutControlAnd(KeyCode.L)
            .withCommand(Command.CommandMCLEAR)
            .build();

    public final InputViewModel memoryRecallButton = newInputViewModel()
            .withText("MR")
            .withKeyboardShortcutControlAnd(KeyCode.R)
            .withCommand(Command.CommandRECALL)
            .build();

    public final InputViewModel memoryStoreButton = newInputViewModel()
            .withText("MS")
            .withKeyboardShortcutControlAnd(KeyCode.M)
            .withCommand(Command.CommandSTORE)
            .build();

    public final InputViewModel memoryAddButton = newInputViewModel()
            .withText("M+")
            .withKeyboardShortcutControlAnd(KeyCode.P)
            .withCommand(Command.CommandMPLUS)
            .build();

    // --- SCIENTIFIC FUNCTIONS ---

    public final InputViewModel scientificNotationOnOffButton = newInputViewModel()
            .withText("F-E")
            .withCommand(Command.CommandFE)
            .withKeyboardShortcut(KeyCode.V)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel dmsButton = newInputViewModel()
            .withText("dms")
            .withCommand(Command.CommandDMS)
            .withKeyboardShortcut(KeyCode.M)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel sineButton = newInputViewModel()
            .withModeText("sin", "asin", "sinh", "asinh")
            .withKeyboardShortcut(KeyCode.S)
            .withModeCommand(Command.CommandSIN, Command.CommandASIN, Command.CommandSINH, Command.CommandASINH)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel cosineButton = newInputViewModel()
            .withModeText("cos", "acos", "cosh", "acosh")
            .withKeyboardShortcut(KeyCode.O)
            .withModeCommand(Command.CommandCOS, Command.CommandACOS, Command.CommandCOSH, Command.CommandACOSH)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel tangentButton = newInputViewModel()
            .withModeText("tan", "atan", "tanh", "atanh")
            .withKeyboardShortcut(KeyCode.T)
            .withModeCommand(Command.CommandTAN, Command.CommandATAN, Command.CommandTANH, Command.CommandATANH)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    // --

    public final InputViewModel openBracketButton = newInputViewModel()
            .withText("(")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT9)
            .withCommand(Command.CommandOPENP)
            .build();

    public final InputViewModel enterExponentButton = newInputViewModel()
            .withText("Exp")
            .withKeyboardShortcut(KeyCode.X)
            .withCommand(Command.CommandEXP)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel powerButton = newInputViewModel()
            .withText("x^y")
            .withKeyboardShortcut(KeyCode.Y)
            .withCommand(Command.CommandEXP)
            .withCommand(Command.CommandPWR)
            .build();

    public final InputViewModel cubeButton = newInputViewModel()
            .withText("x^3")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT3)
            .withCommand(Command.CommandCUB)
            .build();

    public final InputViewModel squareButton = newInputViewModel()
            .withText("x^2")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT2)
            .withCommand(Command.CommandSQR)
            .build();

    // --

    public final InputViewModel closeBracketButton = newInputViewModel()
            .withText(")")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT0)
            .withCommand(Command.CommandCLOSEP)
            .build();

    public final InputViewModel lnButton = newInputViewModel()
            .withModeText("ln", "e\u02E3")
            .withKeyboardShortcut(KeyCode.N)
            .withModeCommand(Command.CommandLN, Command.CommandPOWE)
            .build();

    public final InputViewModel logButton = newInputViewModel()
            .withModeText("log", "10\u02E3")
            .withKeyboardShortcut(KeyCode.L)
            .withModeCommand(Command.CommandLOG, Command.CommandPOW10)
            .build();

    public final InputViewModel factorialButton = newInputViewModel()
            .withText("n!")
            .withKeyboardShortcutShiftAnd(KeyCode.DIGIT1)
            .withCommand(Command.CommandFAC)
            .build();

    public final InputViewModel reciprocalButton = newInputViewModel()
            .withText("1/x")
            .withKeyboardShortcut(KeyCode.R)
            .withCommand(Command.CommandREC)
            .build();

    public ScientificCalculatorViewModel() {
        // Initialize Scientific mode
        calculatorManager.sendCommand(Command.ModeScientific);
        // Setup some initial configuration
        calculatorManager.sendCommand(Command.CommandQword);
        calculatorManager.sendCommand(Command.CommandDec);
        calculatorManager.sendCommand(Command.CommandRAD);
    }

    public InputViewModelBuilder newInputViewModel() {
        return new InputViewModelBuilder();
    }

    private void processRadixChangeCommand(Command command) {
        switch (command) {
            case CommandHex -> {
                radixProperty.set(RadixType.Hex);
                calculatorManager.sendCommand(Command.ModeProgrammer);
            }

            case CommandDec -> {
                radixProperty.set(RadixType.Decimal);
                calculatorManager.sendCommand(Command.ModeScientific);
            }

            case CommandOct -> {
                radixProperty.set(RadixType.Octal);
                calculatorManager.sendCommand(Command.ModeProgrammer);
            }

            case CommandBin -> {
                radixProperty.set(RadixType.Binary);
                calculatorManager.sendCommand(Command.ModeProgrammer);
            }
        }
    }

    public record KeyboardCode(
            KeyCode key,
            boolean control,
            boolean shift
    ) {
        public String toShortcutDescription() {
            return String.format("Keyboard shortcut: %s%s%s",
                    shift ? "Shift+" : "",
                    control ? "Ctrl+" : "",
                    Character.isISOControl(key.getChar().charAt(0))
                            ? key.getName() : key.getChar());
        }
    }

    public class InputViewModelBuilder {
        private BooleanExpression activeProperty = new ReadOnlyBooleanWrapper(true);
        private StringExpression textProperty;
        private StringExpression tooltipProperty = new ReadOnlyStringWrapper(null);
        private ObjectExpression<Command> commandProperty;
        private BooleanExpression enabledProperty = new ReadOnlyBooleanWrapper(true);
        private KeyboardCode keyboardShortcut;

        public InputViewModelBuilder onlyActiveWhen(BooleanExpression condition) {
            this.activeProperty = condition;
            return this;
        }

        /*
         * Set text for normal, inverted, hyperbolic and hyperbolic-inverted modes.
         * You may specify only one, two or four names.
         */
        public InputViewModelBuilder withModeText(String text, String... modes) {
            List<String> names = new ArrayList<>();
            names.add(text);
            names.addAll(Arrays.asList(modes));

            textProperty = Bindings.createStringBinding(() -> {
                boolean inv = invertedModeProperty.get();
                boolean hyp = hyperbolicModeProperty.get();

                int index = (inv ? 1 : 0) + (hyp ? 2 : 0);
                return names.get(index % names.size());
            }, invertedModeProperty, hyperbolicModeProperty);

            return this;
        }

        public InputViewModelBuilder withText(String staticText) {
            return withText(new ReadOnlyStringWrapper(staticText));
        }

        public InputViewModelBuilder withText(StringProperty textProperty) {
            this.textProperty = textProperty;
            return this;
        }

        public InputViewModelBuilder withTooltip(String staticText) {
            this.tooltipProperty = new ReadOnlyStringWrapper(staticText);
            return this;
        }

        public InputViewModelBuilder withKeyboardShortcut(KeyCode key) {
            this.keyboardShortcut = new KeyboardCode(key, false, false);
            return this;
        }

        public InputViewModelBuilder withKeyboardShortcutShiftAnd(KeyCode key) {
            this.keyboardShortcut = new KeyboardCode(key, false, true);
            return this;
        }

        public InputViewModelBuilder withKeyboardShortcutControlAnd(KeyCode key) {
            this.keyboardShortcut = new KeyboardCode(key, true, false);
            return this;
        }

        public InputViewModelBuilder withCommand(Command cmd) {
            return withCommand(new ReadOnlyObjectWrapper<>(cmd));
        }

        public InputViewModelBuilder withModeCommand(Command cmd, Command... modes) {
            List<Command> names = new ArrayList<>();
            names.add(cmd);
            names.addAll(Arrays.asList(modes));

            commandProperty = Bindings.<Command>createObjectBinding(() -> {
                boolean inv = invertedModeProperty.get();
                boolean hyp = hyperbolicModeProperty.get();

                int index = (inv ? 1 : 0) + (hyp ? 2 : 0);
                return names.get(index % names.size());
            }, invertedModeProperty, hyperbolicModeProperty);

            return this;
        }

        public InputViewModelBuilder withCommand(ObjectProperty<Command> commandProperty) {
            this.commandProperty = commandProperty;
            return this;
        }

        public InputViewModelBuilder withEnabled(BooleanExpression enabledProperty) {
            this.enabledProperty = enabledProperty;
            return this;
        }

        public InputViewModel build() {
            if (keyboardShortcut != null) {
                tooltipProperty = new ReadOnlyStringWrapper(
                        ((tooltipProperty.get() != null) ? (tooltipProperty.get() + "\n\n") : "") +
                        keyboardShortcut.toShortcutDescription());
            }

            return new InputViewModel(activeProperty, textProperty, tooltipProperty,
                    commandProperty, enabledProperty, keyboardShortcut);
        }
    }

    public class InputViewModel {
        private final BooleanExpression activeProperty;
        private final StringExpression textProperty;
        private final StringExpression tooltipProperty;
        private final ObjectExpression<Command> commandProperty;
        private final BooleanExpression enabledProperty;
        private final KeyboardCode keyboardShortcut;

        public InputViewModel(BooleanExpression activeProperty,
                              StringExpression textProperty,
                              StringExpression tooltipProperty,
                              ObjectExpression<Command> commandProperty,
                              BooleanExpression enabledProperty,
                              @Nullable KeyboardCode keyboardShortcut) {

            this.activeProperty = Objects.requireNonNull(activeProperty);
            this.textProperty = Objects.requireNonNull(textProperty);
            this.tooltipProperty = Objects.requireNonNull(tooltipProperty);
            this.commandProperty = Objects.requireNonNull(commandProperty);
            this.enabledProperty = Objects.requireNonNull(enabledProperty);
            this.keyboardShortcut = keyboardShortcut;
        }

        public boolean isActive() {
            return activeProperty.get();
        }

        public void execute() {
            if (!activeProperty.get()) {
                return;
            }

            switch (this.commandProperty.get()) {
                case CommandINV -> {
                    invertedModeProperty.set(!invertedModeProperty.get());
                }

                case CommandHYP -> {
                    hyperbolicModeProperty.set(!hyperbolicModeProperty.get());
                }

                default -> {
                    processRadixChangeCommand(this.commandProperty.get());
                    calculatorManager.sendCommand(this.commandProperty.get());
                }
            }
        }

        public StringExpression textProperty() {
            return this.textProperty;
        }

        public StringExpression tooltipProperty() {
            return this.tooltipProperty;
        }

        public ObjectExpression<Command> commandProperty() {
            return this.commandProperty;
        }

        public BooleanExpression enabledProperty() {
            return this.enabledProperty;
        }

        public Optional<KeyboardCode> keyboardShortcut() {
            return Optional.ofNullable(keyboardShortcut);
        }
    }


    public class ThisViewModelCalculatorDisplay implements CalcDisplay {
        private static final Logger logger = LogManager.getLogger(ThisViewModelCalculatorDisplay.class);

        @Override
        public void setPrimaryDisplay(String text, boolean isError) {
            logger.info("primary display text={}, isErr={}", text, isError);

            displayProperty.set(text);
            if (isError) {
                // TODO: https://freesound.org/people/anthonychartier2020/sounds/560189/
                // TODO: Make it work, save as a field to not load constantly
                // AudioClip plonkSound = new AudioClip("http://somehost/path/plonk.aiff");
                // plonkSound.play();
            }
        }

        @Override
        public void setIsInError(boolean isInError) {
            logger.info("set isErr={}", isInError);
        }

        @Override
        public void setExpressionDisplay(List<Pair<String, Integer>> tokens, List<IExpressionCommand> commands) {
            logger.info("expr display: tokens={} commands={}", tokens, commands);
        }

        @Override
        public void setParenthesisNumber(int count) {
            logger.info("set parens={}", count);
        }

        @Override
        public void onNoRightParenAdded() {
            logger.info("no right paren!");
        }

        @Override
        public void maxDigitsReached() {
            logger.info("max digits reached");
        }

        @Override
        public void binaryOperatorReceived() {
            logger.info("binary operatior received");
        }

        @Override
        public void onHistoryItemAdded(int addedItemIndex) {
        }

        @Override
        public void setMemorizedNumbers(List<String> memorizedNumbers) {

        }

        @Override
        public void memoryItemChanged(int indexOfMemory) {

        }

        @Override
        public void inputChanged() {
            logger.info("input changed");
        }
    }
}
