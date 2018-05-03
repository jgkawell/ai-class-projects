
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * ******************************************************************************
 * //*******************************************************************************
 * //Class: GamePlayer //Description: This class is the driver for playing the
 * game. It contains methods // for showing and getting setting values from the
 * user and making a move // in three different modes: manual, random, and
 * intelligent.
*
 */
public class GamePlayer {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">

    //Private variables
    private static KeyboardInputClass keyboardInput = new KeyboardInputClass();
    private static StringBuilder rowOptions = new StringBuilder("");
    private static StringBuilder colOptions = new StringBuilder("");
    private static GameBoard curBoard;
    private static int moveRow;
    private static int moveCol;

    //User defined variables
    private static int playType = 3;
    private static boolean useIterativeDeepeningSearch = true;
    private static int searchDepth = 8;
    public static long errorTime = 3000L;
    public static int cornerValue = 5000;
    public static int edgeValue = 50;
    public static int singleFlipValue = 10;
    public static int winValue = 100000;

    //Public variables
    public static char myColor;
    public static char opponentColor;
    public static String playerMove;
    public static long maxTime = 0L;
    public static int numRows;
    public static int numCols;
    public static boolean continueSearch;
    public static long startTime;

    //Info variables
    private static final Logger LOG = Logger.getLogger(GamePlayer.class.getName());

