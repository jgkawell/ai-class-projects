/********************************************************************************
//*******************************************************************************
//Class:        PuzzleGameUserInput
//Description:  This is the user input object for the Kawell2 program solving
//              the 8-puzzle. It contains many helper methods for getting user
//              input.
**/


import java.util.ArrayList;

public class PuzzleGameUserInput {

    static KeyboardInputClass keyboardInput = new KeyboardInputClass();

/*******************************************************************************
//Method:       getPuzzleSize
//Description:  Gets the puzzle size from the user.
//Parameters:   None
//Returns:      int     the puzzle size
//Calls:        keyboardInput.getInteger
//Globals:      keyboardInput
**/
    int getPuzzleSize() {
        int puzzleSize = 0;
        boolean validSize = false;
        while (!validSize) {
            //Get the size of the puzzle from the user
            puzzleSize = keyboardInput.getInteger(true, 0, 8, 35,
                    "Specify the puzzle size (8, 15, 24, or 35: 0 to exit):");

            if (puzzleSize == 8 || puzzleSize == 15
                    || puzzleSize == 24 || puzzleSize == 35) {
                TilePuzzleSolver.sideLength = (int) Math.sqrt(puzzleSize + 1);
                validSize = true;
            } else if (puzzleSize == 0) {
                puzzleSize = 0;
                validSize = true;
            }
        }
        return puzzleSize;
    }

/*******************************************************************************
//Method:       getUserDefinedBoard
//Description:  Gets the user-defined board from the user.
//Parameters:   puzzleSize      the size of the puzzle
//              sideLength      the side length of the puzzle
//Returns:      PuzzleBoard     the user-defined board
//Calls:        keyboardInput.getInteger
//Globals:      keyboardInput
**/
    PuzzleBoard getUserDefinedBoard(int puzzleSize, int sideLength) {
        ArrayList numbersAlreadyUsed = new ArrayList();
        PuzzleBoard tempBoard = new PuzzleBoard(sideLength);
        //Get the user defined board
        int[][] boardArray = new int[sideLength][sideLength];
        System.out.println("Enter the piece number for the specified board position (open position=0):");
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                boolean allowedTileNumber = false;
                while (!allowedTileNumber) {
                    String prompt = "Row " + (i + 1) + ", column " + (j + 1) + ":";
                    int tileNumber = keyboardInput.getInteger(true, 0, 0, puzzleSize, prompt);
                    if (!numbersAlreadyUsed.contains(tileNumber)) {
                        allowedTileNumber = true;
                        boardArray[i][j] = tileNumber;
                        numbersAlreadyUsed.add(tileNumber);
                        if (tileNumber == 0) {
                            tempBoard.blank.row = i;
                            tempBoard.blank.col = j;
                        }
                    } else {
                        allowedTileNumber = false;
                        System.out.println("The number " + tileNumber
                                + " has already been used."
                                + "Please enter another number:");
                    }
                }
            }
        }
        tempBoard.board = boardArray;
        return tempBoard;
    }

/*******************************************************************************
//Method:       getNumberOfShuffleMoves
//Description:  Gets the number of shuffle moves.
//Parameters:   None
//Returns:      int     the number of shuffle moves
//Calls:        keyboardInput.getInteger
//Globals:      keyboardInput
**/
    int getNumberOfShuffleMoves() {
        return keyboardInput.getInteger(true, -1, 1, 5000,
                "Number of shuffle moves desired?"
                + "(press ENTER alone to specify starting board)");
    }

/*******************************************************************************
//Method:       getSearchMode
//Description:  Gets the search mode from the user.
//Parameters:   None
//Returns:      int     1=breadth first; 2=best first
//Calls:        keyboardInput.getInteger
//Globals:      keyboardInput
**/
    int getSearchMode() {
        return keyboardInput.getInteger(true, 1, 1, 2,
                "Search mode (1=breadth-first (default); 2=best-first):");
    }

/*******************************************************************************
//Method:       shouldStepThroughSolution
//Description:  Asks the user if they would like to step through solution.
//Parameters:   None
//Returns:      boolean     true=should step through; false=shouldn't step through
//Calls:        keyboardInput.getCharacter
//Globals:      keyboardInput
**/
    boolean shouldStepThroughSolution() {
        System.out.println("");
        char input = keyboardInput.getCharacter(true, 'N', "SN", 1,
                "Success! Press ENTER to show all boards; S and ENTER to step through:");
        return input == 'S';
    }

/*******************************************************************************
//Method:       shouldUseDepthPenalty
//Description:  Asks the user if they would like to use the depth penalty.
//Parameters:   None
//Returns:      boolean     true=should use; false=shouldn't use
//Calls:        keyboardInput.getCharacter
//Globals:      keyboardInput
**/
    boolean shouldUseDepthPenalty() {
        char input = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                "Include depth in heuristic evaluation? (Y/N: Default=Y):");
        return input == 'Y';
    }

/*******************************************************************************
//Method:       shouldRepeat
//Description:  Asks the user if they would like to repeat the program.
//Parameters:   None
//Returns:      boolean     true=should repeat; false=shouldn't repeat
//Calls:        keyboardInput.getCharacter
//Globals:      keyboardInput
**/
    boolean shouldRepeat() {
        char input = keyboardInput.getCharacter(true, 'N', "YN", 1,
                "Would you like to try another puzzle? (Y/N: Default=N)");
        return input == 'Y';
    }

/*******************************************************************************
//Method:       pauseProgram
//Description:  Pauses the program until the user hits ENTER
//Parameters:   None
//Returns:      Nothing
//Calls:        keyboardInput.getKeyboardInput
//Globals:      keyboardInput
**/
    void pauseProgram() {
        keyboardInput.getKeyboardInput("Press enter...");
    }
}
