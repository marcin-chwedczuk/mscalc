package mscalc.gui.viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.function.Function;
import java.util.function.Predicate;

public class MoreBindings {
    public static <T> BooleanBinding map(ObjectProperty<T> objProperty, Predicate<T> pred) {
        return Bindings.createBooleanBinding(() -> {
            T value = objProperty.get();
            return (value != null) && pred.test(value);
        }, objProperty);
    }
}
