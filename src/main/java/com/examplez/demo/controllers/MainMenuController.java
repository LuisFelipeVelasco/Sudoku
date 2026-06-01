package com.examplez.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;

import java.util.ArrayList;

public class MainMenuController {

    SudokuGame modelSudoku= new SudokuGame();
    @FXML
    protected void onButtonPlay() {
        modelSudoku.fillFullBoard();
        modelSudoku.printBoard();

    }
}
