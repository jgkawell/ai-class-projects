public class Regions {

    // <editor-fold defaultstate="collapsed" desc="Global Variables">
    private int imageType;                           //2=256 level gray scale;3=binary (b/w). Type 1
                                                     //(24 bit color) is not supported by this routine.
    private int imageWidth;                          //image width in pixels
    private int imageHeight;                         //image height in pixels
    private int[][] sourceImage;                     //image for which regions are to be found
    private int neighborhood;                        //4 or 8-neighborhood for determining neighbors
    private boolean includeBlackPixels;              //for binary images only: true=label all black
                                                    //pixels; false=do not label black pixels
                                                    //(note the assumption that black is the typical
                                                    //background color)
    private int grayTolerance;                       //for grayscale images only: the number of
                                                    //grayvalues to include on either side of a pixel
                                                    //value when determining if a given pixel belongs
                                                    //to the region under construction
    private int labelCount;                          //actual # of regions calculated
    public int[][] labeledImage;                    //labeled image (after the algorithm completes)
    private int maxCount;                            //maximum # of regions for which a count of the
                                                    //pixels in those regions will be accumulated
    private int[] pixelsInRegion;                    //the # of pixels in each region (up to maxCount)
    private int[][] firstPixelInRegion;              //the row and column coords of the first pixel found
                                                    //in a new region
    private int[] borderPixelCount;                  //the # of pixels in each region on the border of
                                                    //the image space. (Regions on the border may not
                                                    //be entirely within the image space)
    private int[] sumOfRows;                         //sum of all row values for each region (used for
                                                    //computing region centroids)
    private int[] sumOfColumns;                      //sum of all column values for each region
    public int[][] centroids;                       //row and column values for centroids of each region
    private double[] bestAxes;                       //angle in radians of the best axis for each labeled
                                                    //region with respect to the x-axis
    //</editor-fold>

    /**************************************************************************************
    //Method:		Regions
    //Description:	Initializes the parameters needed to do connected components labeling
    //Parameters:	Just the variables corresponding to the object variables
    //Returns:		nothing
    //Calls:		nothing
    */
    Regions(int type, int width, int height, int[][] image, int pixelNeighborhood, boolean includeBlack, int tolerance) {
        imageType = type;
        imageWidth = width;
        imageHeight = height;
        sourceImage = image;
        neighborhood = pixelNeighborhood;
        includeBlackPixels = imageType != 3 || includeBlack;
        if (imageType == 2) {
            grayTolerance = tolerance;
        } else {
            grayTolerance = 0;
        }
        labelCount = 0;
        labeledImage = new int[imageHeight][imageWidth];
        if (imageType == 3) {
            grayTolerance = 0;
        }
        maxCount = 500000;
        pixelsInRegion = new int[maxCount + 2];            //position 0 not used in these arrays
        firstPixelInRegion = new int[maxCount + 2][2];
        borderPixelCount = new int[maxCount + 2];
        sumOfRows = new int[maxCount + 2];
        sumOfColumns = new int[maxCount + 2];
        centroids = new int[maxCount + 2][2];
        bestAxes = new double[maxCount + 2];

        for (int r = 0; r < imageHeight; r++) //set-up by negating pixel values
        {
            for (int c = 0; c < imageWidth; c++) {
                labeledImage[r][c] = -sourceImage[r][c];
            }
        }
    }
    /**************************************************************************************
    //Method:		Regions
    //Description:	Initializes the parameters needed to do connected components labeling
    //Parameters:	Just the variables corresponding to the object variables
    //Returns:		labeledImage[][] - see object variable description
    //Calls:		nothing
    */
    public void findRegions() {
        StackInterface stack = new LinkedStack();
        SearchNode currentSearchObject;
        int row;
        int c;
        int r;
        int k;
        int pixel;
        int kStart;
        int baseRow;
        int baseColumn;
        int nextRow;
        int imageHeightMinus1 = imageHeight - 1;
        int imageWidthMinus1 = imageWidth - 1;
        if (neighborhood == 4) {
            kStart = 0;
        } else {
            kStart = 1;
        }
        int searchCutoff = 0;
        if ((includeBlackPixels) || (imageType == 2)) {
            searchCutoff = 1;
        }
        int label = 0;
        for (row = 0; row < imageHeight; row++) {
            for (int column = 0; column < imageWidth; column++) {
                if (labeledImage[row][column] < searchCutoff) {	//found a new region to label
                    int unlabeledPixelValue = labeledImage[row][column];
                    label++;
                    labeledImage[row][column] = label;	//label first pixel found in region
                    if (label < maxCount) {
                        pixelsInRegion[label]++;
                        firstPixelInRegion[label][0] = row;
                        firstPixelInRegion[label][1] = column;
                        sumOfRows[label] += row;
                        sumOfColumns[label] += column;
                        if ((row == 0) || (row == imageHeightMinus1) || (column == 0) || (column == imageWidthMinus1)) {
                            borderPixelCount[label]++;
                        }
                    }

                    //simulate recursive connected components
                    currentSearchObject = new SearchNode(row, column, row - 1, column - 1, kStart);
                    stack.push(currentSearchObject);
                    while (!stack.isEmpty()) {
                        currentSearchObject = (SearchNode) stack.peek();
                        baseRow = currentSearchObject.row;
                        baseColumn = currentSearchObject.column;
                        r = currentSearchObject.r;
                        c = currentSearchObject.c;
                        k = currentSearchObject.k;

                        //determine the row and column of the next potential neighbor
                        int nextColumn = c + 1;
                        nextRow = r;
                        if (nextColumn > baseColumn + 1) {
                            nextColumn = baseColumn - 1;
                            nextRow = r + 1;
                        }
                        //is the row in the allowable range for neighbors?
                        if (nextRow <= baseRow + 1) {
                            //prepare to skip diagonal pixels for 4-neighborhood
                            int nextK;
                            if (neighborhood == 4) {
                                nextK = 1 - k;
                            } else {
                                nextK = k;
                            }
                            //update parent values on stack for neighborhood processing
                            currentSearchObject.r = nextRow;
                            currentSearchObject.c = nextColumn;
                            currentSearchObject.k = nextK;
                        } else {
                            stack.pop();
                        }

                        //is pixel at (r,c) legitimate for the neighborhood and in bounds?
                        if ((k == 1) && (r >= 0) && (r < imageHeight) && (c >= 0) && (c < imageWidth) && ((r != baseRow) || (c != baseColumn))) {
                            pixel = labeledImage[r][c];
                            //is the pixel unlabeled and within the specified tolerance?
                            if ((pixel < searchCutoff) && (Math.abs(unlabeledPixelValue - pixel) <= grayTolerance)) {
                                labeledImage[r][c] = label;		//label the pixel
                                if (label < maxCount) {
                                    pixelsInRegion[label]++;
                                    sumOfRows[label] += r;
                                    sumOfColumns[label] += c;
                                    if ((r == 0) || (r == imageHeightMinus1) || (c == 0) || (c == imageWidthMinus1)) {
                                        borderPixelCount[label]++;
                                    }
                                }
                                //search neighbors of pixel at (r,c)
                                SearchNode nextSearchObject = new SearchNode(r, c, r - 1, c - 1, kStart);
                                stack.push(nextSearchObject);
                            }
                        }
                    }
                }
            }
        }
        labelCount = label;
    }
    /**************************************************************************************
    //Method:		getSingleRegion
    //Description:	Returns (from a labeled image) an array containing a specified region
    //				labeled with 255 and having background set to 0. Assumes that
    //				findRegions() has been previously called.
    //Parameters:	regionID			 - identification of the region to be singled out
    //Returns:		singleRegionImage[][]- array having only pixels in the specified region
    //										set to non-zero values (255). Array is the same
    //										size as labeledImage.
    //Calls:		nothing
    */
    public int[][] getSingleRegion(int regionID) {
        int[][] singleRegionImage = new int[imageHeight][imageWidth];	//initialized to 0
        for (int r = 0; r < imageHeight; r++) {
            for (int c = 0; c < imageWidth; c++) {
                if (labeledImage[r][c] == regionID) {
                    singleRegionImage[r][c] = 255;
                }
            }
        }
        return singleRegionImage;
    }
    /**************************************************************************************
    //Method:		filterRegions
    //Description:	Removes regions whose size is outside specified lower and upper bounds.
    //				Optionally removes regions lying on the boundary of the image space.
    //				(The logic behind use of this feature is that a region lying on the
    //				boundary is quite possibly only a portion of a complete region and
    //				might not necessarily be reliably used in an image recognition task.)
    //				Note: regions are NOT renumbered and labelCount is NOT adjusted.
    //				Consequently, routines that use region object data after this method
    //				has run must take into account the fact that there can be 0 values
    //				in pixelsInRegion[] and firstPixelInRegion[][] between the remaining
    //				legitimate region entries. Pixels are removed from the labeled image
    //				by setting them to the background color, which is assumed to be 0
    //				(for both binary and grayscale images). Note that this routine
    //				removes at most maxCount regions.
    //Parameters:	lowerBound	- regions with fewer than this # of pixels are eliminated
    //                  upperBound	- regions with more than this # of pixels are eliminated
    //			omitBoundaryRegions	- true=remove any region having one or more pixels
    //                                            that lie on the boundary of the image space
    //			borderThreshold		- if omitBoundaryRegions is true, regions having a
    //                                          # of boundary pixels > this amount will be eliminated
    //Returns:		labeledImage[][]
    //				Also updates pixelsInRegion[], firstPixelInRegion[][], and
    //				borderPixelCount[].
    //Calls:		nothing
    */
    public void filterRegions(int lowerBound, int upperBound, boolean omitBoundaryRegions, int borderThreshold) {
        int label;
        int oldLabelCount = labelCount;
        if (oldLabelCount > maxCount) {
            oldLabelCount = maxCount;
        }

        //remove pixels in regions not meeting specified size criteria
        int pixelCount;
        for (int r = 0; r < imageHeight; r++) {
            for (int c = 0; c < imageWidth; c++) {
                label = labeledImage[r][c];
                pixelCount = pixelsInRegion[label];
                if ((pixelCount < lowerBound) || (pixelCount > upperBound) || (omitBoundaryRegions && (borderPixelCount[label] > borderThreshold))) {
                    labeledImage[r][c] = 0;
                }
            }
        }
        for (label = 1; label <= oldLabelCount; label++) {
            pixelCount = pixelsInRegion[label];
            if ((pixelCount < lowerBound) || (pixelCount > upperBound) || (omitBoundaryRegions && (borderPixelCount[label] > borderThreshold))) {
                pixelsInRegion[label] = 0;
                firstPixelInRegion[label][0] = 0;
                firstPixelInRegion[label][1] = 0;
                borderPixelCount[label] = 0;
            }
        }
    }
    /**************************************************************************************
    //Method:		computeRegionProperties
    //Description:	Computes specific properties for all regions in a labeled image and
    //				updates (for the region object under consideration) the relevant object
    //				parameters (arrays, etc.) storing those property values. Current
    //				properties computed and saved are centroids and best axes.
    //Parameters:	none
    //Returns:		nothing (but updates object parameters)
    //Calls:		nothing
    */
    public void computeRegionProperties() {
        int maxLabels = labelCount;
        if (labelCount > maxCount) {
            maxLabels = maxCount;
        }
        for (int label = 1; label <= maxLabels; label++) {
            if (pixelsInRegion[label] > 0) {
                centroids[label][0] = sumOfRows[label] / pixelsInRegion[label];
                centroids[label][1] = sumOfColumns[label] / pixelsInRegion[label];
                bestAxes[label] = findBestAxisForRegion(label);
            }
        }
    }
    /**************************************************************************************
    //Method:		findBestAxisForRegion
    //Description:	Computes the best axis for a region in a binary image using the
    //				technique provided in Shapiro and Stockman (2001).
    //Parameters:	regionID - identification of the region for which the axis is calculated
    //Returns:		bestAxis	- angle (with the horizontal) of the best axis in radians
    //Calls:		nothing
    //Authors:		Greg Brazda, Ben Dennis, Steve Donaldson
    */
    private double findBestAxisForRegion(int regionID) {
        double piOverTwo = Math.PI / 2;
        int area = pixelsInRegion[regionID];
        int centroidRow = centroids[regionID][0];
        int centroidCol = centroids[regionID][1];
        double mrr = 0.0;
        double mrc = 0.0;
        double mcc = 0.0;
        int deltaR;

        for (int row = 0; row < imageHeight; row++) {
            for (int column = 0; column < imageWidth; column++) {
                if (labeledImage[row][column] == regionID) {
                    deltaR = row - centroidRow;
                    int deltaC = column - centroidCol;
                    mrr += (deltaR * deltaR);
                    mrc += ((deltaR) * (deltaC));
                    mcc += (deltaC * deltaC);
                }
            }
        }
        mrr /= area;
        mrc /= area;
        mcc /= area;

        double bestAxis;
        if (mrr - mcc != 0) {
            bestAxis = (2 * mrc) / (mrr - mcc);
            bestAxis = Math.atan(bestAxis) / 2;
        } else {
            bestAxis = piOverTwo;
        }
        //At this point "bestAxis" may actually be the worst axis, so we must check to see
        //if it needs to be adjusted by PI/2 radians.
        if (mcc < mrr)
        {
            bestAxis += piOverTwo;
        }
        //if not already there, place angle in range -PI/2 to +PI/2
        if (bestAxis > piOverTwo) {
            bestAxis -= Math.PI;
        }
        if (bestAxis < -piOverTwo) {
            bestAxis += Math.PI;
        }

        return bestAxis;
    }
    //**************************************************************************************
}	//end Regions class

//******************************************************************************************
//******************************************************************************************
// <editor-fold defaultstate="collapsed" desc="SearchNode Class">
class SearchNode {

    public int row;			//row for a pixel that has been labeled
    public int column;		//column for a pixel that has been labeled
    public int r;			//row of a pixel neighboring the pixel at (row,column)
    public int c;			//column of a pixel neighboring the pixel at (row,column)
    public int k;			//flag to control how pixels in 4 or 8 neighborhood are processed.
    //Value is 0 or 1. For a 4-neighborhood, only every other pixel
    //in an 8 neighborhood is considered.)

    SearchNode(int row, int column, int r, int c, int k) {
        this.row = row;
        this.column = column;
        this.r = r;
        this.c = c;
        this.k = k;
    }
}
// </editor-fold>
//******************************************************************************************
//******************************************************************************************
