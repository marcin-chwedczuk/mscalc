package csstool;

import javafx.scene.control.Control;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class JavaFxControlClassesFinder {
    private JavaFxControlClassesFinder() { }

    public static List<Class<? extends Control>> findControlClasses() {
        // This library is unreliable with Java modules, be warned.
        var reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClass(Control.class)));

        Set<Class<? extends Control>> subTypesOf = reflections.getSubTypesOf(Control.class);

        return subTypesOf.stream()
                .filter(controlClass -> {
                    if (controlClass.getName().contains("$")) return false;
                    if (controlClass.getName().endsWith("Cell")) return false;
                    if (!"javafx.scene.control".equals(controlClass.getPackageName())) return false;
                    return Modifier.isPublic(controlClass.getModifiers()) && !Modifier.isAbstract(controlClass.getModifiers());
                })
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }
}
