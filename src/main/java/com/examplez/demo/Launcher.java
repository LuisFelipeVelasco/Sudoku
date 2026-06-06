package com.examplez.demo;

import javafx.application.Application;

/**
 * Entry point of the Sudoku application.
 *
 * <p>This class serves as the main launcher to work around the JavaFX
 * module system restriction that prevents extending {@link Application}
 * directly from the class containing {@code main}.</p>
 *
 *  @author jeronimo rojas imbachi
 *  @author Luis Felipe Velasco
 * @version 1.0
 * @see GameLauncher
 */
public class Launcher {

    /**
     * Application entry point. Delegates to {@link GameLauncher} via
     * {@link Application#launch(Class, String[])}.
     *
     * @param args command-line arguments forwarded to the JavaFX runtime
     */
    public static void main(String[] args) {
        Application.launch(GameLauncher.class, args);
    }
}
