package com.swiftsoft.colossus.mobileoil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTests
{
    @Test
    public void convert2int_zero_string()
    {
        assertEquals("Not zero", 0, Utils.Convert2Int("0"));
    }
}
