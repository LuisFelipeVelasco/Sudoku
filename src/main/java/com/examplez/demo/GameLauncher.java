package com.examplez.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application bootstrap for the Sudoku game.
 *
 * <p>Loads the main FXML layout, creates the primary {@link Scene} with a
 * fixed 550×560-pixel viewport, and displays the application window.</p>
 *
 * @author jeronimo rojas imbachi
 * @author Luis Felipe Velasco
 * @version 1.0
 * @see Launcher
 * @see javafx.application.Application
 */
public class GameLauncher extends Application {

    /**
     * Initialises and shows the primary stage of the application.
     *
     * <p>Loads {@code main-menu-view.fxml} relative to this class, wraps it
     * in a non-resizable {@link Scene}, sets the window title to
     * {@code "Game"} and makes the stage visible.</p>
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws IOException if the FXML resource cannot be found or parsed
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GameLauncher.class.getResource("main-menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 550, 560);
        stage.setTitle("Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
