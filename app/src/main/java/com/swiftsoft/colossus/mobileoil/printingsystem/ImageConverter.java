package com.swiftsoft.colossus.mobileoil.printingsystem;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Alan on 11/06/2015.
 */
public class ImageConverter
{
    public static byte[] convertToPcx(Bitmap bitmap)
    {
        // Create output object
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Construct the header to be written
        byte[] header = createPcxHeader(bitmap);

        // Write the header to the output.
        output.write(header, 0, header.length);

        int bytesPerPlane = (int)Math.ceil(bitmap.getWidth() / 8.0);

        if (bytesPerPlane % 2 == 1)
        {
            bytesPerPlane++;
        }

        // Get height & width of bitmap to slightly speed things by not
        // continually having to call getters.
        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();

        // Get buffer large enough to hold row of pixels
        int[] pixelRow = new int[bitmapWidth * bitmapHeight];

        // Get row of pixels using Bitmap.getPixels()
        bitmap.getPixels(pixelRow, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

        // Process each of the lines in the Bitmap.
        for (int y = 0; y < bitmapHeight; y++)
        {
            // Create a byte array large enough to store the line of
            // pixels converted to 1-bit per pixel.
            byte[] lineBuffer = new byte[bytesPerPlane];

            // Process each pixel in the Bitmap line
            for (int i = 0, x = 0; x < bitmapWidth; i++, x += 8)
            {
                byte pixelOctet = (byte)0xff;

                int offset = (y * bitmapWidth) + x;

                if (pixelRow[offset + 0] != Color.WHITE) pixelOctet &= (byte)0x7f;
                if (pixelRow[offset + 1] != Color.WHITE) pixelOctet &= (byte)0xbf;
                if (pixelRow[offset + 2] != Color.WHITE) pixelOctet &= (byte)0xdf;
                if (pixelRow[offset + 3] != Color.WHITE) pixelOctet &= (byte)0xef;
                if (pixelRow[offset + 4] != Color.WHITE) pixelOctet &= (byte)0xf7;
                if (pixelRow[offset + 5] != Color.WHITE) pixelOctet &= (byte)0xfb;
                if (pixelRow[offset + 6] != Color.WHITE) pixelOctet &= (byte)0xfd;
                if (pixelRow[offset + 7] != Color.WHITE) pixelOctet &= (byte)0xfe;

                lineBuffer[i] = pixelOctet;
            }

            // The run count is initially set to 1
            int runCount = 1;

            // Set the byte whose run length is being determined to the
            // first byte in the line.
            byte lastByte = lineBuffer[0];

            // Process over all the bytes in the line, writing the
            // run length data to the output as required.
            for (int i = 1; i < bytesPerPlane; i++)
            {
                // Determine if the next character is part of a 'run'
                if (lineBuffer[i] == lastByte)
                {
                    // Increment the number of bytes in the 'run'
                    runCount++;

                    // When we reach the maximum run count of 63
                    // write the data to the output. Then set the
                    // run count back to zero.
                    if (runCount == 63)
                    {
                        writeData(output, lastByte, runCount);

                        runCount = 0;
                    }
                }
                else
                {
                    // No 'run'
                    if (runCount > 0)
                    {
                        writeData(output, lastByte, runCount);
                    }

                    runCount = 1;

                    // Store the new byte that is the target of the next 'run'
                    lastByte = lineBuffer[i];
                }
            }

            if (runCount > 0)
            {
                writeData(output, lastByte, runCount);
            }
        }

        // Return the data as a byte array.
        return output.toByteArray();
    }

    private static Bitmap createOutputBitmap(PcxHeader header)
    {
        Bitmap bitmap = Bitmap.createBitmap(header.getImageWidth(), header.getImageHeight(), Bitmap.Config.ARGB_8888);

        bitmap.eraseColor(Color.WHITE);

        return bitmap;
    }

    private static int[] readLine(PcxHeader header, DataInputStream stream) throws IOException
    {
        // Calculate the total number of pixels in one line of the image
        int numberOfPixels = header.getImageWidth();

        // Create buffer large enough to hold this
        int[] buffer = new int[numberOfPixels];

        int runCount;
        byte runValue;

        int bufferIndex = 0;

        do
        {
            // Read byte from the stream
            byte b = stream.readByte();

            if ((b & (byte)0xc0) == (byte)0xc0)
            {
                // Calculate the run count
                runCount = b & (byte)0x3f;

                // Read the actual value from the stream
                runValue = stream.readByte();
            }
            else
            {
                // There is a run count of only one
                runCount = 1;

                // The actual pixel value(s)
                runValue = b;
            }

            // Write the pixel run to the line buffer
            for (int i = 0; i < runCount; i++)
            {
                // Loop through each bit generating a pixel color for each one
                for (int bitIndex = 0; bitIndex < 8; bitIndex++)
                {
                    if (bufferIndex < numberOfPixels)
                    {
                        buffer[bufferIndex] = getPixelValue(bitIndex, runValue);
                    }

                    bufferIndex++;
                }
            }
        }
        while (bufferIndex < numberOfPixels);

        return buffer;
    }

    private static int getPixelValue(int bitIndex, byte value)
    {
        if ((value & (byte)(0x01 << (7 - bitIndex))) != (byte)0x00)
        {
            return Color.WHITE;
        }

        return Color.BLACK;
    }

    public static Bitmap parsePcx(byte[] buffer) throws Exception
    {
        // Extract the PCX Header from the buffer
        PcxHeader pcxHeader = getPcxHeader(buffer);

        // Create the Bitmap that is to be returned with the rendered PCX image.
        Bitmap bitmap = createOutputBitmap(pcxHeader);

        // Create streams for reading RLE data from buffer
        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer, 128, buffer.length - 128);
        DataInputStream dataStream = new DataInputStream(byteStream);

        try
        {
            int imageHeight = pcxHeader.getImageHeight();

            // Process each line of the PCX image in turn
            for (int line = 0; line < imageHeight; line++)
            {
                // Read line of pixels from the PCX buffer
                int[] lineBuffer = readLine(pcxHeader, dataStream);

                int lineBufferLength = lineBuffer.length;

                bitmap.setPixels(lineBuffer, 0, bitmap.getWidth(), 0, line, lineBufferLength, 1);
            }

            return bitmap;
        }
        finally
        {
            dataStream.close();

            byteStream.close();
        }
    }

