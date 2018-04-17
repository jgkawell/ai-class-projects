//Description:  Generic graphics routine(s)
//Author:       Steve Donaldson
//Revised:      1/28/09

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//**************************************************************************************************************************
//**************************************************************************************************************************
//Class:		ImageDisplay
//Description:	Controls graphic output to a window.
public class ImageDisplay extends Frame {
	private int rowCount;                           //image array dimensions
    private int columnCount;						//image array dimensions
	private int scaleFactor;									//image is enlarged by this factor
	Color[][] image;										//the image data (Color values)
	private static int windowHeaderOffset = 30;				//space for window title bar
	private static int windowSideOffset = 4;					//space for window side bar(s)
    private boolean showContinuously;						//controls continuous processing in paint() routine
	//**********************************************************************************************************************
	ImageDisplay(int numberOfRows, int numberOfColumns, int scaleValue, String windowTitle) {
		rowCount = numberOfRows;
		columnCount = numberOfColumns;
		image = new Color[rowCount][columnCount];
		scaleFactor = scaleValue;
		showContinuously = true;

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		setTitle(windowTitle);
		int imageHeight = rowCount * scaleFactor;
		int imageWidth = columnCount * scaleFactor;
        int windowBottomOffset = 4;
        setSize(imageWidth + 2 * windowSideOffset, imageHeight + windowHeaderOffset + windowBottomOffset);
		setVisible(true);
	}
	//**********************************************************************************************************************
	//Method:		closeImageDisplay
	//Description:	Terminates continuous display (if applicable) and closes the graphics
	//				window.
	//Parameters:	none
	//Returns:		nothing
	//Calls:		Java setVisible
	public void closeImageDisplay() {
		showContinuously = false;				//exit endless loop in paint()
		setVisible(false);
	}
	//**********************************************************************************************************************
	//Displays the image (possibly enlarged by scaleFactor). Loops continuously (assuming updates will be made to image[][]
	//by an external routine) as long as showContinuously=true.
	@Override
	public void paint(Graphics g) {
		int row;
        int column;
        Color pixel;
		int newRow;
        int r;
        int c;

        while (showContinuously) {
			newRow = 0;
			for (row = 0; row < rowCount; row++) {
                int newColumn = 0;
                for (column = 0; column < columnCount; column++) {
					pixel = image[row][column];
					for (r = newRow; r < newRow + scaleFactor; r++) {//set all pixels in block to same color
						for (c = newColumn; c < newColumn + scaleFactor; c++) {
							g.setColor(pixel);
							g.drawLine(c + windowSideOffset, r + windowHeaderOffset, c + windowSideOffset, r + windowHeaderOffset);
						}
					}
					newColumn += scaleFactor;
				}
				newRow += scaleFactor;
			}
		}
	}
	//**********************************************************************************************************************
}	//end ImageDisplayClass
//**************************************************************************************************************************
//**************************************************************************************************************************