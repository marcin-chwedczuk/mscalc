package csstool;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.stream.Collectors;

public class ControlStructureTreeView extends TreeView<String> {
    private final SimpleObjectProperty<Control> observedControl = new SimpleObjectProperty<>(
            this, "observedControl", null);

    private final InvalidationListener onObservedControlSkinChange = o -> {
        Control control = (Control) ((ReadOnlyProperty<?>) o).getBean();

        Platform.runLater(() -> {
            TreeItem<String> root = new TreeItem<>();
            fillControlStructure(control, root);
            setRoot(root);
        });
    };

    public ControlStructureTreeView() {
        observedControl.addListener((o, oldValue, newValue) -> {
            setRoot(null);

            // Skin is only available when control is added to scene graph.

            if (oldValue != null) {
                oldValue.skinProperty().removeListener(onObservedControlSkinChange);
            }

            if (newValue != null) {
                newValue.skinProperty().addListener(onObservedControlSkinChange);

                // TODO: Handle case when is is already available
            }
        });
    }

    private void fillControlStructure(Node current, TreeItem<String> currentItem) {
        String cssClasses = current.getStyleClass().stream()
                .map(cssClass -> "." + cssClass)
                .collect(Collectors.joining(" "));

        String javaClass = current.getClass().getSimpleName();

        // Anonymous class e.g. new Button { foo... }, in that case use superclass
        if (javaClass.isEmpty()) {
            javaClass = current.getClass().getSuperclass().getSimpleName();
        }

        currentItem.setValue(String.format("%s(%s)", javaClass, cssClasses));
        currentItem.setExpanded(true);

        if (current instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                TreeItem<String> childItem = new TreeItem<>();
                fillControlStructure(child, childItem);
                currentItem.getChildren().add(childItem);
            }
        }
    }

    public void setObservedControl(Control control) {
        observedControl.set(control);
    }
}
