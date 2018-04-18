/********************************************************************************
//*******************************************************************************
//Class:        PuzzleBoard
//Description:  This is the class object for the Kawell2 program solving the 8-puzzle.
//              It contains many helper methods for solving the puzzle.
**/
public class PuzzleBoard implements Comparable<PuzzleBoard> {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">
    public int depth;//The depth of the board in the tree
    public int parent;//The parent of the board in the tree
    public int sideLength;//The length of each board side
    public int rawScore;//The raw score calcualted by the heuristic
    public int totalScore;//The total score calculated by the heuristic
    public int[][] board;//The board array that holds the board data
    public Coordinate blank = new Coordinate();//The location of the blank
    // </editor-fold>

/*******************************************************************************
//Method:       PuzzleBoard
//Description:  A basic constructor that initializes the global variables.
//Parameters:   givenSideLength         the side length provided by the user
//Returns:      Nothing
//Calls:        Nothing
//Globals:      depth
//              parent
//              sideLength
//              rawScore
//              totalScore
//              board
//              blank
**/
    public PuzzleBoard(int givenSideLength) {
        depth = 0;
        parent = -1;
        sideLength = givenSideLength;
        rawScore = 0;
        totalScore = 0;
        board = new int[sideLength][sideLength];
        blank = new Coordinate(0, 0);
    }

/*******************************************************************************
//Method:       generateGoalState
//Description:  Generates the goal state from the side length.
//Parameters:   None
//Returns:      Nothing
//Calls:        Nothing
//Globals:      sideLength
//              board
//              blank
**/
    public void generateGoalState() {
        int tileNumber = 1;
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if (i != sideLength - 1 || j != sideLength - 1) {
                    board[i][j] = tileNumber;
                    tileNumber++;
                } else {
                    blank.row = i;
                    blank.col = j;
                }
            }
        }
    }

/*******************************************************************************
//Method:       moveTile
//Description:  Attempts to move the blank tile in the specified direction.
//Parameters:   direction           the direction to move the tile
//Returns:      boolean             success/failure
//Calls:        reassignValues
//Globals:      blank
**/
    public boolean moveTile(int direction) {
        Coordinate movingTileCoordinate;
        //0 = up, 1 = right, 2 = down, 3 = left
        switch (direction) {
            case 0:
                movingTileCoordinate = new Coordinate(blank.row - 1, blank.col);
                return reassignValues(movingTileCoordinate);
            case 1:
                movingTileCoordinate = new Coordinate(blank.row, blank.col + 1);
                return reassignValues(movingTileCoordinate);
            case 2:
                movingTileCoordinate = new Coordinate(blank.row + 1, blank.col);
                return reassignValues(movingTileCoordinate);
            case 3:
                movingTileCoordinate = new Coordinate(blank.row, blank.col - 1);
                return reassignValues(movingTileCoordinate);
            default:
                //Wrong direction
                break;
        }
        return false;
    }

/*******************************************************************************
//Method:       reassignValues
//Description:  Moves the tile (if valid) and reassigns all values.
//Parameters:   movingTileCoordinate    the tile that will move into the blank spot
//Returns:      boolean             success/failure
//Calls:        Nothing
//Globals:      sideLength
//              board
//              blank
**/
    public boolean reassignValues(Coordinate movingTileCoordinate) {
        //Check if the move is valid
        if (movingTileCoordinate.row >= 0 && movingTileCoordinate.row < sideLength
                && movingTileCoordinate.col >= 0 && movingTileCoordinate.col < sideLength) {
            //Pull out the value of the moving tile
            int movingTileValue = board[movingTileCoordinate.row][movingTileCoordinate.col];
            //Move the tile/blank
            board[blank.row][blank.col] = movingTileValue;
            board[movingTileCoordinate.row][movingTileCoordinate.col] = 0;
            //Relabel the row and col for the blank
            blank.row = movingTileCoordinate.row;
            blank.col = movingTileCoordinate.col;
            return true;
        } else {
            return false;
        }
    }

