package com.swiftsoft.colossus.mobileoil.printingsystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

/**
 * Created by Alan on 09/06/2015.
 */
public class BitmapPrinter extends Printer
{
    private final int HEIGHT_INCREMENT = 400;

    // Constant to hold the printer width (in pixels)
    private static final int PRINTER_WIDTH = 800;

    // The size of the large font
    private static final int FONT_SIZE_LARGE = 40;

    // The size of the normal font
    private static final int FONT_SIZE_NORMAL = 25;

    // The size of the small font
    private static final int FONT_SIZE_SMALL = 18;

    Bitmap bitmap = null;

    Context context;

    int maximumY = 0;

    // Create Hashtable<> to store Typeface objects
    private Hashtable<String, Typeface> typeFaces = new Hashtable<String, Typeface>();

    private Typeface getTypeFace(String typeName)
    {
        // Check if the font being searched for has not already
        // got a Typeface object created for it
        if (!typeFaces.containsKey(typeName))
        {
            // Get the full name of the type path in assets
            String typePath = String.format("fonts/%s", typeName);

            // Create the Typeface object from the Android assets
            Typeface t = Typeface.createFromAsset(context.getAssets(), typePath);

            // Add to the Hashtable
            typeFaces.put(typeName, t);
        }

        // Return the created/existing Typeface
        return typeFaces.get(typeName);
    }

    public BitmapPrinter(Context context)
    {
        this.context = context;

        // Create the initial bitmap
        bitmap = createWhiteBitmap(PRINTER_WIDTH, HEIGHT_INCREMENT);
    }

    public int getMaximumY()
    {
        return maximumY;
    }

    private static Bitmap createWhiteBitmap(int width, int height)
    {
        // Create the bitmap of appropriate size
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        bitmap.eraseColor(Color.WHITE);

        return bitmap;
    }

    /**
     * Extend the bitmap by 800 pixels or height
     * whichever is the greater.
     * @param height
     */
    private void extendBitmap(int height)
    {
        Bitmap outputBitmap;

        // Create a new larger bitmap
        if (height > HEIGHT_INCREMENT)
        {
            outputBitmap = createWhiteBitmap(PRINTER_WIDTH, height + bitmap.getHeight());
        }
        else
        {
            outputBitmap = createWhiteBitmap(PRINTER_WIDTH, HEIGHT_INCREMENT + bitmap.getHeight());
        }

        Canvas canvas = new Canvas(outputBitmap);

        // Draw the original bitmap onto the new extended one
        canvas.drawBitmap(bitmap, 0, 0, null);

        bitmap = outputBitmap;
    }

    @Override
    public int addSignature(String title, String name, int yPosition, byte[] signatureArray, long datetime)
    {
        final int xRightColumn = 560;
        final int widthRightColumn = 220;

        final int xLeftColumn = 30;
        final int widthLeftColumn = 400;

        int finalPosition  = yPosition;

        DateFormat df1 = new SimpleDateFormat("dd-MMM-yyyy");
        DateFormat df2 = new SimpleDateFormat("@ HH:mm");

        // Decode the signature to a Bitmap
        Bitmap signature = BitmapFactory.decodeByteArray(signatureArray, 0, signatureArray.length);

        // Resize bitmap.
        signature = getResizedBitmap(signature, 480, 171);

        finalPosition = addSpacer(finalPosition, SpacerHeight.Normal);

        // Print title of the signature
        addTextLeft(Size.Large, xLeftColumn, finalPosition, widthLeftColumn, title);

        // Print the Name header
        finalPosition = addTextLeft(Size.Large, xRightColumn, finalPosition, widthRightColumn, "Name");

        // Store the position so that we know where to pring the
        // graphic signature
        int signaturePosition = addSpacer(finalPosition, SpacerHeight.Small);

        finalPosition = addSpacer(finalPosition, SpacerHeight.Small);

        // Print the actual name
        finalPosition = addTextLeft(Size.Normal, xRightColumn, finalPosition, widthRightColumn, name);

        finalPosition = addSpacer(finalPosition, SpacerHeight.Large);

        // Print the Date header
        finalPosition = addTextLeft(Size.Large, xRightColumn, finalPosition, widthRightColumn, "Date");

        finalPosition = addSpacer(finalPosition, SpacerHeight.Small);

        // Print the date
        finalPosition = addTextLeft(Size.Normal, xRightColumn, finalPosition, widthRightColumn, df1.format(datetime));

        finalPosition = addSpacer(finalPosition, SpacerHeight.Small);

        // Print the time
        finalPosition = addTextLeft(Size.Normal, xRightColumn, finalPosition, widthRightColumn, df2.format(datetime));

        finalPosition = addSpacer(finalPosition, SpacerHeight.Normal);

        // Pring line at bottom of signature
        finalPosition = addLine(finalPosition);

        Canvas c = new Canvas(bitmap);

        // Draw the signature image at the previously stored position
        c.drawBitmap(signature, 20, signaturePosition, null);

        return finalPosition;
    }

