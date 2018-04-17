/**
//Program:      tile_puzzle_solver.java
//Course:       COSC470
//Description:
//Author:       Jack Kawell
//Revised:      2/28/18
//Language:     Java
//IDE:          NetBeans 8.2
//*******************************************************************************
//******************************************************************************/

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;


/********************************************************************************
//*******************************************************************************
//Class:        tile_puzzle_solver
//Description:  This class contains the search logic to solve the puzzle using
//              breadth and best-first algorithms. The best-first can be done
//              with either a depth penalty or no penalty. This class also contains
//              output methods for printing user info.
**/
public class TilePuzzleSolver {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">
    //Basic puzzle values
    static boolean useDepthPenalty = true;//States whether to use the depth penalty
    static int totalBoardsCreated;//Tracks total number of boards built
    static int sideLength;//The side length of the puzzle chosen
    static int searchMode;//1=breadth-first; 2=best-first

    //Special objects
    static PuzzleGameUserInput userInput = new PuzzleGameUserInput();//Class to get user input
    static PuzzleBoard goalBoard;//Goal state
    static PuzzleBoard gameBoard;//Generic start/current state

    //List objects
    static PriorityQueue<PuzzleBoard> open = new PriorityQueue<>();//Nodes yet to be evaluated
    static ArrayList<PuzzleBoard> closed = new ArrayList<>();//Nodes already evaluated
    static Stack<PuzzleBoard> solutionPath = new Stack<>();//Nodes in solution
    // </editor-fold>

/*******************************************************************************
//Method:       main
//Description:  Main method that calls all other relevant methods within an
//              ongoing loop that is stopped by the user.
//Parameters:   None
//Returns:      Nothing
//Calls:        getPuzzleAndValuesFromUser
//              userInput.pauseProgram
//              beginSearch
//              showSolution
//              userInput.shouldRepeat
//              resetProgram
//              endProgram
//Globals:      userInput
**/
    public static void main(String[] args) {
        System.out.println("Program to solve the 8, 15, 24, or 36 Puzzle: Jack Kawell\n");
        boolean keepRunning = true;
        while (keepRunning) {
            getPuzzleAndValuesFromUser();
            userInput.pauseProgram();
            beginSearch();
            showSolution();
            userInput.pauseProgram();
            if (userInput.shouldRepeat()) {
                resetProgram();
            } else {
                keepRunning = false;
            }
        }
        endProgram();
    }

/*******************************************************************************
//Method:       getPuzzleAndValuesFromUser
//Description:  Requests many values from the user such as search type and
//              whether to use a depth penalty.
//              Generally sets up the puzzle to solve by getting user data
//              and building the goal and starting boards.
//Parameters:   None
//Returns:      Nothing
//Calls:        userInput.getPuzzleSize
//              endProgram
//              PuzzleBoard (constructor)
//              PuzzleBoard.generateGoalState
//              userInput.getNumberOfShuffleMoves
//              userInput.getUserDefinedBoard
//              shuffleBoard
//              userInput.getSearchMode
//              userInput.shouldUseDepthPenalty
//              PuzzleBoard.calculateHeuristic
//              printBoard
//Globals:      userInput
//              goalBoard
//              gameBoard
//              sideLength
//              searchMode
//              useDepthPenalty
**/
    private static void getPuzzleAndValuesFromUser() {
        //Get the puzzle size from the user
        int puzzleSize = userInput.getPuzzleSize();
        if (puzzleSize == 0) {
            endProgram();
        }
        //Build the goal board
        goalBoard = new PuzzleBoard(sideLength);
        goalBoard.generateGoalState();
        //Shuffle board or get user defined board from user
        int numberOfShuffleMoves = userInput.getNumberOfShuffleMoves();
        if (numberOfShuffleMoves == -1) {
            gameBoard = userInput.getUserDefinedBoard(puzzleSize, sideLength);
        } else {
            gameBoard = new PuzzleBoard(sideLength);
            gameBoard.generateGoalState();
            shuffleBoard(gameBoard, numberOfShuffleMoves);
        }
        //Ask user which search mode to use
        searchMode = userInput.getSearchMode();
        if (searchMode == 2) {
            useDepthPenalty = userInput.shouldUseDepthPenalty();
        }
        //Calculate heuristics
        goalBoard.calculateHeuristic(goalBoard, searchMode, 0, useDepthPenalty);
        gameBoard.calculateHeuristic(goalBoard, searchMode, 0, useDepthPenalty);
        //Print out the goal and start states
        System.out.println("This is the goal state:");
        printBoard(goalBoard);
        System.out.println("This is the start state:");
        printBoard(gameBoard);
    }

/*******************************************************************************
//Method:       shuffleBoard
//Description:  Shuffles the goal state to form a disordered (but solvable) board.
//Parameters:   boardToShuffle          The board to shuffle
//              numberOfShuffleMoves    The number of shuffle moves
//Returns:      Nothing
//Calls:        boardToShuffle.moveTile
//Globals:      None
**/
    private static void shuffleBoard(PuzzleBoard boardToShuffle, int numberOfShuffleMoves) {
        int backtrackDirection = -1;
        for (int i = 0; i < numberOfShuffleMoves; i++) {
            //Generate a random direction
            Random random = new Random();
            int direction = random.nextInt(5);
            if (direction != backtrackDirection) {
                boolean success = boardToShuffle.moveTile(direction);
                //Assign the backtrack direction to keep from reshuffling
                if (success) {
                    switch (direction) {
                        case 0:
                            backtrackDirection = 2;
                            break;
                        case 1:
                            backtrackDirection = 3;
                            break;
                        case 2:
                            backtrackDirection = 0;
                            break;
                        case 3:
                            backtrackDirection = 1;
                            break;
                        default:
                            System.out.println("Bad direction");
                            break;
                    }
                } else {
                    i--;
                }
            } else {
                i--;
            }
        }
    }

/*******************************************************************************
//Method:       beginSearch
//Description:  Contains the loop that runs the search. Controls pulling out the
//              boards from open and adding them to closed calls the method to
//              check if a solution has been found. Basically the controller of
//              the search.
//Parameters:   None
//Returns:      Nothing
//Calls:        PuzzleBoard.equals
//              buildSolutionPath
//              generateMoves
//Globals:      open
//              closed
//              gameBoard
//              goalBoard
**/
    private static void beginSearch() {
        //Tell the user that the search has starting
        System.out.println("Working...");
        //Add the first board to open
        open.add(gameBoard);
        //Loop to find solution
        boolean foundSolution = false;
        while (!foundSolution) {
            //Pull in the board at the front of the queue
            PuzzleBoard curBoard = open.poll();
            //Check if the current board is the goal board
            foundSolution = curBoard.equals(goalBoard);
            //Add the current board to closed
            closed.add(curBoard);
            //If the solution is found, build the path... else, continue searching
            if (foundSolution) {
                buildSolutionPath(curBoard);
            } else {
                //Get the index (parent index) of the current board in closed
                int curParentIndex = closed.indexOf(curBoard);
                generateMoves(curBoard, curParentIndex);
            }
        }
    }

/*******************************************************************************
//Method:       generateMoves
//Description:  Generates the children of each parent node in the puzzle tree.
//              Verifies that the children are unique and adds them to the open
//              queue.
//Parameters:   curBoard            the parent node
//              curParentIndex      the parent index
//Returns:      Nothing
//Calls:        PuzzleBoard.deepCopy
//              PuzzleBoard.moveTile
//              PuzzleBoard.equals
//              PuzzleBoard.calculateHeuristic
//Globals:      open
//              closed
//              goalBoard
//              totalBoardsCreated
//              searchMode
//              useDepthPenalty
**/
    private static void generateMoves(PuzzleBoard curBoard, int curParentIndex) {
        for (int directionToMove = 0; directionToMove < 4; directionToMove++) {
            //Copy the current board to a temp board
            PuzzleBoard tempBoard = curBoard.deepCopy();
            //Assign the parent index to the new board
            tempBoard.parent = curParentIndex;
            //Attempt to move the tile
            boolean moveSucceeded = tempBoard.moveTile(directionToMove);
            //If the move succeeded, check to see if unique and save if true
            if (moveSucceeded) {
                //Increment the depth
                tempBoard.depth++;
                //Check for uniqueness
                boolean uniqueBoard = true;
                for (int i = 0; i < closed.size(); i++) {
                    if (tempBoard.equals(closed.get(i))) {
                        uniqueBoard = false;
                        break;
                    }
                }
                //If unique, add the board to open (calculate the score first)
                if (uniqueBoard) {
                    totalBoardsCreated++;
                    tempBoard.calculateHeuristic(goalBoard, searchMode, totalBoardsCreated, useDepthPenalty);
                    open.add(tempBoard);
                }
            }
        }
    }

/*******************************************************************************
//Method:       buildSolutionPath
//Description:  Generates the solution path from the closed array.
//Parameters:   finalBoard      last board (equals the goal state)
//Returns:      Nothing
//Calls:        Nothing
//Globals:      solutionPath
//              closed
**/
    private static void buildSolutionPath(PuzzleBoard finalBoard) {
        solutionPath.push(finalBoard);
        if (finalBoard.parent != -1) {
            boolean keepRunning = true;
            PuzzleBoard tempBoard = closed.get(finalBoard.parent);
            while (keepRunning) {
                solutionPath.push(tempBoard);
                if (tempBoard.parent == -1) {
                    keepRunning = false;
                } else {
                    tempBoard = closed.get(tempBoard.parent);
                }
            }
        }
    }

/*******************************************************************************
//Method:       printBoard
//Description:  Prints the given board (and it's scores) to the output. Accounts
//              for double spacing when using larger numbers.
//Parameters:   boardToPrint      the board to print
//Returns:      Nothing
//Calls:        Nothing
//Globals:      sideLength
**/
    private static void printBoard(PuzzleBoard boardToPrint) {
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                System.out.print(" ");
                if (boardToPrint.board[i][j] == 0) {
                    if (sideLength > 3) {
                        System.out.print(" ");
                    }
                    System.out.print(" ");
                } else if (boardToPrint.board[i][j] > 9) {
                    System.out.print(boardToPrint.board[i][j]);
                } else {
                    if (sideLength > 3) {
                        System.out.print(" ");
                    }
                    System.out.print(boardToPrint.board[i][j]);
                }
            }
            System.out.println("");
        }
        System.out.println("(Raw score = " + boardToPrint.rawScore
                + "   Depth = " + boardToPrint.depth
                + "   Total score = " + boardToPrint.totalScore
                + ")");
        System.out.println("");
    }

