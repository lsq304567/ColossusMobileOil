package com.swiftsoft.colossus.mobileoil.utilities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView3Line;

/**
 * Created by Alan on 02/09/2015.
 */
public class ControlSaver
{
    public static void save(View control, String baseName, Bundle bundle)
    {
        if (control instanceof Button)
        {
            bundle.putString(baseName + ".Text", ((Button) control).getText().toString());
            bundle.putBoolean(baseName + ".Enabled", control.isEnabled());
            bundle.putInt(baseName + ".Visibility", control.getVisibility());
        }
        else if (control instanceof TextView)
        {
            bundle.putString(baseName + ".Text", ((TextView) control).getText().toString());
            bundle.putBoolean(baseName + ".Enabled", control.isEnabled());
            bundle.putInt(baseName + ".Visibility", control.getVisibility());
        }
        else if (control instanceof MyEditText)
        {
            bundle.putString(baseName + ".Text", ((MyEditText) control).getText().toString());
            bundle.putBoolean(baseName + ".Enabled", control.isEnabled());
            bundle.putInt(baseName + ".Visibility", control.getVisibility());
        }
        else if (control instanceof MyInfoView1Line)
        {
            bundle.putString(baseName + ".TV1", ((MyInfoView1Line) control).getDefaultTv1());
            bundle.putString(baseName + ".TV2", ((MyInfoView1Line) control).getDefaultTv2());
        }
        else if (control instanceof MyInfoView3Line)
        {
            bundle.putString(baseName + ".TV1", ((MyInfoView3Line) control).getDefaultTv1());
            bundle.putString(baseName + ".TV2", ((MyInfoView3Line) control).getDefaultTv2());
            bundle.putString(baseName + ".TV3", ((MyInfoView3Line) control).getDefaultTv3());
        }
        else if (control instanceof CheckBox)
        {
            bundle.putString(baseName + ".Text", ((CheckBox) control).getText().toString());
            bundle.getBoolean(baseName + ".Enabled", control.isEnabled());
            bundle.getInt(baseName + ".Visibility", control.getVisibility());
            bundle.getBoolean(baseName + ".Checked", ((CheckBox) control).isChecked());
        }
        else if (control instanceof LinearLayout)
        {
            bundle.putBoolean(baseName + ".Enabled", control.isEnabled());
            bundle.putInt(baseName + ".Visibility", control.getVisibility());
        }
    }

    @SuppressWarnings("ResourceType")
    public static void restore(View control, String baseName, Bundle bundle)
    {
        if (control instanceof Button)
        {
            ((Button) control).setText(bundle.getString(baseName + ".Text"));
            control.setEnabled(bundle.getBoolean(baseName + ".Enabled"));
            control.setVisibility(bundle.getInt(baseName + ".Visibility"));
        }
        else if (control instanceof TextView)
        {
            ((TextView) control).setText(bundle.getString(baseName + ".Text"));
            control.setEnabled(bundle.getBoolean(baseName + ".Enabled"));
            control.setVisibility(bundle.getInt(baseName + ".Visibility"));
        }
        else if (control instanceof  MyEditText)
        {
            ((MyEditText) control).setText(bundle.getString(baseName + ".Text"));
            control.setEnabled(bundle.getBoolean(baseName + ".Enabled"));
            control.setVisibility(bundle.getInt(baseName + ".Visibility"));
        }
        else if (control instanceof  MyInfoView1Line)
        {
            ((MyInfoView1Line) control).setDefaultTv1(bundle.getString(baseName + ".TV1"));
            ((MyInfoView1Line) control).setDefaultTv2(bundle.getString(baseName + ".TV2"));
        }
        else if (control instanceof MyInfoView3Line)
        {
            ((MyInfoView3Line) control).setDefaultTv1(bundle.getString(baseName + ".TV1"));
            ((MyInfoView3Line) control).setDefaultTv2(bundle.getString(baseName + ".TV2"));
            ((MyInfoView3Line) control).setDefaultTv3(bundle.getString(baseName + ".TV3"));
        }
        else if (control instanceof CheckBox)
        {
            ((CheckBox) control).setText(bundle.getString(baseName + ".Text"));
            control.setEnabled(bundle.getBoolean(baseName + ".Enabled"));
            control.setVisibility(bundle.getInt(baseName + ".Visibility"));
            ((CheckBox) control).setChecked(bundle.getBoolean(baseName + ".Checked"));
        }
        else if (control instanceof LinearLayout)
        {
            control.setEnabled(bundle.getBoolean(baseName + ".Enabled"));
            control.setVisibility(bundle.getInt(baseName + ".Visibility"));
        }
    }
}
