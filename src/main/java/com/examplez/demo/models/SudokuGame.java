package com.examplez.demo.models;

import com.examplez.demo.SudokuInitializable;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Game-logic model for a 6×6 Sudoku puzzle.
 *
 * <p>Responsibilities of this class:</p>
 * <ul>
 *   <li>Generate a fully solved, randomised 6×6 board using a backtracking
 *       algorithm.</li>
 *   <li>Select an initial set of cells to reveal as starting clues.</li>
 *   <li>Validate player moves against the standard Sudoku constraints
 *       (unique values per row, column, and 2×3 sub-block).</li>
 *   <li>Detect board completion.</li>
 *   <li>Provide utility methods consumed by
 *       {@link com.examplez.demo.controllers.MainMenuController}.</li>
 * </ul>
 *
 * <p>The grid follows the standard 6×6 Sudoku layout with 2-row × 3-column
 * sub-blocks (six blocks in total).</p>
 *
 *  @author jeronimo rojas imbachi
 *  @author Luis Felipe Velasco
 * @version 1.0
 * @see com.examplez.demo.controllers.MainMenuController
 */
public class SudokuGame implements SudokuInitializable {

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /**
     * Dimension of the board (number of rows and columns).
     * For a standard 6×6 variant this value is {@code 6}.
     */
    private final int size = 6;

