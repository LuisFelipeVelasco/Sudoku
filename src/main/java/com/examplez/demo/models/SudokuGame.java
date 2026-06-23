package com.examplez.demo.models;

import com.examplez.demo.SudokuInitializable;
import com.examplez.demo.controllers.MainMenuController;
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
 * <li>Generate a fully solved, randomized 6×6 board using a recursive
 * backtracking algorithm.</li>
 * <li>Select a distributed set of initial cells to reveal as starting clues.</li>
 * <li>Validate player real-time entries directly against standard Sudoku constraints
 * (unique values per row, column, and 2×3 sub-block) using both solution
 * matrices and UI text field states.</li>
 * <li>Evaluate board completion based on programmatic state masks.</li>
 * <li>Provide utility and lookup coordinate methods consumed by the presentation layer.</li>
 * </ul>
 *
 * <p>The grid utilizes a standard 6×6 Sudoku layout with 2-row × 3-column
 * sub-cells (6 cells total).</p>
 *
 * @author jeronimo rojas imbachi
 * @author Luis Felipe Velasco
 * @version 1.1
 * @see MainMenuController
 */
public class SudokuGame implements SudokuInitializable {

    // -----------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------

    /**
     * Dimension of the square board (number of rows and columns).
     * For this custom Sudoku variant, the value is strictly fixed to {@code 6}.
     */
    private final int size = 6;

    /**
     * Internal master matrixSudokuSolve representing the complete, valid solution board.
     * Outer list index maps to rows; inner list index maps to columns.
     * Cells are initially filled with {@code 0} and populated by {@link #initialize()}.
     */
    private ArrayList<ArrayList<String>> matrixSudokuSolve;


