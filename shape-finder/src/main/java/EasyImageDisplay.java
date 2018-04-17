import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//******************************************************************************************
//******************************************************************************************
//Class:		EasyImageDisplay
//Description:	Implements the paint() routine using one or more 2D arrays containing the
//				image (one for grayscale or binary images, 3 for color images). Assumes
//				that the image is oriented correctly (i.e,. paint() does not flip it).
//				Allows for showing an image one time or continuously (for example, if it
//				is being constantly updated by another routine).
//				Provides routines for referencing or copying image data. The referencing
//				options provide for potentially faster processing but at the expense of
//				creating aliases. The copy options are slower but safer.
//				Also includes the capability to show text on the image.
//Author:		Steve Donaldson
//Date:			4/2/09

public class EasyImageDisplay extends Frame {

    public int imageType;				//1=24 bit color;2=256 level gray scale (for binary, use color values 0 and 255)
    public int imageWidth;				//image width in pixels
    public int imageHeight;				//image height in pixels
    public int imageSize;				//height*width (*3 for images of type 1)
    public int pixels[][];				//pixel values in 2D for type 1 or type 2 images
    public int redPixels[][];			//red pixel values for type 1 images
    public int greenPixels[][];			//green pixel values for type 1 images
    public int bluePixels[][];			//blue pixel values for type 1 images
    public boolean showOnce;			//true=show the image one time; false=continuously
    //redraw the image. The "false" option is useful for
    //situations such as those when the image pixels are
    //being changed in another routine.
    public static int windowHeaderOffset = 30;		//space for window title bar
    public static int windowSideOffset = 4;			//space for window side bar(s)
    public static int windowBotttomOffset = 4;		//space for window bottom bar

    public int textLineCount;			//the number of lines of text in array text[]
    public String text[];				//each row contains a line of text to be displayed
    public int textPosition[][];		//the row and column positions in the image at which
    //to display the text
    //**************************************************************************************

    EasyImageDisplay(int type, int width, int height, int redValues[][], int greenValues[][], int blueValues[][], int values[][]) {
        imageType = type;
        imageWidth = width;
        imageHeight = height;
        imageSize = height * width;
        if (imageType == 1) {
            imageSize *= 3;
        }
        redPixels = redValues;
        greenPixels = greenValues;
        bluePixels = blueValues;
        pixels = values;
        showOnce = true;
        textLineCount = 0;
        text = null;
        textPosition = null;
    }
    //**************************************************************************************

    EasyImageDisplay(EasyImageDisplay displayObject) {
        imageType = displayObject.imageType;
        imageWidth = displayObject.imageWidth;
        imageHeight = displayObject.imageHeight;
        imageSize = displayObject.imageSize;
        redPixels = displayObject.redPixels;
        greenPixels = displayObject.greenPixels;
        bluePixels = displayObject.bluePixels;
        pixels = displayObject.pixels;
        showOnce = true;
    }
    //**************************************************************************************
    //Method:		referenceColorArrayData
    //Description:	Makes the color arrays for this image reference the RGB pixel values
    //				from a specified set of color arrays. Note that a change to the source
    //				arrays will be reflected in this image if subsequently displayed.
    //Parameters:	redValues[][]	- source red pixels
    //				greenValues[][]	- source green pixels
    //				blueValues[][]	- source blue pixels
    //Returns:		true if successful; false otherwise
    //Calls:		nothing

    public boolean referenceColorArrayData(int redValues[][], int greenValues[][], int blueValues[][]) {
        if ((redValues != null) && (greenValues != null) && (blueValues != null)) {
            redPixels = redValues;
            greenPixels = greenValues;
            bluePixels = blueValues;
            return true;
        }
        return false;
    }
    //**************************************************************************************
    //Method:		referenceGrayArrayData
    //Description:	Makes the grayscale array for this image reference the gray pixel values
    //				from a specified grayscale array. Note that a change to the source
    //				array will be reflected in this image if subsequently displayed.
    //Parameters:	values[][]	- source gray pixels
    //Returns:		true if successful; false otherwise
    //Calls:		nothing

    public boolean referenceGrayArrayData(int values[][]) {
        if (values != null) {
            pixels = values;
            return true;
        }
        return false;
    }
    //**************************************************************************************
    //Method:		copyColorArrayData
    //Description:	Copies RGB pixel values from a specified set of arrays into the arrays
    //				for this image. Making a copy prevents a change to the source arrays
    //				from subsequently affecting this image.
    //Parameters:	redValues[][]	- source red pixels
    //				greenValues[][]	- source green pixels
    //				blueValues[][]	- source blue pixels
    //Returns:		true if successful; false otherwise
    //Calls:		nothing

