//Program:      othello-player
//Course:       COSC470
//Description:  Permits two programs, each using this control structure (but each with additional
//              customized classes and/or methods)to play Othello (i.e, against each other).
//              This Othello playing game uses a recursive, depth-first search, mini-max with alpha-beta
//              cutoffs, and iterative-deepening. The heuristic is based on weighting different positions
//              on the board (i.e. corners are best, edges are good, and one space from an edge is bad)
//Author:       Jack Kawell
//Revised:      5/3/18

import java.io.*;
//***************************************************************************************************
//***************************************************************************************************
//Class:        OthelloShell
//Description:  Main class for the program. Allows set-up and plays one side.
public class OthelloShell {

    //INSERT ANY ADDITIONAL GLOBAL VARIABLES HERE
    //===========================================
    //===========================================

    //===========================================
    //===========================================
    //***************************************************************************************************
    //Method:		main
    //Description:	Calls routines to play Othello
    //Parameters:	none
    //Returns:		nothing
    //Calls:        loadBoard, saveBoard, showBoard, constructor in Board class
    //              getCharacter, getInteger, getKeyboardInput, constructor in KeyboardInputClass
    public static void main(String[] args) {
        //INSERT ANY ADDITIONAL CONTROL VARIABLES HERE
        //============================================
        //============================================

        //============================================
        //============================================
        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        int pollDelay = 250;
        long moveStartTime;
        long moveEndTime;
        long moveGraceTime = 10000;  //times in milliseconds
        Board currentBoard = Board.loadBoard();
        String myMove;
        String myColorText;
        System.out.println("--- Othello ---");
        System.out.println("Player: Jack Kawell\n");
        if (currentBoard != null && currentBoard.status == 1) {     //board found, make sure it can be used and check if a game is in progress
            if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "A game appears to be in progress. Abort it? (Y/N (default = Y)") == 'Y')
                currentBoard = null;
            else {
                System.out.println("Exiting program. Try again later...");
                System.exit(0);
            }
        }
        if ((currentBoard == null)||(currentBoard.status == 2)) {   //create a board for a new game
            int rows = 8;
            int cols = 8;
            if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "Use standard board? (Y/N: default = Y):") == 'N') {
                rows = keyboardInput.getInteger(true, rows, 4, 26, "Specify the number of rows for the board (default = "+rows+"):");
                cols = keyboardInput.getInteger(true, cols, 4, 26, "Specify the number of columns for the board (default = "+cols+"):");
            }
            int maxTime = 60;
            maxTime = keyboardInput.getInteger(true, maxTime, 10, 600, "Max time (seconds) allowed per move (Default = "+maxTime+"):");
            currentBoard = new Board(rows, cols, maxTime);
            while (currentBoard.saveBoard()){
                //try until board is saved (necessary in case of access conflict)
            }
        }

        //INSERT CODE HERE FOR ANY ADDITIONAL SET-UP OPTIONS
        //==================================================
        //==================================================

        GamePlayer.numRows = currentBoard.boardRows;
        GamePlayer.numCols = currentBoard.boardCols;
        GamePlayer.maxTime = (long) currentBoard.maxMoveTime * 1000;
        GamePlayer.showSettings();

        //==================================================
        //==================================================

        //At this point set-up must be in progress so colors can be assigned
        char myColor;
        if (currentBoard.colorSelected == '?') {                    //if no one has chosen a color yet, choose one (player #1)
            myColor = keyboardInput.getCharacter(true, 'B', "BW", 1, "Select color: B=Black; W=White (Default = Black):");
            currentBoard.colorSelected = myColor;

            while (currentBoard.saveBoard()){
                //try until the board is saved
            }
            System.out.println("You may now start the opponent's program...");
            while (currentBoard.status == 0) {                      //wait for other player to join in
                currentBoard = null;                                //get the updated board
                while (currentBoard == null)
                    currentBoard = Board.loadBoard();
            }
        }
        else {                                                      //otherwise take the other color (this is player #2)
            if (currentBoard.colorSelected == 'B')
                myColor = 'W';
            else
                myColor = 'B';
            currentBoard.status = 1;                                //by now, both players are engaged and play can begin
            while (currentBoard.saveBoard()){
                //try until the board is saved
            }
        }

        char opponentColor;
        if (myColor == 'B'){
            myColorText = "Black";
            opponentColor = 'W';
        }
        else{
            myColorText = "White";
            opponentColor = 'B';
        }
        System.out.println("This player will be "+myColorText+"\n");

        //INSERT CODE HERE FOR ANY ADDITIONAL OUTPUT OPTIONS
        //==================================================
        //==================================================

        GamePlayer.myColor = myColor;
        GamePlayer.opponentColor = opponentColor;

        //==================================================
        //==================================================

        //Now play can begin. (At this point each player should have an identical copy of currentBoard.)
        while (currentBoard.status == 1) {
            if (currentBoard.whoseTurn == myColor) {
                if (currentBoard.whoseTurn == 'B')
                    System.out.println("Black's turn to move...");
                else
                    System.out.println("White's turn to move");
                currentBoard.showBoard();
                String previousMove = currentBoard.move;
                moveStartTime = System.currentTimeMillis();


                //CALL METHOD(S) HERE TO SELECT AND MAKE A VALID MOVE
                //===================================================
                //===================================================

                //Print previous move
                if (opponentColor == 'W') {
                    System.out.println("White chose " + previousMove);
                } else {
                    System.out.println("Black chose " + previousMove);
                }

                //Choose move
                currentBoard.board = GamePlayer.makeMove(currentBoard.board);

                //Log move
                myMove = GamePlayer.playerMove;

                //===================================================
                //===================================================
                //YOU MAY ADD NEW CLASSES AND/OR METHODS BUT DO NOT
                //CHANGE ANY EXISTING CODE BELOW THIS POINT


                moveEndTime = System.currentTimeMillis();
                if ((moveEndTime - moveStartTime) > (currentBoard.maxMoveTime*1000 + moveGraceTime)){
                    System.out.println("\nMaximum allotted move time exceeded--Opponent wins by default...\n");
                    keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
                    currentBoard.status = 2;
                    while (currentBoard.saveBoard()){
                        //try until the board is saved
                    }
                    System.exit(0);
                }

                if (myMove.length() != 0){
                    System.out.println(myColorText+" chooses "+myMove+"\n");
                    currentBoard.showBoard();
                    System.out.println("Waiting for opponent's move...\n");
                }
                else{
                    if (previousMove.length() == 0) {               //neither player can move
                        currentBoard.status = 2;                    //game over...
                        System.out.println("\nGame over!");
                        int blackScore = 0;
                        int whiteScore = 0;
                        for (int r = 0; r < currentBoard.boardRows; r++)
                            for (int c = 0; c < currentBoard.boardCols; c++)
                                if(currentBoard.board[r][c] == 'B')
                                    blackScore++;
                                else if(currentBoard.board[r][c] == 'W')
                                    whiteScore++;
                        if (blackScore > whiteScore)
                            System.out.println("Blacks wins "+blackScore+" to "+whiteScore);
                        else if (whiteScore > blackScore)
                            System.out.println("White wins "+whiteScore+" to "+blackScore);
                        else
                            System.out.println("Black and White tie with scores of "+blackScore+" each");
                    }
                    else
                        System.out.println("No move available. Opponent gets to move again...");
                }
                currentBoard.move = myMove;
                currentBoard.whoseTurn = opponentColor;
                while (currentBoard.saveBoard()){
                    //try until the board is saved
                }
            }
            else{                                                   //wait a moment then poll again
                try{
                    Thread.sleep(pollDelay);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            currentBoard = null;                                    //get the updated board
            while (currentBoard == null)
                currentBoard = Board.loadBoard();
        }
        keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
    }
    //***************************************************************************************************
}
//*******************************************************************************************************
//*******************************************************************************************************
//Class:        Board
//Description:  Othello board and related parms
class Board implements Serializable {
    char status;        //0=set-up for a new game is in progress; 1=a game is in progress; 2=game is over
    char whoseTurn;     //'?'=no one's turn yet--game has not begun; 'B'=black; 'W'=white
    String move;        //the move selected by the current player (as indicated by whoseTurn)
    char colorSelected; //'B' or 'W' indicating the color chosen by the first player to access the file
    //for a new game ('?' if neither player has yet chosen a color)
    //Note: this may or may not be the color for the player accessing the file
    int maxMoveTime;    //maximum time allotted for a move (in seconds)
    int boardRows;      //size of the board (allows for variations on the standard 8x8 board)
    int boardCols;
    char[][] board;     //the board. Positions are filled with: blank = no piece; 'B'=black; 'W'=white
    //***************************************************************************************************
    //Method:       Board
    //Description:  Constructor to create a new board object
    //Parameters:	rows - size of the board
    //              cols
    //              time - maximum time (in seconds) allowed per move
    //Calls:		nothing
    //Returns:		nothing
    Board(int rows, int cols, int time){
        int r;
        int c;
        status = 0;
        whoseTurn = 'B';        //Black always makes the first move
        move = "*";
        colorSelected = '?';
        maxMoveTime = time;
        boardRows = rows;
        boardCols = cols;
        board = new char[boardRows][boardCols];
        for (r = 0; r < boardRows; r++)
            for (c = 0; c < boardCols; c++)
                board[r][c] = ' ';
        r = boardRows/2 - 1;
        c = boardCols/2 - 1;
        board[r][c] = 'W';
        board[r][c+1] = 'B';
        board[r+1][c] = 'B';
        board[r+1][c+1] = 'W';
    }
    //***************************************************************************************************
    //Method:       saveBoard
    //Description:  Saves the current board to disk as a binary file named "OthelloBoard"
    //Parameters:	none
    //Calls:		nothing
    //Returns:		true if successful; false otherwise
    public boolean saveBoard() {
        try {
            try (ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("OthelloBoard"))) {
                outStream.writeObject(this);
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }
    //***************************************************************************************************
    //Method:       loadBoard
    //Description:  Loads the current Othello board and data from a binary file
    //Parameters:   none
    //Calls:        nothing
    //Returns:      a Board object (or null if routine is unsuccessful)
    public static Board loadBoard() {
        try {
            Board boardObject;
            try (ObjectInputStream inStream = new ObjectInputStream(new FileInputStream("OthelloBoard"))) {
                boardObject = (Board) inStream.readObject();
            }
            return boardObject;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    //***************************************************************************************************
    //Method:       showBoard
    //Description:  Displays the current Othello board using extended Unicode characters. Looks fine
    //               in a command window but may not display well in the NetBeans IDE...
    //Parameters:   none
    //Calls:        nothing
    //Returns:      nothing
    public void showBoard() {
        int r;
        int c;
        System.out.print("  ");                         //column identifiers
        for (c = 0; c < boardCols; c++){
            System.out.print(" "+(char)(c+65));
        }
        System.out.println();

        //top border
        System.out.print("  "+(char)9484);                   //top left corner \u250C
        for (c = 0; c < boardCols - 1; c++){
            System.out.print((char)9472);               //horizontal \u2500
            System.out.print((char)9516);               //vertical T \u252C
        }
        System.out.print((char)9472);                   //horizontal \u2500
        System.out.println((char)9488);                 //top right corner \u2510

        //board rows
        for (r = 0; r < boardRows; r++) {
            System.out.print(" "+(char)(r+65));         //row identifier
            System.out.print((char)9474);               //vertical \u2502
            for (c = 0; c < boardCols; c++){
                System.out.print(board[r][c]);
                System.out.print((char)9474);           //vertical \u2502
            }
            System.out.println();

            //insert row separators
            if (r < boardRows - 1) {
                System.out.print("  "+(char)9500);           //left T \u251C
                for (c = 0; c < boardCols - 1; c++){
                    System.out.print((char)9472);       //horizontal \u2500
                    System.out.print((char)9532);       //+ (cross) \u253C
                }
                System.out.print((char)9472);           //horizontal \u2500
                System.out.println((char)9508);         //right T \u2524
            }
        }

        //bottom border
        System.out.print("  "+(char)9492);                   //lower left corner \u2514
        for (c = 0; c < boardCols - 1; c++){
            System.out.print((char)9472);               //horizontal \u2500
            System.out.print((char)9524);               //upside down T \u2534
        }
        System.out.print((char)9472);                   //horizontal \u2500
        System.out.println((char)9496);                 //lower right corner \u2518

    }
    //***************************************************************************************************
}
//*******************************************************************************************************
//*******************************************************************************************************
