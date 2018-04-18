/**
//Program:      shape_finder.java
//Course:       COSC470
//Description:  A program that takes in grey-scale images with shapes overlaid on
//              and finds and labels the squares, rectangles, and circles in the
//              image.
//Author:       Jack Kawell
//Revised:      3/29/18
//Language:     Java
//IDE:          NetBeans 8.2
//*******************************************************************************
//******************************************************************************/

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ShapeFinder {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">

    private static final Logger LOGGER = Logger.getLogger(ShapeFinder.class.getName());
    private static KeyboardInputClass keyboardInput = new KeyboardInputClass();
    private static EasyImageDisplay image;          //image display object
    private static Regions regionFinder;            //region class object
    private static int width;                       //width of image
    private static int height;                      //height of image
    private static int greyScaleTolerance;
    private static int minRegionSize;
    private static int maxRegionSize;
    private static int tracerThreshold;             //threshold for tracers (see below)
    private static int circleThreshold;
    private static int rectangleThreshold;
    private static int neighborhood;
    private static boolean showCentroids = true;
    private static ArrayList<Integer> regionLabels = new ArrayList<>(); //labels for regions
    private static ArrayList<Integer> regionShapes = new ArrayList<>(); //Types: square=1, rectangle=2, circle=3

    // </editor-fold>

    public static void main(String[] args) {
        System.out.println("Imbedded Shape Finder: Jack Kawell\n\n");

        boolean keepRunning = true;
        while (keepRunning) {
            getUserInput();

            initialDisplayAndAnalysis();

            findShapes();

            displayFinalImage();

            pauseProgram();

            boolean endProgram = 'Y' == keyboardInput.getCharacter(true, 'N', "YN", 1, "End program? (Y/N: default = N):");
            if (endProgram) {
                keepRunning = false;
            } else {
                resetProgram();
            }
        }
        endProgram();
    }

    private static void runRegionsMethods() {
        System.out.println("Finding regions...");
        regionFinder = new Regions(image.imageType, width, height, image.pixels, neighborhood, false, greyScaleTolerance);
        regionFinder.findRegions();
        regionFinder.filterRegions(minRegionSize, maxRegionSize, false, 0);
        regionFinder.computeRegionProperties();
    }

    private static void displayImage(String imageName, int[][] array, int type, int labelColor) {
        int[][] redArray = null;
        int[][] greenArray = null;
        int[][] blueArray = null;
        //1 = color conversion, 2 = no color conversion
        switch (type) {
            case 1:
                ArrayList<int[][]> colorArrays = overlayRegions(image.pixels, null, null, null, array, labelColor);
                redArray = colorArrays.get(0);
                greenArray = colorArrays.get(1);
                blueArray = colorArrays.get(2);
                break;
            case 2:
                break;
            default:
                LOGGER.severe("Bad type");
                break;
        }

        EasyImageDisplay imageDisplayLabeled = new EasyImageDisplay(type, image.imageWidth, image.imageHeight,
                redArray, greenArray, blueArray, array);
        System.out.println(imageName + ":");
        imageDisplayLabeled.showImage(imageName, true);
        keyboardInput.getKeyboardInput("Press ENTER to close image...");
        imageDisplayLabeled.closeImageDisplay();
    }

    private static ArrayList<int[][]> overlayRegions(int[][] oImage, int[][] oRed, int[][] oGreen, int[][] oBlue, int[][] regionedImage, int labelColor) {
        int[][] red = new int[height][width];
        int[][] green = new int[height][width];
        int[][] blue = new int[height][width];
        ArrayList<int[][]> colorArrays = new ArrayList<>();
        boolean multiOverlay = true;
        if (oRed == null || oGreen == null || oBlue == null) {
            multiOverlay = false;
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                setPixelValue(oImage[i][j], oRed, oGreen, oBlue, regionedImage[i][j], labelColor, red, green, blue, multiOverlay, i, j);
            }
        }

        assignCentroidPixels(red, green, blue);

        colorArrays.add(red);
        colorArrays.add(green);
        colorArrays.add(blue);
        return colorArrays;
    }

    private static void assignCentroidPixels(int[][] red, int[][] green, int[][] blue) {
        if (showCentroids) {
            for (int[] centroid : regionFinder.centroids) {
                int row = centroid[0];
                int col = centroid[1];
                red[row][col] = 255;
                for (int i = 1; i <= 10; i++) {
                    if (row + i < height) {
                        red[row + i][col] = 255;
                        green[row + i][col] = 255;
                        blue[row + i][col] = 255;
                    }
                    if (row - i > -1) {
                        red[row - i][col] = 255;
                        green[row - i][col] = 255;
                        blue[row - i][col] = 255;
                    }
                    if (col + i < width) {
                        red[row][col + i] = 255;
                        green[row][col + i] = 255;
                        blue[row][col + i] = 255;
                    }
                    if (col - i > -1) {
                        red[row][col - i] = 255;
                        green[row][col - i] = 255;
                        blue[row][col - i] = 255;
                    }
                }
            }
        }
    }

    private static void setPixelValue(int originalPixel, int[][] oRed, int[][] oGreen, int[][] oBlue, int regionedPixel, int labelColor, int[][] red, int[][] green, int[][] blue, boolean multiOverlay, int i, int j) {
        if (regionedPixel == 0) {
            if (multiOverlay) {
                red[i][j] = oRed[i][j];
                green[i][j] = oGreen[i][j];
                blue[i][j] = oBlue[i][j];
            } else {
                red[i][j] = originalPixel;
                green[i][j] = originalPixel;
                blue[i][j] = originalPixel;
            }
        } else {
            switch (labelColor) {
                case 1:
                    red[i][j] = 255;
                    break;
                case 2:
                    green[i][j] = 255;
                    break;
                case 3:
                    blue[i][j] = 255;
                    break;
                default:
                    break;
            }
        }
    }

    private static void initialDisplayAndAnalysis() {
        //Show info about image
        System.out.println("The chosen file has these attributes:"
                + "\n Width: " + width
                + "\n Height: " + height
                + "\n Total pixels: " + (width * height));

        //Display the selected image
        displayImage("Selected image", image.pixels, 2, 0);

        //Find the regions of the image
        runRegionsMethods();

        //Show the filtered and simplified image
        int[][] imageWithOverlayedRegions = simplifyImage(regionFinder.labeledImage);
        displayImage("Detected regions", imageWithOverlayedRegions, 1, 3);
    }

    private static void getUserInput() {
        greyScaleTolerance = keyboardInput.getInteger(true, 0, 0,
                Integer.MAX_VALUE, "Enter the greyscale tolerance (default = 0):");
        minRegionSize = keyboardInput.getInteger(true, 200, 0,
                Integer.MAX_VALUE, "Enter the minimum region size (default = 200):");
        maxRegionSize = keyboardInput.getInteger(true, 10000, 0,
                Integer.MAX_VALUE, "Enter the maximum region size (default = 10000):");
        tracerThreshold = keyboardInput.getInteger(true, 5, 0,
                Integer.MAX_VALUE, "Enter the tracer threshold (default = 5):");
        circleThreshold = keyboardInput.getInteger(true, 5, 0,
                Integer.MAX_VALUE, "Enter the circle threshold (default = 5):");
        rectangleThreshold = keyboardInput.getInteger(true, 5, 0,
                Integer.MAX_VALUE, "Enter the rectangle threshold (default = 5):");
        while (neighborhood != 4 && neighborhood != 8) {
            neighborhood = keyboardInput.getInteger(true, 8, 4, 8,
                    "Enter the neighborhood (4/8: default = 8):");
        }
        showCentroids = 'Y' == keyboardInput.getCharacter(true, 'Y', "YN", 1,
                "Show centroids? (Y/N: default = Y):");

        //Get the image from the user
        String fileName = keyboardInput.getString("given", "Specify a file name:");
        image = getImage(fileName);
        if (image != null) {
            width = image.imageWidth;
            height = image.imageHeight;
        } else {
            endProgram();
        }
    }

    private static EasyImageDisplay getImage(String fileName) {
        System.out.println("Fetching image...");
        int imageType = 0;
        int width = 0;
        int height = 0;
        int row;
        int start;
        int[][] red = null;
        int[][] green = null;
        int[][] blue = null;
        int[][] gray = null;
        char c1;
        char c2;
        char c3;
        char c4;

        if (fileName.length() > 0) {
            try {
                RandomAccessFile imageFile = new RandomAccessFile(fileName, "r");
                start = 0;
                imageFile.seek(start);					//move pointer to beginning of file
                //Read the file type, rows, and columns (stored as integers). This requires reading
                //four bytes per value. These bytes represent an integer stored by C++ or Basic
                //(i.e., in low byte to high byte order (not reversed bit order!)). The routine
                //converts to a Java integer representation (i.e., high byte to low byte order).
                c1 = (char) imageFile.read();
                c2 = (char) imageFile.read();
                c3 = (char) imageFile.read();
                c4 = (char) imageFile.read();
                imageType = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
                if ((imageType != 1) && (imageType != 2) && (imageType != 3)) {
                    keyboardInput.getKeyboardInput("Bad file type. Press ENTER to continue...");
                    System.exit(0);
                }
                c1 = (char) imageFile.read();
                c2 = (char) imageFile.read();
                c3 = (char) imageFile.read();
                c4 = (char) imageFile.read();
                width = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
                c1 = (char) imageFile.read();
                c2 = (char) imageFile.read();
                c3 = (char) imageFile.read();
                c4 = (char) imageFile.read();
                height = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
                //set up color or grayscale array(s)
                if (imageType == 1) {
                    red = new int[height][width];
                    green = new int[height][width];
                    blue = new int[height][width];
                } else {
                    gray = new int[height][width];
                }

                for (row = 0; row < height; row++) {
                    for (int column = 0; column < width; column++) {
                        if (imageType == 1) {			//color
                            blue[row][column] = (char) imageFile.read();
                            green[row][column] = (char) imageFile.read();
                            red[row][column] = (char) imageFile.read();
                        } else if (imageType == 2) //grayscale
                        {
                            gray[row][column] = (char) imageFile.read();
                        }
                    }
                }
                imageFile.close();
            } catch (Exception e) {
                keyboardInput.getKeyboardInput("Error trying to read file. Press ENTER to continue...");
                System.exit(0);
            }
            return new EasyImageDisplay(imageType, width, height, red, green, blue, gray);
        }
        return null;
    }

    private static int[][] simplifyImage(int[][] array) {
        int[][] newArray = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int curPixel = array[i][j];
                if (curPixel != 0 && !regionLabels.contains(curPixel)) {
                    regionLabels.add(curPixel);
                    newArray[i][j] = regionLabels.indexOf(curPixel) + 1;
                } else if (curPixel != 0) {
                    newArray[i][j] = regionLabels.indexOf(curPixel) + 1;
                }
            }
        }
        return newArray;
    }

    private static void displayFinalImage() {
        EasyImageDisplay compositeImage = new EasyImageDisplay(1, width, height, null, null, null, image.pixels);
        for (int i = 0; i < regionLabels.size(); i++) {
            int[][] curRegion = regionFinder.getSingleRegion(regionLabels.get(i));
            ArrayList<int[][]> colorArrays = overlayRegions(image.pixels, compositeImage.redPixels,
                    compositeImage.greenPixels, compositeImage.bluePixels, curRegion, regionShapes.get(i));
            compositeImage.redPixels = colorArrays.get(0);
            compositeImage.greenPixels = colorArrays.get(1);
            compositeImage.bluePixels = colorArrays.get(2);
        }

        int numSquares = 0;
        int numRectangles = 0;
        int numCircles = 0;
        for (int i : regionShapes) {
            if (i == 1) {
                numSquares++;
            } else if (i == 2) {
                numRectangles++;
            } else if (i == 3) {
                numCircles++;
            }
        }

        System.out.println(numSquares + " square(s), " + numRectangles
                + " rectangle(s), and " + numCircles + " circle(s) were found.");
        compositeImage.showImage("Composite image", true);
    }

    private static void findShapes() {
        System.out.println("Finding shapes...");
        for (int label : regionLabels) {

            int shapeType = 0;
            int[][] curRegion = regionFinder.getSingleRegion(label);
            int centroidRow = regionFinder.centroids[label][0];
            int centroidCol = regionFinder.centroids[label][1];

            //Get the initial tracer values (down=1, left=2, up=3, right=4)
            int downTracer = findTracer(curRegion, centroidRow, centroidCol, 1, tracerThreshold);
            int leftTracer = findTracer(curRegion, centroidRow, centroidCol, 2, tracerThreshold);
            int upTracer = findTracer(curRegion, centroidRow, centroidCol, 3, tracerThreshold);
            int rightTracer = findTracer(curRegion, centroidRow, centroidCol, 4, tracerThreshold);


            boolean hasInnerShape = hasInnerShape(label, downTracer, leftTracer, upTracer, rightTracer);
            if (!hasInnerShape) {
                double regionHeight = ((double) upTracer + (double) downTracer);
                double regionWidth = ((double) rightTracer + (double) leftTracer);

                //Finds the symmetry of the shape
                int symmetryType = findSymmetry(upTracer, downTracer, rightTracer, leftTracer);

                //Checks shape type based on shape symmetry
                shapeType = getShapeType(label, shapeType, curRegion, regionHeight, regionWidth, symmetryType);
            } else {
                shapeType = 0;
            }
            regionShapes.add(shapeType);
        }
    }

    private static int getShapeType(int label, int shapeType, int[][] curRegion, double regionHeight, double regionWidth, int symmetryType) {
        switch (symmetryType) {
            case 0:
                shapeType = 0;
                break;
            case 1:
                if (isRectangle(curRegion, label, regionHeight, regionWidth)) {
                    shapeType = 1;
                } else if (isCircle(curRegion, label, regionHeight, regionWidth)) {
                    shapeType = 3;
                } else {
                    shapeType = 0;
                }
                break;
            case 2:
                if (isRectangle(curRegion, label, regionHeight, regionWidth)) {
                    shapeType = 2;
                } else {
                    shapeType = 0;
                }
                break;
            default:
                LOGGER.severe("Symmetry type");
                break;
        }
        return shapeType;
    }

    private static boolean hasInnerShape(int label, int downTracer, int leftTracer, int upTracer, int rightTracer) {
        int centroidRow = regionFinder.centroids[label][0];
        int centroidCol = regionFinder.centroids[label][1];

        int up = centroidRow - upTracer;
        int down = centroidRow + downTracer;
        int right = centroidCol + rightTracer;
        int left = centroidCol - leftTracer;

        for (int curLabel : regionLabels) {
            if (label != curLabel) {
                int curRow = regionFinder.centroids[curLabel][0];
                int curCol = regionFinder.centroids[curLabel][1];

                if (up <= curRow && curRow <= down && left <= curCol && curCol <= right) {
                    int areaThis = getRegionArea(label);
                    int areaThat = getRegionArea(curLabel);

                    if (areaThis > areaThat) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int getRegionArea(int label) {
        int[][] curRegion = regionFinder.getSingleRegion(label);
        int area = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (curRegion[i][j] == 255) {
                    area++;
                }
            }
        }
        return area;
    }

    private static boolean isRectangle(int[][] curRegion, int label, double regionHeight, double regionWidth) {
        //Initialize values for checks
        int centroidRow = regionFinder.centroids[label][0];
        int centroidCol = regionFinder.centroids[label][1];

        //Check to see if there are holes in the region based on the estimated area
        boolean isAreaPlausible = isAreaPlausible(1, regionHeight, regionWidth, label);
        if (!isAreaPlausible) {
            return false;
        }

        //Convert doubles to integers
        int intHeight = (int) Math.round(regionHeight);
        int intWidth = (int) Math.round(regionWidth);

        int startRow = centroidRow - (intHeight / 2);
        int startCol = centroidCol + (intWidth / 2);
        for (int direction = 1; direction <= 4; direction++) {
            boolean stop = false;
            for (int offset = 0; (offset < rectangleThreshold) && !stop; offset++) {
                int rowOffset = 0;
                int colOffset = 0;
                int sideLength = 0;
                switch (direction) {
                    case 1:
                        colOffset = offset;
                        sideLength = intHeight;
                        break;
                    case 2:
                        rowOffset = offset;
                        sideLength = intWidth;
                        break;
                    case 3:
                        colOffset = -offset;
                        sideLength = intHeight;
                        break;
                    case 4:
                        rowOffset = -offset;
                        sideLength = intWidth;
                        break;
                    default:
                        LOGGER.severe("Bad direction");
                        break;
                }
                startRow -= rowOffset;
                startCol -= colOffset;
                int sideTracer = findTracer(curRegion, startRow, startCol, direction, rectangleThreshold);

                boolean isSideLengthEligible = Math.abs(sideLength - sideTracer) < rectangleThreshold;
                if (isSideLengthEligible) {
                    switch (direction) {
                        case 1:
                            startRow += sideTracer;
                            break;
                        case 2:
                            startCol -= sideTracer;
                            break;
                        case 3:
                            startRow -= sideTracer;
                            break;
                        case 4:
                            startCol += sideTracer;
                            break;
                        default:
                            LOGGER.severe("Bad direction");
                            break;
                    }
                    stop = true;
                } else if (offset == (rectangleThreshold - 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isCircle(int[][] curRegion, int label, double regionHeight, double regionWidth) {
        //Initialize values for checks
        int centroidRow = regionFinder.centroids[label][0];
        int centroidCol = regionFinder.centroids[label][1];

        boolean isAreaPlausible = isAreaPlausible(2, regionHeight, regionWidth, label);
        if (isAreaPlausible) {
            double radius = (regionWidth + regionHeight) / 4.0;
            for (int i = 0; i < 360; i += 5) {
                if (checkCircleShape(curRegion, centroidRow, centroidCol, radius, i)) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkCircleShape(int[][] curRegion, int centroidRow, int centroidCol, double radius, int i) {
        int row = (int) Math.round(radius * Math.sin(i));
        int col = (int) Math.round(radius * Math.cos(i));

        if ((curRegion[centroidRow + row][centroidCol + col] != 255)) {
            boolean valid = false;
            for (int offset = 0; offset < circleThreshold / 2; offset++) {
                int rowOffset = offset * (row / Math.abs(row));
                int colOffset = offset * (col / Math.abs(col));
                if (curRegion[centroidRow + row + rowOffset][centroidCol + col + colOffset] == 255
                        || curRegion[centroidRow + row - rowOffset][centroidCol + col - colOffset] == 255) {
                    valid = true;
                }
            }
            return !valid;
        }
        return false;
    }

    private static boolean isAreaPlausible(int shapeType, double regionHeight, double regionWidth, int label) {
        double actualArea = (double) getRegionArea(label);
        double idealArea;
        double areaError;
        switch (shapeType) {
            case 1:
                idealArea = regionWidth * regionHeight;
                areaError = Math.abs((idealArea - actualArea) / idealArea);
                return areaError <= 0.2;
            case 2:
                double radius = (regionWidth + regionHeight) / 4.0;
                idealArea = Math.PI * Math.pow(radius, 2);
                areaError = Math.abs((idealArea - actualArea) / idealArea);
                return areaError <= 0.5;
            default:
                LOGGER.severe("Bad shape type");
                break;
        }
        return false;
    }

    private static int findTracer(int[][] curRegion, int row, int col, int direction, int tracerErrorLimit) {
        boolean loop = true;
        int errorCounter = 0;
        int tracer = 0;
        while (loop) {
            switch (direction) {
                case 1:             //DOWN
                    row++;
                    break;
                case 2:             //LEFT
                    col--;
                    break;
                case 3:             //UP
                    row--;
                    break;
                case 4:             //RIGHT
                    col++;
                    break;
                default:
                    LOGGER.severe("Bad direction");
                    break;
            }
            //Check for out of bounds
            boolean isInBounds = (0 <= row && row < height && 0 <= col && col < width);
            if (isInBounds) {
                tracer++;
                //Check for wrong value
                if (curRegion[row][col] == 0) {
                    errorCounter++;
                }
                //Check for over error limit
                if (errorCounter >= tracerErrorLimit) {
                    tracer -= (tracerErrorLimit + 1);
                    loop = false;
                }
            } else {
                loop = false;
            }
        }
        return tracer;
    }

    private static int findSymmetry(int upTracer, int downTracer, int rightTracer, int leftTracer) {
        if (upTracer == -1 || downTracer == -1 || rightTracer == -1 || leftTracer == -1) {
            return 0;
        }

        //Calculate if it is fully symmetric
        double avgTracer = (upTracer + downTracer + rightTracer + leftTracer) / (double) 4;
        boolean isFullySymmetric = !(Math.abs(avgTracer - upTracer) >= tracerThreshold
                || Math.abs(avgTracer - downTracer) >= tracerThreshold
                || Math.abs(avgTracer - rightTracer) >= tracerThreshold
                || Math.abs(avgTracer - leftTracer) >= tracerThreshold);
        if (isFullySymmetric) {
            return 1;
        }

        //Calculate if it is half symmetric
        boolean isHalfSymmetric = !(Math.abs(upTracer - downTracer) >= tracerThreshold
                || Math.abs(rightTracer - leftTracer) >= tracerThreshold);
        if (isHalfSymmetric) {
            return 2;
        }

        return 0;
    }

    private static void pauseProgram() {
        keyboardInput.getKeyboardInput("Press ENTER...");
    }

    private static void resetProgram() {
        keyboardInput = new KeyboardInputClass();
        image = null;
        regionFinder = null;
        width = 0;
        height = 0;
        greyScaleTolerance = 0;
        minRegionSize = 0;
        maxRegionSize = 0;
        tracerThreshold = 0;
        circleThreshold = 0;
        rectangleThreshold = 0;
        neighborhood = 0;
        showCentroids = true;
        regionLabels = new ArrayList<>();
        regionShapes = new ArrayList<>();
    }

    private static void endProgram() {
        System.out.println("\n\nExit...");
        System.exit(0);
    }
}
