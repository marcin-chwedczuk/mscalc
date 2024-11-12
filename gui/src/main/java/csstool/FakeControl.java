package csstool;

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mscalc.gui.views.scientific.ScientificView;

import java.io.IOException;
import java.util.List;

// FakeControl component used to load arbitrary FXML
// and work on the stylesheet
@DefaultProperty("children")
public class FakeControl extends Control {
    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinBase<>(this) {
            {
                try {
                    ScientificView ctrl = new ScientificView();
                    getChildren().add(new Pane(ctrl));

                    FakeControl.this.getStylesheets().addListener((InvalidationListener) (o) -> {
                        List<String> stylesheets = FakeControl.this.getStylesheets().stream().toList();
                        ctrl.getStylesheets().clear();
                        ctrl.getStylesheets().addAll(stylesheets);
                    });
                } catch (Exception exception) {
                    getChildren().add(new ScrollPane(new Text(ExceptionUtil.toReadableString(exception))));
                }
            }
        };
    }
}
