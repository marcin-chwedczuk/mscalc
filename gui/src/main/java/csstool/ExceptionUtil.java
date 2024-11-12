package csstool;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    private ExceptionUtil() { }

    public static String toReadableString(Exception e) {
        if (e == null) return "null";

        String exceptionClass = e.getClass().getName();
        String exceptionMessage = e.getMessage();
        String stackTrace = printStackTraceToString(e);

        return String.format("%s: %s%s%s", exceptionClass, exceptionMessage, System.lineSeparator(), stackTrace);
    }

    public static String printStackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();

        PrintWriter writer = new PrintWriter(sw);
        e.printStackTrace(writer);
        writer.flush();

        return sw.toString();
    }
}
