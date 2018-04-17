/**
//Program:      maze_runner.java
//Course:       COSC470
//Description:  This program finds the shortest solution to escape from a maze
//              using a recursive algorithm. Per the user's choice, it can
//              display the maze and the solution progress using either a text-
//              based output or a graphical output. It utilizes depth-first
//              search with a branch-and-cut limit that kills any path longer
//              than the shortest known solution.
//Author:       Jack Kawell
//Revised:      2/13/18
//Language:     Java
//IDE:          NetBeans 8.2
//*******************************************************************************
//******************************************************************************/

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

/**
//Class:        maze_runner
//Description:  This class contains all the necessary methods to solve the maze.
//              It also calls KeyboardInputClass to get input from the user,
//              ImageDisplay for graphical output, and Coordinate to store the
//              path information.
**/
public class MazeRunner {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">
    //Basic maze values
    private static char[][] masterMaze;
    private static int numRows;
    private static int numCols;
    private static int startingRow;
    private static int startingCol;

    //Path data values
    private static Deque<Coordinate> currentPath = new LinkedList();
    private static Deque<Coordinate> shortestSolutionPath = new LinkedList();
    private static int shortestSolutionLength = 0;
    private static int totalCellsVisited = 0;

    //User necessary values
    private static int displayType; //1 = text view, 2 = graphic view
    private static boolean useBranchAndBound = false; //True if user chooses branch & bound
    private static boolean showEachStep = false; //True if user chooses to see each step
    private static boolean pauseBetweenSteps = false; //True if user chooses to pause each step

    //Instances of given classes
    private static KeyboardInputClass keyboardInput = new KeyboardInputClass();
    private static ImageDisplay graphicView; //Instance of ImageDisplay for graphic view
    // </editor-fold>

/*******************************************************************************
//Method:       main
//Description:  Main method that calls all other relevant methods within an
//              ongoing loop that is stopped by the user.
//Parameters:   None
//Returns:      Nothing
//Calls:        getMazeAndValuesFromUser
//              navigateMaze
//              copyMaze
//              showSolution
//              requestRepeatFromUser
//Globals:      startingRow
//              startingCol
//              masterMaze
**/
    public static void main(String[] args) {
        System.out.println("Shortest Maze Solution Finder: Jack Kawell\n");

        boolean keepRunning = true;
        while (keepRunning) {
            //Get the maze file and user defined values
            getMazeAndValuesFromUser();

            //Begin navigation
            navigateMaze(startingRow, startingCol, copyMaze(masterMaze));

            //Show the solution
            showSolution();

            //Ask if user would like to continue with another maze
            boolean repeat = requestRepeatFromUser();

            if (repeat) {
                resetProgram();
            } else {
                keepRunning = false;
            }
        }

        endProgram();
    }

/*******************************************************************************
//Method:       getMazeAndValuesFromUser
//Description:  Requests many values from the user such as display type and
//              whether to show each step of the maze traversal.
//              Also calls retrieveAndBuildMaze and printMaze to both build
//              and print the maze respectively.
//              Does a check on whether the maze contains a possible starting
//              point.
//Parameters:   None
//Returns:      Nothing
//Calls:        keyboardInput.getInteger
//              retrieveAndBuildMaze
//              printMaze
//              getColorMaze
//              ImageDisplay (constructor)
//              keyboardInput.getCharacter
//              endProgram
//Globals:      displayType
//              keyboardInput
//              masterMaze
//              graphicView
//              numRows, numCols
//              startingRow, startingCol
//              showEachStep
**/
    private static void getMazeAndValuesFromUser() {
        //Get the maze file from the user
        retrieveAndBuildMaze();

        //Get the display type from the user
        displayType = keyboardInput.getInteger(true, 1, 1, 2, "Display type:"
                + "\n1 = text"
                + "\n2 = graphic");

        if (displayType == 1) {
            //Print the maze for the user to see
            System.out.println("This is the maze you've chosen:");
            printMaze(masterMaze);
        } else {
            int scaleFactor = keyboardInput.getInteger(false, 20, 1, 500000,
                    "Type a scaling factor... (default: 20)");
            graphicView = new ImageDisplay(numRows, numCols, scaleFactor, "Maze");
            graphicView.image = getColorMaze(masterMaze);
        }

        char keepRunning = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                "Would you like to continue? (Y/N)");

