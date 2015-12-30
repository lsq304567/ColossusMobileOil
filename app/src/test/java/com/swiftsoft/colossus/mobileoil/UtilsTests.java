package com.swiftsoft.colossus.mobileoil;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class UtilsTests
{
    @Test
    public void convert_2_int_null()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int(null));
    }

    @Test
    public void convert_2_int_empty()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int(""));
    }

    @Test
    public void convert_2_int_zero()
    {
        assertEquals("Not zero", 0, Utils.convert2Int("0"));
        assertEquals("Not minus zero", 0, Utils.convert2Int("-0"));
    }

    @Test
    public void convert_2_int_one()
    {
        assertEquals("Not one", 1, Utils.convert2Int("1"));
    }

    @Test
    public void convert_2_int_minus_one()
    {
        assertEquals("Not minus one", -1, Utils.convert2Int("-1"));
    }

    @Test
    public void convert_2_int_one_thousand()
    {
        assertEquals("Not one thousand", 1000, Utils.convert2Int("1000"));
    }

    @Test
    public void convert_2_int_one_thousand_comma()
    {
        assertEquals("Expected zero to be returned", 0, Utils.convert2Int("1,000"));
    }

    @Test
    public void convert_2_int_minus_one_thousand()
    {
        assertEquals("Not one thousand", -1000, Utils.convert2Int("-1000"));
    }

    @Test
    public void convert_2_int_minus_one_thousand_comma()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int("-1,000"));
    }

    @Test
    public void convert_2_int_decimal_point()
    {
        assertEquals("Expected 0 to be returned", 0, Utils.convert2Int("1.5"));
    }

    @Test
    public void to_string_to_null_value_present()
    {
        assertEquals("Expected value", "String value", Utils.toStringNoNull("String value", "Null string"));
    }

    @Test
    public void to_string_to_null_value_empty()
    {
        assertEquals("Expected value", "", Utils.toStringNoNull("", "Null string"));
    }

    @Test
    public void to_string_to_null_value_null()
    {
        assertEquals("Expected null value", "Null string", Utils.toStringNoNull(null, "Null string"));
    }

    @Test
    public void round_nearest_2_decimal_places()
    {
        assertEquals("Expected 1.23", 1.23, Utils.roundNearest(1.234, 2), 0.0001);
        assertEquals("Expected 1.24", 1.24, Utils.roundNearest(1.235, 2), 0.0001);

        assertEquals("Expected -1.23", -1.23, Utils.roundNearest(-1.234, 2), 0.0001);
        assertEquals("Expected -1.24", -1.24, Utils.roundNearest(-1.235, 2), 0.0001);
    }

    @Test
    public void round_nearest_big_decimal_2_decimal_places()
    {
        assertEquals("Expected 1.23", new BigDecimal("1.23"), Utils.roundNearest(new BigDecimal("1.234"), 2));
        assertEquals("Expected 1.24", new BigDecimal("1.24"), Utils.roundNearest(new BigDecimal("1.235"), 2));

        assertEquals("Expected -1.23", new BigDecimal("-1.23"), Utils.roundNearest(new BigDecimal("-1.234"), 2));
        assertEquals("Expected -1.24", new BigDecimal("-1.24"), Utils.roundNearest(new BigDecimal("-1.235"), 2));
    }
}