    /**
     * Tracks which cells have been correctly filled or revealed.
     * {@code true} means the cell value is finalized or free of conflicts.
     */
    private ArrayList<ArrayList<Boolean>> confirmedCells;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a new {@code SudokuGame} instance with an unpopulated grid state.
     *
     * <p>Allocates a 6×6 nested {@link ArrayList} matrixSudokuSolve and pre-fills every
     * coordinate with {@code 0} to establish an unsolved base layout.</p>
     */
    public SudokuGame() {
        this.matrixSudokuSolve = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<String> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add("0"); // 0 represents an empty cell
            }
            this.matrixSudokuSolve.add(row);
        }

        this.confirmedCells = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<Boolean> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add(false); // 0 represents an empty cell
            }
            this.confirmedCells.add(row);
        }
    }

    // -----------------------------------------------------------------------
    // Board generation
    // -----------------------------------------------------------------------

    /**
     * Generates a complete, structurally valid, and randomized Sudoku puzzle board.
     *
     * <p>Invokes the internal recursive backtracking routine {@link #solve(int, int)}
     * starting at position (0, 0). Upon return, {@link #matrixSudokuSolve} is guaranteed to
     * hold a fully filled legal solution matrixSudokuSolve.</p>
     */
    public void initialize() {
        solve(0, 0);
        chooseCluesToShow();
        
    }

    /**
     * Recursively populates the internal board using a randomized backtracking algorithm.
     *
     * <p>Shuffles values from 1 to 6 uniquely for each cell node processing stage. If a
     * candidate number passes row, column, and sub-block isolation constraints, it is assigned
     * to the matrixSudokuSolve index and execution moves forward. If a downstream path fails, the value is
     * reset to {@code 0} and backtracks.</p>
     *
     * @param row zero-based index of the target row being evaluated
     * @param col zero-based index of the target column being evaluated
     * @return {@code true} if a valid board solution path was discovered from this node layout
     * onwards; {@code false} if a constraint conflict forces a backtrack step
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
            if (isValidCell(row, col, String.valueOf(num),this.matrixSudokuSolve)) {
                this.matrixSudokuSolve.get(row).set(col,String.valueOf(num));

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
     * Generates a visibility tracking map array denoting initial game starting hints.
     *
     * <p>Enforces a balanced game puzzle distribution rule ensuring exactly <strong>two
     * revealed fields per localized 2×3 sub-block</strong> region. Iteratively selects
     * coordinates randomly within block limits until 12 locations across the board
     * return marked as visible clues.</p>
     *
     * @return a 6×6 boolean matrixSudokuSolve mapping grid locations; where {@code true} signals
     * a visible system clue field visible on start
     */
    private void chooseCluesToShow() {
        for (int RowStart = 0; RowStart < 6; RowStart += 2) {
            for (int ColStart = 0; ColStart < 6; ColStart += 3) {
                while (countSubBlock(this.confirmedCells, RowStart, ColStart) != 2) {
                    int Row = RowStart + ThreadLocalRandom.current().nextInt(2);
                    int Col = ColStart + ThreadLocalRandom.current().nextInt(3);
                    this.confirmedCells.get(Row).set(Col,true);
                }
            }
        }
    }

    public ArrayList<ArrayList<Boolean>> getConfirmedCells(){
        return this.confirmedCells;
    }

    public void setConfirmedStateOfCell(int column,int row, boolean confirmedState){
        confirmedCells.get(row).set(column,confirmedState);
    }

    public boolean getConfirmedStateOfCell(int column,int row){
        return confirmedCells.get(row).get(column);
    }

    /**
     * Verifies if placing a target integer at specific coordinates complies with solution rules.
     *
     * <p>Evaluates constraints across three separate planes:</p>
     * <ol>
     * <li><b>Row Isolation:</b> {@code num} must not already occupy any cell across {@code row}.</li>
     * <li><b>Column Isolation:</b> {@code num} must not already occupy any cell across {@code col}.</li>
     * <li><b>Sub-block Isolation:</b> {@code num} must not exist inside the local 2×3 sub-block boundaries
     * calculated via floor coordinates {@code (row/2)*2} and {@code (col/3)*3}.</li>
     * </ol>
     *
     * @param row zero-based index of the target placement row
     * @param col zero-based index of the target placement column
     * @param num candidate value (1–6) being evaluated for safety
     * @return {@code true} if the integer satisfies all solution criteria; {@code false} if a rule conflict occurs
     */
    private boolean isValidCell(int row, int col, String num,ArrayList<ArrayList<String>> cell) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(cell.get(row).get(i), num) && i!=col) {
                System.out.println("hola1");
                return false;
            } // Row conflict
            if (Objects.equals(cell.get(i).get(col), num)&& i!=row) {
                System.out.println("hola2");
                return false;} // Column conflict
        }

        // Sub-block check (2 rows × 3 columns)
        int boxRowStart = (row / 2) * 2;
        int boxColStart = (col / 3) * 3;

        for (int r = boxRowStart; r < boxRowStart + 2; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (Objects.equals(cell.get(r).get(c), num) && (r!=row || c!=col )) {

                    return false;};
            }
        }

        return true;
    }


    public List<List<Integer>> getCoordinatesRepeatedInvalidCells(String value, int column, int row , ArrayList<ArrayList<String>> cells ) {
        int rowValueRepeated    = sameNumberInSameColumn(value, column, row, cells);
        int columnValueRepeated = sameNumberInSameRow(value, column, row, cells);
        List<Integer> blockValueRepeated = sameNumberInSameBlock(value, column, row, cells);
        List<List<Integer>> repeatedInvalidCells = new ArrayList<>(List.of());
        
        if (columnValueRepeated != -1) {
            setConfirmedStateOfCell(columnValueRepeated,row,false);
            repeatedInvalidCells.add(List.of(row,columnValueRepeated));
        }
        if (rowValueRepeated != -1) {
            setConfirmedStateOfCell(column,rowValueRepeated,false);
            repeatedInvalidCells.add(List.of(rowValueRepeated,column));
        }
        if (!blockValueRepeated.isEmpty()) {
            rowValueRepeated = blockValueRepeated.get(0);
            columnValueRepeated = blockValueRepeated.get(1);
            setConfirmedStateOfCell(columnValueRepeated,rowValueRepeated,false);
            repeatedInvalidCells.add(List.of(rowValueRepeated,columnValueRepeated));
        }
        return repeatedInvalidCells;
    }


    public List<List<Integer>> getRepeatedValidCells(String value, int column, int row, ArrayList<ArrayList<String>> cells) {
        int rowValueRepeated = sameNumberInSameColumn(value, column, row, cells);
        int columnValueRepeated = sameNumberInSameRow(value, column, row, cells);
        List<Integer> blockValueRepeated = sameNumberInSameBlock(value, column, row, cells);
        List<List<Integer>> repeatedValidCells = new ArrayList<>(List.of());

        if (rowValueRepeated != -1 && !getConfirmedStateOfCell(column,rowValueRepeated)) {
            if (isValidCell(rowValueRepeated, column, value,cells)) {
                setConfirmedStateOfCell(column,rowValueRepeated,true);
                repeatedValidCells.add(List.of(rowValueRepeated,column));
            }
        }
        if (columnValueRepeated != -1 && !getConfirmedStateOfCell(columnValueRepeated,row)) {
            if (isValidCell(row, columnValueRepeated, value, cells)) {
                setConfirmedStateOfCell(columnValueRepeated,row,true);
                repeatedValidCells.add(List.of(row,columnValueRepeated));
            }
        }
        if (!blockValueRepeated.isEmpty()) {
            int rowValueBlockRepeated = blockValueRepeated.get(0);
            int columnValueBlockRepeated = blockValueRepeated.get(1);
            if (isValidCell(rowValueBlockRepeated, columnValueBlockRepeated, value, cells) && !getConfirmedStateOfCell(columnValueBlockRepeated,rowValueBlockRepeated)) {
                setConfirmedStateOfCell(columnValueBlockRepeated,rowValueBlockRepeated,true);
                repeatedValidCells.add(List.of(rowValueBlockRepeated,columnValueBlockRepeated));
            }
        }
        return repeatedValidCells;
    }
    
    /**
     * Calculates total accumulated clue configurations presently activated within a single 2×3 sub-block.
     *
     * @param matrix     the current 6×6 starting visibility tracker mask array layout
     * @param RowStart upper-most top row bound index location of the target sub-block
     * @param ColStart left-most start column bound index location of the target sub-block
     * @return aggregate total number of coordinates flagged as visible truths ({@code true}) inside the box region
     */
    private int countSubBlock(ArrayList<ArrayList<Boolean>> matrix, int RowStart, int ColStart) {
        int counter = 0;
        for (int i = RowStart; i < RowStart + 2; i++) {
            for (int j = ColStart; j < ColStart + 3; j++) {
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
     * Locates the first empty element coordinate space in row-major sequence to serve as a user hint.
     *
     * <p>Inspects text fields linearly, returning the coordinate indices of the initial element
     * displaying an empty string sequence.</p>
     *
     * @param cells 6×6 reference matrixSudokuSolve array container holding active interface {@link TextField} elements
     * @return a coordinate list pair consisting of {@code [row, col]} locating the target node position;
     * empty list array if every cell space contains input text characters
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
     * Validates whether a user remains permitted to access additional system clue coordinates.
     *
     * <p>To safeguard gameplay, requests are blocked once 35 of 36 grid puzzle pieces
     * register as complete, forcing the user to commit the final input string manually.</p>
     *
     * @return {@code true} if clue distribution paths remain unlocked; {@code false} if 35 fields resolve as finalized
     */
    public boolean isPossibleGiveClue(ArrayList<ArrayList<String>> cells) {
        int numberCorrectcells = 0;
        int numberFilledCells=0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (confirmedCells.get(row).get(col)) numberCorrectcells++;
                if(!Objects.equals(cells.get(row).get(col),"0")) numberFilledCells++;
            }
        }
        return numberCorrectcells != 35 && numberFilledCells!=36;
    }

    // -----------------------------------------------------------------------
    // Input validation
    // -----------------------------------------------------------------------

    /**
     * Validates that an incoming text string contains exactly one digital character matching numerical scale values 1 to 6.
     *
     * @param user_input character string variable evaluated from UI interaction nodes
     * @return {@code true} if text content matches single character regex bounds {@code [1-6]}; {@code false} otherwise
     */
    public boolean isNumberOneToSix(String user_input) {
        return user_input.matches("[1-6]");
    }

    /**
     * Assesses vertical column lines for matching duplicate value entry items.
     *
     * <p>Iterates across column tracks while ignoring origin coordinate row locations to track
     * structural rule conflicts.</p>
     *
     * @param user_input character value string searched across target track limits
     * @param column     zero-based index coordinate of the target vertical track column line
     * @param row        zero-based source coordinate row index excluded from matching processes
     * @param cells     6×6 tracking interface grid container hosting live display {@link TextField} nodes
     * @return matching zero-based row index pointing to structural duplication location; {@code -1} if line remains safe
     */
    public int sameNumberInSameColumn(String user_input, int column, int row, ArrayList<ArrayList<String>> cells) {
        for (int i = 0; i <= 5; i++) {
            String value_block = cells.get(i).get(column);
            if (value_block.equals(user_input) && i != row) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Assesses horizontal row lines for matching duplicate value entry items.
     *
     * <p>Iterates across row tracks while ignoring origin coordinate column locations to track
     * structural rule conflicts.</p>
     *
     * @param user_input character value string searched across target track limits
     * @param column     zero-based source coordinate column index excluded from matching processes
     * @param row        zero-based index coordinate of the target horizontal track row line
     * @param cells     6×6 tracking interface grid container hosting live display {@link TextField} nodes
     * @return matching zero-based column index pointing to structural duplication location; {@code -1} if line remains safe
     */
    public int sameNumberInSameRow(String user_input, int column, int row, ArrayList<ArrayList<String>> cells) {
        for (int i = 0; i <= 5; i++) {
            String value_block = cells.get(row).get(i);
            if (value_block.equals(user_input) && i != column) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Assesses local 2×3 box boundaries to catch duplicate collision entries.
     *
     * <p>Skips calculation logic on the active origin coordinate block to find matching duplication anomalies.</p>
     *
     * @param user_input string input value string searched across local boundary arrays
     * @param column     zero-based lookup source element coordinate index column position
     * @param row        zero-based lookup source element coordinate index row position
     * @param cells     6×6 tracking interface grid container hosting live display {@link TextField} nodes
     * @return coordinate container pair matching {@code [row, col]} pinpointing conflict origin; empty list if safe
     */
    public List<Integer> sameNumberInSameBlock(String user_input, int column, int row, ArrayList<ArrayList<String>> cells) {
        int startRow = (row / 2) * 2;
        int startCol = (column / 3) * 3;
        List<Integer> coordinates = new ArrayList<>();
        for (int i = startRow; i < startRow + 2; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                String value_SubBlock = cells.get(i).get(j);
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
     * Determines if the player has successfully finalized all 36 fields across the puzzle board layout.
     *
     * @return {@code true} if every single cell coordinate reads marked valid and finalized; {@code false} otherwise
     */
    public boolean isTheSudokuCompleted() {
        int numberCorrectcells = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (confirmedCells.get(row).get(col)) {
                    numberCorrectcells++;
                }
            }
        }
        return numberCorrectcells == 36;
    }

    /**
     * Traces reference coordinate mappings of an active layout interface node element.
     *
     * <p>Executes linear pointer checks across the block grid array variables using reference-equality checks
     * to resolve coordinate index maps.</p>
     *
     * @param cells    6×6 tracking layout array container tracking interface UI elements
     * @param textField current active visual interaction display component being tracked down
     * @return index coordinate identifier array pair containing {@code [row, col]}; empty list layout if target missing
     */


    // -----------------------------------------------------------------------
    // Accessor
    // -----------------------------------------------------------------------

    /**
     * Retrieves the target master key answer value mapped at a specific grid position.
     *
     * @param row zero-based index mapping row coordinates
     * @param col zero-based index mapping column coordinates
     * @return solution integer mapping answer metrics; returns {@code 0} if unallocated or empty
     */
    public String  getValue(int row, int col) {
        return matrixSudokuSolve.get(row).get(col);
    }

    // -----------------------------------------------------------------------
    // Debug / console output
    // -----------------------------------------------------------------------

    /**
     * Outputs the master solution matrixSudokuSolve configurations to standard output trace streams.
     *
     * <p>Renders structural box breaks utilizing visual lines to clearly map
     * out localized 2×3 segment distributions.</p>
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
     * Outputs a boolean layout visualization map matrixSudokuSolve directly onto console system streams.
     *
     * <p>Helpful tool to inspect active mask distributions and evaluate programmatic status updates.</p>
     *
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