    //</editor-fold>

/*******************************************************************************
//Method:       showSettings
//Description:  Shows the settings to the user and asks if any of them should be
//              changed. Calls get settings if the user asks to change settings.
//Parameters:   None
//Returns:      Nothing
//Calls:        getSettings
//Globals:      playType
//              useIterativeDeepeningSearch
//              searchDepth
//              errorTime
//              cornerValue
//              singleFlipValue
//              winValue
//              keyboardInput
**/
    public static void showSettings() {
        System.out.println("Settings:");
        System.out.println("+++++++++++++++++++++++++++++++");
        switch (playType) {
            case 1:
                System.out.println("Manual mode");
                break;
            case 2:
                System.out.println("Random mode");
                break;
            case 3:
                System.out.println("Intelligent mode");
                if (useIterativeDeepeningSearch) {
                    System.out.println("Using iterative deepening search");
                } else {
                    System.out.println("Not using iterative deepening search");
                }
                System.out.println("The maximum search depth is: " + searchDepth);
                System.out.println("The leeway time is: " + errorTime);
                System.out.println("The corner value is: " + cornerValue);
                System.out.println("The edge value is: " + edgeValue);
                System.out.println("The single flip value is: " + singleFlipValue);
                System.out.println("The win value is: " + winValue);
                break;
            default:
                LOG.severe("Bad mode");
                break;
        }

        char input = keyboardInput.getCharacter(true, 'N', "YN", 1,
                "Change current settings? (Y/N: Default=N):");
        if (input == 'Y') {
            getSettings();
        }
    }

/*******************************************************************************
//Method:       getSettings
//Description:  Gets the new settings from the user if requested.
//Parameters:   None
//Returns:      Nothing
//Calls:        keyboardInput.getCharacter
//              keyboardInput.getInteger
//Globals:      playType
//              useIterativeDeepeningSearch
//              searchDepth
//              errorTime
//              cornerValue
//              singleFlipValue
//              winValue
//              keyboardInput
//              rowOptions
//              colOptions
//              numRows
//              numCols
//              LOG
**/
    private static void getSettings() {
        playType = keyboardInput.getInteger(true, 3, 1, 3,
                "Select method of play: 1=manual, 2=random, 3=intelligent (default=3)");

        switch (playType) {
            case 1:
                //Build the row and column letter options
                for (int i = 0; i < numRows; i++) {
                    rowOptions.append((char) (65 + i));
                }
                for (int i = 0; i < numCols; i++) {
                    colOptions.append((char) (65 + i));
                }
                break;
            case 2:
                break;
            case 3:
                char defaultResponse = 'N';
                if (useIterativeDeepeningSearch) {
                    defaultResponse = 'Y';
                }

                useIterativeDeepeningSearch = (keyboardInput.getCharacter(true, defaultResponse, "YN", 1,
                        "Use iterative deepening? (Y/N) (Default=)" + defaultResponse + ")") == 'Y');

                searchDepth = keyboardInput.getInteger(true, 8, 1, searchDepth,
                        "Select search depth: (default=" + searchDepth + ")");

                errorTime = 1000 * (long) keyboardInput.getInteger(true, (int) (errorTime / 1000), 0, Integer.MAX_VALUE,
                        "Set the value for the leeway time (seconds): (Default=" + (errorTime / 1000) + ")");

                cornerValue = keyboardInput.getInteger(true, cornerValue, 0, Integer.MAX_VALUE,
                        "Set the value for the corner value: (Default=" + cornerValue + ")");

                edgeValue = keyboardInput.getInteger(true, edgeValue, 0, Integer.MAX_VALUE,
                        "Set the value for the edge value: (Default=" + edgeValue + ")");

                singleFlipValue = keyboardInput.getInteger(true, singleFlipValue, 0, Integer.MAX_VALUE,
                        "Set the value for the single flip value: (Default=" + singleFlipValue + ")");

                winValue = keyboardInput.getInteger(true, winValue, 0, Integer.MAX_VALUE,
                        "Set the value for the win value: (Default=" + winValue + ")");

                break;
            default:
                LOG.severe("Bad play type");
                break;
        }
    }

/*******************************************************************************
//Method:       makeMove
//Description:  Calls the required methods to make a move for either manual,
//              random, or intelligent.
//Parameters:   pBoardArray             The board from the last move
//Returns:      char[][]                either pBoardArray (no move) or boardArray (move)
//Calls:        GameBoard
//              curBoard.generateChildren
//              manualMove
//              randomMove
//              intelligentMove
//              validMove
//Globals:      curBoard
//              myColor
//              opponentColor
//              startTime
//              playType
//              searchDepth
//              continueSearch
//              useIterativeDeepeningSearch
//              numRows
//              numCols
//              LOG
**/
    public static char[][] makeMove(char[][] pBoardArray) {
        //Set the current board
        curBoard = new GameBoard(-1, 0, 0, 0, myColor, opponentColor, pBoardArray);

        char[][] boardArray = null;

        //Start timer
        startTime = System.currentTimeMillis();

        //Choose which move to make based on play type
        switch (playType) {
            case 1:
                //Make a manual move
                curBoard.generateChildren(1, Integer.MAX_VALUE);
                if (!curBoard.childBoards.isEmpty()) {
                    boardArray = manualMove();
                }
                curBoard = null;
                break;
            case 2:
                //Make a random move
                curBoard.generateChildren(1, Integer.MAX_VALUE);
                if (!curBoard.childBoards.isEmpty()) {
                    boardArray = randomMove();
                }
                curBoard = null;
                break;
            case 3:

                int curSearchDepth = searchDepth;
                continueSearch = true;

                if (useIterativeDeepeningSearch) {

                    //Keep searching if the depth or time hasn't run out
                    while (continueSearch) {
                        //Reset the curBoard
                        curBoard = new GameBoard(-1, 0, 0, 0, myColor, opponentColor, pBoardArray);

                        //If search depth >= the max moves, stop after current search
                        if (curSearchDepth >= (numRows * numCols)) {
                            continueSearch = false;
                        }

                        //Make an intelligent move
                        curBoard.depth = 0;
                        curBoard.generateChildren(curSearchDepth, Integer.MAX_VALUE);

                        if (!curBoard.childBoards.isEmpty()) {
                            //Save move if search was completed or there is no current move
                            if (continueSearch || boardArray == null) {
                                boardArray = intelligentMove();
                            }
                        } else {
                            curBoard = null;
                        }

                        //Set current time and iterate counter
                        curSearchDepth++;
                    }
                } else {
                    //Make an intelligent move
                    curBoard.depth = 0;
                    curBoard.generateChildren(curSearchDepth, Integer.MAX_VALUE);

                    if (!curBoard.childBoards.isEmpty()) {
                        boardArray = intelligentMove();
                    } else {
                        curBoard = null;
                    }
                }

                //Print time and depth of search
                long curRuntime = System.currentTimeMillis() - startTime;
                System.out.println("Searched to depth: " + curSearchDepth);
                System.out.println("Time taken: " + curRuntime / 1000.0);

                break;
            default:
                //Error: bad play type
                LOG.severe("Bad play type.");
                break;
        }

        //Check to see if a move was chosen
        if (!validMove(boardArray)) {
            return pBoardArray;
        }

        return boardArray;
    }

/*******************************************************************************
//Method:       validMove
//Description:  Sets the move string if there is a valid move.
//Parameters:   pBoardArray             The board from the current move
//Returns:      boolean                 true if move, false if not move
//Calls:        nothing
//Globals:      moveRow
//              moveCol
//              playerMove
**/
    private static boolean validMove(char[][] pBoardArray) {
        char rowChar = (char) (65 + moveRow);
        char colChar = (char) (65 + moveCol);

        if (pBoardArray != null) {
            //Make the move for printing out
            playerMove = "(" + rowChar + ", " + colChar + ")";
        } else {
            playerMove = "";
            return false;
        }
        return true;
    }

/*******************************************************************************
//Method:       manualMove
//Description:  Prompts the user to make a manual move and validates input.
//Parameters:   none
//Returns:      char[][]                either the chosen board, or null (no move)
//Calls:        keyboardInput.getCharacter
//              curBoard.copyBoard
//Globals:      keyboardInput
//              rowOptions
//              colOptions
//              moveRow
//              moveCol
//              curBoard
//              maxTime
//              startTime
**/
    private static char[][] manualMove() {
        boolean keepTrying = true;
        while (keepTrying) {
            //Get the user's choice
            char rowChar = keyboardInput.getCharacter(true, 'A', rowOptions.toString(), 1,
                    "Choose a row for your move:");
            char colChar = keyboardInput.getCharacter(true, 'A', colOptions.toString(), 1,
                    "Choose a col for your move:");

            //Assign the row and col as integers
            moveRow = rowOptions.toString().indexOf(rowChar);
            moveCol = colOptions.toString().indexOf(colChar);

            for (int i = 0; i < curBoard.childBoards.size(); i++) {
                GameBoard curMove = curBoard.childBoards.get(i);
                //If the current valid move matches the chosen move
                if (curMove.moveRow == moveRow && curMove.moveCol == moveCol) {
                    return curBoard.copyBoard(curMove.board);
                }
            }

            Long curTime = System.currentTimeMillis();

            if (maxTime > curTime - startTime) {
                System.out.println("Invalid move... choose another move.");
            } else {
                keepTrying = false;
            }
        }
        return null;
    }

/*******************************************************************************
//Method:       randomMove
//Description:  Chooses a random move from the generated children.
//Parameters:   none
//Returns:      char[][]                the chosen board
//Calls:        curBoard.copyBoard
//Globals:      curBoard
//              moveRow
//              moveCol
**/
    private static char[][] randomMove() {
        //Get a random board
        int selection = ThreadLocalRandom.current().nextInt(0, curBoard.childBoards.size());
        GameBoard randomMove = curBoard.childBoards.get(selection);

        //Assign values
        moveRow = randomMove.moveRow;
        moveCol = randomMove.moveCol;
        return curBoard.copyBoard(randomMove.board);
    }

/*******************************************************************************
//Method:       intelligentMove
//Description:  Chooses the best move from the generated children.
//Parameters:   none
//Returns:      char[][]                the chosen board
//Calls:        curBoard.copyBoard
//Globals:      curBoard
//              moveRow
//              moveCol
**/
    private static char[][] intelligentMove() {
        //Get the best move
        GameBoard bestBoard = curBoard.getBestBoard();

        //Assign values
        moveRow = bestBoard.moveRow;
        moveCol = bestBoard.moveCol;
        return bestBoard.copyBoard(bestBoard.board);
    }
}
