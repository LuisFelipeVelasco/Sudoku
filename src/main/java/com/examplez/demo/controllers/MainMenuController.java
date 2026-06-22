package com.examplez.demo.controllers;

import com.examplez.demo.SudokuInitializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;

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
    private TextField[][] blocks = new TextField[6][6];

    /**
     * Two-dimensional array that stores the initial CSS styles of each
     * FXML TextField node.
     * * <p>This prevents colour strings and dynamic highlights from accumulating
     * across successive validation checks and allows safe restoration of a
     * block's base appearance.</p>
     */
    private String[][] blocksStyle = new String[6][6];

    /** The game-logic model for the current session. */
    private SudokuGame modelSudoku;

    /**
     * Tracks which cells have been correctly filled or revealed.
     * {@code true} means the cell value is finalized or free of conflicts.
     */
    boolean[][] isBlockValid;

    /**
     * {@code true} while a game is in progress; prevents listener logic
     * from running before a game has started or after it is completed.
     */
    boolean newGame = false;

    // -----------------------------------------------------------------------
    // JavaFX lifecycle
    // -----------------------------------------------------------------------

    /**
     * Called automatically by the JavaFX FXML loader after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Populates the {@link #blocks} convenience array with references to
     * the injected {@link TextField} nodes so that the rest of the controller
     * can address cells by {@code [row][col]} index rather than by field
     * name. Also caches their default visual styling in {@link #blocksStyle}.</p>
     */
    public void initialize() {
        blocks[0][0]=T00; blocks[0][1]=T01; blocks[0][2]=T02;
        blocks[0][3]=T03; blocks[0][4]=T04; blocks[0][5]=T05;

        blocks[1][0]=T10; blocks[1][1]=T11; blocks[1][2]=T12;
        blocks[1][3]=T13; blocks[1][4]=T14; blocks[1][5]=T15;

        blocks[2][0]=T20; blocks[2][1]=T21; blocks[2][2]=T22;
        blocks[2][3]=T23; blocks[2][4]=T24; blocks[2][5]=T25;

        blocks[3][0]=T30; blocks[3][1]=T31; blocks[3][2]=T32;
        blocks[3][3]=T33; blocks[3][4]=T34; blocks[3][5]=T35;

        blocks[4][0]=T40; blocks[4][1]=T41; blocks[4][2]=T42;
        blocks[4][3]=T43; blocks[4][4]=T44; blocks[4][5]=T45;

        blocks[5][0]=T50; blocks[5][1]=T51; blocks[5][2]=T52;
        blocks[5][3]=T53; blocks[5][4]=T54; blocks[5][5]=T55;

        blocksStyle[0][0]=T00.getStyle(); blocksStyle[0][1]=T01.getStyle(); blocksStyle[0][2]=T02.getStyle();
        blocksStyle[0][3]=T03.getStyle(); blocksStyle[0][4]=T04.getStyle(); blocksStyle[0][5]=T05.getStyle();

        blocksStyle[1][0]=T10.getStyle(); blocksStyle[1][1]=T11.getStyle(); blocksStyle[1][2]=T12.getStyle();
        blocksStyle[1][3]=T13.getStyle(); blocksStyle[1][4]=T14.getStyle(); blocksStyle[1][5]=T15.getStyle();

        blocksStyle[2][0]=T20.getStyle(); blocksStyle[2][1]=T21.getStyle(); blocksStyle[2][2]=T22.getStyle();
        blocksStyle[2][3]=T23.getStyle(); blocksStyle[2][4]=T24.getStyle(); blocksStyle[2][5]=T25.getStyle();

        blocksStyle[3][0]=T30.getStyle(); blocksStyle[3][1]=T31.getStyle(); blocksStyle[3][2]=T32.getStyle();
        blocksStyle[3][3]=T33.getStyle(); blocksStyle[3][4]=T34.getStyle(); blocksStyle[3][5]=T35.getStyle();

        blocksStyle[4][0]=T40.getStyle(); blocksStyle[4][1]=T41.getStyle(); blocksStyle[4][2]=T42.getStyle();
        blocksStyle[4][3]=T43.getStyle(); blocksStyle[4][4]=T44.getStyle(); blocksStyle[4][5]=T45.getStyle();

        blocksStyle[5][0]=T50.getStyle(); blocksStyle[5][1]=T51.getStyle(); blocksStyle[5][2]=T52.getStyle();
        blocksStyle[5][3]=T53.getStyle(); blocksStyle[5][4]=T54.getStyle(); blocksStyle[5][5]=T55.getStyle();
    }

    // -----------------------------------------------------------------------
    // FXML event handlers
    // -----------------------------------------------------------------------

    /**
     * Handles the <em>Play</em> button click.
     *
     * <p>Generates a new fully solved board via {@link SudokuGame#initialize()},
     * selects which cells to reveal using {@link SudokuGame#chooseCluesToShow()},
     * clears the UI grid, renders the initial clues, registers text-change
     * listeners on every editable cell, and transitions the toolbar to
     * in-game state (shows the Clue button, hides the Play button).</p>
     */
    @FXML
    private void onButtonPlay() {
        modelSudoku = new SudokuGame();
        labelText.setText("Start playing");
        labelText.setStyle("-fx-text-fill: #185723;");
        modelSudoku.initialize();
        modelSudoku.printBoard();
        isBlockValid = modelSudoku.chooseCluesToShow();
        modelSudoku.printBoardBool(isBlockValid);
        cleanBoard();
        showBoard(isBlockValid);
        setListenerToTextFields();
        clueID.setVisible(true);
        playID.setVisible(false);
        newGame = true;
    }

    /**
     * Handles the <em>Clue</em> button click.
     *
     * <p>Requests the next hidden cell from {@link SudokuGame#giveClue(TextField[][])}
     * and reveals its correct value in the board. After revealing the clue,
     * checks whether the newly placed value conflicts with any existing entry
     * in the same row, column, or block via {@link #manageRepeatedInvalidBlocks}.</p>
     *
     * <p>If the maximum number of clues has already been reached
     * ({@link SudokuGame#isPossibleGiveClue(boolean[][])} returns {@code false}),
     * displays an informational message and takes no further action.</p>
     */
    @FXML
    private void onButtonClue() {
        if (modelSudoku.isPossibleGiveClue(isBlockValid)) {
            List<Integer> coordinates = modelSudoku.giveClue(blocks);
            int rowClue = coordinates.get(0);
            int columnClue = coordinates.get(1);
            isBlockValid[rowClue][columnClue] = true;
            showClue(rowClue, columnClue);
            String valueClue = blocks[rowClue][columnClue].getText();
            manageRepeatedInvalidBlocks(valueClue, columnClue, rowClue);

        } else {
            labelText.setText("You can't ask for more clues");
            labelText.setStyle("-fx-text-fill: #69261C;");
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
     * @param matrix a 6×6 boolean mask; {@code true} means the cell is
     * revealed as a starting clue
     */
    private void showBoard(boolean[][] matrix) {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (matrix[row][col]) {
                    blocks[row][col].setText(String.valueOf(modelSudoku.getValue(row, col)));
                    blocks[row][col].setDisable(true);
                } else {
                    blocks[row][col].setDisable(false);
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
        blocks[row][col].setText(String.valueOf(modelSudoku.getValue(row, col)));
        blocks[row][col].setDisable(true);
    }

    /**
     * Attaches a {@code textProperty} change listener to every editable
     * {@link TextField} in the board.
     * * <p>This ensures that {@link #verification(String, String, TextField)}
     * is executed whenever the cell's text changes, providing live validation feedback.</p>
     */
    private void setListenerToTextFields() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                TextField textField = blocks[row][col];
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    verification(newValue, oldValue, textField);
                });
            }
        }
    }

    /**
     * Detaches the {@code textProperty} change listener from every editable
     * {@link TextField} in the board.
     *
     * <p>Typically called when the board is completed to clean up resources
     * and avoid triggering redundant validation checks.</p>
     */
    private void removeListenerToTextFields() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                TextField textField = blocks[row][col];
                textField.textProperty().removeListener((observable, oldValue, newValue) -> {
                    verification(newValue, oldValue, textField);
                });
            }
        }
    }

    /**
     * Validates a player's input whenever the text of a {@link TextField}
     * changes and updates the UI accordingly.
     *
     * <p>Validation rules (applied in order):</p>
     * <ol>
     * <li>If the new value is empty, remove any error highlight from the
     * cell, mark it as unfilled in {@link #isBlockValid}, and check if
     * this deletion resolves errors in other blocks via
     * {@link #manageOldRepeatedInvalidBlocks}.</li>
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
        if (newGame) {

            List<Integer> coordinatesTextField = modelSudoku.getCoordinatestextField(blocks, textField);
            int rowTextField    = coordinatesTextField.get(0);
            int columnTextField = coordinatesTextField.get(1);

            if (userInput.equals("")) {
                labelText.setText("");
                isBlockValid[rowTextField][columnTextField] = false;
                textField.setStyle(blocksStyle[rowTextField][columnTextField]);
                manageOldRepeatedInvalidBlocks(oldUserInput, columnTextField, rowTextField, textField);
            }
            else if (!modelSudoku.isNumberOneToSix(userInput)) {
                labelText.setText("Type a number 1-6");
                labelText.setStyle("-fx-text-fill: #69261C;");
                textField.setText("");
            }
            else if (!isBlockValid[rowTextField][columnTextField]) {

                if (modelSudoku.sameNumberInSameColumn(userInput, columnTextField, rowTextField, blocks) != -1) {
                    editInterfaceDependingOnInputValidation("This number is in the column already", false, textField);
                } else if (modelSudoku.sameNumberInSameRow(userInput, columnTextField, rowTextField, blocks) != -1) {
                    editInterfaceDependingOnInputValidation("This number is in the row already", false, textField);
                } else if (!modelSudoku.sameNumberInSameBlock(userInput, columnTextField, rowTextField, blocks).isEmpty()) {
                    editInterfaceDependingOnInputValidation("This number is in the block already", false, textField);
                } else {
                    isBlockValid[rowTextField][columnTextField] = true;
                    labelText.setText("");
                }
            }
            if (modelSudoku.isTheSudokuCompleted(isBlockValid)) editInterfaceSudokuCompleted();
        }
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
                blocks[row][col].setText("");
            }
        }
    }

    /**
     * Detects and highlights player-entered blocks that become invalid
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
    private void manageRepeatedInvalidBlocks(String originalValue, int originalColumn, int originalRow) {
        int rowValueRepeated    = modelSudoku.sameNumberInSameColumn(originalValue, originalColumn, originalRow, blocks);
        int columnValueRepeated = modelSudoku.sameNumberInSameRow(originalValue, originalColumn, originalRow, blocks);
        List<Integer> blockValueRepeated = modelSudoku.sameNumberInSameBlock(originalValue, originalColumn, originalRow, blocks);

        labelText.setText("");

        if (columnValueRepeated != -1) {
            TextField textField = blocks[originalRow][columnValueRepeated];
            isBlockValid[originalRow][columnValueRepeated] = false;
            editInterfaceDependingOnInputValidation("same numbers in row", false, textField);
        }
        if (rowValueRepeated != -1) {
            TextField textField = blocks[rowValueRepeated][originalColumn];
            isBlockValid[rowValueRepeated][originalColumn] = false;
            editInterfaceDependingOnInputValidation("same numbers in column", false, textField);
        }
        if (!blockValueRepeated.isEmpty()) {
            rowValueRepeated = blockValueRepeated.get(0);
            columnValueRepeated = blockValueRepeated.get(1);
            TextField textField = blocks[rowValueRepeated][columnValueRepeated];
            isBlockValid[rowValueRepeated][columnValueRepeated] = false;
            editInterfaceDependingOnInputValidation("same numbers in block", false, textField);
        }
    }

    /**
     * Re-evaluates previously invalid blocks to determine if deleting an input
     * has resolved their conflict state.
     *
     * <p>When a player clears a cell (e.g., deletes a mistaken entry), this
     * method checks if any other blocks that were highlighted as errors in
     * the same row, column, or sub-block can now be deemed valid and restored
     * to their default styling.</p>
     *
     * @param oldInput       the string value that was just deleted by the player
     * @param oldInputColumn the zero-based column index of the deleted value
     * @param oldInputRow    the zero-based row index of the deleted value
     * @param textField      the {@link TextField} that was modified/cleared
     */
    private void manageOldRepeatedInvalidBlocks(String oldInput, int oldInputColumn, int oldInputRow, TextField textField) {
        int rowValueRepeated = modelSudoku.sameNumberInSameColumn(oldInput, oldInputColumn, oldInputRow, blocks);
        int columnValueRepeated = modelSudoku.sameNumberInSameRow(oldInput, oldInputColumn, oldInputRow, blocks);
        List<Integer> blockValueRepeated = modelSudoku.sameNumberInSameBlock(oldInput, oldInputColumn, oldInputRow, blocks);

        if (rowValueRepeated != -1 && !isBlockValid[rowValueRepeated][oldInputColumn]) {
            if (modelSudoku.isValidBlock(rowValueRepeated, oldInputColumn, blocks[rowValueRepeated][oldInputColumn].getText(), blocks)) {
                blocks[rowValueRepeated][oldInputColumn].setStyle(blocksStyle[rowValueRepeated][oldInputColumn]);
                isBlockValid[rowValueRepeated][oldInputColumn] = true;
                editInterfaceDependingOnInputValidation("old block is valid", true, textField);
            }
        }
        if (columnValueRepeated != -1 && !isBlockValid[oldInputRow][columnValueRepeated]) {
            if (modelSudoku.isValidBlock(oldInputRow, columnValueRepeated, blocks[oldInputRow][columnValueRepeated].getText(), blocks)) {
                blocks[oldInputRow][columnValueRepeated].setStyle(blocksStyle[oldInputRow][columnValueRepeated]);
                isBlockValid[oldInputRow][columnValueRepeated] = true;
                editInterfaceDependingOnInputValidation("old block is valid", true, textField);
            }
        }
        if (!blockValueRepeated.isEmpty()) {
            int rowValueBlockRepeated = blockValueRepeated.get(0);
            int columnValueBlockRepeated = blockValueRepeated.get(1);
            if (!isBlockValid[rowValueBlockRepeated][columnValueBlockRepeated] && modelSudoku.isValidBlock(rowValueBlockRepeated, columnValueBlockRepeated, blocks[rowValueBlockRepeated][columnValueBlockRepeated].getText(), blocks)) {
                blocks[rowValueBlockRepeated][columnValueBlockRepeated].setStyle(blocksStyle[rowValueBlockRepeated][columnValueBlockRepeated]);
                isBlockValid[rowValueBlockRepeated][columnValueBlockRepeated] = true;
                editInterfaceDependingOnInputValidation("old block is valid", true, textField);
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
            labelText.setStyle("-fx-text-fill: #185723;");
        } else {
            labelText.setText(text);
            labelText.setStyle("-fx-text-fill: #69261C;");
            textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
        }
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
        labelText.setStyle("-fx-text-fill: #185723;");
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                blocks[row][col].setDisable(true);
            }
        }
        clueID.setVisible(false);
        playID.setVisible(true);
        newGame = false;
        removeListenerToTextFields();
    }
}