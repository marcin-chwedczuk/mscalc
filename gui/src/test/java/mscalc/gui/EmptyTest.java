package mscalc.gui;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.css.sac.InputSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyTest {
    @Test
    public void empty() {
        assertThat(1)
                .isEqualTo(1);
    }

    @Test
    @Disabled("demo test")
    public void test() {
        try {
            Path repoRoot = findRepoRoot();
            Path srcRoot = repoRoot.resolve("gui/src/main/java");
            String oldCW = System.setProperty("user.dir", srcRoot.toAbsolutePath().toString());

            try {
                Files.writeString(Path.of("test-file.scss"), """
                @import 'mscalc/gui/Base';
                @import 'mscalc/gui/views/Common';
                
                
                .bar { .foo { -fx-foo: $example-variable; } }
                """.stripLeading());

                ScssStylesheet scss = ScssStylesheet.get("test-file.scss");
                scss.addResolver(new ScssStylesheetResolver() {
                    @Override
                    public InputSource resolve(ScssStylesheet scssStylesheet, String s) {
                        System.out.println("resolve: " + s);
                        s = s.replaceAll("[\\\\/](\\w+)$", "/_$1.scss");
                        System.out.println("Normalized to: " + s);
                        Path file = srcRoot.resolve(s).toAbsolutePath();
                        try {
                            InputStream is = Files.newInputStream(file);

                            InputSource source = new InputSource();
                            source.setByteStream(is);
                            source.setURI(file.toString());
                            return source;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
                scss.compile();

                StringWriter sw = new StringWriter();
                scss.write(sw);

                System.out.println(sw.toString());
            } finally {
                System.setProperty("user.dir", oldCW);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Path findRepoRoot() {
        Path currentDirectory = Paths.get(".").toAbsolutePath();
        Path root = currentDirectory.getRoot();

        while (!root.equals(currentDirectory)) {
            Path tmp = currentDirectory.resolve(".git");
            if (Files.isDirectory(tmp)) {
                return currentDirectory;
            }
            currentDirectory = currentDirectory.getParent().toAbsolutePath();
        }

        return null;
    }
}
