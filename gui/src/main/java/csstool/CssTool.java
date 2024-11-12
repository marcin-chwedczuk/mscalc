package csstool;

import com.vaadin.sass.SassCompiler;
import com.vaadin.sass.internal.ScssStylesheet;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class CssTool implements Initializable {
    public static CssTool showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(CssTool.class.getResource("CssTool.fxml"));
            Scene scene = new Scene(loader.load());

            CssTool controller = (CssTool) loader.getController();

            window.setTitle("CSS Tool");
            window.setScene(scene);
            window.setResizable(true);

            window.setOnShown(controller::onWindowShown);
            window.setOnCloseRequest(controller::onWindowClose);
            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private AutocompleteTextArea cssText;

    @FXML
    private HtmlHelpViewer htmlHelpViewer;

    @FXML
    private ComboBox<String> selectedControl;

    @FXML
    private BorderPane controlContainer;

    @FXML
    private ListView<String> cssProperties;

    @FXML
    private ControlStructureTreeView controlStructure;

    @FXML
    private CheckBox controlEnabled;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Rectangle statusIcon;

    @FXML
    private TextField extraClasses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("INITIALIZING CSS TOOL");

        List<String> controlClassNames = JavaFxControlClassesFinder.findControlClasses().stream()
                .map(Class::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        // Extra "fake" control to load custom FXML forms into the tool
        controlClassNames.add(FakeControl.class.getName());

        selectedControl.setItems(FXCollections.observableArrayList(controlClassNames));

        cssText.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            statusIcon.setFill(Color.YELLOW);

            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
                e.consume();

                reloadCss();
            }
        });

        selectedControl.getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> {
            uninstallControl();

            try {
                installControl(newValue);
            } catch (Exception e) {
                statusIcon.setFill(Color.RED);

                String msg = ExceptionUtil.toReadableString(e);
                new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
            }
        });

        tryLoadPreviousSession();
    }

    private void onWindowShown(WindowEvent e) {
    }


    /// TODO: Extract to a sepratate module for preserving last state
    private void onWindowClose(WindowEvent windowEvent) {
        try {
            Files.writeString(Paths.get("css-tool-last-edited.css"), cssText.getText(), StandardCharsets.UTF_8, WRITE, CREATE, TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryLoadPreviousSession() {
        try {
            String text = Files.readString(Paths.get("css-tool-last-edited.css"), StandardCharsets.UTF_8);
            cssText.setText(text);
        } catch (Exception e) {
            // TODO: add logging library
            e.printStackTrace();
        }
    }

    private ChangeListener<String> extraClassesTextChangeListener = null;

    private void installControl(String controlClassName) throws Exception {
        Class<?> controlClass = Class.forName(controlClassName);
        Control control = (Control) controlClass.newInstance();

        controlContainer.setCenter(control);
        BorderPane.setAlignment(control, Pos.CENTER);

        controlStructure.setObservedControl(control);

        addExampleContent(control);

        // Control enabled state
        controlEnabled.setSelected(true);
        control.disableProperty().bind(controlEnabled.selectedProperty().not());

        // Control Zoom
        setupZoom(control);

        List<CssProperty> cssProps = JavaFxCssUtil.discoverCssProperties(controlClass);

        var cssPropsWithValues = cssProps.stream()
                .map(prop -> String.format("%s: %s", prop.name(), prop.defaultValue()))
                .toList();
        cssProperties.setItems(FXCollections.observableArrayList(cssPropsWithValues));

        var cssPropsNames = cssProps.stream()
                        .map(CssProperty::name)
                        .toList();
        cssText.setSuggestions(cssPropsNames);

        htmlHelpViewer.showHelpFor(controlClass);

        extraClasses.setText(String.join(" ", control.getStyleClass()));
        extraClassesTextChangeListener = (e, oldValue, newValue) -> {
            control.getStyleClass().clear();
            if (newValue != null) {
                control.getStyleClass().addAll(newValue.split(" "));
            }
        };
        extraClasses.textProperty().addListener(extraClassesTextChangeListener);

        statusIcon.setFill(Color.YELLOW);
    }


    private void uninstallControl() {
        if (controlContainer.getChildren().isEmpty()) return;

        var controlToRemove = (Control)controlContainer.getChildren().get(0);
        controlContainer.getChildren().clear();

        controlStructure.setObservedControl(null);

        controlToRemove.disableProperty().unbind();

        if (!controlToRemove.getTransforms().isEmpty()) {
            Scale transform = (Scale) controlToRemove.getTransforms().get(0);
            transform.xProperty().unbind();
            transform.yProperty().unbind();
        }

        if (zoomListenerForBounds != null) {
            controlContainer.layoutBoundsProperty().removeListener(zoomListenerForBounds);
        }

        if (extraClassesTextChangeListener != null) {
            extraClasses.textProperty().removeListener(extraClassesTextChangeListener);
        }
    }

    private ChangeListener<Bounds> zoomListenerForBounds = null;

    private void setupZoom(Control control) {
        Scale scaleTransform = new Scale(1, 1);
        scaleTransform.xProperty().bind(zoomSlider.valueProperty());
        scaleTransform.yProperty().bind(zoomSlider.valueProperty());
        control.getTransforms().add(scaleTransform);
        // Be default scales around top left corner, we need to fix manually...
        zoomListenerForBounds = (o, oldValue, newValue) -> {
            System.out.println("LAYOUT EVENT");
            scaleTransform.setPivotX(control.getWidth() / 2);
            scaleTransform.setPivotY(control.getHeight() / 2);
        };
        controlContainer.layoutBoundsProperty().addListener(zoomListenerForBounds);

        Platform.runLater(() -> {
            // manually fire once - layoutBounds will not change until container is resized.
            // TODO: This delay is not enough, wait on either skin prop or on some other prop
            zoomListenerForBounds.changed(null, null, null);
        });
    }

    @SuppressWarnings("unchecked")
    private static void addExampleContent(Control control) {
        if (control instanceof Labeled) {
            ((Labeled) control).setText("Lorem ipsum");
        }

        if (control instanceof ListView) {
            ((ListView<Object>) control).setItems(FXCollections.observableArrayList(
                    "Option 1",
                    "Option 2",
                    "Option 3"
            ));
        }

        if (control instanceof TreeView) {
            var root = new TreeItem<Object>("Root");
            root.getChildren().add(new TreeItem<>("Child 1"));
            root.getChildren().add(new TreeItem<>("Child 2"));
            ((TreeView<Object>)control).setRoot(root);
        }

        // TODO: Add other controls...
    }

    private void reloadCss() {
        InMemoryCssStylesheet.setContents(cssText.getText());
        Node node = controlContainer.getCenter();
        if (node != null) {
            ((Control)node).getStylesheets().clear();
            ((Control)node).getStylesheets().add(InMemoryCssStylesheet.getStylesheetUrl());
        }

        statusIcon.setFill(Color.GREEN);
    }
}
