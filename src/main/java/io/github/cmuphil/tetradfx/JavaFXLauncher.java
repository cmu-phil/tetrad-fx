package io.github.cmuphil.tetradfx;

/**
 * Basic launcher for JavaFX. This is needed because the JavaFX runtime is not
 * included in the JDK starting with Java 11.
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class JavaFXLauncher {
    public static void main(String[] args) {
        ParameterEditorUiStarter.main(args);
    }
}