    @Override
    /**
     * Adds a space of height @ the current maximum y position.
     * @param height - Space to be added.
     */
    public int addSpacer(int yPosition, SpacerHeight spacerHeight)
    {
        int height;

        switch (spacerHeight)
        {
            case Small:
                height = 10;
                break;

            case Normal:
                height = 20;
                break;

            case Large:
                height = 30;
                break;

            case XLarge:
                height = 40;
                break;

            default:
                height = 20;
                break;
        }

        // Test if this will exceed the bounds of the current bitmap
        if (getMaximumY() + height > bitmap.getHeight())
        {
            extendBitmap(height);
        }

        // Update the current maximum height
        maximumY += height;

        return yPosition + height;
    }

    private static Paint createBlackPaint()
    {
        Paint paint = new Paint();

        paint.setColor(Color.BLACK);

        return paint;
    }

    private Paint createTextPaint(Size textSize, String font, Alignment alignment)
    {
        Paint paint = createBlackPaint();

        // Set the size of the font to be used
        switch (textSize)
        {
            case Large:
                paint.setTextSize(FONT_SIZE_LARGE);
                break;

            case Normal:
                paint.setTextSize(FONT_SIZE_NORMAL);
                break;

            case Small:
                paint.setTextSize(FONT_SIZE_SMALL);
                break;

            default:
                paint.setTextSize(FONT_SIZE_NORMAL);
                break;
        }

        // Set the font to be used
        paint.setTypeface(getTypeFace(font));

        // Set the text alignment
        switch (alignment)
        {
            case Centre:
                paint.setTextAlign(Paint.Align.CENTER);
                break;

            case Right:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;

            default:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
        }

        return paint;
    }

    private static Rect getTextBounds(Paint paint, String text)
    {
        Rect bounds = new Rect();

        paint.getTextBounds(text, 0, text.length(), bounds);

        return bounds;
    }

    @Override
    public int addTextCentre(Size size, int xPosition, int yPosition, int width, String text)
    {
        // New Paint
        Paint paint = createTextPaint(size, "trebuc.ttf", Alignment.Centre);

        // Get the rectangular bounds of the text
        Rect bounds = getTextBounds(paint, text); // new Rect();

        // Get the total height of the text to be output
        int height = bounds.bottom - bounds.top;

        int maximumHeight = yPosition + height;

        int bitmapHeight = bitmap.getHeight();

        if (maximumHeight > bitmapHeight)
        {
            // Extend the bitmap to accommodate
            extendBitmap(maximumHeight - bitmapHeight);
        }

        Canvas canvas = new Canvas(bitmap);

        canvas.clipRect(xPosition, yPosition, xPosition + width, yPosition + height);

        // Draw the text
        canvas.drawText(text, width / 2 + xPosition, yPosition - bounds.top, paint);

        if (maximumHeight > maximumY)
        {
            maximumY = maximumHeight;
        }

        return maximumHeight;
    }

    @Override
    public int addTextLeft(Size size, int xPosition, int yPosition, int width, String text)
    {
        // New Paint
        Paint paint = createTextPaint(size, "trebuc.ttf", Alignment.Left);

        // Get the rectangular bounds of the text
        Rect bounds = getTextBounds(paint, text); // new Rect();

        // Get the total height of the text to be output
        int height = bounds.bottom - bounds.top;

        int maximumHeight = yPosition + height;

        int bitmapHeight = bitmap.getHeight();

        if (maximumHeight > bitmapHeight)
        {
            // Extend the bitmap to accommodate
            extendBitmap(maximumHeight - bitmapHeight);
        }

        Canvas canvas = new Canvas(bitmap);

        canvas.clipRect(xPosition, yPosition, xPosition + width, yPosition + height);

        // Draw the text
        canvas.drawText(text, xPosition, yPosition - bounds.top, paint);

        if (maximumHeight > maximumY)
        {
            maximumY = maximumHeight;
        }

        return maximumHeight;
    }

