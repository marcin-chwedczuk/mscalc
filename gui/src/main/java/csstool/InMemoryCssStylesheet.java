package csstool;

import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryCssStylesheet implements DynamicCssProvider {
    private static final String NO_CSS = "/* nothing */";

    private static String contents = NO_CSS;
    private static final AtomicInteger cacheBuster = new AtomicInteger(10000);

    @Override
    public String loadCss() {
        return contents;
    }

    public static void setContents(String css) {
        contents = (css == null) ? NO_CSS : css;
    }

    public static String getStylesheetUrl() {
        return "fxcss://" + InMemoryCssStylesheet.class.getName() + "?cacheBuster=" + cacheBuster.incrementAndGet();
    }
}

