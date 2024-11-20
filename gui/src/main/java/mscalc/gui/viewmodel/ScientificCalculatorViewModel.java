package mscalc.gui.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import mscalc.engine.*;
import mscalc.engine.commands.Command;
import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.gui.views.scientific.ScientificView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScientificCalculatorViewModel {
    private static final Logger logger = LogManager.getLogger(ScientificCalculatorViewModel.class);



    private final List<InputViewModel> allInputs = new ArrayList<>();

    private final CalculatorManager calculatorManager = new CalculatorManager(
            new ThisViewModelCalculatorDisplay(),
            new JavaBundleResourceProvider());

    public final BooleanProperty invertedModeProperty = new SimpleBooleanProperty(false);
    public final BooleanProperty hyperbolicModeProperty = new SimpleBooleanProperty(false);

    public final BooleanProperty degreeGroupingProperty = new SimpleBooleanProperty(true);

    public final ObjectProperty<RadixType> radixProperty = new SimpleObjectProperty<>(RadixType.Decimal);
    public final ObjectProperty<NumberWidth> integerNumberWidthProperty = new SimpleObjectProperty<>(NumberWidth.QWORD_WIDTH);
    public final ObjectProperty<DegreeType> degreeTypeProperty = new SimpleObjectProperty<>(DegreeType.Degrees);

    public final StringProperty displayProperty = new SimpleStringProperty("");

    // --- INPUT DIGITS & CONSTANTS ----
    public final InputViewModel digit0Button = newInputViewModel()
            .withText("0")
            .withCommand(Command.Command0)
            .build();

    public final InputViewModel digit1Button = newInputViewModel()
            .withText("1")
            .withCommand(Command.Command1)
            .build();

    public final InputViewModel digit2Button = newInputViewModel()
            .withText("2")
            .withCommand(Command.Command2)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(2)))
            .build();

    public final InputViewModel digit3Button = newInputViewModel()
            .withText("3")
            .withCommand(Command.Command3)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(3)))
            .build();

    public final InputViewModel digit4Button = newInputViewModel()
            .withText("4")
            .withCommand(Command.Command4)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(4)))
            .build();

    public final InputViewModel digit5Button = newInputViewModel()
            .withText("5")
            .withCommand(Command.Command5)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(5)))
            .build();

    public final InputViewModel digit6Button = newInputViewModel()
            .withText("6")
            .withCommand(Command.Command6)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(6)))
            .build();

    public final InputViewModel digit7Button = newInputViewModel()
            .withText("7")
            .withCommand(Command.Command7)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(7)))
            .build();

    public final InputViewModel digit8Button = newInputViewModel()
            .withText("8")
            .withCommand(Command.Command8)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(8)))
            .build();

    public final InputViewModel digit9Button = newInputViewModel()
            .withText("9")
            .withCommand(Command.Command9)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(9)))
            .build();

    public final InputViewModel digitAButton = newInputViewModel()
            .withText("A")
            .withCommand(Command.CommandA)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xA)))
            .build();

    public final InputViewModel digitBButton = newInputViewModel()
            .withText("B")
            .withCommand(Command.CommandB)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xB)))
            .build();

    public final InputViewModel digitCButton = newInputViewModel()
            .withText("C")
            .withCommand(Command.CommandC)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xC)))
            .build();

    public final InputViewModel digitDButton = newInputViewModel()
            .withText("D")
            .withCommand(Command.CommandD)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xD)))
            .build();

    public final InputViewModel digitEButton = newInputViewModel()
            .withText("E")
            .withCommand(Command.CommandE)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xE)))
            .build();

    public final InputViewModel digitFButton = newInputViewModel()
            .withText("F")
            .withCommand(Command.CommandF)
            .withEnabled(MoreBindings.map(radixProperty, r -> r.hasDigit(0xF)))
            .build();

    public final InputViewModel piButton = newInputViewModel()
            .withText("π")
            .withCommand(Command.CommandPI)
            .withEnabled(radixProperty.isEqualTo(RadixType.Decimal))
            .build();

    public final InputViewModel sineButton = newInputViewModel()
            .withText("sine")
            .withCommand(Command.CommandSIN)
            .build();

    public ScientificCalculatorViewModel() {
        // Initialize Scientific mode
        calculatorManager.sendCommand(Command.ModeScientific);
    }

    public InputViewModelBuilder newInputViewModel() {
        return new InputViewModelBuilder();
    }

    public class InputViewModelBuilder {
        private StringProperty textProperty;
        private ObjectProperty<Command> commandProperty;
        private BooleanExpression enabledProperty = new ReadOnlyBooleanWrapper(true);

        public InputViewModelBuilder withText(String staticText) {
            return withText(new ReadOnlyStringWrapper(staticText));
        }

        public InputViewModelBuilder withText(StringProperty textProperty) {
            this.textProperty = textProperty;
            return this;
        }

        public InputViewModelBuilder withCommand(Command cmd) {
            return withCommand(new ReadOnlyObjectWrapper<>(cmd));
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
            var ivm = new InputViewModel(textProperty, commandProperty, enabledProperty);
            allInputs.add(ivm);
            return ivm;
        }
    }

    public class InputViewModel {
        private final StringProperty textProperty;
        private final ObjectProperty<Command> commandProperty;
        private final BooleanExpression enabledProperty;

        public InputViewModel(StringProperty textProperty,
                              ObjectProperty<Command> commandProperty,
                              BooleanExpression enabledProperty) {

            this.textProperty = Objects.requireNonNull(textProperty);
            this.commandProperty = Objects.requireNonNull(commandProperty);
            this.enabledProperty = Objects.requireNonNull(enabledProperty);
        }

        public void execute() {
            calculatorManager.sendCommand(this.commandProperty.get());
        }

        public ReadOnlyStringProperty textProperty() {
            return this.textProperty;
        }

        public ReadOnlyObjectProperty<Command> commandProperty() {
            return this.commandProperty;
        }

        public BooleanExpression enabledProperty() {
            return this.enabledProperty;
        }
    }

    // TODO: Add logger

    public class ThisViewModelCalculatorDisplay implements CalcDisplay {
        @Override
        public void setPrimaryDisplay(String text, boolean isError) {
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

        }

        @Override
        public void setExpressionDisplay(List<Pair<String, Integer>> tokens, List<IExpressionCommand> commands) {

        }

        @Override
        public void setParenthesisNumber(int count) {

        }

        @Override
        public void onNoRightParenAdded() {

        }

        @Override
        public void maxDigitsReached() {

        }

        @Override
        public void binaryOperatorReceived() {

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

        }
    }
}
