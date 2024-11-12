package csstool;

import java.util.Optional;

public record CssProperty(String name, String defaultValue, Optional<String> subPropertyOf) {
    public CssProperty(String name, String defaultValue) {
        this(name, defaultValue, Optional.empty());
    }
}
