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
 *   <li>Generate a fully solved, randomized 6×6 board using a recursive
 *       backtracking algorithm.</li>
 *   <li>Select a distributed set of initial cells to reveal as starting clues.</li>
 *   <li>Validate player real-time entries against standard Sudoku constraints
 *       (unique values per row, column, and 2×3 sub-block).</li>
 *   <li>Evaluate board completion based on the confirmed-cell state mask.</li>
 *   <li>Provide utility and coordinate-lookup methods consumed by the
 *       presentation layer.</li>
 * </ul>
 *
 * <p>The grid uses a standard 6×6 Sudoku layout divided into six 2-row × 3-column
 * sub-blocks.</p>
 *
 * @author jeronimo rojas imbachi
 * @author Luis Felipe Velasco
 * @version 1.1
 */
public class SudokuGame implements SudokuInitializable {

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /**
     * Dimension of the square board (number of rows and columns).
     * For this Sudoku variant the value is strictly fixed to {@code 6}.
     */
    private final int size = 6;

    /**
     * The complete, valid solution board produced by {@link #solve(int, int)}.
     * Outer list index maps to rows; inner list index maps to columns.
     * Every cell is pre-filled with {@code "0"} in the constructor and
     * replaced with a digit string ({@code "1"}–{@code "6"}) during
     * {@link #initialize()}.
     */
    private ArrayList<ArrayList<String>> matrixSudokuSolve;

