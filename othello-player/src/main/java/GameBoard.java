import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/********************************************************************************
 //*******************************************************************************
 //Class:        GameBoard
 //Description:  This class is the board for playing the game. It contains methods
 //              for recursively creating the game tree and analyzing the tree using
 //              mini-max and alpha-beta cutoffs.
 **/
public class GameBoard {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">

    private static final Logger LOG = Logger.getLogger(GameBoard.class.getName());
    private final int numRows = GamePlayer.numRows;
    private final int numCols = GamePlayer.numCols;
    //Public variables
    public List<GameBoard> childBoards = new ArrayList<>();
    public char[][] board;
    public int moveRow;
    public int moveCol;
    public int depth;
    //Private variables
    private char myColor;
    private char opponentColor;
    private char[][] alterationBoard;
    private int score;
    private int gameId;
    private int alpha = Integer.MIN_VALUE;
    private int beta = Integer.MAX_VALUE;
    private int bestChildId;

    //</editor-fold>

    /*******************************************************************************
     //Method:       GameBoard
     //Description:  Constructor with all necessary values.
     //Parameters:   pGameId
     //              pMoveRow
     //              pMoveCol
     //              pDepth
     //              pMyColor
     //              pOpponentColor
     //              pBoard
     //Returns:      Nothing
     //Calls:        copyBoard
     //Globals:      gameId
     //              moveRow
     //              moveCol
     //              depth
     //              myColor
     //              opponentColor
     //              board
     **/
    GameBoard(int pGameId, int pMoveRow, int pMoveCol, int pDepth, char pMyColor, char pOpponentColor, char[][] pBoard) {
        gameId = pGameId;
        moveRow = pMoveRow;
        moveCol = pMoveCol;
        depth = pDepth;
        myColor = pMyColor;
        opponentColor = pOpponentColor;
        board = copyBoard(pBoard);
    }

