
# Rewrite of Microsoft Calculator in JavaFX

Project under construction.

Engine source code was translated from C++, from the official Calculator repository: https://github.com/microsoft/calculator

Current project status:

- [X] ratpack engine fully translated to Java with tests
- [X] UI for scientific view

What is missing:

- [ ] Glue code between UI & calculator engine
- [ ] A lot of polishing e.g. full keyboard support as in the original calc.

# JavaFX Opinionated Application Template

![Build Status](https://github.com/marcin-chwedczuk/javafx-template/actions/workflows/ci.yaml/badge.svg)

* Created for JDK 17+, with Java 9 modules support
* Multi-module Maven project by default
* Sass used instead of CSS
* assertJ and jUnit 5 used for unit testing
* TestFX used for integration testing
* Example GitHub action that builds the project and runs integration tests in headless mode

### How to run application
```
./mvnw javafx:run -pl gui
```

### How to rebuild `.css` files from `.scss`
```
./mvnw nl.geodienstencentrum.maven:sass-maven-plugin:update-stylesheets -pl gui
```

### How to watch for SCSS changes and regenerate them when they change
```
fswatch --exclude='.*' --include='.*[.]scss$' --print0 . | while read -d "" event; do
    ./mvnw nl.geodienstencentrum.maven:sass-maven-plugin:update-stylesheets -pl gui 
done
```
You need to install `fswatch` command for this to work.

### How to generate `jlink` image
```
./mvnw javafx:jlink -pl gui 
```
You can now send `./gui/target/gui.zip` to your friends :tada:

### How to run integration tests (TestFX)
```
./mvnw verify -Dskip.integration.tests=false -Dskip.unit.tests=true
```

### How to run single integration test
```
./mvnw clean verify -Dskip.integration.tests=false -Dskip.unit.tests=true \
    -pl gui -Dit.test=MainWindowIT
```

### How to run integration tests in the headless mode
```
_JAVA_OPTIONS="-Djava.awt.headless=true \
-Dtestfx.robot=glass \
-Dtestfx.headless=true \
-Dprism.order=sw \
-Dprism.verbose=true" ./mvnw verify -Dskip.integration.tests=false -pl gui 
```

On macOS you must allow IntelliJ or the terminal app that you are using,
to "take over your computer":
![macOS settings needed for IT](docs/macOS-it-perm.png)