    /**
     * Tracks which cells have been correctly filled or revealed as clues.
     * {@code true} means the cell value is finalized (conflict-free and
     * confirmed by the model or exposed as a starting hint);
     * {@code false} means the cell is still editable or has an unresolved
     * conflict.
     */
    private ArrayList<ArrayList<Boolean>> confirmedCells;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a new {@code SudokuGame} instance with an empty, unsolved grid.
     *
     * <p>Allocates two 6×6 nested {@link ArrayList} structures:
     * {@link #matrixSudokuSolve}, pre-filled with {@code "0"} to represent
     * empty cells, and {@link #confirmedCells}, pre-filled with {@code false}
     * to mark every cell as unconfirmed.</p>
     */
    public SudokuGame() {
        this.matrixSudokuSolve = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<String> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add("0"); // "0" represents an empty cell
            }
            this.matrixSudokuSolve.add(row);
        }

        this.confirmedCells = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<Boolean> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add(false); // false = cell not yet confirmed
            }
            this.confirmedCells.add(row);
        }
    }

    // -----------------------------------------------------------------------
    // Board generation
    // -----------------------------------------------------------------------

    /**
     * Generates a complete, structurally valid, and randomized Sudoku board,
     * then selects the initial clues to expose to the player.
     *
     * <p>Delegates board generation to the recursive backtracking routine
     * {@link #solve(int, int)} starting at position (0, 0). After
     * {@code solve} returns, {@link #matrixSudokuSolve} is guaranteed to hold
     * a fully filled legal solution. {@link #chooseCluesToShow()} is then
     * called to populate {@link #confirmedCells} with the starting hints.</p>
     */
    public void initialize() {
        solve(0, 0);
        chooseCluesToShow();
    }

    /**
     * Recursively fills the board using a randomized backtracking algorithm.
     *
     * <p>At each cell {@code (row, col)}, the digits 1–6 are shuffled to
     * ensure every generated board is unique. Each candidate is tested against
     * row, column, and 2×3 sub-block constraints via
     * {@link #isValidCell(int, int, String, ArrayList)}. If a candidate is
     * valid it is placed and the algorithm advances to the next cell; if no
     * candidate works, the cell is reset to {@code "0"} and the method
     * returns {@code false} to trigger backtracking in the caller.</p>
     *
     * @param row zero-based row index of the cell currently being filled
     * @param col zero-based column index of the cell currently being filled
     * @return {@code true} if a valid solution was found from this cell
     *         onward; {@code false} if no valid placement exists and
     *         backtracking is required
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
            if (isValidCell(row, col, String.valueOf(num), this.matrixSudokuSolve)) {
                this.matrixSudokuSolve.get(row).set(col, String.valueOf(num));

                if (solve(row, col + 1)) {
                    return true;
                }

                // Backtrack: undo the placement
                this.matrixSudokuSolve.get(row).set(col, "0");
            }
        }

        return false; // No valid number found — trigger backtracking
    }

    // -----------------------------------------------------------------------
    // Clue selection
    // -----------------------------------------------------------------------

    /**
     * Selects exactly two cells per 2×3 sub-block to expose as starting clues
     * and marks them as confirmed in {@link #confirmedCells}.
     *
     * <p>Iterates over each of the six sub-blocks (identified by their
     * top-left corner at {@code (rowStart, colStart)}). Within each block,
     * random coordinates are drawn until the block contains exactly two
     * {@code true} entries in {@link #confirmedCells}. This guarantees a
     * total of 12 revealed clues uniformly distributed across the board.</p>
     */
    private void chooseCluesToShow() {
        for (int RowStart = 0; RowStart < 6; RowStart += 2) {
            for (int ColStart = 0; ColStart < 6; ColStart += 3) {
                while (countSubBlock(this.confirmedCells, RowStart, ColStart) != 2) {
                    int Row = RowStart + ThreadLocalRandom.current().nextInt(2);
                    int Col = ColStart + ThreadLocalRandom.current().nextInt(3);
                    this.confirmedCells.get(Row).set(Col, true);
                }
            }
        }
    }

    /**
     * Returns the full 6×6 confirmed-cells mask for the current game session.
     *
     * <p>Each element is {@code true} when the corresponding cell holds a
     * finalized or initially revealed value, and {@code false} while the cell
     * is still editable or empty.</p>
     *
     * @return the internal {@link ArrayList} of boolean rows representing the
     *         confirmed state of every board position
     */
    public ArrayList<ArrayList<Boolean>> getConfirmedCells() {
        return this.confirmedCells;
    }

    /**
     * Updates the confirmed state of a single cell in the tracking mask.
     *
     * <p>Setting a cell to {@code true} marks it as finalized (correctly
     * placed or revealed as a clue). Setting it to {@code false} marks it as
     * unresolved or conflicting.</p>
     *
     * @param column         zero-based column index of the target cell
     * @param row            zero-based row index of the target cell
     * @param confirmedState {@code true} to finalize the cell; {@code false}
     *                       to revert it to an unresolved state
     */
    public void setConfirmedStateOfCell(int column, int row, boolean confirmedState) {
        confirmedCells.get(row).set(column, confirmedState);
    }

    /**
     * Returns the confirmed state of a single cell.
     *
     * @param column zero-based column index of the target cell
     * @param row    zero-based row index of the target cell
     * @return {@code true} if the cell is finalized or was revealed as a clue;
     *         {@code false} if it is still editable or unresolved
     */
    public boolean getConfirmedStateOfCell(int column, int row) {
        return confirmedCells.get(row).get(column);
    }

    /**
     * Checks whether placing {@code num} at {@code (row, col)} satisfies all
     * three Sudoku constraints against the given board matrix.
     *
     * <p>Constraints evaluated:</p>
     * <ol>
     *   <li><b>Row uniqueness:</b> {@code num} must not already appear in any
     *       other cell of the same row.</li>
     *   <li><b>Column uniqueness:</b> {@code num} must not already appear in
     *       any other cell of the same column.</li>
     *   <li><b>Sub-block uniqueness:</b> {@code num} must not already appear
     *       in the 2×3 sub-block whose top-left corner is at
     *       {@code ((row/2)*2, (col/3)*3)}.</li>
     * </ol>
     *
     * <p>The cell at {@code (row, col)} itself is excluded from all three
     * checks so the method can be used both during board generation and
     * during live validation of player entries.</p>
     *
     * @param row  zero-based row index of the cell being evaluated
     * @param col  zero-based column index of the cell being evaluated
     * @param num  candidate digit string ({@code "1"}–{@code "6"}) to test
     * @param cell the 6×6 string matrix to validate against (either the
     *             solution board or the current UI state)
     * @return {@code true} if {@code num} satisfies all constraints;
     *         {@code false} if any constraint is violated
     */
    private boolean isValidCell(int row, int col, String num, ArrayList<ArrayList<String>> cell) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(cell.get(row).get(i), num) && i != col) {
                return false;
            } // Row conflict
            if (Objects.equals(cell.get(i).get(col), num) && i != row) {
                return false;
            } // Column conflict
        }

        // Sub-block check (2 rows × 3 columns)
        int boxRowStart = (row / 2) * 2;
        int boxColStart = (col / 3) * 3;

        for (int r = boxRowStart; r < boxRowStart + 2; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (Objects.equals(cell.get(r).get(c), num) && (r != row || c != col)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Identifies cells that conflict with a newly placed value and marks them
     * as unconfirmed.
     *
     * <p>Checks the same row, column, and 2×3 sub-block for occurrences of
     * {@code value} at positions other than the origin {@code (row, column)}.
     * For each conflicting cell found, its confirmed state is set to
     * {@code false} so the UI can highlight it as invalid.</p>
     *
     * @param value  the digit string ({@code "1"}–{@code "6"}) just placed
     * @param column zero-based column index of the newly placed value
     * @param row    zero-based row index of the newly placed value
     * @param cells  the current 6×6 string value matrix of the board
     * @return a list of {@code [row, col]} coordinate pairs for every cell
     *         that now conflicts with the placed digit; empty if no conflicts
     *         exist
     */
    public List<List<Integer>> getCoordinatesRepeatedInvalidCells(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        int rowValueRepeated    = sameNumberInSameColumn(value, column, row, cells);
        int columnValueRepeated = sameNumberInSameRow(value, column, row, cells);
        List<Integer> blockValueRepeated = sameNumberInSameBlock(value, column, row, cells);
        List<List<Integer>> repeatedInvalidCells = new ArrayList<>(List.of());

        if (columnValueRepeated != -1) {
            setConfirmedStateOfCell(columnValueRepeated, row, false);
            repeatedInvalidCells.add(List.of(row, columnValueRepeated));
        }
        if (rowValueRepeated != -1) {
            setConfirmedStateOfCell(column, rowValueRepeated, false);
            repeatedInvalidCells.add(List.of(rowValueRepeated, column));
        }
        if (!blockValueRepeated.isEmpty()) {
            rowValueRepeated    = blockValueRepeated.get(0);
            columnValueRepeated = blockValueRepeated.get(1);
            setConfirmedStateOfCell(columnValueRepeated, rowValueRepeated, false);
            repeatedInvalidCells.add(List.of(rowValueRepeated, columnValueRepeated));
        }
        return repeatedInvalidCells;
    }

    /**
     * Identifies previously conflicting cells that become valid after a value
     * is removed from the board, and marks them as confirmed.
     *
     * <p>When a player deletes an entry, neighbours sharing the same row,
     * column, or sub-block may no longer have a conflict. This method
     * re-evaluates those neighbours: if a neighbour's current value now
     * satisfies all Sudoku constraints and is not already confirmed, its
     * confirmed state is updated to {@code true}.</p>
     *
     * @param value  the digit string ({@code "1"}–{@code "6"}) just removed
     * @param column zero-based column index of the removed value
     * @param row    zero-based row index of the removed value
     * @param cells  the current 6×6 string value matrix of the board
     *               (the cell at {@code [row][column]} should already reflect
     *               the deletion, i.e. contain {@code "0"})
     * @return a list of {@code [row, col]} coordinate pairs for every
     *         neighbour cell that transitioned to a valid state; empty if
     *         none changed
     */
    public List<List<Integer>> getRepeatedValidCells(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        int rowValueRepeated    = sameNumberInSameColumn(value, column, row, cells);
        int columnValueRepeated = sameNumberInSameRow(value, column, row, cells);
        List<Integer> blockValueRepeated = sameNumberInSameBlock(value, column, row, cells);
        List<List<Integer>> repeatedValidCells = new ArrayList<>(List.of());

        if (rowValueRepeated != -1) {
            if (isValidCell(rowValueRepeated, column, value, cells)) {
                setConfirmedStateOfCell(column, rowValueRepeated, true);
                repeatedValidCells.add(List.of(rowValueRepeated, column));
            }
        }
        if (columnValueRepeated != -1) {
            if (isValidCell(row, columnValueRepeated, value, cells)) {
                setConfirmedStateOfCell(columnValueRepeated, row, true);
                repeatedValidCells.add(List.of(row, columnValueRepeated));
            }
        }
        if (!blockValueRepeated.isEmpty()) {
            int rowValueBlockRepeated    = blockValueRepeated.get(0);
            int columnValueBlockRepeated = blockValueRepeated.get(1);
            if (isValidCell(rowValueBlockRepeated, columnValueBlockRepeated, value, cells)) {
                setConfirmedStateOfCell(columnValueBlockRepeated, rowValueBlockRepeated, true);
                repeatedValidCells.add(List.of(rowValueBlockRepeated, columnValueBlockRepeated));
            }
        }
        return repeatedValidCells;
    }

    /**
     * Counts the number of confirmed ({@code true}) cells within a single
     * 2×3 sub-block of the given boolean mask.
     *
     * @param matrix   the 6×6 confirmed-cells mask to inspect
     * @param rowStart zero-based row index of the top-left corner of the sub-block
     * @param colStart zero-based column index of the top-left corner of the sub-block
     * @return the number of cells flagged as {@code true} inside the sub-block
     */
    private int countSubBlock(ArrayList<ArrayList<Boolean>> matrix, int rowStart, int colStart) {
        int counter = 0;
        for (int i = rowStart; i < rowStart + 2; i++) {
            for (int j = colStart; j < colStart + 3; j++) {
                if (matrix.get(i).get(j)) {
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
     * Returns the coordinates of the first empty cell on the board in
     * row-major order, to be used as the next clue position.
     *
     * <p>Scans the board left-to-right, top-to-bottom and returns the
     * {@code [row, col]} indices of the first cell whose value equals
     * {@code "0"}.</p>
     *
     * @param cells the current 6×6 string value matrix of the board,
     *              where {@code "0"} represents an unfilled cell
     * @return a two-element list {@code [row, col]} identifying the first
     *         empty cell; an empty list if every cell is already filled
     */
    public List<Integer> giveClue(ArrayList<ArrayList<String>> cells) {
        List<Integer> coordinates = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (Objects.equals(cells.get(row).get(col), "0")) {
                    coordinates.add(row);
                    coordinates.add(col);
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Determines whether another clue may be dispensed to the player.
     *
     * <p>Two conditions independently block further clues:</p>
     * <ul>
     *   <li>35 or more cells are already confirmed, forcing the player to
     *       place the last value manually.</li>
     *   <li>All 36 cells are filled (no empty cell exists to reveal).</li>
     * </ul>
     *
     * @param cells the current 6×6 string value matrix of the board
     * @return {@code true} if a clue can still be given; {@code false} if
     *         either blocking condition is met
     */
    public boolean isPossibleGiveClue(ArrayList<ArrayList<String>> cells) {
        int numberCorrectCells = 0;
        int numberFilledCells  = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (confirmedCells.get(row).get(col)) numberCorrectCells++;
                if (!Objects.equals(cells.get(row).get(col), "0")) numberFilledCells++;
            }
        }
        return numberCorrectCells != 35 && numberFilledCells != 36;
    }

    // -----------------------------------------------------------------------
    // Input validation
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code value} is a single character in the
     * range {@code "1"}–{@code "6"}, and {@code false} otherwise.
     *
     * @param value the string entered by the player
     * @return {@code true} if {@code value} matches the regex {@code [1-6]};
     *         {@code false} for any other input (empty, multi-character, or
     *         out-of-range)
     */
    public boolean isNumberOneToSix(String value) {
        return value.matches("[1-6]");
    }

    /**
     * Searches the given column for another cell that already contains
     * {@code value}, excluding the origin row.
     *
     * @param value  the digit string to search for
     * @param column zero-based index of the column to scan
     * @param row    zero-based row index of the origin cell, excluded from
     *               the search
     * @param cells  the current 6×6 string value matrix of the board
     * @return the zero-based row index of the conflicting cell, or {@code -1}
     *         if no duplicate exists in the column
     */
    public int sameNumberInSameColumn(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        for (int i = 0; i <= 5; i++) {
            String valueBlock = cells.get(i).get(column);
            if (valueBlock.equals(value) && i != row) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the given row for another cell that already contains
     * {@code value}, excluding the origin column.
     *
     * @param value  the digit string to search for
     * @param column zero-based column index of the origin cell, excluded from
     *               the search
     * @param row    zero-based index of the row to scan
     * @param cells  the current 6×6 string value matrix of the board
     * @return the zero-based column index of the conflicting cell, or
     *         {@code -1} if no duplicate exists in the row
     */
    public int sameNumberInSameRow(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        for (int i = 0; i <= 5; i++) {
            String valueBlock = cells.get(row).get(i);
            if (valueBlock.equals(value) && i != column) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the 2×3 sub-block containing {@code (row, column)} for another
     * cell that already contains {@code value}, excluding the origin cell.
     *
     * @param value  the digit string to search for
     * @param column zero-based column index of the origin cell
     * @param row    zero-based row index of the origin cell
     * @param cells  the current 6×6 string value matrix of the board
     * @return a two-element list {@code [row, col]} identifying the
     *         conflicting cell inside the sub-block; an empty list if no
     *         duplicate exists
     */
    public List<Integer> sameNumberInSameBlock(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        int startRow = (row / 2) * 2;
        int startCol = (column / 3) * 3;
        List<Integer> coordinates = new ArrayList<>();
        for (int i = startRow; i < startRow + 2; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                String valueSubBlock = cells.get(i).get(j);
                if (valueSubBlock.equals(value) && i != row && j != column) {
                    coordinates.add(i);
                    coordinates.add(j);
                    return coordinates;
                }
            }
        }
        return coordinates;
    }

    /**
     * Returns {@code true} if all 36 cells in the board are confirmed,
     * meaning the player has successfully completed the puzzle.
     *
     * @return {@code true} if every cell in {@link #confirmedCells} is
     *         {@code true}; {@code false} if at least one cell remains
     *         unconfirmed
     */
    public boolean isTheSudokuCompleted() {
        int numberCorrectCells = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (confirmedCells.get(row).get(col)) {
                    numberCorrectCells++;
                }
            }
        }
        return numberCorrectCells == 36;
    }

    // -----------------------------------------------------------------------
    // Accessor
    // -----------------------------------------------------------------------

    /**
     * Returns the solution value at the given board position.
     *
     * <p>Since {@link #initialize()} always produces a fully solved board
     * before this method is called, the returned value is always a digit
     * string ({@code "1"}–{@code "6"}) and never {@code "0"}.</p>
     *
     * @param row zero-based row index
     * @param col zero-based column index
     * @return the digit string stored in the solution matrix at
     *         {@code (row, col)}
     */
    public String getValue(int row, int col) {
        return matrixSudokuSolve.get(row).get(col);
    }

    // -----------------------------------------------------------------------
    // Debug / console output
    // -----------------------------------------------------------------------

    /**
     * Prints the complete solution board ({@link #matrixSudokuSolve}) to
     * standard output.
     *
     * <p>A horizontal divider is printed before every third row and a
     * vertical pipe ({@code |}) before every third column to visually
     * separate the 2×3 sub-blocks.</p>
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
                System.out.print(this.matrixSudokuSolve.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------\n");
    }

    /**
     * Prints the {@link #confirmedCells} boolean mask to standard output.
     *
     * <p>Uses the same visual layout as {@link #printBoard()} (horizontal
     * dividers and pipe separators aligned with sub-block boundaries) to make
     * it easy to compare the confirmed-cell state against the solution board
     * during debugging.</p>
     */
    public void printBoardBool() {
        System.out.println("--- SUDOKU BOARD ---");
        for (int i = 0; i < size; i++) {
            if (i > 0 && i % 2 == 0) {
                System.out.println("---------------------");
            }
            for (int j = 0; j < size; j++) {
                if (j > 0 && j % 3 == 0) {
                    System.out.print("| ");
                }
                System.out.print(this.confirmedCells.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("---------------------\n");
    }
}