/*******************************************************************************
//Method:       showSolution
//Description:  Prints the solution to the user along with extra puzzle info.
//Parameters:   None
//Returns:      Nothing
//Calls:        userInput.shouldStepThroughSolution
//              printBoard
//              userInput.pauseProgram
//Globals:      userInput
//              solutionPath
//              open
//              closed
**/
    private static void showSolution() {
        boolean stepThroughSolution = userInput.shouldStepThroughSolution();
        int solutionLength = solutionPath.size();
        while (!solutionPath.isEmpty()) {
            printBoard(solutionPath.pop());
            if (stepThroughSolution) {
                userInput.pauseProgram();
            }
        }
        System.out.println((solutionLength - 1) + " moves out of " + (closed.size() - 1)
                + " moves considered (" + open.size() + " nodes left in OPEN)");
        System.out.println("");
    }

/*******************************************************************************
//Method:       resetProgram
//Description:  Resets the globals for a new run.
//Parameters:   None
//Returns:      Nothing
//Calls:        Nothing
//Globals:      goalBoard
//              gameBoard
//              sideLength
//              searchMode
//              open
//              closed
//              solutionPath
//              useDepthPenalty
//              totalBoardsCreated
**/
    private static void resetProgram() {
        goalBoard = null;
        gameBoard = null;
        sideLength = 0;
        searchMode = 0;
        open = new PriorityQueue<>();
        closed = new ArrayList<>();
        solutionPath = new Stack<>();
        useDepthPenalty = true;
        totalBoardsCreated = 0;
    }

/*******************************************************************************
//Method:       endProgram
//Description:  Ends the program.
//Parameters:   None
//Returns:      Nothing
//Calls:        Nothing
//Globals:      None
**/
    private static void endProgram() {
        System.out.println("\n\nExit...");
        System.exit(0);
    }
}