        if (keepRunning == 'Y') {
            //Check if starting position is on a path
            char charToCheck = masterMaze[startingRow][startingCol];
            boolean isValidStart = Character.compare(charToCheck, '1') != 0;
            if (isValidStart) {
                //Ask user whether to use branch & bound
                char input = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                        "Would you like to use branch and bound? (Y/N)");
                useBranchAndBound = input == 'Y';
                //Ask user about showing every step
                input = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                        "Would you like to see each step? (Y/N)");
                showEachStep = input == 'Y';
                if (showEachStep) {
                    //Ask user about pausing between steps
                    input = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                            "Would you like to pause between steps? (Y/N)");
                    pauseBetweenSteps = input == 'Y';
                }
            } else {
                System.out.println("The beginning location is invalid.\n");
                endProgram();
            }
        } else {
            endProgram();
        }
    }

/*******************************************************************************
//Method:       retrieveAndBuildMaze
//Description:  Prompts the user for a file name and reads in the file to create
//              the masterMaze char[][] array.
//Parameters:   None
//Returns:      Nothing
//Calls:        textFile.getFileName
//              textFile.getFileContents
//Globals:      numRows, numCols
//              startingRow, startingCol
//              masterMaze
**/
    private static void retrieveAndBuildMaze() {
        TextFileClass textFile = new TextFileClass();

        boolean workingFile = false;
        while (!workingFile){
            textFile.getFileName("Specify the text file to be read:", "-1");
            //Check to make sure that the user has entered a file name
            if (textFile.fileName.length() > 0) {
                textFile.getFileContents();
                if (textFile.lineCount != 0){
                    workingFile = true;
                }
            }
        }

        //Pull in values fom text file
        String[] textFileAsStringArray = textFile.text;
        numRows = Integer.parseInt(textFileAsStringArray[0]);
        numCols = Integer.parseInt(textFileAsStringArray[1]);
        startingRow = Integer.parseInt(textFileAsStringArray[2]);
        startingCol = Integer.parseInt(textFileAsStringArray[3]);

        //Initialize with sizes
        masterMaze = new char[numRows][numCols];

        //This is to change the string array into a 2D character array
        //We start at index 4 so that we skip the first 4 lines that hold the
        //values of the # of rows, # of columns, and the starting coordinates
        for (int i = 0; i < numRows; i++) {
            String currentString = textFileAsStringArray[i + 4];
            for (int j = 0; j < numCols; j++) {
                char currentCharacter = currentString.charAt(j);
                masterMaze[i][j] = currentCharacter;
            }
        }
    }

/*******************************************************************************
//Method:       printMaze
//Description:  Prints the maze to the console window.
//Parameters:   currentMaze     The maze as a char[][] to be printed
//Returns:      Nothing
//Calls:        None
//Globals:      numRows, numCols
**/
    private static void printMaze(char[][] currentMaze) {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                switch (currentMaze[i][j]) {
                    case '1':
                        //System.out.print('#'); //For NetBeans testing
                        System.out.print('\u2588');
                        break;
                    case '0':
                        //System.out.print(' '); //For NetBeans testing
                        System.out.print('\u0020');
                        break;
                    case '+':
                        System.out.print('+');
                        break;
                    case 'X':
                        System.out.print('X');
                        break;
                    default:
                        System.out.println("ERROR: incorrect character");
                        break;
                }
            }
            System.out.println("");
        }
        System.out.println("");
    }

/*******************************************************************************
//Method:       getColorMaze
//Description:  Creates a color maze out of a given char[][] array for graphical
//              output.
//Parameters:   maze        The maze to be converted to a Color[][]
//Returns:      colorMaze   The new color maze
//Calls:        None
//Globals:      numRows, numCols
**/
    private static Color[][] getColorMaze(char[][] maze) {
        Color[][] colorMaze = new Color[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Color tempColor = Color.RED;
                switch (maze[i][j]) {
                    case '1':
                        tempColor = Color.DARK_GRAY;
                        break;
                    case '0':
                        tempColor = Color.WHITE;
                        break;
                    case '+':
                        tempColor = Color.RED;
                        break;
                    case 'X':
                        tempColor = Color.BLUE;
                        break;
                    default:
                        break;
                }
                colorMaze[i][j] = tempColor;
            }
        }
        return colorMaze;
    }

