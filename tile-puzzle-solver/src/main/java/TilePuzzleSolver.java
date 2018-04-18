/**
//Program:      TilePuzzleSolver
//Course:       COSC470
//Description:  Solves the 15 Puzzle (https://en.wikipedia.org/wiki/15_puzzle) for
//              variable board sizes. The program can be given a start state or
//              can generate it's own shuffled start state. It then finds the
//              shortest solution to the puzzle. It can be adjusted by the user
//              to use different search parameters.
//Author:       Jack Kawell
//Revised:      2/28/18
//Language:     Java
//IDE:          NetBeans 8.2
//*******************************************************************************
//******************************************************************************/

import java.util.*;


public class TilePuzzleSolver {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">
    //Basic puzzle values
    static boolean useDepthPenalty = true;//States whether to use the depth penalty
    static int totalBoardsCreated;//Tracks total number of boards built
    static int sideLength;//The side length of the puzzle chosen
    static int searchMode;//1=breadth-first; 2=best-first

    //Special objects
    static PuzzleBoard goalBoard;//Goal state
    static PuzzleBoard gameBoard;//Generic start/current state

    //List objects
    static PriorityQueue<PuzzleBoard> open = new PriorityQueue<>();//Nodes yet to be evaluated
    static ArrayList<PuzzleBoard> closed = new ArrayList<>();//Nodes already evaluated
    static Deque<PuzzleBoard> solutionPath = new LinkedList<>();//Nodes in solution
    // </editor-fold>

    public static void main(String[] args) {
        System.out.println("Program to solve the 8, 15, 24, or 36 Puzzle: Jack Kawell\n");
        boolean keepRunning = true;
        while (keepRunning) {
            getPuzzleAndValuesFromUser();
            PuzzleGameUserInput.pauseProgram();
            beginSearch();
            showSolution();
            PuzzleGameUserInput.pauseProgram();
            if (PuzzleGameUserInput.shouldRepeat()) {
                resetProgram();
            } else {
                keepRunning = false;
            }
        }
        endProgram();
    }

    private static void getPuzzleAndValuesFromUser() {
        //Get the puzzle size from the user
        int puzzleSize = PuzzleGameUserInput.getPuzzleSize();
        if (puzzleSize == 0) {
            endProgram();
        }
        //Build the goal board
        goalBoard = new PuzzleBoard(sideLength);
        goalBoard.generateGoalState();
        //Shuffle board or get user defined board from user
        int numberOfShuffleMoves = PuzzleGameUserInput.getNumberOfShuffleMoves();
        if (numberOfShuffleMoves == -1) {
            gameBoard = PuzzleGameUserInput.getUserDefinedBoard(puzzleSize, sideLength);
        } else {
            gameBoard = new PuzzleBoard(sideLength);
            gameBoard.generateGoalState();
            shuffleBoard(gameBoard, numberOfShuffleMoves);
        }
        //Ask user which search mode to use
        searchMode = PuzzleGameUserInput.getSearchMode();
        if (searchMode == 2) {
            useDepthPenalty = PuzzleGameUserInput.shouldUseDepthPenalty();
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

    private static void shuffleBoard(PuzzleBoard boardToShuffle, int maxShuffles) {
        int backtrackDirection = -1;
        int successfulShuffles = 0;
        while (successfulShuffles < maxShuffles) {
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
                    successfulShuffles++;
                }
            }
        }
    }

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

    private static void printBoard(PuzzleBoard boardToPrint) {
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                printCharacter(boardToPrint.board[i][j]);
            }
            System.out.println("");
        }
        System.out.println("(Raw score = " + boardToPrint.rawScore
                + "   Depth = " + boardToPrint.depth
                + "   Total score = " + boardToPrint.totalScore
                + ")");
        System.out.println("");
    }

    private static void printCharacter(int i) {
        System.out.print(" ");
        if (i == 0) {
            if (sideLength > 3) {
                System.out.print(" ");
            }
            System.out.print(" ");
        } else if (i > 9) {
            System.out.print(i);
        } else {
            if (sideLength > 3) {
                System.out.print(" ");
            }
            System.out.print(i);
        }
    }

    private static void showSolution() {
        boolean stepThroughSolution = PuzzleGameUserInput.shouldStepThroughSolution();
        int solutionLength = solutionPath.size();
        while (!solutionPath.isEmpty()) {
            printBoard(solutionPath.pop());
            if (stepThroughSolution) {
                PuzzleGameUserInput.pauseProgram();
            }
        }
        System.out.println((solutionLength - 1) + " moves out of " + (closed.size() - 1)
                + " moves considered (" + open.size() + " nodes left in OPEN)");
        System.out.println("");
    }

    private static void resetProgram() {
        goalBoard = null;
        gameBoard = null;
        sideLength = 0;
        searchMode = 0;
        open = new PriorityQueue<>();
        closed = new ArrayList<>();
        solutionPath = new LinkedList<>();
        useDepthPenalty = true;
        totalBoardsCreated = 0;
    }

    private static void endProgram() {
        System.out.println("\n\nExit...");
        System.exit(0);
    }
}
