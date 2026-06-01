package com.examplez.demo.models;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SudokuGame {


    //Attributes
    private final int size = 6;

    //Create the outer list and pre-allocate memory for 6 rows
    private ArrayList<ArrayList<Integer>> matrix;

    //Constructor
    public SudokuGame(){
        this.matrix = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ArrayList<Integer> row = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                row.add(0); // Fill with zeros (empty cells)
            }
            this.matrix.add(row);
        }
    }

    public void fillFullBoard() {
        solve(0, 0);
    }

    /**
     * BACKTRACKING ALGORITHM: Recursively tries numbers from 1 to 6.
     */
    private boolean solve(int row, int col) {
        // If we reach row 6, we successfully filled the entire board!
        if (row == size) {
            return true;
        }

        // Move to the next row if we reach the end of the columns
        if (col == size) {
            return solve(row + 1, 0);
        }

        // Generate numbers 1 to 6 and shuffle them so the game is random every time
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Collections.shuffle(numbers);

        // Try placing each number in the current cell
        for (int num : numbers) {
            if (isValidMove(row, col, num)) {
                this.matrix.get(row).set(col, num); // Place the number

                // Move to the next column
                if (solve(row, col + 1)) {
                    return true; // Keep it if it leads to a complete solution
                }

                // BACKTRACK: If it didn't work out, reset to 0 and try the next number
                this.matrix.get(row).set(col, 0);
            }
        }

        return false; // Triggers backtracking if no number 1-6 fits
    }

    /**
     * VALIDATION METHOD: Checks row, column, and the 2x3 box constraints.
     */
    private boolean isValidMove(int row, int col, int num) {
        for (int i = 0; i < size; i++) {
            // 1. Check Row
            if (this.matrix.get(row).get(i) == num) return false;

            // 2. Check Column
            if (this.matrix.get(i).get(col) == num) return false;
        }

        // 3. Check the 2x3 Box
        // For a 6x6 grid, boxes are 2 rows tall and 3 columns wide
        int boxRowStart = (row / 2) * 2;
        int boxColStart = (col / 3) * 3;

        for (int r = boxRowStart; r < boxRowStart + 2; r++) {
            for (int c = boxColStart; c < boxColStart + 3; c++) {
                if (this.matrix.get(r).get(c) == num) {
                    return false;
                }
            }
        }

        return true; // Safe to place!
    }

    /**
     * Prints the current state of the matrix cleanly in the console.
     */
    public void printBoard() {
        System.out.println("--- SUDOKU BOARD ---");

        for (int i = 0; i < size; i++) {
            // 1. Print vertical box dividers every 2 rows (for 6x6 grid blocks)
            if (i > 0 && i % 2 == 0) {
                System.out.println("---------------------");
            }

            for (int j = 0; j < size; j++) {
                // 2. Print horizontal box dividers every 3 columns
                if (j > 0 && j % 3 == 0) {
                    System.out.print("| ");
                }

                // 3. Print the actual number followed by a space
                int number = this.matrix.get(i).get(j);
                System.out.print(number + " ");
            }

            // 4. Hit enter at the end of every row to move to the next line
            System.out.println();
        }
        System.out.println("---------------------\n");
    }
    public ArrayList<ArrayList<Integer>> getBoard() {
        return matrix;
    }
    public int getValue(int row, int col) {
        return matrix.get(row).get(col);
    }

}