    /**
     * Internal representation of the Sudoku board.
     * Outer list index → row; inner list index → column.
     * Cells are initialised to {@code 0} (empty) and filled by
     * {@link #initialize()}.
     */
    private ArrayList<ArrayList<Integer>> matrix;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new, empty {@code SudokuGame}.
     *
     * <p>Allocates a 6×6 {@link ArrayList} of {@link ArrayList}s and fills
     * every cell with {@code 0} to represent an unsolved board.</p>
     */
    public SudokuGame() {
        this.matrix = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<Integer> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add(0); // 0 represents an empty cell
            }
            this.matrix.add(row);
        }
    }

    // -----------------------------------------------------------------------
    // Board generation
    // -----------------------------------------------------------------------

    /**
     * Generates a fully solved, randomised Sudoku board in place.
     *
     * <p>Delegates to the recursive {@link #solve(int, int)} backtracking
     * method starting at cell (0, 0).  After this method returns the
     * {@link #matrix} is guaranteed to contain a valid, complete 6×6
     * solution.</p>
     */
    public void initialize() {
        solve(0, 0);
    }

    /**
     * Recursively fills the board using a randomised backtracking algorithm.
     *
     * <p>The algorithm tries each of the numbers 1–6 in a randomly shuffled
     * order for the current cell.  If a number satisfies all constraints
     * (row, column, and sub-block uniqueness), it is placed and the algorithm
     * advances to the next cell.  If no number fits, the method returns
     * {@code false} to trigger backtracking in the calling frame.</p>
     *
     * @param row the zero-based row index of the cell being processed
     * @param col the zero-based column index of the cell being processed
     * @return {@code true} if the board was successfully filled from this
     *         position onwards; {@code false} if no valid placement exists
     *         and backtracking is required
     */
    private boolean solve(int row, int col) {
        if (row == size) {
            return true; // All rows filled — solution found
        }
        if (col == size) {
            return solve(row + 1, 0); // Move to the next row
        }

        // Shuffle 1-6 so each generated board is unique
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Collections.shuffle(numbers);

        for (int num : numbers) {
            if (isValidMove(row, col, num)) {
                this.matrix.get(row).set(col, num);

                if (solve(row, col + 1)) {
                    return true;
                }

                // Backtrack: undo the placement
                this.matrix.get(row).set(col, 0);
            }
        }

        return false; // No valid number found — trigger backtracking
    }

    /**
     * Checks whether placing {@code num} at ({@code row}, {@code col}) is
     * legal according to the three Sudoku constraints.
     *
     * <ol>
     *   <li><b>Row uniqueness</b> – {@code num} must not already appear in
     *       row {@code row}.</li>
     *   <li><b>Column uniqueness</b> – {@code num} must not already appear
     *       in column {@code col}.</li>
     *   <li><b>Sub-block uniqueness</b> – {@code num} must not already appear
     *       in the 2×3 sub-block that contains ({@code row}, {@code col}).
     *       Sub-block boundaries are computed as
     *       {@code rowStart = (row/2)*2} and {@code colStart = (col/3)*3}.</li>
     * </ol>
     *
     * @param row the zero-based row index of the target cell
     * @param col the zero-based column index of the target cell
     * @param num the candidate number (1–6) to validate
     * @return {@code true} if placing {@code num} at the given position
     *         violates none of the three constraints; {@code false} otherwise
     */
    private boolean isValidMove(int row, int col, int num) {
        for (int i = 0; i < size; i++) {
            if (this.matrix.get(row).get(i) == num) return false; // Row conflict
            if (this.matrix.get(i).get(col) == num) return false; // Column conflict
        }

        // Sub-block check (2 rows × 3 columns)
        int boxRowStart = (row / 2) * 2;
        int boxColStart = (col / 3) * 3;

        for (int r = boxRowStart; r < boxRowStart + 2; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (this.matrix.get(r).get(c) == num) return false;
            }
        }

        return true;
    }

    // -----------------------------------------------------------------------
    // Clue selection
    // -----------------------------------------------------------------------

    /**
     * Produces a boolean mask that indicates which cells should be revealed
     * to the player as the initial clues.
     *
     * <p>The selection strategy guarantees exactly <strong>two revealed cells
     * per 2×3 sub-block</strong> by iterating over each sub-block and
     * randomly picking cells until the per-block count reaches two.
     * The resulting mask typically exposes 12 of 36 cells (one per block ×
     * 6 blocks × 2 per block).</p>
     *
     * @return a 6×6 boolean array; {@code true} at position
     *         {@code [row][col]} means that cell is a starting clue
     */
    public boolean[][] chooseCluesToShow() {
        boolean[][] show = new boolean[6][6];
        for (int RowStart = 0; RowStart < 6; RowStart += 2) {
            for (int ColStart = 0; ColStart < 6; ColStart += 3) {
                while (countSubBlock(show, RowStart, ColStart) != 2) {
                    int Row = RowStart + ThreadLocalRandom.current().nextInt(2);
                    int Col = ColStart + ThreadLocalRandom.current().nextInt(3);
                    show[Row][Col] = true;
                }
            }
        }
        return show;
    }


    /**
     * Counts the number of revealed cells within a specific 2×3 sub-block.
     *
     * @param show     the current clue-visibility mask
     * @param RowStart the zero-based row index of the top-left corner of the
     *                 sub-block
     * @param ColStart the zero-based column index of the top-left corner of
     *                 the sub-block
     * @return the number of cells in the sub-block that are marked as
     *         revealed ({@code true}) in {@code show}
     */
    private int countSubBlock(boolean[][] show, int RowStart, int ColStart) {
        int counter = 0;
        for (int i = RowStart; i < RowStart + 2; i++) {
            for (int j = ColStart; j < ColStart + 3; j++) {
                if (show[i][j]) {
                    counter++;
                }
            }
        }
        return counter;
    }

    // -----------------------------------------------------------------------
    // In-game helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the coordinates of the next cell that should be revealed as a
     * hint to the player.
     *
     * <p>Iterates the grid in row-major order and returns the first cell
     * whose {@link TextField} is currently empty.  The returned list always
     * contains exactly two elements: {@code [row, col]}.</p>
     *
     * @param blocks the 6×6 grid of {@link TextField} nodes representing the
     *               current UI state
     * @return a {@link List} with two elements {@code [row, col]} identifying
     *         the first empty cell; an empty list if no empty cell exists
     */
    public List<Integer> giveClue(TextField[][] blocks) {
        List<Integer> coordinates = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (Objects.equals(blocks[row][col].getText(), "")) {
                    coordinates.add(row);
                    coordinates.add(col);
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Determines whether the player is still eligible to request additional
     * clues.
     *
     * <p>Clues are forbidden once 35 of the 36 cells are already correctly
     * filled, ensuring the player must solve at least one cell themselves.</p>
     *
     * @param matrixBools the 6×6 state mask ({@code true} = cell is
     *                    finalised)
     * @return {@code true} if the player may still receive a clue;
     *         {@code false} if 35 or more cells are already filled
     */
    public boolean isPossibleGiveClue(boolean[][] matrixBools) {
        int numberCorrectBlocks = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (matrixBools[row][col]) {
                    numberCorrectBlocks++;
                }
            }
        }
        return numberCorrectBlocks != 35;
    }

    // -----------------------------------------------------------------------
    // Input validation
    // -----------------------------------------------------------------------

    /**
     * Validates that the given string represents a single digit between 1 and
     * 6 inclusive.
     *
     * @param user_input the string typed by the player into a cell
     * @return {@code true} if {@code user_input} matches exactly one character
     *         in the range {@code [1-6]}; {@code false} otherwise
     */
    public boolean isNumberOneToSix(String user_input) {
        return user_input.matches("[1-6]");
    }

    /**
     * Finds a conflicting cell in the same column as the cell being validated.
     *
     * <p>Scans every row in {@code column}, comparing the text of each
     * {@link TextField} to {@code user_input}, and skips the cell at
     * {@code row} (the cell being entered).</p>
     *
     * @param user_input the value being validated (a digit string 1–6)
     * @param column     the zero-based column index of the cell being validated
     * @param row        the zero-based row index of the cell being validated
     *                   (excluded from the scan)
     * @param blocks     the 6×6 grid of {@link TextField} nodes
     * @return the zero-based row index of the first conflicting cell in the
     *         column, or {@code -1} if no conflict is found
     */
    public int sameNumberInSameColumn(String user_input, int column, int row, TextField[][] blocks) {
        for (int i = 0; i <= 5; i++) {
            String value_block = blocks[i][column].getText();
            if (value_block.equals(user_input) && i != row) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds a conflicting cell in the same row as the cell being validated.
     *
     * <p>Scans every column in {@code row}, comparing the text of each
     * {@link TextField} to {@code user_input}, and skips the cell at
     * {@code column} (the cell being entered).</p>
     *
     * @param user_input the value being validated (a digit string 1–6)
     * @param column     the zero-based column index of the cell being validated
     *                   (excluded from the scan)
     * @param row        the zero-based row index of the cell being validated
     * @param blocks     the 6×6 grid of {@link TextField} nodes
     * @return the zero-based column index of the first conflicting cell in
     *         the row, or {@code -1} if no conflict is found
     */
    public int sameNumberInSameRow(String user_input, int column, int row, TextField[][] blocks) {
        for (int i = 0; i <= 5; i++) {
            String value_block = blocks[row][i].getText();
            if (value_block.equals(user_input) && i != column) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds a conflicting cell within the 2×3 sub-block that contains the
     * cell being validated.
     *
     * <p>The sub-block boundaries are derived as:
     * {@code startRow = (row/2)*2} and {@code startCol = (column/3)*3}.
     * The cell at ({@code row}, {@code column}) itself is excluded from the
     * scan.</p>
     *
     * @param user_input the value being validated (a digit string 1–6)
     * @param column     the zero-based column index of the cell being validated
     * @param row        the zero-based row index of the cell being validated
     * @param blocks     the 6×6 grid of {@link TextField} nodes
     * @return a {@link List} containing the {@code [row, col]} coordinates of
     *         the first conflicting cell in the sub-block; an empty list if no
     *         conflict is found
     */
    public List<Integer> sameNumberInSameBlock(String user_input, int column, int row, TextField[][] blocks) {
        int startRow = (row / 2) * 2;
        int startCol = (column / 3) * 3;
        List<Integer> coordinates = new ArrayList<>();
        for (int i = startRow; i < startRow + 2; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                String value_SubBlock = blocks[i][j].getText();
                if (value_SubBlock.equals(user_input) && i != row && j != column) {
                    coordinates.add(i);
                    coordinates.add(j);
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Checks whether the Sudoku puzzle has been fully and correctly solved.
     *
     * <p>The puzzle is considered complete when all 36 cells are marked as
     * finalised in {@code matrixBools} (i.e., every cell holds a confirmed
     * correct value).</p>
     *
     * @param matrixBools the 6×6 state mask ({@code true} = cell is
     *                    finalised / correctly filled)
     * @return {@code true} if all 36 cells are finalised; {@code false}
     *         otherwise
     */
    public boolean isTheSudokuCompleted(boolean[][] matrixBools) {
        int numberCorrectBlocks = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (matrixBools[row][col]) {
                    numberCorrectBlocks++;
                }
            }
        }
        return numberCorrectBlocks == 36;
    }

    /**
     * Resolves the grid coordinates of a given {@link TextField} within the
     * board.
     *
     * <p>Performs a linear search through {@code blocks} using reference
     * equality ({@link Objects#equals}) and returns the first match.</p>
     *
     * @param blocks    the 6×6 grid of {@link TextField} nodes
     * @param textField the {@link TextField} whose position is sought
     * @return a {@link List} containing exactly two elements:
     *         {@code [row, col]} of the matched field; an empty list if the
     *         field is not found in {@code blocks}
     */
    public List<Integer> getCoordinatestextField(TextField[][] blocks, TextField textField) {
        List<Integer> coordinates = new ArrayList<>();
        boolean find = false;
        outer:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (blocks[row][col].equals(textField)) {
                    coordinates.add(row);
                    coordinates.add(col);
                    break outer;
                }
            }
        }
        return coordinates;
    }

    // -----------------------------------------------------------------------
    // Accessor
    // -----------------------------------------------------------------------


    /**
     * Returns the value stored at a specific cell.
     *
     * @param row the zero-based row index
     * @param col the zero-based column index
     * @return the integer value at ({@code row}, {@code col}); {@code 0} if
     *         the cell is empty
     */
    public int getValue(int row, int col) {
        return matrix.get(row).get(col);
    }

    // -----------------------------------------------------------------------
    // Debug / console output
    // -----------------------------------------------------------------------

    /**
     * Prints the current board state to {@link System#out} with visual
     * dividers separating the 2×3 sub-blocks.
     *
     * <p>A horizontal line is printed between every pair of rows (every
     * 2 rows) and a {@code "| "} separator is printed between every group of
     * 3 columns, matching the standard 6×6 Sudoku grid layout.</p>
     */
    public void printBoard() {
        System.out.println("--- SUDOKU BOARD ---");
        for (int i = 0; i < size; i++) {
            if (i > 0 && i % 2 == 0) {
                System.out.println("---------------------");
            }
            for (int j = 0; j < size; j++) {
                if (j > 0 && j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print(this.matrix.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------\n");
    }

    /**
     * Prints a boolean visibility mask to {@link System#out} using the same
     * grid-divider layout as {@link #printBoard()}.
     *
     * <p>Useful for debugging clue selection: {@code true} values indicate
     * cells that have been revealed to the player.</p>
     *
     * @param stateCells the 6×6 boolean mask to display
     */
    public void printBoardBool(boolean[][] stateCells) {
        System.out.println("--- SUDOKU BOARD ---");
        for (int i = 0; i < size; i++) {
            if (i > 0 && i % 2 == 0) {
                System.out.println("---------------------");
            }
            for (int j = 0; j < size; j++) {
                if (j > 0 && j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print(stateCells[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------\n");
    }
}