/*******************************************************************************
//Method:       deepCopy
//Description:  Deep copies the PuzzleBoard object into a new object.
//Parameters:   None
//Returns:      Nothing
//Calls:        copyBoardArray
//Globals:      sideLength
//              depth
//              parent
//              rawScore
//              totalScore
//              blank
**/
    public PuzzleBoard deepCopy() {
        PuzzleBoard copyPuzzleBoard = new PuzzleBoard(sideLength);
        copyPuzzleBoard.depth = this.depth;
        copyPuzzleBoard.parent = this.parent;
        copyPuzzleBoard.sideLength = this.sideLength;
        copyPuzzleBoard.rawScore = this.rawScore;
        copyPuzzleBoard.totalScore = this.totalScore;
        copyPuzzleBoard.board = copyBoardArray();
        copyPuzzleBoard.blank = new Coordinate(this.blank.row, this.blank.col);
        return copyPuzzleBoard;
    }

/*******************************************************************************
//Method:       copyBoardArray
//Description:  Copies the board array for deepCopy().
//Parameters:   None
//Returns:      int[][]     the copy array
//Calls:        Nothing
//Globals:      sideLength
//              board
**/
    private int[][] copyBoardArray() {
        int[][] copyArray = new int[sideLength][sideLength];
        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                copyArray[i][j] = this.board[i][j];
            }
        }
        return copyArray;
    }

/*******************************************************************************
//Method:       calculateHeuristic
//Description:  Calculates the heuristic score for the current board
//Parameters:   goalBoard       the goal state
//              searchMode      1=breadth; 2=best
//              breadthScore    the number of the node
//              useDepthPenalty whether to use the depth penalty in the heuristic
//Returns:      Nothing
//Calls:        Nothing
//Globals:      totalScore
//              rawScore
//              sideLength
//              board
//              depth
**/
    public void calculateHeuristic(PuzzleBoard goalBoard, int searchMode, int breadthScore, boolean useDepthPenalty) {
        switch (searchMode) {
            case 1:
                totalScore = breadthScore;
                break;
            case 2:
                rawScore = 0;
                for (int curRow = 0; curRow < sideLength; curRow++) {
                    for (int curCol = 0; curCol < sideLength; curCol++) {
                        int curTile = this.board[curRow][curCol];
                        int goalTile = goalBoard.board[curRow][curCol];
                        //End iteration if in correct spot
                        if (curTile != goalTile && curTile != 0) {
                            int goalRow = 0;
                            int goalCol = 0;
                            for (int k = 0; k < sideLength; k++) {
                                for (int l = 0; l < sideLength; l++) {
                                    if (curTile == goalBoard.board[k][l]) {
                                        goalRow = k;
                                        goalCol = l;
                                        break;
                                    }
                                }
                            }
                            int scoreToAdd = Math.abs(curRow - goalRow) + Math.abs(curCol - goalCol);
                            rawScore += scoreToAdd;
                        }
                    }
                }
                if (useDepthPenalty) {
                    totalScore = rawScore + depth;
                } else {
                    totalScore = rawScore;
                }
                break;
            default:
                System.out.println("Bad search mode");
                break;
        }
    }

/*******************************************************************************
//Method:       compareTo
//Description:  Compares two PuzzleBoard objects.
//Parameters:   other   the object to be compared to
//Returns:      int     -1, 0, or 1
//Calls:        Nothing
//Globals:      totalScore
**/
    @Override
    public int compareTo(PuzzleBoard other) {
        Integer score1 = this.totalScore;
        Integer score2 = other.totalScore;
        return score1.compareTo(score2);
    }

/*******************************************************************************
//Method:       equals
//Description:  Compares two PuzzleBoard objects for equality.
//Parameters:   other   the object to be compared to
//Returns:      boolean     true=equal; false=not equal
//Calls:        Nothing
//Globals:      sideLength
//              board
**/
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this.getClass() != other.getClass()) {
            return false;
        }

        PuzzleBoard that = (PuzzleBoard) other;
        for (int i = 0; i < this.sideLength; i++) {
            for (int j = 0; j < this.sideLength; j++) {
                if (this.board[i][j] != that.board[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

/********************************************************************************
//*******************************************************************************
//Class:        Coordinate
//Description:  This class contains coordinate values  as rows and columns.
**/
    public class Coordinate {

        public int row;
        public int col;

/*******************************************************************************
//Method:       Coordinate
//Description:  Empty constructor.
//Parameters:   None
//Returns:      Nothing
//Calls:        Nothing
//Globals:      None
**/
        public Coordinate() {

        }

/*******************************************************************************
//Method:       Coordinate
//Description:  Constructor with given values for row and col.
//Parameters:   int r, int c    Given row and col values.
//Returns:      Nothing
//Calls:        None
//Globals:      row, col
**/
        Coordinate(int r, int c) {
            row = r;
            col = c;
        }
    }
}