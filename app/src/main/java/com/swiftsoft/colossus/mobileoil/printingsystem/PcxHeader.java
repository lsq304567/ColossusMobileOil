package com.swiftsoft.colossus.mobileoil.printingsystem;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Alan on 22/06/2015.
 */
public class PcxHeader
{
    public PcxHeader(ByteArrayInputStream stream) throws IOException
    {
        DataInputStream is = new DataInputStream(stream);

        try
        {
            // Read the id
            id = is.readByte();

            // Read the version
            version = is.readByte();

            // Read the encoding
            encoding = is.readByte();

            // Read the bits per pixel
            bitsPerPixel = is.readByte();

            // Read the X Start
            byte lowerByte = is.readByte();
            byte upperByte = is.readByte();

            xStart = upperByte * 256 + lowerByte;

            // Read the Y Start
            lowerByte = is.readByte();
            upperByte = is.readByte();
            yStart = upperByte * 256 + lowerByte;

            // Read the X End
            lowerByte = is.readByte();
            upperByte = is.readByte();
            xEnd = upperByte * 256 + lowerByte;

            // Read the Y End
            lowerByte = is.readByte();
            upperByte = is.readByte();
            yEnd = upperByte * 256 + lowerByte;

            // Read the Horizontal Resolution
            lowerByte = is.readByte();
            upperByte = is.readByte();
            horizontalResolution = upperByte * 256 + lowerByte;

            // Read the Vertical Resolution
            lowerByte = is.readByte();
            upperByte = is.readByte();
            verticalResolution = upperByte * 256 + lowerByte;

            // Skip over the 48 Palette bytes
            is.skipBytes(48);

            // Skip over Reserved byte
            is.skipBytes(1);

            // Read the Number of Bit Planes
            numberOfBitPlanes = is.readByte();

            // Read the Bytes per Line
            lowerByte = is.readByte();
            upperByte = is.readByte();
            bytesPerLine = upperByte * 256 + lowerByte;
        }
        finally
        {
            // Make sure that the DataInputStream is closed
            is.close();
        }
    }

    public int getImageWidth()
    {
        return this.xEnd - this.xStart + 1;
    }

    public int getImageHeight()
    {
        return this.yEnd - this.yStart + 1;
    }

    private byte id;

    public byte getId()
    {
        return this.id;
    }

    private byte version;

    public byte getVersion()
    {
        return version;
    }

    public byte encoding;

    public byte getEncoding()
    {
        return encoding;
    }

    private byte bitsPerPixel;

    private int xStart;

    private int yStart;

    private int xEnd;

    private int yEnd;

    private int horizontalResolution;

    private int verticalResolution;

    private byte numberOfBitPlanes;

    private int bytesPerLine;
}