package csstool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HtmlHelpViewer extends VBox implements Initializable {
    @FXML
    private WebView webView;

    @FXML
    private TextField helpSearchField;

    public HtmlHelpViewer() {
        // TODO: Remove duplication across project
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "HtmlHelpViewer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        // Hack for scene builder "xxx.fxml file not found" error.
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView.getEngine().load(getClass().getResource("/javafx-css-docs.html").toExternalForm());
        webView.setContextMenuEnabled(false);

        // Enable navigation within the document
        webView.getEngine().setOnStatusChanged((WebEvent<String> event) -> {
            // WARNING: Insecure but this is a local HTML file from resources, so should be fine
            if (event.getData() != null && event.getData().contains("javafx-css-docs.html")) {
                webView.getEngine().load(event.getData());
            }
        });

        helpSearchField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();

                findOnPage(helpSearchField.getText());
            }
        });
    }

    public void showHelpFor(Class<?> controlClass) {
        if (controlClass != null) {
            navigateToId(controlClass.getSimpleName());
        } else {
            goHome();
        }
    }

    @FXML
    private void goHome() {
        webView.getEngine().executeScript("{ var h1 = document.querySelector('h1'); if(h1) h1.scrollIntoView(); }");
    }

    private void navigateToId(String id) {
        webView.getEngine().executeScript("{ var el = document.getElementById('" + id.toLowerCase() + "'); if (el) el.scrollIntoView(); }");
    }

    @FXML
    private void goBack() {
        var history = webView.getEngine().getHistory();
        if (history.getCurrentIndex() > 0) history.go(-1);
    }

    @FXML
    private void goForward() {
        var history = webView.getEngine().getHistory();
        if ((history.getCurrentIndex()+1) < history.getEntries().size()) history.go(+1);
    }

    private void findOnPage(String phrase) {
        // https://developer.mozilla.org/en-US/docs/Web/API/Window/find
        webView.getEngine().executeScript("window.find('" + escape(phrase) + "', false, false, true);");
    }

    // TODO: Move to util class
    public static String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\'", "\\'")      // <== not necessary
                .replace("\"", "\\\"");
    }
}
