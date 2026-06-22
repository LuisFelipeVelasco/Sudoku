package com.examplez.demo;

import java.util.List;

/**
 * Defines the initialization contract for all core components of the Sudoku game.
 *
 * <p>Any class that participates in the Sudoku lifecycle — whether generating
 * the board or managing the UI — must implement this interface and provide
 * its own {@link #initialize()} logic.</p>
 */

public interface SudokuInitializable {
    void initialize();

}