/*******************************************************************************
//Method:       copyMaze
//Description:  Creates a deep copy of a given maze.
//Parameters:   mazeToCopy  The maze to be deep copied
//Returns:      copyOfMaze  The new copied maze
//Calls:        None
//Globals:      numRows, numCols
**/
    private static char[][] copyMaze(char[][] mazeToCopy) {
        char[][] copyOfMaze = new char[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                copyOfMaze[i][j] = mazeToCopy[i][j];
            }
        }
        return copyOfMaze;
    }

/*******************************************************************************
//Method:       navigateMaze
//Description:  The recursive method that digs through the maze to find the
//              shortest solution.
//              It keeps track of the shortest path and cuts any branch longer.
//Parameters:   currentRow, currentCol  The current row and col of the search
//              currentMaze             The current maze including the path
//Returns:      Nothing
//Calls:        printMaze
//              getColorMaze
//              keyboardInput.getKeyboardInput
//              showCurrentMaze
//              navigateMaze
//Globals:      numRows, numCols
//              useBranchAndBound
//              currentPath
//              shortestSolutionLength
//              totalCellsVisited
//              showEachStep
//              shortestSolutionPath
**/
    private static void navigateMaze(int currentRow, int currentCol, char[][] currentMaze) {
        //First check to make sure the position is within bounds
        if ((currentRow >= 0 && currentRow < numRows)
                && (currentCol >= 0 && currentCol < numCols)) {
            //Save the current character
            char currentChar = currentMaze[currentRow][currentCol];

            //Check if the location is an eligible path
            boolean validLocation = Character.compare(currentChar, '0') == 0;

            boolean possibleShortest = true;
            if (useBranchAndBound) {
                //Check if the path is short enough
                possibleShortest = currentPath.isEmpty()
                        || shortestSolutionLength == 0
                        || currentPath.size() < shortestSolutionLength;
            }

            if (validLocation && possibleShortest) {
                //Change the current char to the maze runner char
                currentMaze[currentRow][currentCol] = 'X';
                //Add current location to path stack
                currentPath.push(new Coordinate(currentRow, currentCol));
                //Increment the total cells visited
                totalCellsVisited++;

                //Show the step to the user if requested
                if (showEachStep) {
                    showCurrentMaze(currentMaze);
                }

                //Change the current char to the path char
                currentMaze[currentRow][currentCol] = '+';

                navigateMaze(currentRow - 1, currentCol, currentMaze);//UP
                navigateMaze(currentRow, currentCol + 1, currentMaze);//RIGHT
                navigateMaze(currentRow + 1, currentCol, currentMaze);//DOWN
                navigateMaze(currentRow, currentCol - 1, currentMaze);//LEFT

                //Change the current char back to blank
                currentMaze[currentRow][currentCol] = '0';
                //Remove the last location from the stack
                currentPath.pop();
            } else if (validLocation) {
                //Increment the total cells visited
                totalCellsVisited++;
                //Change the current char to the maze runner char
                currentMaze[currentRow][currentCol] = 'X';
                //Tell user the path is canceled
                if (showEachStep) {
                    System.out.println("Current path is longer than shortest solution."
                            + "\nBacktracking...");
                    showCurrentMaze(currentMaze);
                }
                //Change the current char back to blank
                currentMaze[currentRow][currentCol] = '0';
            }
        } else {
            //Make sure it is the shortest maze if B&B isn't used
            if (currentPath.size() < shortestSolutionLength || shortestSolutionLength == 0) {
                //Save shortest path length
                shortestSolutionLength = currentPath.size();

                //Clear any old paths
                if (!shortestSolutionPath.isEmpty()) {
                    shortestSolutionPath.clear();
                }

                //Make a copy of the solution path
                for (int i = 0; i < currentPath.size(); i++) {
                    Coordinate curPosition = currentPath.removeFirst();
                    shortestSolutionPath.push(curPosition);
                    currentPath.addLast(curPosition);
                }

                //Show solution to user
                if (showEachStep) {
                    System.out.println("Solution found!!! Length is: "
                            + (shortestSolutionLength) + "\n");
                    showCurrentMaze(currentMaze);
                }
            }
        }
    }

