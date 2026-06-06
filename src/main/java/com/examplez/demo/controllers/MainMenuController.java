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
    @FXML private Button clueID;
    @FXML private Button playID;
    @FXML private Label labelText;
    private TextField[][] blocks = new TextField[6][6];
    // Maps each highlighted TextField to its CLEAN style (captured before any error color was applied)
    ArrayList<TextField> blocksModified = new ArrayList<>();
    ArrayList<String> styleBlocksModified = new ArrayList<>();

    // A fixed base style for the label so we never accumulate color strings on it
    private static final String LABEL_BASE_STYLE = "";

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

    SudokuGame modelSudoku= new SudokuGame();
    boolean[][] stateCells;
    boolean newGame=false;

    @FXML
    protected void onButtonPlay() {
        labelText.setText("Start playing");
        labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #185723;");
        // Clear any leftover error highlights from a previous game
        blocksModified.clear();
        styleBlocksModified.clear();
        modelSudoku.fillFullBoard();
        modelSudoku.printBoard();
        stateCells=modelSudoku.chooseCluesToShow();
        modelSudoku.printBoardBool(stateCells);
        cleanBoard();
        showBoard(stateCells);
        setListenerToTextFields();
        clueID.setVisible(true);
        playID.setVisible(false);
        newGame=true;
    }
    @FXML protected void onButtonClue() {
        List<Integer> coordinates = modelSudoku.giveClue(blocks);
        if (modelSudoku.isPossibleGiveClue(stateCells) && !coordinates.isEmpty()) {
            int rowClue = coordinates.get(0);
            int columnClue = coordinates.get(1);
            stateCells[rowClue][columnClue] = true;
            showClue(rowClue,columnClue);
            String valueClue = blocks[rowClue][columnClue].getText();
            int rowValueRepeated = modelSudoku.sameNumberInSameColumn(valueClue, columnClue, rowClue, blocks);
            int columnValueRepeated = modelSudoku.sameNumberInSameRow(valueClue, columnClue, rowClue, blocks);
            labelText.setText("");
            labelText.setStyle(LABEL_BASE_STYLE);
            if (columnValueRepeated != -1) {
                TextField textField = blocks[rowClue][columnValueRepeated];
                stateCells[rowClue][columnValueRepeated] = false;
                // Capture the CLEAN style before applying the error color
                blocksModified.add(textField);
                styleBlocksModified.add(textField.getStyle());
                labelText.setText("same numbers in row");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");
            } else if (rowValueRepeated != -1) {
                TextField textField = blocks[rowValueRepeated][columnClue];
                stateCells[rowValueRepeated][columnClue] = false;
                // Capture the CLEAN style before applying the error color
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
/*
function that writes the board with the revealed numbers in the GUI, if the row and col of the blocks
its equal to the row and col of show[][] it will get the values created of the original matrix and show it, in other case it will put it blank
 */
    private void showBoard(boolean[][] matrix){
        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 6; col++){
                if(matrix[row][col]){
                    blocks[row][col].setText(
                            String.valueOf(modelSudoku.getValue(row,col)));
                    blocks[row][col].setDisable(true);

                }
                else{
                    blocks[row][col].setDisable(false);
                }
            }
        }
    }
    private void showClue(int row,int col){
        blocks[row][col].setText(
                String.valueOf(modelSudoku.getValue(row,col)));
        blocks[row][col].setDisable(true);
    }

    private void setListenerToTextFields() {

        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 6; col++){
                TextField textField=blocks[row][col];
                textField.textProperty(). addListener((observable, oldValue, newValue) -> {
                    verification(newValue,oldValue,textField);
                });
            }
        }
    }
    private void verification(String user_input ,String old_input, TextField textField){
        if(newGame){
            List<Integer> coordinatesTextField=modelSudoku.getCoordinatestextField(blocks,textField);
            int rowtextField=coordinatesTextField.get(0);
            int columntextField=coordinatesTextField.get(1);

            if(user_input.equals("")){
                labelText.setText("");
                labelText.setStyle(LABEL_BASE_STYLE);
                // Remove error highlights for this cell AND any other cells it caused to be highlighted
                for(int i = blocksModified.size() - 1; i >= 0; i--){
                    if(blocksModified.get(i) == textField){
                        blocksModified.get(i).setStyle(styleBlocksModified.get(i));
                        blocksModified.remove(i);
                        styleBlocksModified.remove(i);
                    }
                }
                stateCells[rowtextField][columntextField]=false;
            }
            else if(modelSudoku.isNumberOneToSix(user_input)==false) {
                labelText.setText("Type a number 1-6");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                textField.setText("");
            }
            else if(stateCells[rowtextField][columntextField]==false){

                if(modelSudoku.sameNumberInSameColumn(user_input,columntextField,rowtextField,blocks)!=-1){
                    // Capture the CLEAN style before applying the error color
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the  column already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color: #69261C;");

                }
                else if(modelSudoku.sameNumberInSameRow(user_input,columntextField,rowtextField,blocks)!=-1){
                    // Capture the CLEAN style before applying the error color
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the  row already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color:#69261C;");

                }

                else if(!modelSudoku.sameNumberInSameBlock(user_input,columntextField,rowtextField,blocks).isEmpty()){
                    // Capture the CLEAN style before applying the error color
                    blocksModified.add(textField);
                    styleBlocksModified.add(textField.getStyle());
                    labelText.setText("This number is in the block already");
                    labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #69261C;");
                    textField.setStyle(textField.getStyle() + "-fx-background-color:#69261C;");

                }
                else {
                    stateCells[rowtextField][columntextField]=true;
                    labelText.setText("");
                    labelText.setStyle(LABEL_BASE_STYLE);
                }

            }
            if(modelSudoku.isTheSudokuCompleted(stateCells)){
                modelSudoku=new SudokuGame();
                labelText.setText("You Did it , The sudoku is completed");
                labelText.setStyle(LABEL_BASE_STYLE + "-fx-text-fill: #185723;");
                for(int row = 0; row < 6; row++){
                    for(int col = 0; col < 6; col++){
                        blocks[row][col].setDisable(true);
                    }
                }
                clueID.setVisible(false);
                playID.setVisible(true);
                newGame=false;

            }
        }
    }
    private void cleanBoard(){
        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 6; col++){
                    blocks[row][col].setText("");
                }
            }
    }
}






