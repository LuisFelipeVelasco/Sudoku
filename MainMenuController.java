package com.examplez.demo.controllers;

import com.examplez.demo.SudokuInitializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;

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
    // FXML-injected TextField grid (rows 0-5, columns 0-5)
    // -----------------------------------------------------------------------

    /** Row 0, column 0. */ @FXML private TextField T00;
    /** Row 0, column 1. */ @FXML private TextField T01;
    /** Row 0, column 2. */ @FXML private TextField T02;
    /** Row 0, column 3. */ @FXML private TextField T03;
    /** Row 0, column 4. */ @FXML private TextField T04;
    /** Row 0, column 5. */ @FXML private TextField T05;

    /** Row 1, column 0. */ @FXML private TextField T10;
    /** Row 1, column 1. */ @FXML private TextField T11;
    /** Row 1, column 2. */ @FXML private TextField T12;
    /** Row 1, column 3. */ @FXML private TextField T13;
    /** Row 1, column 4. */ @FXML private TextField T14;
    /** Row 1, column 5. */ @FXML private TextField T15;

    /** Row 2, column 0. */ @FXML private TextField T20;
    /** Row 2, column 1. */ @FXML private TextField T21;
    /** Row 2, column 2. */ @FXML private TextField T22;
    /** Row 2, column 3. */ @FXML private TextField T23;
    /** Row 2, column 4. */ @FXML private TextField T24;
    /** Row 2, column 5. */ @FXML private TextField T25;

    /** Row 3, column 0. */ @FXML private TextField T30;
    /** Row 3, column 1. */ @FXML private TextField T31;
    /** Row 3, column 2. */ @FXML private TextField T32;
    /** Row 3, column 3. */ @FXML private TextField T33;
    /** Row 3, column 4. */ @FXML private TextField T34;
    /** Row 3, column 5. */ @FXML private TextField T35;

    /** Row 4, column 0. */ @FXML private TextField T40;
    /** Row 4, column 1. */ @FXML private TextField T41;
    /** Row 4, column 2. */ @FXML private TextField T42;
    /** Row 4, column 3. */ @FXML private TextField T43;
    /** Row 4, column 4. */ @FXML private TextField T44;
    /** Row 4, column 5. */ @FXML private TextField T45;

    /** Row 5, column 0. */ @FXML private TextField T50;
    /** Row 5, column 1. */ @FXML private TextField T51;
    /** Row 5, column 2. */ @FXML private TextField T52;
    /** Row 5, column 3. */ @FXML private TextField T53;
    /** Row 5, column 4. */ @FXML private TextField T54;
    /** Row 5, column 5. */ @FXML private TextField T55;

    // -----------------------------------------------------------------------
    // FXML-injected controls
    // -----------------------------------------------------------------------

    /** Button that requests a clue from the model. Visible only during a game. */
    @FXML private Button clueID;

    /** Button that starts a new game. Visible only on the start/end screen. */
    @FXML private Button playID;

    /** Status label shown below the board (feedback messages to the player). */
    @FXML private Label labelText;

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
     * <p>Populates the {@link #cells} convenience array with references to
     * the injected {@link TextField} nodes so that the rest of the controller
     * can address cells by {@code [row][col]} index rather than by field
     * name. Also caches their default visual styling in {@link #cellsStyle}.</p>
     */
    public void initialize() {
        cells[0][0]=T00; cells[0][1]=T01; cells[0][2]=T02;
        cells[0][3]=T03; cells[0][4]=T04; cells[0][5]=T05;

        cells[1][0]=T10; cells[1][1]=T11; cells[1][2]=T12;
        cells[1][3]=T13; cells[1][4]=T14; cells[1][5]=T15;

        cells[2][0]=T20; cells[2][1]=T21; cells[2][2]=T22;
        cells[2][3]=T23; cells[2][4]=T24; cells[2][5]=T25;

        cells[3][0]=T30; cells[3][1]=T31; cells[3][2]=T32;
        cells[3][3]=T33; cells[3][4]=T34; cells[3][5]=T35;

        cells[4][0]=T40; cells[4][1]=T41; cells[4][2]=T42;
        cells[4][3]=T43; cells[4][4]=T44; cells[4][5]=T45;

        cells[5][0]=T50; cells[5][1]=T51; cells[5][2]=T52;
        cells[5][3]=T53; cells[5][4]=T54; cells[5][5]=T55;

        cellsStyle[0][0]=T00.getStyle(); cellsStyle[0][1]=T01.getStyle(); cellsStyle[0][2]=T02.getStyle();
        cellsStyle[0][3]=T03.getStyle(); cellsStyle[0][4]=T04.getStyle(); cellsStyle[0][5]=T05.getStyle();

        cellsStyle[1][0]=T10.getStyle(); cellsStyle[1][1]=T11.getStyle(); cellsStyle[1][2]=T12.getStyle();
        cellsStyle[1][3]=T13.getStyle(); cellsStyle[1][4]=T14.getStyle(); cellsStyle[1][5]=T15.getStyle();

        cellsStyle[2][0]=T20.getStyle(); cellsStyle[2][1]=T21.getStyle(); cellsStyle[2][2]=T22.getStyle();
        cellsStyle[2][3]=T23.getStyle(); cellsStyle[2][4]=T24.getStyle(); cellsStyle[2][5]=T25.getStyle();

        cellsStyle[3][0]=T30.getStyle(); cellsStyle[3][1]=T31.getStyle(); cellsStyle[3][2]=T32.getStyle();
        cellsStyle[3][3]=T33.getStyle(); cellsStyle[3][4]=T34.getStyle(); cellsStyle[3][5]=T35.getStyle();

        cellsStyle[4][0]=T40.getStyle(); cellsStyle[4][1]=T41.getStyle(); cellsStyle[4][2]=T42.getStyle();
        cellsStyle[4][3]=T43.getStyle(); cellsStyle[4][4]=T44.getStyle(); cellsStyle[4][5]=T45.getStyle();

        cellsStyle[5][0]=T50.getStyle(); cellsStyle[5][1]=T51.getStyle(); cellsStyle[5][2]=T52.getStyle();
        cellsStyle[5][3]=T53.getStyle(); cellsStyle[5][4]=T54.getStyle(); cellsStyle[5][5]=T55.getStyle();
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
        modelSudoku.printBoard();
        showBoard(modelSudoku.getConfirmedCells());
        setListenerToTextFields();
        clueID.setVisible(true);
        playID.setVisible(false);
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
            modelSudoku.printBoardBool();
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
     * Detaches the {@code textProperty} change listener from every editable
     * {@link TextField} in the board.
     *
     * <p>Typically called when the board is completed to clean up resources
     * and avoid triggering redundant validation checks.</p>
     */


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
            printBoard(getMatrixValueCells());
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
        modelSudoku.printBoardBool();
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
     * Detects and highlights player-entered cells that become invalid
     * due to a newly revealed clue.
     *
     * <p>When a clue is requested, its correct value is forced onto the board.
     * This method scans the row, column, and 2x3 sub-block to see if the player
     * had previously guessed the same number incorrectly, dynamically turning
     * those conflicting entries to an invalid state.</p>
     *
     * @param originalValue  the correct value of the newly revealed clue
     * @param originalColumn the zero-based column index of the clue
     * @param originalRow    the zero-based row index of the clue
     */

    /**
     * Re-evaluates previously invalid cells to determine if deleting an input
     * has resolved their conflict state.
     *
     * <p>When a player clears a cell (e.g., deletes a mistaken entry), this
     * method checks if any other cells that were highlighted as errors in
     * the same row, column, or sub-block can now be deemed valid and restored
     * to their default styling.</p>
     *
     * @param oldInput       the string value that was just deleted by the player
     * @param oldInputColumn the zero-based column index of the deleted value
     * @param oldInputRow    the zero-based row index of the deleted value
     * @param textField      the {@link TextField} that was modified/cleared
     */


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
        clueID.setVisible(false);
        playID.setVisible(true);
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
    public List<Integer> getCoordinatestextField(TextField textField) {
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
    public void printBoard(ArrayList<ArrayList<String>> matrix) {
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