package com.swiftsoft.colossus.mobileoil.printingsystem;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Printer
{
	int yPos = 0;
	int brandID = 0;
	
	private int yPosPending = 0;
	private boolean yPosUpdatingPaused = false;
	
	StringBuilder printJob = new StringBuilder();

	public enum Size
	{
		Small,
		Normal,
		Large
	}
	
	public enum Alignment
	{
		Left,
		Centre,
		Right
	}

	public enum SpacerHeight
	{
		Small,
		Normal,
		Large,
		XLarge,
	}

	public byte[] addBitmap() throws Exception
	{
		throw new RuntimeException("addBitmap not implemented in Printer class");
	}

	public int addSpacer(int yPosition, SpacerHeight height)
	{
		throw new RuntimeException("addSpacer not implemented in Printer class");
	}

	public int addSignature(String title, String name, int yPosition, byte[] signatureArray, long driverSignatureDateTime)
	{
		throw new RuntimeException("addSignature not implemented in Printer class");
	}

	public int addLogo(int brandId, int yPosition) throws Exception
	{
		throw new RuntimeException("addLogo not implemented in Printer class");
	}

	public int addLine(int yPosition)
	{
		throw new RuntimeException("addLine not implemented in Printer class");
	}
	
	public int addLine(int xStart, int yStart, int xEnd, int yEnd)
	{
		throw new RuntimeException("addLine not implemented in Printer class");
	}

	// Increate the yPos.
	public void increaseYPos(int pos)
	{
		if (!yPosUpdatingPaused)
		{
			yPos += pos;
		}
		else
		{
			yPosPending = Math.max(pos, yPosPending);
		}
	}
	
	public int addTextLeft(Size size, int xPosition, int yPosition, int width, String text)
	{
		throw new RuntimeException("addTextLeft not implemented in Printer class");
	}

	public int addTextCentre(Size size, int xPosition, int yPosition, int width, String text)
	{
		throw new RuntimeException("addTextCentre not implemented in Printer class");
	}

	public int addTextRight(Size size, int xPosition, int yPosition, int width, String text)
	{
		throw new RuntimeException("addTextRight not implemented in Printer class");
	}
	
	public String getPrinterData() throws Exception
	{
		throw new RuntimeException("getPrinterData not implemented in Printer class");
	}
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) 
	{
		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	}
}
