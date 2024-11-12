package csstool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.Collection;

public class AutocompleteTextArea extends TextArea {
    private static final int WORLD_MAX_LOOKUP_CHARS = 64;

    private final ListView<String> autocompleteListView = new ListView<>();
    private final Popup autocompletePopup = new Popup();

    private final ObservableList<String> unfilteredSuggestions = FXCollections.observableArrayList();
    private final FilteredList<String> filteredSuggestions = new FilteredList<>(unfilteredSuggestions);

    public AutocompleteTextArea() {
        // Prevent listView from capturing key events like Esc, Array Up & Down
        autocompleteListView.setFocusTraversable(false);
        autocompleteListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        autocompleteListView.setItems(filteredSuggestions);

        autocompletePopup.getContent().add(autocompleteListView);
        autocompletePopup.setHideOnEscape(true);

        replaceTabWithSpaces();

        // Show popup when a Control+Space is pressed
        addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                event.consume();

                String world = getWordBeforeCaret();
                filterSuggestions(world);

                showAutocompletePopupAtCaret(true);
            }
        });

        // Hide popup when focus is lost.
        focusedProperty().addListener((o, oldValue, newValue) -> {
            if (!newValue) {
                autocompletePopup.hide();
            }
        });

        // Relocate popup during writing; update applicable suggestions.
        caretPositionProperty().addListener((o, oldValue, newValue) -> {
            if (autocompletePopup.isShowing()) {
                String world = getWordBeforeCaret();

                if (world.isEmpty()) {
                    autocompletePopup.hide();
                    return;
                }

                var selectionModel = autocompleteListView.getSelectionModel();
                String oldSelection = selectionModel.getSelectedItem();

                filterSuggestions(world);
                showAutocompletePopupAtCaret(false);

                // Preserve old selection
                if (oldSelection != null && filteredSuggestions.contains(oldSelection)) {
                    selectionModel.select(oldSelection);
                }

                // If there is no selection because of e.g. backspace select first item
                if (selectionModel.isEmpty() && !filteredSuggestions.isEmpty()) {
                    selectSuggestion(0);
                }
            }
        });

        // Handle Arrow Up, Down and Tab when popup is open.
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!autocompletePopup.isShowing()) return;

            var selectionModel = autocompleteListView.getSelectionModel();
            int selectedIndex = selectionModel.getSelectedIndex();

            if (event.getCode() == KeyCode.UP) {
                event.consume();
                selectSuggestion(selectedIndex - 1);
            } else if (event.getCode() == KeyCode.DOWN) {
                event.consume();
                selectSuggestion(selectedIndex + 1);
            } else if (event.getCode() == KeyCode.ENTER) {
                event.consume();

                autocompletePopup.hide();
                if (selectedIndex != -1) {
                    String suggestion = selectionModel.getSelectedItem();

                    String before = getWordBeforeCaret();
                    var textRange = IndexRange.normalize(getCaretPosition() - before.length(), getCaretPosition());
                    replaceText(textRange, suggestion);
                }
            }
        });
    }

    private void replaceTabWithSpaces() {
        String spaces = " ".repeat(4);

        setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) return change;

            if ("\t".equals(change.getText())) {
                change.setText(spaces);
                change.setAnchor(change.getAnchor() + spaces.length() - 1);
                change.setCaretPosition(change.getCaretPosition() + spaces.length() - 1);
            }

            return change;
        }));
    }

    private void showAutocompletePopupAtCaret(boolean firstTime) {
        if (getSkin() instanceof TextAreaSkin skin) {
            var screenCaretPosition = localToScreen(skin.getCaretBounds());
            autocompletePopup.show(this, screenCaretPosition.getCenterX(), screenCaretPosition.getCenterY() + screenCaretPosition.getHeight());
        }

        if (firstTime && !filteredSuggestions.isEmpty()) {
            selectSuggestion(0);
        }
    }

    private void selectSuggestion(int newIndex) {
        if (filteredSuggestions.isEmpty()) return;

        var selectionModel = autocompleteListView.getSelectionModel();
        selectionModel.select(Math.max(0, Math.min(filteredSuggestions.size() - 1, newIndex)));
    }

    private void filterSuggestions(String byWorld) {
        filteredSuggestions.setPredicate(s -> (s != null) && s.contains(byWorld));
    }

    public void setSuggestions(Collection<String> filteredSuggestions) {
        unfilteredSuggestions.clear();
        unfilteredSuggestions.addAll(filteredSuggestions);
    }

    private String getWordBeforeCaret() {
        int caretPos = getCaretPosition();

        // For efficiency reasons we only look up to WORLD_MAX_LOOKUP_CHARS characters
        String textBeforeCaret = getText(Math.max(0, caretPos - WORLD_MAX_LOOKUP_CHARS), caretPos);

        int length = 0;
        for (int i = textBeforeCaret.length() - 1; i >= 0; i--) {
            if (!isWordCharacter(textBeforeCaret.charAt(i))) break;
            length++;
        }

        return textBeforeCaret.substring(textBeforeCaret.length() - length);
    }

    private String getWordAfterCaret() {
        int caretPos = getCaretPosition();

        // For efficiency reasons we only look up to WORLD_MAX_LOOKUP_CHARS characters
        String textBeforeCaret = getText(caretPos, Math.min(caretPos + WORLD_MAX_LOOKUP_CHARS, getLength()));

        int length = 0;
        for (int i = 0; i < textBeforeCaret.length(); i++) {
            if (!isWordCharacter(textBeforeCaret.charAt(i))) break;
            length++;
        }

        return textBeforeCaret.substring(0, length);
    }

    public String getEditedWord() {
        return getWordBeforeCaret().concat(getWordAfterCaret());
    }

    private static boolean isWordCharacter(char c) {
        return (c == '-') || Character.isLetterOrDigit(c);
    }
}
