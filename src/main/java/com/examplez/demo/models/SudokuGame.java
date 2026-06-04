package com.examplez.demo.models;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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

        /*
    Method that creates a mini matrix which will save the numbers we will show, it starts with the rows
    select a random number in each row, then it goes with each column, if there is a number revealed in that column it skips it
    then it does the same then before ,and lastly it verifies there is two numbers per row, one per column and two per subBlock
    */
    public boolean[][] chooseCluesToShow(){
        boolean[][] show = new boolean[6][6];
        for (int Row=0;Row<6;Row++){
            int Col= ThreadLocalRandom.current().nextInt(0,6);
            show[Row][Col]= true;

        }
        for (int Col=0;Col<6;Col++){
            if(!numberOnCol(show,Col)){
                int Row=ThreadLocalRandom.current().nextInt(0,6);
                show[Row][Col]= true;
            }

        }// It verify  there is at least one number revealed per row. col and two per subBlock, if its valid the mini matrix its true if not it put a random number in the required row or column
        for (int RowStart=0;RowStart<6;RowStart+=2){
            for (int ColStart=0;ColStart<6;ColStart+=3){
                while(countSubBlock(show,RowStart,ColStart)!=2){
                    int Row= RowStart+ThreadLocalRandom.current().nextInt(2);
                    int Col= ColStart+ThreadLocalRandom.current().nextInt(3);
                    show[Row][Col]=true;
                }
            }
        }
        return show;}

    //it counts if there is already a number in the column
    private boolean numberOnCol(boolean[][] show, int Col){
        for (int Row=0;Row<6;Row++){
            if (show[Row][Col]){
                return true;
            }
        }
        return false;
    }

    //it counts if there is atleast two numbers per subBlock
    private int countSubBlock(boolean[][] show,int RowStart,int ColStart){
        int counter=0;
        for (int i=  RowStart;i<RowStart+2;i++){
            for (int j= ColStart;j<ColStart+3;j++){
                if (show[i][j]){
                    counter++;
                }
            }
        }
        return counter;}

    /*
Method that iterate the boolean matrix to find a false cell and show it in the board
Then, sets the cell as true to avoid show it again and disable its cell in the board
 */
    public boolean[][] giveClue(boolean[][] matrixBools,TextField[][] blocks){
        boolean find=false;
        for(int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                //If the value of the cell hasn't been shown , then show it
                if (!matrixBools[row][col] && Objects.equals(blocks[row][col].getText(), "")) {
                    //Set it like show it
                    matrixBools[row][col]=true;
                    find=true;
                    break;
                }
            }
            if(find){break;}
        }
        return matrixBools;

    }

        /*
    Verify if the string received is a number between 1 and 6
    */
    public boolean isNumberOneToSix(String user_input){
        return user_input.matches("[1-6]");
    }

    /*
        Iterate all the blocks in the same column and verify if its value is equal to the value of the current block
        excluding itself,in that case return true;
    */
    public boolean sameNumberInSameColumn(String user_input,int column,int row,TextField[][] blocks) {
        for (int i = 0; i <= 5; i++) {
            String value_block = blocks[i][column].getText();
            if (value_block.equals(user_input) && i!=row) {
                return true;
            }
        }
        return false;

    }

    public boolean sameNumberInSameRow(String user_input,int column,int row,TextField[][] blocks) {
        for (int i = 0; i <= 5; i++) {
            String value_block = blocks[row][i].getText();
            if (value_block.equals(user_input) && i!=column) {
                return true;
            }
        }
        return false;

    }
}

