package com.examplez.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

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
 *  @author jeronimo rojas imbachi
 *  @author Luis Felipe Velasco
 * @version 1.0
 * @see SudokuGame
 */
public class MainMenuController {

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
     * programmatic access.  Populated during {@link #initialize()}.
     */
    private TextField[][] blocks = new TextField[6][6];

    /**
     * TextFields whose background was altered to signal an error.
     * Parallel list with {@link #styleBlocksModified}: index {@code i} in
     * this list corresponds to the same index in that list.
     */
    ArrayList<TextField> blocksModified = new ArrayList<>();

    /**
     * Clean (pre-error) inline styles captured just before the error colour
     * was applied.  Used to restore the original style when the error is
     * resolved.
     *
     * @see #blocksModified
     */
    ArrayList<String> styleBlocksModified = new ArrayList<>();

    /**
     * Immutable base style for the status {@link #labelText}.
     * Prevents colour strings from accumulating across successive calls.
     */
    private static final String LABEL_BASE_STYLE = "";

    /** The game-logic model for the current session. */
    SudokuGame modelSudoku = new SudokuGame();

    /**
     * Tracks which cells have been correctly filled or revealed.
     * {@code true} means the cell value is finalised (disabled in the UI).
     */
    boolean[][] stateCells;

    /**
     * {@code true} while a game is in progress; prevents listener logic
     * from running before a game has started.
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
     * name.</p>
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
    }

    // -----------------------------------------------------------------------
    // FXML event handlers
    // -----------------------------------------------------------------------

    /**
     * Handles the <em>Play</em> button click.
     *
     * <p>Generates a new fully solved board via {@link SudokuGame#fillFullBoard()},
     * selects which cells to reveal using {@link SudokuGame#chooseCluesToShow()},
     * clears the UI grid, renders the initial clues, registers text-change
     * listeners on every editable cell, and transitions the toolbar to
     * in-game state (shows the Clue button, hides the Play button).</p>
     */
    @FXML
    protected void onButtonPlay() {
        labelText.setText("Start playing");
        labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #185723;");
        // Clear any leftover error highlights from a previous game
        blocksModified.clear();
        styleBlocksModified.clear();
        modelSudoku.fillFullBoard();
        modelSudoku.printBoard();
        stateCells = modelSudoku.chooseCluesToShow();
        modelSudoku.printBoardBool(stateCells);
        cleanBoard();
        showBoard(stateCells);
        setListenerToTextFields();
        clueID.setVisible(true);
        playID.setVisible(false);
        newGame = true;
    }

    /**
     * Handles the <em>Clue</em> button click.
     *
     * <p>Requests the next hidden cell from {@link SudokuGame#giveClue(TextField[][])}
     * and reveals its correct value in the board.  After revealing the clue,
     * checks whether the newly placed value conflicts with any existing entry
     * in the same row or column and highlights any offending cell in red.</p>
     *
     * <p>If the maximum number of clues has already been reached
     * ({@link SudokuGame#isPossibleGiveClue(boolean[][])} returns {@code false}),
     * displays an informational message and takes no further action.</p>
     */
    @FXML
    protected void onButtonClue() {
        List<Integer> coordinates = modelSudoku.giveClue(blocks);
        if (modelSudoku.isPossibleGiveClue(stateCells) && !coordinates.isEmpty()) {
            int rowClue    = coordinates.get(0);
            int columnClue = coordinates.get(1);
            stateCells[rowClue][columnClue] = true;
            showClue(rowClue, columnClue);
            String valueClue = blocks[rowClue][columnClue].getText();
            int rowValueRepeated    = modelSudoku.sameNumberInSameColumn(valueClue, columnClue, rowClue, blocks);
            int columnValueRepeated = modelSudoku.sameNumberInSameRow(valueClue, columnClue, rowClue, blocks);
            labelText.setText("");
            labelText.setStyle(LABEL_BASE_STYLE);
            if (columnValueRepeated != -1) {
                TextField textField = blocks[rowClue][columnValueRepeated];
                stateCells[rowClue][columnValueRepeated] = false;
                blocksModified.add(textField);
                styleBlocksModified.add(textField.getStyle());
                labelText.setText("same numbers in row");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
            } else if (rowValueRepeated != -1) {
                TextField textField = blocks[rowValueRepeated][columnClue];
                stateCells[rowValueRepeated][columnClue] = false;
                blocksModified.add(textField);
                styleBlocksModified.add(textField.getStyle());
                labelText.setText("same numbers in column");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
            }
        } else {
            labelText.setText("You can´t ask for more clues");
            labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
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
     * the player cannot modify it.  All other cells are left blank and
     * enabled.</p>
     *
     * @param matrix a 6×6 boolean mask; {@code true} means the cell is
     *               revealed as a starting clue
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
     *
     * <p>Each listener delegates to {@link #verification(String, String, TextField)}
     * whenever the cell's text changes, providing live validation feedback.</p>
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
     * Validates a player's input whenever the text of a {@link TextField}
     * changes and updates the UI accordingly.
     *
     * <p>Validation rules (applied in order):</p>
     * <ol>
     *   <li>If the new value is empty, remove any error highlight from the
     *       cell and mark it as unfilled in {@link #stateCells}.</li>
     *   <li>If the value is not a digit 1–6, show an error message and reset
     *       the field to empty.</li>
     *   <li>If the digit already appears in the same column, row, or 2×3
     *       sub-block, highlight the cell in red and show a descriptive
     *       message.</li>
     *   <li>Otherwise, accept the value and check whether the board is now
     *       fully completed.</li>
     * </ol>
     *
     * <p>When the board is completed, all cells are disabled, the Clue button
     * is hidden, and the Play button is shown again so the user can start a
     * new game.</p>
     *
     * @param user_input the new text entered by the player
     * @param old_input  the previous text of the field (before the change)
     * @param textField  the {@link TextField} whose value changed
     */
    private void verification(String user_input, String old_input, TextField textField) {
        if (newGame) {
            List<Integer> coordinatesTextField = modelSudoku.getCoordinatestextField(blocks, textField);
            int rowtextField    = coordinatesTextField.get(0);
            int columntextField = coordinatesTextField.get(1);

            if (user_input.equals("")) {
                labelText.setText("");
                labelText.setStyle(LABEL_BASE_STYLE);
                for (int i = blocksModified.size() - 1; i >= 0; i--) {
                    if (blocksModified.get(i) == textField) {
                        blocksModified.get(i).setStyle(styleBlocksModified.get(i));
                        blocksModified.remove(i);
                        styleBlocksModified.remove(i);
                    }
                }
                stateCells[rowtextField][columntextField] = false;
            } else if (!modelSudoku.isNumberOneToSix(user_input)) {
                labelText.setText("Type a number 1-6");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                textField.setText("");
            } else if (!stateCells[rowtextField][columntextField]) {

                if (modelSudoku.sameNumberInSameColumn(user_input, columntextField, rowtextField, blocks) != -1) {
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the  column already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
                } else if (modelSudoku.sameNumberInSameRow(user_input, columntextField, rowtextField, blocks) != -1) {
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the  row already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color:#69261C;");
                } else if (!modelSudoku.sameNumberInSameBlock(user_input, columntextField, rowtextField, blocks).isEmpty()) {
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the block already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color:#69261C;");
                } else {
                    stateCells[rowtextField][columntextField] = true;
                    labelText.setText("");
                    labelText.setStyle(LABEL_BASE_STYLE);
                }
            }

            if (modelSudoku.isTheSudokuCompleted(stateCells)) {
                modelSudoku = new SudokuGame();
                labelText.setText("You Did it , The sudoku is completed");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #185723;");
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 6; col++) {
                        blocks[row][col].setDisable(true);
                    }
                }
                clueID.setVisible(false);
                playID.setVisible(true);
                newGame = false;
            }
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
}
