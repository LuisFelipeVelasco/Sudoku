package com.examplez.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.examplez.demo.models.SudokuGame;
import javafx.scene.control.TextField;
import java.util.concurrent.ThreadLocalRandom;

import java.util.ArrayList;


public class MainMenuController {
    @FXML private TextField T00;
    @FXML private TextField T01;
    @FXML private TextField T02;
    @FXML private TextField T03;
    @FXML private TextField T04;
    @FXML private TextField T05;
    @FXML private TextField T10;
    @FXML private TextField T11;
    @FXML private TextField T12;
    @FXML private TextField T13;
    @FXML private TextField T14;
    @FXML private TextField T15;
    @FXML private TextField T20;
    @FXML private TextField T21;
    @FXML private TextField T22;
    @FXML private TextField T23;
    @FXML private TextField T24;
    @FXML private TextField T25;
    @FXML private TextField T30;
    @FXML private TextField T31;
    @FXML private TextField T32;
    @FXML private TextField T33;
    @FXML private TextField T34;
    @FXML private TextField T35;
    @FXML private TextField T40;
    @FXML private TextField T41;
    @FXML private TextField T42;
    @FXML private TextField T43;
    @FXML private TextField T44;
    @FXML private TextField T45;
    @FXML private TextField T50;
    @FXML private TextField T51;
    @FXML private TextField T52;
    @FXML private TextField T53;
    @FXML private TextField T54;
    @FXML private TextField T55;
    private TextField[][] blocks = new TextField[6][6];
    public void initialize(){
        blocks[0][0]=T00;
        blocks[0][1]=T01;
        blocks[0][2]=T02;
        blocks[0][3]=T03;
        blocks[0][4]=T04;
        blocks[0][5]=T05;
        blocks[1][0]=T10;
        blocks[1][1]=T11;
        blocks[1][2]=T12;
        blocks[1][3]=T13;
        blocks[1][4]=T14;
        blocks[1][5]=T15;
        blocks[2][0]=T20;
        blocks[2][1]=T21;
        blocks[2][2]=T22;
        blocks[2][3]=T23;
        blocks[2][4]=T24;
        blocks[2][5]=T25;
        blocks[3][0]=T30;
        blocks[3][1]=T31;
        blocks[3][2]=T32;
        blocks[3][3]=T33;
        blocks[3][4]=T34;
        blocks[3][5]=T35;
        blocks[4][0]=T40;
        blocks[4][1]=T41;
        blocks[4][2]=T42;
        blocks[4][3]=T43;
        blocks[4][4]=T44;
        blocks[4][5]=T45;
        blocks[5][0]=T50;
        blocks[5][1]=T51;
        blocks[5][2]=T52;
        blocks[5][3]=T53;
        blocks[5][4]=T54;
        blocks[5][5]=T55;





    }
    //library used to generate a random number between 1 and 6
   // int numRow=ThreadLocalRandom.current().nextInt(1,7);


    SudokuGame modelSudoku= new SudokuGame();
    @FXML
    protected void onButtonPlay() {
        modelSudoku.fillFullBoard();

        modelSudoku.printBoard();

        boolean[][] show=showClues();
        showBoard(show);



    }



    private void showBoard(boolean[][] show){

        for(int row = 0; row < 6; row++){

            for(int col = 0; col < 6; col++){

                if(show[row][col]){

                    blocks[row][col].setText(
                            String.valueOf(
                                    modelSudoku.getValue(row,col)
                            )
                    );

                }else{

                    blocks[row][col].setText("");

                }
            }
        }
    }
    private boolean[][] showClues(){
        boolean[][] show = new boolean[6][6];
        for (int Row=0;Row<6;Row++){
            int Col=ThreadLocalRandom.current().nextInt(0,6);
            show[Row][Col]= true;

    }
        for (int Col=0;Col<6;Col++){
            if(!numberOnCol(show,Col)){

                    int Row=ThreadLocalRandom.current().nextInt(0,6);
                    show[Row][Col]= true;

            }
    }
        for (int RowStart=0;RowStart<6;RowStart+=2){
            for (int ColStart=0;ColStart<6;ColStart+=3){
                while(countSubBlock(show,RowStart,ColStart)<2){
                    int Row= RowStart+ThreadLocalRandom.current().nextInt(2);
                    int Col= ColStart+ThreadLocalRandom.current().nextInt(3);
                    show[Row][Col]=true;
                }
            }
        }
    return show;}
    private boolean numberOnCol(boolean[][] show, int Col){
        for (int Row=0;Row<6;Row++){
            if (show[Row][Col]){
                return true;
            }
        }
return false;
    }
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
}

