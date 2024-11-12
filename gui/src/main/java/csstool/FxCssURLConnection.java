package csstool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

// TODO: Move to a newer registration method
// https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java/56088592#56088592
public class FxCssURLConnection extends URLConnection {
    private String cssStylesheet;

    public FxCssURLConnection(URL u) {
        super(u);
    }

    @Override
    public void connect() throws IOException {
        System.out.println("Got URL: " + getURL());
        String className = getURL().getHost();

        try {
            Object o = Class.forName(className).newInstance();
            cssStylesheet = ((DynamicCssProvider)o).loadCss();
        } catch (Exception e) {
            e.printStackTrace();
            cssStylesheet = "/* invalid */";
        }
    }

    @Override
    public String getContentType() {
        return "text/css; charset=utf-8";
    }

    @Override
    public int getContentLength() {
        return cssStylesheet.length();
    }

    @Override
    public long getContentLengthLong() {
        return cssStylesheet.length();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return super.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(cssStylesheet.getBytes(StandardCharsets.UTF_8));
    }
}
