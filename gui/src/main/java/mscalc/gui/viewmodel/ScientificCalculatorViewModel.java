package mscalc.gui.viewmodel;

import javafx.beans.property.*;
import javafx.scene.media.AudioClip;
import mscalc.engine.CalcDisplay;
import mscalc.engine.CalculatorManager;
import mscalc.engine.Pair;
import mscalc.engine.commands.Command;
import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.resource.JavaBundleResourceProvider;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ScientificCalculatorViewModel {
    private final CalculatorManager calculatorManager = new CalculatorManager(
            new ThisViewModelCalculatorDisplay(),
            new JavaBundleResourceProvider());

    private final BooleanProperty invertedMode = new SimpleBooleanProperty(false);
    private final BooleanProperty hyperbolicMode = new SimpleBooleanProperty(false);

    public final StringProperty displayProperty = new SimpleStringProperty("");

    public final InputViewModel digitOneButton = newInputViewModel()
            .withText("1")
            .withCommand(Command.Command1)
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

    public class InputViewModel {
        private final StringProperty textProperty;
        private final ObjectProperty<Command> commandProperty;
        private final BooleanProperty enabledProperty;

        public InputViewModel(StringProperty textProperty,
                              ObjectProperty<Command> commandProperty,
                              BooleanProperty enabledProperty) {

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

        public ReadOnlyBooleanProperty enabledProperty() {
            return this.enabledProperty;
        }
    }

    public class InputViewModelBuilder {
        private StringProperty textProperty;
        private ObjectProperty<Command> commandProperty;
        private BooleanProperty enabledProperty = new ReadOnlyBooleanWrapper(true);

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

        public InputViewModelBuilder withEnabled(BooleanProperty enabledProperty) {
            this.enabledProperty = enabledProperty;
            return this;
        }

        public InputViewModel build() {
            return new InputViewModel(
                    textProperty, commandProperty, enabledProperty);
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
