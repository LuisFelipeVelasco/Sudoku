package com.examplez.demo.controllers;

import com.examplez.demo.SudokuInitializable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * FXML controller for the main game view ({@code main-menu-view.fxml}).
 *
 * <p>Manages the 6×6 Sudoku board rendered as a grid of {@link TextField}
 * nodes, handles user input validation in real time, and coordinates with
 * {@link SudokuGame} to generate boards, dispense clues, and detect
 * completion.</p>
 *
 * <p>Field naming convention: {@code Trc} where {@code r} is the zero-based
 * row index and {@code c} is the zero-based column index
 * (e.g. {@code T23} → row 2, column 3).</p>
 *
 * @author jeronimo rojas imbachi
 * @author Luis Felipe Velasco
 * @version 1.0
 * @see SudokuGame
 */
public class MainMenuController implements SudokuInitializable {

    // -----------------------------------------------------------------------
    // FXML-injected controls
    // -----------------------------------------------------------------------

    /** Button that requests a clue from the model. Visible only during a game. */
    @FXML private Button buttonClue;

    /** Button that starts a new game. Visible only on the start/end screen. */
    @FXML private Button buttonPlay;

    /** Status label shown below the board (feedback messages to the player). */
    @FXML private Label labelText;

    /** GridPane that holds all the TextFields (cells) of the board. */
    @FXML private GridPane gridPane;

    // -----------------------------------------------------------------------
    // Instance state
    // -----------------------------------------------------------------------

    /**
     * Two-dimensional ArrayList that mirrors the FXML {@link TextField} grid
     * for programmatic access. Populated during {@link #initialize()}.
     */
    private ArrayList<ArrayList<TextField>> cells;

    /**
     * Two-dimensional ArrayList that caches the initial inline CSS style of
     * each {@link TextField} node as it was set in the FXML file.
     *
     * <p>This baseline snapshot prevents dynamic highlight strings (e.g. error
     * background colours) from accumulating across successive validation checks,
     * and allows each cell to be safely restored to its original appearance
     * after a conflict is resolved.</p>
     */
    private ArrayList<ArrayList<String>> cellsStyle;

    /** The game-logic model for the current session. */
    private SudokuGame modelSudoku;

    /**
     * Guards the one-time listener registration in
     * {@link #configureTextFields()} ()}.
     *
     * <p>Set to {@code false} after the first call so that subsequent
     * invocations of {@link #onButtonPlay()} do not attach duplicate
     * {@code textProperty} listeners to the same {@link TextField} nodes.</p>
     */
    boolean firstGame = true;

    // -----------------------------------------------------------------------
    // JavaFX lifecycle
    // -----------------------------------------------------------------------

    /**
     * Called automatically by the JavaFX FXML loader after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Iterates over the children of {@link #gridPane} in row-major order
     * and populates the {@link #cells} ArrayList so that
     * {@code cells.get(i).get(j)} always refers to the {@link TextField} at
     * row {@code i}, column {@code j}. The same traversal is then used to
     * snapshot each cell's initial inline CSS style into {@link #cellsStyle},
     * enabling later style restoration.</p>
     *
     * <p><strong>Note:</strong> this method assumes that the GridPane's
     * child list is ordered left-to-right, top-to-bottom and contains
     * exactly 36 {@link TextField} nodes with no other interleaved children.</p>
     */
    public void initialize() {
        ObservableList<Node> textFieldCells = gridPane.getChildren();

        cells = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ArrayList<TextField> row = new ArrayList<>(6);
            for (int j = 0; j < 6; j++) {
                row.add((TextField) textFieldCells.get(6 * i + j));
            }
            cells.add(row);
        }

