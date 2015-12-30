package com.swiftsoft.colossus.mobileoil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTests
{
    @Test
    public void convert2int_null()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int(null));
    }

    @Test
    public void convert2int_empty()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int(""));
    }

    @Test
    public void convert2int_zero()
    {
        assertEquals("Not zero", 0, Utils.convert2Int("0"));
        assertEquals("Not minus zero", 0, Utils.convert2Int("-0"));
    }

    @Test
    public void convert2int_one()
    {
        assertEquals("Not one", 1, Utils.convert2Int("1"));
    }

    @Test
    public void convert2int_minus_one()
    {
        assertEquals("Not minus one", -1, Utils.convert2Int("-1"));
    }

    @Test
    public void convert2int_one_thousand()
    {
        assertEquals("Not one thousand", 1000, Utils.convert2Int("1000"));
    }

    @Test
    public void convert2int_one_thousand_comma()
    {
        assertEquals("Expected zero to be returned", 0, Utils.convert2Int("1,000"));
    }

    @Test
    public void convert2int_minus_one_thousand()
    {
        assertEquals("Not one thousand", -1000, Utils.convert2Int("-1000"));
    }

    @Test
    public void convert2int_minus_one_thousand_comma()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int("-1,000"));
    }

    @Test
    public void convert2int_decimal_point()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int("1.5"));
    }
}
