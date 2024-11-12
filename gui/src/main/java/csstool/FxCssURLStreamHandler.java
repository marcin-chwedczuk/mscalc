package csstool;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class FxCssURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        // TODO: Load CSS Class
        var conn = new FxCssURLConnection(u);
        conn.connect();
        return conn;
    }
}