    @Override
    public int addTextRight(Size size, int xPosition, int yPosition, int width, String text)
    {
        // New Paint
        Paint paint = createTextPaint(size, "trebuc.ttf", Alignment.Right);

        // Get the rectangular bounds of the text
        Rect bounds = getTextBounds(paint, text);

        // Get the total height of the text to be output
        int height = bounds.bottom - bounds.top;

        int maximumHeight = yPosition + height;

        int bitmapHeight = bitmap.getHeight();

        if (maximumHeight > bitmapHeight)
        {
            // Extend the bitmap to accommodate
            extendBitmap(maximumHeight - bitmapHeight);
        }

        Canvas canvas = new Canvas(bitmap);

        canvas.clipRect(xPosition, yPosition, xPosition + width, yPosition + height);

        // Draw the text
        canvas.drawText(text, width + xPosition, yPosition - bounds.top, paint);

        if (maximumHeight > maximumY)
        {
            maximumY = maximumHeight;
        }

        return maximumHeight;
    }

    /**
     * Adds a horizontal line to the bitmap
     * @param yPosition The distance from the top of the bitmap where
     *                  the horizontal line is to be drawn.
     * @return
     */
    @Override
    public int addLine(int yPosition)
    {
        // Call the overloaded method ...
        return addLine(0, yPosition, PRINTER_WIDTH, yPosition);
    }

    @Override
    public int addLine(int xStart, int yStart, int xEnd, int yEnd)
    {
        // Calculate the total height of the line difference
        int height = yEnd - yStart;

        int maximumHeight = yStart + height;

        int bitmapHeight = bitmap.getHeight();

        if (maximumHeight > bitmapHeight)
        {
            // Extend the bitmap to accommodate
            extendBitmap(maximumHeight - bitmapHeight);
        }

        Canvas canvas = new Canvas(bitmap);

        Paint blackPaint = createBlackPaint();

        blackPaint.setStrokeWidth(2.0f);

        // Draw the text
        canvas.drawLine(xStart, yStart, xEnd, yEnd, blackPaint);

        if (maximumHeight > maximumY)
        {
            maximumY = maximumHeight;
        }

        return yEnd;
    }

    public int addLogo(int brandId, int yPosition) throws Exception
    {
        int finalPosition = yPosition;

        dbSetting logo = dbSetting.FindByKey("Brand Logo:" + brandId);

        byte[] buffer = null;

        if (logo != null)
        {
            buffer = logo.BinaryValue;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inDither = true;
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inScaled = false;
        opts.inDensity = 240;

        // Convert from PCX format to Android Bitmap object
        Bitmap bm = ImageConverter.parsePcx(buffer);

        // Get the total height of the Bitmap to be output
        int height = bm.getHeight();

        int maximumHeight = finalPosition + height;

        int bitmapHeight = bitmap.getHeight();

        if (maximumHeight > bitmapHeight)
        {
            // Extend the bitmap to accommodate
            extendBitmap(maximumHeight - bitmapHeight);
        }

        Canvas c = new Canvas(bitmap);

        c.drawBitmap(bm, 0, 0, null);

        return maximumHeight;
    }

    /**
     * Truncate the bitmap before printing to the maximum print
     * position plus 30 (thirty) pixels.
     * @param bitmap The Bitmap to be truncated.
     * @param maxHeight The height (plus 30 pixels) of the output Bitmap.
     * @return The truncated bitmap.
     */
    private static Bitmap truncateBitmap(Bitmap bitmap, int maxHeight)
    {
        Bitmap outputBitmap = createWhiteBitmap(bitmap.getWidth(), maxHeight + 30);

        Canvas canvas = new Canvas(outputBitmap);

        Rect sourceRectangle = new Rect(0, 0, PRINTER_WIDTH, maxHeight);
        Rect destinationRectangle = new Rect(0, 0, PRINTER_WIDTH, maxHeight);

        canvas.drawBitmap(bitmap, sourceRectangle, destinationRectangle, null);

        return outputBitmap;
    }

    @Override
    public byte[] addBitmap() throws UnsupportedEncodingException
    {
        // Get the truncated bitmap
        Bitmap truncatedBitmap = truncateBitmap(bitmap, maximumY);

        // Convert to a byte array holding a 1-bit PCX format graphic
        byte[] imageData = ImageConverter.convertToPcx(truncatedBitmap);

        printJob.append("PCX 0 0\n");
        printJob.append(new String(imageData, "ISO-8859-1"));
        printJob.append("\r\n");

        increaseYPos(truncatedBitmap.getHeight());

        return imageData;
    }

    @Override
    public String getPrinterData() throws Exception
    {
        StringBuilder builder = new StringBuilder();

        builder.append("! DF RUN.BAT\r\n");
        builder.append("! UTILITIES\r\n");
        builder.append("JOURNAL\r\n");
        builder.append("SETFF 0 5\r\n");
        builder.append("PRINT\r\n");
        builder.append("! 0 200 200 ");
        builder.append(yPos);
        builder.append(" 1\r\n");
        builder.append(printJob);
        builder.append("PRINT\r\n");

        return builder.toString();
    }
}