    private static PcxHeader getPcxHeader(byte[] buffer) throws IOException
    {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer, 0, 128);

        try
        {
            return new PcxHeader(stream);
        }
        finally
        {
            // Make sure that the stream is closed
            stream.close();
        }
    }

    private static byte[] createPcxHeader(Bitmap bitmap)
    {
        // The header is 128 bytes long
        byte[] header = new byte[0x80];

        header[0x00] = (byte)0x0a; // 10 = ZSoft .pcx
        header[0x01] = (byte)0x05; // 5 = Version 3.0 of PC Paintbrush

        // Set encoding to RLE (Run Length Encoding)
        setEncoding(header);

        // Set to 1 bit monochrome
        setBitsPerPixel(header);

        // X-Min
        header[0x04] = (byte)0x00;
        header[0x05] = (byte)0x00;

        // Y-Min
        header[0x06] = (byte)0x00;
        header[0x07] = (byte)0x00;

        // X-Max
        setMaximumX(header, bitmap.getWidth());

        // Y-Max
        setMaximumY(header, bitmap.getHeight());

        // Horizontal DPI
        setHorizontalDpi(header, 96);

        // Vertical DPI
        setVerticalDpi(header, 96);

        // Colour Map
        setColorMap(header);

        // Reserved
        header[0x40] = (byte)0x00;

        // Set the number of bit planes
        header[0x41] = (byte)0x01;

        // Set the number of bytes per scan line
        setBytesPerScanLine(header, bitmap.getWidth());

        return header;
    }

    private static void setBytesPerScanLine(byte[] header, int width)
    {
        int bytesPerPlane = (int)Math.ceil(width / 8.0);

        if (bytesPerPlane % 2 == 1)
        {
            bytesPerPlane++;
        }

        header[0x42] = (byte)bytesPerPlane;
        header[0x43] = (byte)(bytesPerPlane >> 8);
    }

    private static void setEncoding(byte[] header)
    {
        header[0x02] = (byte)0x01; // 1 = PCX run length encoding
    }

    private static void setBitsPerPixel(byte[] header)
    {
        header[0x03] = (byte)0x01; // 1 = 1 bit per pixel
    }

    private static void setColorMap(byte[] header)
    {
        header[0x13] = (byte)0xff;
        header[0x14] = (byte)0xff;
        header[0x15] = (byte)0xff;

        for (int i = 0; i < 45; i++)
        {
            header[i + 0x16] = (byte)0x00;
        }
    }

    private static void setMaximumX(byte[] header, int width)
    {
        header[0x08] = (byte)(width - 1);
        header[0x09] = (byte)((width - 1) >> 8);
    }

    private static void setMaximumY(byte[] header, int height)
    {
        header[0x0a] = (byte)(height - 1);
        header[0x0b] = (byte)((height - 1) >> 8);
    }

    private static void setVerticalDpi(byte[] header, int dpi)
    {
        header[0x0e] = (byte)dpi;
        header[0x0f] = (byte)(dpi >> 8);
    }

    private static void setHorizontalDpi(byte[] header, int dpi)
    {
        header[0x0c] = (byte)dpi;
        header[0x0d] = (byte)(dpi >> 8);
    }

    /**
     * Write run length encoded data to the output stream.
     * @param stream - The stream to which the data is written.
     * @param lastByte - The value of the run length encoded byte to be output.
     * @param runCount - The number of bytes in the run.
     */
    private static void writeData(ByteArrayOutputStream stream, byte lastByte, int runCount)
    {
        // Only write to the output if there is at least one
        // byte in the run count
        if (runCount > 0)
        {
            if (runCount == 1 && (byte)(lastByte & 0xc0) != (byte)0xc0)
            {
                stream.write(lastByte);
            }
            else
            {
                stream.write((byte)(0xc0 | runCount));
                stream.write(lastByte);
            }
        }
    }
}
