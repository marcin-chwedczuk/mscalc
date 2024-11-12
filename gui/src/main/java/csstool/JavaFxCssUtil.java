package csstool;

import javafx.css.CssMetaData;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderWidths;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JavaFxCssUtil {
    private JavaFxCssUtil() {
    }

    @SuppressWarnings("unchecked")
    public static List<CssProperty> discoverCssProperties(Class<?> controlClass) throws Exception {
        var control = (Control) controlClass.newInstance();

        var results = new ArrayList<CssProperty>();

        Method m = controlClass.getMethod("getClassCssMetaData");
        var cssMetadata = (List<CssMetaData<Control, ?>>) m.invoke(null);

        for (CssMetaData<Control, ?> cssProperty : cssMetadata) {
            var name = cssProperty.getProperty();
            var value = formatCssPropertyValue(control, cssProperty);
            results.add(new CssProperty(name, value));

            var cssSubProperties = (List<CssMetaData<Control, ?>>) (Object) cssProperty.getSubProperties();
            if (cssSubProperties == null) continue;

            for (var subCssProperty : cssSubProperties) {
                var subName = subCssProperty.getProperty();
                var subValue = formatCssPropertyValue(control, subCssProperty);
                results.add(new CssProperty(subName, subValue, Optional.of(name)));
            }
        }

        results.sort(Comparator.comparing(CssProperty::name));

        return results;
    }

    private static String formatCssPropertyValue(Control control, CssMetaData<Control, ?> cssPropertyMetadata) {
        StyleableProperty<?> prop = cssPropertyMetadata.getStyleableProperty(control);

        if (prop != null) {
            return formatValue(prop.getValue());
        }

        if (cssPropertyMetadata.isInherits()) {
            return "inherits";
        }

        return "#" + formatValue(cssPropertyMetadata.getInitialValue(control));
    }

    private static String formatValue(Object value) {
        switch (value) {
            case null -> { return "null"; }

            case Object[] array -> {
                return Arrays.stream(array)
                        .map(JavaFxCssUtil::formatValue)
                        .collect(Collectors.joining(",", "[", "]"));
            }

            // Missing toString's from accessible classes
            case BorderWidths bw -> {
                // TODO: Print all fields
                return String.format("BorderWidths %f %f %f %f",
                        bw.getTop(), bw.getRight(), bw.getBottom(), bw.getLeft());
            }

            case BackgroundSize bs -> {
                // TODO: Print all fields
                return String.format("BackgroundSize %f %f", bs.getWidth(), bs.getHeight());
            }

            case BackgroundPosition bp -> {
                // TODO: Pritn all fields
                return String.format("BackgroundPosition %f %f", bp.getHorizontalPosition(), bp.getVerticalPosition());
            }

            default -> {
                return Objects.toString(value);
            }
        }
    }
}