/*******************************************************************************
//Method:       showCurrentMaze
//Description:  Displays the current maze to the user.
//Parameters:   maze  The maze to be displayed
//Returns:      Nothing
//Calls:        printMaze
//              getColorMaze
//              keyboardInput.getKeyboardInput
//Globals:      displayType
//              graphicView
//              keyboardInput
**/
    private static void showCurrentMaze(char[][] maze) {
        if (displayType == 1) {
            printMaze(maze);
        } else {
            graphicView.image = getColorMaze(maze);
        }
        if (pauseBetweenSteps) {
            keyboardInput.getKeyboardInput("Press ENTER to continue...");
        }
    }

/*******************************************************************************
//Method:       showSolution
//Description:  Adds the solution to the master maze and displays it to the user.
//Parameters:   None
//Returns:      Nothing
//Calls:        printMaze
//              getColorMaze
//Globals:      shortestSolutionPath
//              masterMaze
//              shortestSolutionLength
//              totalCellsVisited
//              displayType
//              graphicView
**/
    private static void showSolution() {
        if (shortestSolutionLength != 0){
            //Copy solution path onto the master maze
            for (int i = 0; i < shortestSolutionPath.size(); i++) {
                Coordinate curPosition = shortestSolutionPath.removeFirst();
                int r = curPosition.getRow();
                int c = curPosition.getCol();
                masterMaze[r][c] = '+';
                shortestSolutionPath.addLast(curPosition);
            }

            System.out.println("Shortest path length: " + (shortestSolutionLength)
                    + "\nTotal cells visited: " + totalCellsVisited
                    + "\nThe shortest solution path is:\n");
            //Print final solution
            if (displayType == 1) {
                printMaze(masterMaze);
            } else {
                graphicView.image = getColorMaze(masterMaze);
            }
        } else {
            System.out.println("No solutions found.\n");
        }

    }

/*******************************************************************************
//Method:       requestRepeatFromUser
//Description:  Checks to see if the user would like to run another maze.
//Parameters:   None
//Returns:      Nothing
//Calls:        keyboardInput.getCharacter
//              endProgram
//              resetProgram
//Globals:      keyboardInput
**/
    private static boolean requestRepeatFromUser() {
        char input = keyboardInput.getCharacter(true, 'Y', "YN", 1,
                "\nWould you like to choose another maze? (Y/N)");
        return input != 'N';
    }

/*******************************************************************************
//Method:       resetProgram
//Description:  Resets the values for the next maze run.
//Parameters:   None
//Returns:      Nothing
//Calls:        graphicView.closeImageDisplay
//Globals:      displayType
//              graphicView
//              currentPath
//              shortestSolutionPath
//              shortestSolutionLength
//              totalCellsVisited
**/
    private static void resetProgram() {
        //Close maze window if graphic display is chosen
        if (displayType == 2) {
            graphicView.closeImageDisplay();
        }
        //Reset the variables for a restart
        currentPath.clear();
        shortestSolutionPath.clear();
        shortestSolutionLength = 0;
        totalCellsVisited = 0;
    }

/*******************************************************************************
//Method:       endProgram
//Description:  Closes any open windows and ends the program.
//Parameters:   None
//Returns:      Nothing
//Calls:        graphicView.closeImageDisplay
//Globals:      displayType
//              graphicView
**/
    private static void endProgram() {
        //Close maze window if gaphic display is chosen
        if (displayType == 2) {
            graphicView.closeImageDisplay();
        }
        System.out.println("\n\nExit...");
        System.exit(0);
    }
}

/********************************************************************************
//*******************************************************************************
//Class:        Coordinate
//Description:  This class contains coordinate values  as rows and columns.
**/
class Coordinate {

    private int row;
    private int col;

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

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