    public boolean copyColorArrayData(int redValues[][], int greenValues[][], int blueValues[][]) {
        if ((redValues != null) && (greenValues != null) && (blueValues != null)) {
            redPixels = new int[imageHeight][imageWidth];
            greenPixels = new int[imageHeight][imageWidth];
            bluePixels = new int[imageHeight][imageWidth];
            for (int r = 0; r < imageHeight; r++) {
                for (int c = 0; c < imageWidth; c++) {
                    redPixels[r][c] = redValues[r][c];
                    greenPixels[r][c] = greenValues[r][c];
                    bluePixels[r][c] = blueValues[r][c];
                }
            }
            return true;
        }
        return false;
    }
    //**************************************************************************************
    //Method:		copyGrayArrayData
    //Description:	Copies grayscale pixel values from a specified array into the array
    //				for this image. Making a copy prevents a change to the source array
    //				from subsequently affecting this image.
    //Parameters:	values[][]	- source gray pixels
    //Returns:		true if successful; false otherwise
    //Calls:		nothing

    public boolean copyGrayArrayData(int values[][]) {
        if (values != null) {
            pixels = new int[imageHeight][imageWidth];
            for (int r = 0; r < imageHeight; r++) {
                for (int c = 0; c < imageWidth; c++) {
                    pixels[r][c] = values[r][c];
                }
            }
            return true;
        }
        return false;
    }
    //**************************************************************************************
    //Method:		showImage
    //Description:	Initializes graphics window parameters and displays its contents.
    //Parameters:	title		- title of the graphics window
    //				displayOnce	- show the image a single time or display it continuously
    //								(i.e., as for continuous digital camera input)
    //Returns:		nothing
    //Calls:		various Java graphics routines

    public void showImage(String title, boolean displayOnce) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //ADDITION: Don't exit the program when the window is closed.
                //System.exit(0);
                closeImageDisplay();
            }
        });
        showOnce = displayOnce;
        setTitle(title);
        setSize(imageWidth + 2 * windowSideOffset, imageHeight + windowHeaderOffset + windowBotttomOffset);
        setVisible(true);
    }
    //**************************************************************************************
    //Method:		closeImageDisplay
    //Description:	Terminates continuous display (if applicable) and closes the graphics
    //				window.
    //Parameters:	none
    //Returns:		nothing
    //Calls:		Java setVisible

    public void closeImageDisplay() {
        showOnce = true;				//exit endless loop in paint()
        setVisible(false);
    }
    //**************************************************************************************
    //Method:		paint
    //Description:	Display an image stored in a 2D array. Overrides the paint method
    //				inherited from Frame (via Container). Allows for showing an image
    //				one time or continuously (for example, if it is being constantly
    //				updated by another routine).
    //Parameters:	g	- the graphics object
    //Returns:		nothing
    //Calls:		setColorValues for an ImageClass object
    //				setGrayValues for an ImageClass object
    //				referenceColorArrayData
    //				referenceGrayArrayData
    //				plus various Java graphics routines

    public void paint(Graphics g) {
        int row, column, pixel;
        Color color = new Color(0);
        int i, a = 0, b = 0;

        while (a == b) {
            if (imageType == 1) {
                for (row = 0; row < imageHeight; row++) {
                    for (column = 0; column < imageWidth; column++) {
                        color = new Color(redPixels[row][column], greenPixels[row][column], bluePixels[row][column]);
                        g.setColor(color);
                        g.drawLine(column + windowSideOffset, row + windowHeaderOffset, column + windowSideOffset, row + windowHeaderOffset);
                    }
                }
            } else if ((imageType == 2) || (imageType == 3)) {
                for (row = 0; row < imageHeight; row++) {
                    for (column = 0; column < imageWidth; column++) {
                        pixel = pixels[row][column];
                        color = new Color(pixel, pixel, pixel);
                        g.setColor(color);
                        g.drawLine(column + windowSideOffset, row + windowHeaderOffset, column + windowSideOffset, row + windowHeaderOffset);
                    }
                }
            }

            g.setColor(Color.white);
            Font f = new Font("sansserif", Font.BOLD, 12);
            g.setFont(f);
            for (i = 0; i < textLineCount; i++) {
                g.drawString(text[i], textPosition[i][1] + windowSideOffset, textPosition[i][0] + windowHeaderOffset);
            }

            if (showOnce) {
                b++;			//exit
            }
        }
    }
    //end paint()
    //**************************************************************************************
}	//end EasyImageDisplay class
//******************************************************************************************
//******************************************************************************************
//To use this class:
//Create a new instance and load the arrays with the pixel data to be displayed...
