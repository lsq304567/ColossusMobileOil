package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.swiftsoft.colossus.mobileoil.utilities.SecureSettings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.th

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

    @Test
    public void truncate_double_2_decimal_places()
    {
        assertEquals("Expected 1.23", 1.23, Utils.truncate(1.234, 2), 0.0001);
        assertEquals("Expected 1.23", 1.23, Utils.truncate(1.236, 2), 0.0001);

        assertEquals("Expected -1.23", -1.23, Utils.truncate(-1.234, 2), 0.0001);
        assertEquals("Expected -1.23", -1.23, Utils.truncate(-1.236, 2), 0.0001);
    }

    @Test
    public void truncate_big_decimal_2_decimal_places()
    {
        assertEquals("Expected 1.23", new BigDecimal("1.23"), Utils.truncate(new BigDecimal("1.234"), 2));
        assertEquals("Expected 1.23", new BigDecimal("1.23"), Utils.truncate(new BigDecimal("1.236"), 2));

        assertEquals("Expected -1.23", new BigDecimal("-1.23"), Utils.truncate(new BigDecimal("-1.234"), 2));
        assertEquals("Expected -1.23", new BigDecimal("-1.23"), Utils.truncate(new BigDecimal("-1.236"), 2));
    }

    @Test
    public void get_serial_no_context_null()
    {
        assertEquals("Expected empty serial number", "", Utils.getSerialNo(null));
    }

    @Test
    public void get_serial_no_failed_to_get_telephony_manager()
    {
        // Mock Context
        Context context = Mockito.mock(Context.class);

        // Failed to retrieve TelephonyManager
        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);

        assertEquals("Expected empty serial number", "", Utils.getSerialNo(context));
    }

    @Test
    public void get_serial_no_failed_to_get_device_id()
    {
        // Mock the TelephonyManager
        TelephonyManager tm = Mockito.mock(TelephonyManager.class);

        when(tm.getDeviceId()).thenReturn(null);

        // Mock the Context
        Context context = Mockito.mock(Context.class);

        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(tm);

        SecureSettings settings = Mockito.mock(SecureSettings.class);

        when(settings.getSerialNumber(context)).thenReturn("1234567890");

        Utils.SecureSettings = settings;

        assertEquals("Expected something else", "1234567890", Utils.getSerialNo(context));
    }

    @Test
    public void get_serial_no()
    {
        // Mock the TelephonyManager
        TelephonyManager tm = Mockito.mock(TelephonyManager.class);

        when(tm.getDeviceId()).thenReturn("666777888999");

        // Mock the Context
        Context context = Mockito.mock(Context.class);

        when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(tm);

        assertEquals("", "666777888999", Utils.getSerialNo(context));
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void show_keyboard_edit_text_null()
    {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Valid EditText object must be passed.");

        // This should cause a NullPointerException to be thrown ...
        Utils.showKeyboard(null);
    }

    @Test
    public void show_keyboard_edit_text()
    {
        InputMethodManager imm = Mockito.mock(InputMethodManager.class);

        // Mock Context object
        Context context = Mockito.mock(Context.class);

        when(context.getSystemService(Context.INPUT_METHOD_SERVICE)).thenReturn(imm);

        // Mock EditText control
        EditText et = Mockito.mock(EditText.class);

        when(et.getContext()).thenReturn(context);

        Utils.showKeyboard(et);

        verify(imm).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
        verify(imm).showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }
}