    /*******************************************************************************
     //Method:       generateChildren
     //Description:  Master recursive generator for all children boards.
     //Parameters:   pMaxDepth               The max depth to generate tree
     //              pPruneValue             The prune value (alpha/beta) of parent board
     //Returns:      score                   The score of the board
     //Calls:        isEven
     //              checkMove
     //              pruneGameTree
     //Globals:      depth
     //              alpha
     //              beta
     //              childBoards
     //              opponentColor
     //              myColor
     //              opponentColor
     //              numRows
     //              numCols
     //              score
     **/
    public int generateChildren(int pMaxDepth, int pPruneValue) {
        //Set the depth for each child
        int childDepth = depth + 1;

        alpha = Integer.MIN_VALUE;
        beta = Integer.MAX_VALUE;

        //Assign color for children
        char moveColor;
        if (isEven(childDepth)) {
            moveColor = opponentColor;
        } else {
            moveColor = myColor;
        }

        //Loop through all board positions
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int totalCaptures = checkMove(row, col, moveColor);
                //Check if it is a valid move
                if (totalCaptures > 0) {
                    //Save the game board as a child board
                    int newGameId = childBoards.size();
                    GameBoard child = new GameBoard(newGameId, row, col, childDepth, myColor, opponentColor, alterationBoard);
                    childBoards.add(child);

                    //Prune the tree for intelligent search
                    if (pruneGameTree(pMaxDepth, child, pPruneValue)) {
                        return score;
                    }
                }
            }
        }


        return score;
    }

    /*******************************************************************************
     //Method:       pruneGameTree
     //Description:  Prunes the tree based off of alpha/beta values.
     //Parameters:   pMaxDepth               The max depth to generate tree
     //              pChild                  The current child of the current board
     //              pPruneValue             The prune value (alpha/beta) of parent board
     //Returns:      boolean                 true=prune, false=don't prune
     //Calls:        isEven
     //              evaluateForScore
     //Globals:      depth
     //              alpha
     //              beta
     //              score
     //              bestChildId
     //              gameId
     **/
    private boolean pruneGameTree(int pMaxDepth, GameBoard pChild, int pPruneValue) {
        //Pull out alpha/beta value for pruning
        int pruneValue;
        if (isEven(depth)) {                                    //MAX
            pruneValue = alpha;
        } else {                                                //MIN
            pruneValue = beta;
        }

        //Check and set the new beta and alpha values
        //Uses a recursive call to generate children depth-first
        int childScore = pChild.evaluateForScore(pMaxDepth, pruneValue);
        if (isEven(depth)) {                    //MAX
            //Increase the alpha value
            if (childScore > alpha) {
                alpha = childScore;
                score = alpha;
                bestChildId = pChild.gameId;
            }

            //Prune the branch
            if (pPruneValue <= alpha) {
                score = alpha;
                return true;
            }
        } else {                                //MIN
            //Decrease the beta value
            if (childScore < beta) {
                beta = childScore;
                score = beta;
                bestChildId = pChild.gameId;
            }

            //Prune the branch
            if (beta <= pPruneValue) {
                score = beta;
                return true;
            }
        }
        return false;
    }

    /*******************************************************************************
     //Method:       evaluateForScore
     //Description:  Evaluates the board based off of depth and time to find the score
     //              or keep search depth-first.
     //Parameters:   pMaxDepth               The max depth to generate tree
     //              pPruneValue             The prune value (alpha/beta) of parent board
     //Returns:      int                     The score of the board
     //Calls:        isTimeUp
     //              generateChildren
     //              calculateScore
     //              isEven
     //Globals:      depth
     //              alpha
     //              beta
     //              score
     //              LOG
     **/
    private int evaluateForScore(int pMaxDepth, int pPruneValue) {
        //Make sure to stop at max depth
        if (depth < pMaxDepth && !isTimeUp()) {
            score = generateChildren(pMaxDepth, pPruneValue);
            return score;
        } else if (depth <= pMaxDepth) {
            //Find the score and set alpha or beta value if the child is a leaf node
            calculateScore();

            //Set new alpha/beta values
            if (isEven(depth)) {            //MAX
                alpha = score;
                return alpha;
            } else {                        //MIN
                beta = score;
                return beta;
            }
        } else {
            LOG.warning("Past the max depth of tree");
            return -1;
        }
    }

    /*******************************************************************************
     //Method:       checkMove
     //Description:  Evaluates the board to find if the given move is valid. Does this
     //              by calling check for captures in all 8 directions.
     //Parameters:   pRow                    The row of the move
     //              pCol                    The col of the move
     //              pColor                  The current player's color
     //Returns:      int                     The number of tiles flipped by the move
     //Calls:        copyBoard
     //              checkForCaptures
     //Globals:      alterationBoard
     //              board
     **/
    private int checkMove(int pRow, int pCol, char pColor) {
        //Initialize variables for checking algorithm
        alterationBoard = copyBoard(board);
        int totalCaptures = 0;

        //If the current position is taken, return false
        char curPosition = alterationBoard[pRow][pCol];
        if (curPosition == 'B' || curPosition == 'W') {
            return 0;
        }

        //Change the position to the player's color
        alterationBoard[pRow][pCol] = pColor;

        //Check in all 8 directions
        for (int direction = 1; direction <= 8; direction++) {
            totalCaptures += checkForCaptures(pRow, pCol, pColor, -1, direction);
        }

        return totalCaptures;
    }

    /*******************************************************************************
     //Method:       checkForCaptures
     //Description:  Checks to see if there are any captures in a specific direction
     //              from a specific location and flips pieces if so (recursive).
     //Parameters:   pRow                    The row of the move
     //              pCol                    The col of the move
     //              pColor                  The current player's color
     //              pCaptureCount           The current number of captures
     //              pDirection              The direction to search
     //Returns:      int                     The number of tiles flipped by the move
     //Calls:        getCaptureCount
     //Globals:      alterationBoard
     //              numRows
     //              numCols
     **/
    private int checkForCaptures(int pRow, int pCol, char pColor, int pCaptureCount, int pDirection) {

        //Make sure the location is within bounds
        if (0 <= pRow && pRow < numRows && 0 <= pCol && pCol < numCols) {
            //Pull out the current character
            char curChar = alterationBoard[pRow][pCol];

            //Only check if it is not the starting position
            if (pCaptureCount > -1) {
                //Check if the current piece is the player's piece
                if (curChar == pColor) {
                    //Check if at least one opponent piece is between
                    if (pCaptureCount > 0) {
                        alterationBoard[pRow][pCol] = pColor;
                        return pCaptureCount;
                    } else {
                        //If there are no opponent pieces between, return false
                        return 0;
                    }
                } else if (curChar == ' ') {
                    //If the current position is blank, then it's not a valid move
                    return 0;
                } else {
                    //The position is the opponent's piece so increment the capture count
                    pCaptureCount++;
                }
            } else {
                //Increment the counter after the first position
                pCaptureCount++;
            }

            //Check the different directions and get the capture count
            return getCaptureCount(pRow, pCol, pColor, pCaptureCount, pDirection);
        } else {
            //If the positions is off the board then it's not a valid move
            return 0;
        }
    }

    /*******************************************************************************
     //Method:       getCaptureCount
     //Description:  Runs a switch to separate the different direction calls and alter
     //              their directions accordingly.
     //Parameters:   pRow                    The row of the move
     //              pCol                    The col of the move
     //              pColor                  The current player's color
     //              pCaptureCount           The current number of captures
     //              pDirection              The direction to search
     //Returns:      int                     The number of tiles flipped by the move
     //Calls:        checkForCaptures
     //Globals:      alterationBoard
     //              LOG
     **/
    private int getCaptureCount(int pRow, int pCol, char pColor, int pCaptureCount, int pDirection) {
        //Recursive call based on the direction given
        switch (pDirection) {
            case 1:         //NORTH
                pCaptureCount = checkForCaptures(pRow - 1, pCol, pColor, pCaptureCount, pDirection);
                break;
            case 2:         //NORTH EAST
                pCaptureCount = checkForCaptures(pRow - 1, pCol + 1, pColor, pCaptureCount, pDirection);
                break;
            case 3:         //EAST
                pCaptureCount = checkForCaptures(pRow, pCol + 1, pColor, pCaptureCount, pDirection);
                break;
            case 4:         //SOUTH EAST
                pCaptureCount = checkForCaptures(pRow + 1, pCol + 1, pColor, pCaptureCount, pDirection);
                break;
            case 5:         //SOUTH
                pCaptureCount = checkForCaptures(pRow + 1, pCol, pColor, pCaptureCount, pDirection);
                break;
            case 6:         //SOUTH WEST
                pCaptureCount = checkForCaptures(pRow + 1, pCol - 1, pColor, pCaptureCount, pDirection);
                break;
            case 7:         //WEST
                pCaptureCount = checkForCaptures(pRow, pCol - 1, pColor, pCaptureCount, pDirection);
                break;
            case 8:         //NORTH WEST
                pCaptureCount = checkForCaptures(pRow - 1, pCol - 1, pColor, pCaptureCount, pDirection);
                break;
            default:
                LOG.warning("Bad direction");
                break;
        }

        //If the recursive call returns true, change the tile and return true
        if (pCaptureCount > 0) {
            alterationBoard[pRow][pCol] = pColor;
            return pCaptureCount;
        } else {
            //Return not valid up the recursive stack
            return 0;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    /*******************************************************************************
     //Method:       getBestBoard
     //Description:  Pulls out the best board based on id.
     //Parameters:   none
     //Returns:      GameBoard               The best child board
     //Calls:        nothing
     //Globals:      childBoards
     //              bestChildId
     **/
    public GameBoard getBestBoard() {
        //Pull out the best board
        return childBoards.get(bestChildId);
    }

    /*******************************************************************************
     //Method:       isEven
     //Description:  Decides if the depth is even or not for mini-max.
     //Parameters:   pDepth                  The current depth
     //Returns:      boolean                 true=even, false=odd
     //Calls:        nothing
     //Globals:      none
     **/
    private boolean isEven(int pDepth) {
        boolean even;
        //Assign MAX or MIN: EVEN = MAX, ODD = MIN
        even = (pDepth % 2) == 0;
        return even;
    }

    /*******************************************************************************
     //Method:       copyBoard
     //Description:  Copies a given board
     //Parameters:   pBoard                  The board to copy
     //Returns:      newBoard                The copy of the board
     //Calls:        nothing
     //Globals:      numRows
     //              numCols
     **/
    public char[][] copyBoard(char[][] pBoard) {
        char[][] newBoard = new char[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            System.arraycopy(pBoard[i], 0, newBoard[i], 0, numCols);
        }
        return newBoard;
    }

    /*******************************************************************************
     //Method:       isTimeUp
     //Description:  Checks to see if the time is up.
     //Parameters:   nothing
     //Returns:      boolean                 true=time is up, false=still time left
     //Calls:        nothing
     //Globals:      GamePlayer.startTime
     //              GamePlayer.maxTime
     //              GamePlayer.errorTime
     //              GamePlayer.continueSearch
     **/
    private boolean isTimeUp() {
        long curRuntime = System.currentTimeMillis() - GamePlayer.startTime;
        long allowedTime = GamePlayer.maxTime - GamePlayer.errorTime;

        if (curRuntime >= allowedTime) {
            GamePlayer.continueSearch = false;
            return true;
        } else {
            return false;
        }
    }

    /*******************************************************************************
     //Method:       calculateScore
     //Description:  Calculates the score of the board.
     //Parameters:   none
     //Returns:      nothing
     //Calls:        isCorner
     //              isEdge
     //              isJustOffEdge
     //              isWinningBoard
     //Globals:      score
     //              numRows
     //              numCols
     //              board
     //              myColor
     //              opponentColor
     //              cornerValue
     //              edgeValue
     //              singleFlipValue
     //              winValue
     **/
    private void calculateScore() {
        score = 0;
        int myCount = 0;
        int opponentCount = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (board[row][col] == myColor) {
                    myCount++;

                    //Check for corners and edges
                    if (isCorner(row, col)) {
                        score += GamePlayer.cornerValue;
                    } else if (isEdge(row, col)) {
                        score += GamePlayer.edgeValue;
                    } else if (isJustOffEdge(row, col)) {
                        score -= GamePlayer.singleFlipValue;
                    } else {
                        score += GamePlayer.singleFlipValue;
                    }

                } else if (board[row][col] == opponentColor) {
                    opponentCount++;

                    //Check for corners and edges
                    if (isCorner(row, col)) {
                        score -= GamePlayer.cornerValue;
                    } else if (isEdge(row, col)) {
                        score -= GamePlayer.edgeValue;
                    } else if (isJustOffEdge(row, col)) {
                        score += GamePlayer.singleFlipValue;
                    } else {
                        score -= GamePlayer.singleFlipValue;
                    }
                }
            }
        }

        //Check if winning board
        if (isWinningBoard(myCount, opponentCount)) {
            score += GamePlayer.winValue;
        } else if (isWinningBoard(opponentCount, myCount)) {
            score -= GamePlayer.winValue;
        }
    }

    /*******************************************************************************
     //Method:       isJustOffEdge
     //Description:  Checks to see if the position is just off from the edge.
     //Parameters:   pRow, pCol              The position
     //Returns:      boolean
     //Calls:        nothing
     //Globals:      numRows
     //              numCols
     **/
    private boolean isJustOffEdge(int pRow, int pCol) {
        return pRow == 1
                || pRow == numRows - 2
                || pCol == 1
                || pCol == numCols - 2;
    }

    /*******************************************************************************
     //Method:       isEdge
     //Description:  Checks to see if the position is on the edge.
     //Parameters:   pRow, pCol              The position
     //Returns:      boolean
     //Calls:        nothing
     //Globals:      numRows
     //              numCols
     **/
    private boolean isEdge(int pRow, int pCol) {
        return pRow == 0
                || pRow == numRows - 1
                || pCol == 0
                || pCol == numCols - 1;
    }

    /*******************************************************************************
     //Method:       isCorner
     //Description:  Checks to see if the position is a corner.
     //Parameters:   pRow, pCol              The position
     //Returns:      boolean
     //Calls:        nothing
     //Globals:      numRows
     //              numCols
     **/
    private boolean isCorner(int pRow, int pCol) {
        return (pRow == 0 && pCol == 0)
                || (pRow == 0 && pCol == numCols - 1)
                || (pRow == numRows - 1 && pCol == 0)
                || (pRow == numRows - 1 && pCol == numCols - 1);

    }

    /*******************************************************************************
     //Method:       isWinningBoard
     //Description:  Checks to see if the board is a winning state.
     //Parameters:   pRow, pCol              The position
     //Returns:      boolean
     //Calls:        nothing
     //Globals:      numRows
     //              numCols
     **/
    private boolean isWinningBoard(int pCount1, int pCount2) {
        //Finds if pCount1 has a winning board
        if (pCount2 == 0) {
            return true;
        } else if (pCount1 + pCount2 >= numCols * numRows && pCount1 > pCount2) {
            return true;
        } else {
            return false;
        }
    }

    //</editor-fold>
}