        cellsStyle = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ArrayList<String> row = new ArrayList<>(6);
            for (int j = 0; j < 6; j++) {
                row.add(textFieldCells.get(6 * i + j).getStyle());
            }
            cellsStyle.add(row);
        }
    }

    // -----------------------------------------------------------------------
    // FXML event handlers
    // -----------------------------------------------------------------------

    /**
     * Handles the <em>Play</em> button click.
     *
     * <p>This method prepares a new game session by first resetting the board UI
     * through {@link #cleanBoard()}. Then, it creates a new {@link SudokuGame}
     * instance, initializes the solved board with {@link SudokuGame#initialize()},
     * and displays the starting clue cells using {@link #showBoard()}.</p>
     *
     * <p>It also calls {@link #configureTextFields()}, which configures each
     * {@link TextField} only once during the lifetime of the controller. That
     * configuration includes limiting each field to a single character with a
     * {@link javafx.scene.control.TextFormatter} and attaching the text-change
     * listeners used to validate the player's input.</p>
     *
     * <p>Finally, the interface is moved into the in-game state: the Clue button
     * becomes visible, the Play button is hidden, and the status label is updated
     * to indicate that the game has started.</p>
     */

    @FXML
    private void onButtonPlay() {
        cleanBoard();
        modelSudoku = new SudokuGame();
        labelText.setText("Start playing");
        labelText.setStyle("-fx-text-fill: #278438;");
        modelSudoku.initialize();
        showBoard();
        configureTextFields();
        buttonClue.setVisible(true);
        buttonPlay.setVisible(false);
    }

    /**
     * Handles the <em>Clue</em> button click.
     *
     * <p>Delegates to {@link SudokuGame#isPossibleGiveClue(ArrayList)} to
     * check whether another clue may be dispensed. If allowed, the first
     * empty cell is located by {@link SudokuGame#giveClue(ArrayList)}, its
     * confirmed state is set to {@code true}, and its correct value is revealed
     * in the UI via {@link #showClue(int, int)}. Any player entries in the
     * same row, column, or 2×3 sub-block that now conflict with the revealed
     * value are highlighted as invalid.</p>
     *
     * <p>If the model reports that no further clues are available, an
     * informational message is displayed and no board change is made.</p>
     */
    @FXML
    private void onButtonClue() {
        if (modelSudoku.isPossibleGiveClue(getMatrixValueCells())) {
            List<Integer> coordinatesClue = modelSudoku.giveClue(getMatrixValueCells());
            int rowClue = coordinatesClue.get(0);
            int columnClue = coordinatesClue.get(1);
            modelSudoku.setConfirmedStateOfCell(columnClue, rowClue, true);
            showClue(rowClue, columnClue);
            String valueClue = cells.get(rowClue).get(columnClue).getText();
            List<List<Integer>> repeatedInvalidCells = modelSudoku.getCoordinatesRepeatedInvalidCells(valueClue, columnClue, rowClue, getMatrixValueCells());
            if (!repeatedInvalidCells.isEmpty()) {
                for (List<Integer> c : repeatedInvalidCells) {
                    editInterfaceDependingOnInputValidation("Watch out, a number you typed nearby is invalid.", false, cells.get(c.get(0)).get(c.get(1)));
                }
                editInterfaceDependingOnInputValidation("Watch out, a number you typed nearby is invalid.", false, cells.get(rowClue).get(columnClue));
            }
        } else {
            labelText.setText("You can't ask for more clues");
            labelText.setStyle("-fx-text-fill: #8e2115;");
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Renders the initial board state in the UI according to the given
     * confirmed-cell mask.
     *
     * <p>For every cell where {@code modelSudoku.getConfirmedCells().get(row).get(col)} is
     * {@code true}, the correct value from the model is displayed and the
     * {@link TextField} is disabled so the player cannot modify it.
     * All other cells are cleared and left enabled for player input.</p>
     */
    private void showBoard() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (modelSudoku.getConfirmedCells().get(row).get(col)) {
                    cells.get(row).get(col).setText(String.valueOf(modelSudoku.getValue(row, col)));
                    cells.get(row).get(col).setDisable(true);
                } else {
                    cells.get(row).get(col).setDisable(false);
                }
            }
        }
    }

    /**
     * Reveals the correct value for a single cell in the UI and disables
     * the corresponding {@link TextField} so the player cannot overwrite it.
     *
     * @param row the zero-based row index of the cell to reveal
     * @param col the zero-based column index of the cell to reveal
     */
    private void showClue(int row, int col) {
        cells.get(row).get(col).setText(String.valueOf(modelSudoku.getValue(row, col)));
        cells.get(row).get(col).setDisable(true);
    }

    /**
     * Configures every {@link TextField} in the board only on the first call.
     *
     * <p>This method assigns a {@link TextFormatter} to each text field to limit
     * the input to a single character. Any change that would make the field contain
     * more than one character is rejected.</p>
     *
     * <p>It also attaches a {@code textProperty} change listener to each field.
     * The listener calls {@link #verification(String, String, TextField)} every time
     * the text changes, passing the new value, the old value, and the text field
     * where the change occurred.</p>
     *
     * <p>The {@link #firstGame} flag ensures that formatters and listeners are
     * registered exactly once during the lifetime of the controller. Without this
     * guard, each subsequent call to {@link #onButtonPlay()} could stack additional
     * listeners on the same nodes, causing the verification logic to run multiple
     * times for a single keystroke.</p>
     *
     * <p>After the first configuration, {@link #firstGame} is set to {@code false},
     * preventing the same text fields from being configured again in later game
     * sessions.</p>
     */

    private void configureTextFields(){
        if(firstGame){
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    TextField textField = cells.get(row).get(col);

                    textField.setTextFormatter(new TextFormatter<String>(change -> {
                        if (change.getControlNewText().length()<=1){
                            return change;
                        }
                        labelText.setText("you can´t write more then 1 number");
                        return null;
                    }));

                    textField.textProperty().addListener((observable, oldValue, newValue) -> {
                        verification(newValue, oldValue, textField);
                    });

                }
            }
        }

        firstGame=false;
    }




    /**
     * Validates a player's input whenever the text of a {@link TextField}
     * changes and updates the UI accordingly.
     *
     * <p>Validation rules (applied in order):</p>
     * <ol>
     *   <li>If {@code userInput} is empty, remove any error highlight from the
     *       cell, mark it as unconfirmed in the model, and re-validate adjacent
     *       cells that may have been flagged because of the now-deleted value.</li>
     *   <li>If the value is not a digit 1–6, display an error message and reset
     *       the field to empty.</li>
     *   <li>If the cell is not yet confirmed and the digit already appears in the
     *       same row, column, or 2×3 sub-block, highlight the conflicting cells
     *       (including this one) as invalid.</li>
     *   <li>Otherwise, mark the cell as confirmed in the model, clear the status
     *       label, and check whether the board is now fully completed via
     *       {@link #editInterfaceSudokuCompleted()}.</li>
     * </ol>
     *
     * @param userInput    the new text entered by the player
     * @param oldUserInput the text that was in the cell prior to the change
     * @param textField    the {@link TextField} whose value changed
     */
    private void verification(String userInput, String oldUserInput, TextField textField) {
        List<Integer> coordinatesTextField = getCoordinatestextField(textField);
        int rowTextField    = coordinatesTextField.get(0);
        int columnTextField = coordinatesTextField.get(1);
        if (labelText.getText().equals("Start playing")) labelText.setText("");
        if (userInput.isEmpty() && !labelText.getText().equals("Type a number between 1 and 6")) {
            labelText.setText("");
            modelSudoku.setConfirmedStateOfCell(columnTextField, rowTextField, false);
            textField.setStyle(cellsStyle.get(rowTextField).get(columnTextField));
            List<List<Integer>> repeatedValidCells = modelSudoku.getRepeatedValidCells(oldUserInput, columnTextField, rowTextField, getMatrixValueCells());
            if (!repeatedValidCells.isEmpty()) {
                for (List<Integer> c : repeatedValidCells) {
                    editInterfaceDependingOnInputValidation("", true, cells.get(c.get(0)).get(c.get(1)));
                }
            }
        } else if (!modelSudoku.isNumberOneToSix(textField.getText())) {
            System.out.println("here");
            labelText.setText("Type a number between 1 and 6");
            labelText.setStyle("-fx-text-fill: #8e2115;");
            textField.setText("");
        } else if (!modelSudoku.getConfirmedStateOfCell(columnTextField, rowTextField)) {
            List<List<Integer>> repeatedInvalidCells = modelSudoku.getCoordinatesRepeatedInvalidCells(userInput, columnTextField, rowTextField, getMatrixValueCells());
            if (!repeatedInvalidCells.isEmpty()) {
                for (List<Integer> c : repeatedInvalidCells) {
                    editInterfaceDependingOnInputValidation("The number is invalid", false, cells.get(c.get(0)).get(c.get(1)));
                }
                editInterfaceDependingOnInputValidation("The number is invalid", false, cells.get(rowTextField).get(columnTextField));
            } else {
                modelSudoku.setConfirmedStateOfCell(columnTextField, rowTextField, true);
                labelText.setText("");
            }
        }
        if (modelSudoku.isTheSudokuCompleted()) editInterfaceSudokuCompleted();
    }

    /**
     * Clears the text of every {@link TextField} in the 6×6 grid.
     *
     * <p>Called at the start of each new game to remove any values left
     * over from the previous session before the initial clues are rendered.</p>
     */
    private void cleanBoard() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                cells.get(row).get(col).setText("");
            }
        }
    }

    /**
     * Provides visual feedback to the player about the validity of their
     * latest input or a clue reveal.
     *
     * <p>When {@code isValidInput} is {@code true}, the status label is
     * updated in green and the target {@link TextField} is restored to its
     * original baseline style from {@link #cellsStyle}. When {@code false},
     * the label switches to red and a red background highlight is appended to
     * the field's current inline style to indicate a conflict.</p>
     *
     * @param text         the feedback message to display in the status label
     * @param isValidInput {@code true} if the placement is valid and should be
     *                     un-highlighted; {@code false} if there is a conflict
     *                     that should be highlighted
     * @param textField    the {@link TextField} associated with the validated cell
     */
    private void editInterfaceDependingOnInputValidation(String text, boolean isValidInput, TextField textField) {
        if (isValidInput) {
            labelText.setText(text);
            labelText.setStyle("-fx-text-fill: #278438;");
            textField.setStyle(getInitialStyleOfTextField(textField));
        } else {
            labelText.setText(text);
            labelText.setStyle("-fx-text-fill: #8e2115;");
            textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
        }
    }

    /**
     * Retrieves the baseline CSS style string that was cached for a given
     * {@link TextField} when the controller was first initialized.
     *
     * <p>This allows dynamic highlight styles (e.g. error backgrounds) to be
     * safely removed and the field restored to its original FXML appearance
     * without hard-coding style values in the controller.</p>
     *
     * @param textField the {@link TextField} whose baseline style is requested
     * @return the original inline CSS style string for the field; an empty
     *         string if the field is not found in the {@link #cells} grid
     */
    private String getInitialStyleOfTextField(TextField textField) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (cells.get(i).get(j) == textField) return cellsStyle.get(i).get(j);
            }
        }
        return "";
    }

    /**
     * Transitions the application out of the active game state when the board
     * is successfully completed.
     *
     * <p>Displays a congratulatory message in green, disables all
     * {@link TextField} nodes to prevent further edits, and swaps the toolbar
     * buttons back to their pre-game state (hides the Clue button, shows the
     * Play button). {@link #firstGame} is intentionally left {@code false} so
     * that the already-registered listeners are not duplicated on the next
     * game start.</p>
     */
    private void editInterfaceSudokuCompleted() {
        labelText.setText("You Did it, The sudoku is completed");
        labelText.setStyle("-fx-text-fill: #278438;");
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                cells.get(row).get(col).setDisable(true);
            }
        }
        buttonClue.setVisible(false);
        buttonPlay.setVisible(true);
    }

    /**
     * Builds a 6×6 string matrix that mirrors the current text content of
     * every {@link TextField} in the UI grid.
     *
     * <p>Empty cells are represented as {@code "0"} so that the matrix can be
     * passed directly to {@link SudokuGame} validation methods, which treat
     * {@code "0"} as the sentinel value for an unfilled position.</p>
     *
     * @return a new {@link ArrayList} of rows, each containing six string
     *         values representing the current player input ({@code "1"}–{@code "6"})
     *         or {@code "0"} for empty cells
     */
    private ArrayList<ArrayList<String>> getMatrixValueCells() {
        ArrayList<ArrayList<String>> matrixValueCells = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ArrayList<String> row = new ArrayList<>(6);
            for (int j = 0; j < 6; j++) {
                String valueCell = cells.get(i).get(j).getText();
                if (valueCell.isEmpty()) row.add("0");
                else row.add(valueCell);
            }
            matrixValueCells.add(row);
        }
        return matrixValueCells;
    }

    /**
     * Resolves the board coordinates of a given {@link TextField} by scanning
     * the {@link #cells} grid for a reference match.
     *
     * <p>Uses reference equality ({@code ==}) to locate the field, which is
     * reliable here because {@link #cells} holds the exact same object
     * references injected by the FXML loader. This is the canonical way to
     * convert a UI event source back into {@code [row, col]} indices
     * consumable by the model layer.</p>
     *
     * @param textField the {@link TextField} whose grid position is needed
     * @return a two-element list {@code [row, col]} with zero-based indices;
     *         returns an empty list if the field is not found in the grid
     */
    private List<Integer> getCoordinatestextField(TextField textField) {
        List<Integer> coordinates = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (cells.get(row).get(col) == textField) {
                    coordinates.add(row);
                    coordinates.add(col);
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Prints the given 6×6 string matrix to standard output for debugging.
     *
     * <p>A horizontal divider is printed before every third row (i.e. before
     * row 2 and row 4) to visually separate the three horizontal bands of
     * 2×3 sub-blocks. A vertical pipe ({@code |}) is inserted before every
     * third column (i.e. before column 3) to mark the boundary between the
     * two column groups, reproducing the block structure of this Sudoku
     * variant.</p>
     *
     * @param matrix a 6×6 string matrix to display; values are expected to be
     *               digit characters ({@code "1"}–{@code "6"}) or {@code "0"}
     *               for empty cells
     */
    private void printBoard(ArrayList<ArrayList<String>> matrix) {
        System.out.println("--- SUDOKU BOARD ---");
        for (int i = 0; i < 6; i++) {
            if (i > 0 && i % 2 == 0) {
                System.out.println("---------------------");
            }
            for (int j = 0; j < 6; j++) {
                if (j > 0 && j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print(matrix.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------\n");
    }
}