package com.examplez.demo.controllers;

import com.examplez.demo.SudokuInitializable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;
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
    @FXML private Button buttonClue ;

    /** Button that starts a new game. Visible only on the start/end screen. */
    @FXML private Button buttonPlay;

    /** Status label shown below the board (feedback messages to the player). */
    @FXML private Label labelText;

    /** Grid-Pane that store all the text-fields (cells) */
    @FXML private GridPane gridPane;

    // -----------------------------------------------------------------------
    // Instance state
    // -----------------------------------------------------------------------

    /**
     * Two-dimensional array that mirrors the FXML TextField grid for
     * programmatic access. Populated during {@link #initialize()}.
     */
    private TextField[][] cells = new TextField[6][6];

    /**
     * Two-dimensional array that stores the initial CSS styles of each
     * FXML TextField node.
     * * <p>This prevents colour strings and dynamic highlights from accumulating
     * across successive validation checks and allows safe restoration of a
     * block's base appearance.</p>
     */
    private String[][] cellsStyle = new String[6][6];

    /** The game-logic model for the current session. */
    private SudokuGame modelSudoku;

    /**
     * {@code true} while a game is in progress; prevents listener logic
     * from running before a game has started or after it is completed.
     */
    boolean firstGame = true;

    // -----------------------------------------------------------------------
    // JavaFX lifecycle
    // -----------------------------------------------------------------------
    /**
     * Called automatically by the JavaFX FXML loader after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Initializes the {@link #cells} array by mapping the
     * {@link TextField} nodes contained in the {@link #gridPane}
     * to their corresponding row and column indices. The method
     * also stores each cell's initial CSS style in
     * {@link #cellsStyle} so it can be restored later if needed.</p>
     */
    public void initialize() {
        ObservableList<Node> textFieldCells = gridPane.getChildren();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                cells[i][j] = (TextField) textFieldCells.get(6 * i + j);
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                cellsStyle[i][j] = textFieldCells.get(6 * i + j).getStyle();
            }
        }
    }

    // -----------------------------------------------------------------------
    // FXML event handlers
    // -----------------------------------------------------------------------

    /**
     * Handles the <em>Play</em> button click.
     *
     * <p>Generates a new fully solved board via {@link SudokuGame#initialize()},
     * clears the UI grid, renders the initial clues, registers text-change
     * listeners on every editable cell, and transitions the toolbar to
     * in-game state (shows the Clue button, hides the Play button).</p>
     */
    @FXML
    private void onButtonPlay() {
        cleanBoard();
        modelSudoku = new SudokuGame();
        labelText.setText("Start playing");
        labelText.setStyle("-fx-text-fill: #278438;");
        modelSudoku.initialize();
        showBoard(modelSudoku.getConfirmedCells());
        setListenerToTextFields();
        buttonClue.setVisible(true);
        buttonPlay.setVisible(false);
    }

    /**
     * Handles the <em>Clue</em> button click.
     *
     * <p>Asks {@link SudokuGame} for the first empty cell on the board,
     * marks it as confirmed, and reveals its correct value in the UI.
     * After revealing the clue, scans the surrounding row, column, and
     * sub-block for player entries that now conflict with the revealed
     * value and highlights them as invalid.</p>
     *
     * <p>If the maximum number of clues has already been reached (35 cells
     * confirmed), displays an informational message and takes no further
     * action.</p>
     */
    @FXML
    private void onButtonClue() {
        if (modelSudoku.isPossibleGiveClue(getMatrixValueCells())) {
            List<Integer> coordinatesClue = modelSudoku.giveClue(getMatrixValueCells());
            int rowClue = coordinatesClue.get(0);
            int columnClue = coordinatesClue.get(1);
            modelSudoku.setConfirmedStateOfCell(columnClue,rowClue,true);
            showClue(rowClue, columnClue);
            String valueClue = cells[rowClue][columnClue].getText();
            List<List<Integer>> repeatedInvalidCells=modelSudoku.getCoordinatesRepeatedInvalidCells(valueClue,columnClue,rowClue,getMatrixValueCells());
            if(repeatedInvalidCells.isEmpty())labelText.setText("");
            else{
                for(List<Integer> c:repeatedInvalidCells){
                    editInterfaceDependingOnInputValidation("Watch out, a number you typed nearby is invalid.", false, cells[c.get(0)][c.get(1)]);
                }
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
     * Renders the initial board state in the UI according to the
     * {@code matrix} mask.
     *
     * <p>For every cell where {@code matrix[row][col]} is {@code true}, the
     * correct value from the model is displayed and the field is disabled so
     * the player cannot modify it. All other cells are left blank and
     * enabled.</p>
     *
     * @param validCells a 6×6 boolean mask; {@code true} means the cell is
     * revealed as a starting clue
     */
    private void showBoard(ArrayList<ArrayList<Boolean>> validCells) {
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    if (validCells.get(row).get(col)) {
                        cells[row][col].setText(String.valueOf(modelSudoku.getValue(row, col)));
                        cells[row][col].setDisable(true);
                    } else {
                        cells[row][col].setDisable(false);
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
        cells[row][col].setText(String.valueOf(modelSudoku.getValue(row, col)));
        cells[row][col].setDisable(true);
    }

    /**
     * Attaches a {@code textProperty} change listener to every editable
     * {@link TextField} in the board.
     * * <p>This ensures that {@link #verification(String, String, TextField)}
     * is executed whenever the cell's text changes, providing live validation feedback.</p>
     */
    private void setListenerToTextFields() {
        if(firstGame){
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 6; col++) {
                    TextField textField = cells[row][col];
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
     * <li>If the new value is empty, remove any error highlight from the
     * this deletion resolves errors in other cells via
     * <li>If the value is not a digit 1–6, show an error message and reset
     * the field to empty.</li>
     * <li>If the digit already appears in the same column, row, or 2×3
     * sub-block, visually highlight the cell as invalid.</li>
     * <li>Otherwise, accept the value and check whether the board is now
     * fully completed via {@link #editInterfaceSudokuCompleted()}.</li>
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

        if (userInput.isEmpty()&& !labelText.getText().equals("Type a number between 1 and 6")) {
            labelText.setText("");
            modelSudoku.setConfirmedStateOfCell(columnTextField,rowTextField,false);
            textField.setStyle(cellsStyle[rowTextField][columnTextField]);
            List<List<Integer>> repeatedValidCells=modelSudoku.getRepeatedValidCells(oldUserInput, columnTextField, rowTextField, getMatrixValueCells());
            if(!repeatedValidCells.isEmpty()){
                for(List<Integer> c:repeatedValidCells){
                    editInterfaceDependingOnInputValidation("", true, cells[c.get(0)][c.get(1)]);
                }
            }
        }
        else if (!modelSudoku.isNumberOneToSix(userInput)) {
            labelText.setText("Type a number between 1 and 6");
            labelText.setStyle("-fx-text-fill: #8e2115;");
            textField.setText("");
        }
        else if (!modelSudoku.getConfirmedStateOfCell(columnTextField,rowTextField)) {

            if (modelSudoku.sameNumberInSameColumn(userInput, columnTextField, rowTextField, getMatrixValueCells()) != -1) {
                editInterfaceDependingOnInputValidation("This number is in the column already", false, textField);
            } else if (modelSudoku.sameNumberInSameRow(userInput, columnTextField, rowTextField, getMatrixValueCells()) != -1) {
                editInterfaceDependingOnInputValidation("This number is in the row already", false, textField);
            } else if (!modelSudoku.sameNumberInSameBlock(userInput, columnTextField, rowTextField, getMatrixValueCells()).isEmpty()) {
                editInterfaceDependingOnInputValidation("This number is in the block already", false, textField);
            } else {
                modelSudoku.setConfirmedStateOfCell(columnTextField,rowTextField,true);
                labelText.setText("");
            }
        }
        if (modelSudoku.isTheSudokuCompleted()) editInterfaceSudokuCompleted();
    }


    /**
     * Clears the text of every {@link TextField} in the 6×6 grid.
     *
     * <p>Called at the beginning of each new game to remove any values left
     * over from the previous session before the initial clues are rendered.</p>
     */
    private void cleanBoard() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                cells[row][col].setText("");
            }
        }
    }
    /**
     * Modifies the UI to give visual feedback to the player regarding
     * the validity of their latest input or a clue reveal.
     *
     * <p>If valid, the status label is updated with a positive color (green).
     * If invalid, the label changes to an error color (red) and the specific
     * text field receives a red background highlight indicating a conflict.</p>
     *
     * @param text         the feedback message to display to the user
     * @param isValidInput {@code true} if the placement is correct, {@code false} if there is a conflict
     * @param textField    the {@link TextField} that triggered the validation check
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
     * Retrieves the original CSS style string that was cached for a given
     * {@link TextField} when the controller was first initialized.
     *
     * <p>This allows dynamic highlight styles (e.g. error backgrounds) to be
     * safely removed and the field restored to its default FXML appearance
     * without hard-coding style values in the controller logic.</p>
     *
     * @param textField the {@link TextField} whose baseline style is requested
     * @return the original inline CSS style string for the field; an empty
     *         string if the field is not found in the {@link #cells} grid
     */
    private String getInitialStyleOfTextField(TextField textField){
        for(int i=0; i<6; i++){
            for(int j=0; j<6;j++){
                if(cells[i][j]==textField) return cellsStyle[i][j];
            }
        }
        return "";
    }

    /**
     * Transitions the application out of the active game state when the board
     * is successfully completed.
     *
     * <p>It sets a congratulatory status message, disables all text fields to
     * prevent further edits, swaps the visibility of the control buttons
     * (hides 'Clue', shows 'Play'), flags the game loop as finished, and
     * removes the real-time event listeners.</p>
     */
    private void editInterfaceSudokuCompleted() {
        labelText.setText("You Did it, The sudoku is completed");
        labelText.setStyle("-fx-text-fill: #278438;");
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                cells[row][col].setDisable(true);
            }
        }
        buttonClue.setVisible(false);
        buttonPlay.setVisible(true);
    }
    /**
     * Builds a 6×6 string matrix that mirrors the current text content of
     * every {@link TextField} in the UI grid.
     *
     * <p>Empty cells (those whose text is blank) are represented as {@code "0"}
     * so that the matrix can be passed directly to {@link SudokuGame} validation
     * methods, which use {@code "0"} as the sentinel for an unfilled position.</p>
     *
     * @return a new {@link ArrayList} of rows, each containing six string values
     *         representing the current player input ({@code "1"}–{@code "6"})
     *         or {@code "0"} for empty cells
     */
    private ArrayList<ArrayList<String>> getMatrixValueCells(){
        ArrayList<ArrayList<String>> matrixValueCells = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ArrayList<String> row = new ArrayList<>(6);
            for (int j = 0; j < 6; j++) {
                String valueCell=cells[i][j].getText();
                if(valueCell.isEmpty()) row.add("0");
                else row.add(valueCell);
            }
            matrixValueCells.add(row);
        }
        return matrixValueCells;
    }

    /**
     * Resolves the board coordinates of a given {@link TextField} by scanning
     * the internal {@link #cells} grid for a reference match.
     *
     * <p>Uses reference equality ({@link Object#equals}) to locate the field.
     * This is the canonical way to convert a UI event source back into
     * {@code [row, col]} indices consumable by the model layer.</p>
     *
     * @param textField the {@link TextField} whose grid position is needed
     * @return a two-element list {@code [row, col]} with zero-based indices;
     *         returns an empty list if the field is not found in the grid
     */
    private List<Integer> getCoordinatestextField(TextField textField) {
        List<Integer> coordinates = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (cells[row][col].equals(textField)) {
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
     * <p>Renders horizontal dividers between every pair of rows and a vertical
     * pipe ({@code |}) separator between every group of three columns, matching
     * the visual layout of the 2×3 sub-blocks used in this Sudoku variant.</p>
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