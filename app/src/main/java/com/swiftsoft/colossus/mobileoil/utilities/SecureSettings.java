package com.swiftsoft.colossus.mobileoil.utilities;

import android.content.Context;

public class SecureSettings implements ISecureSettings
{
    public SecureSettings()
    {

    }

    @Override
    public String getSerialNumber(Context context)
    {
        return android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}
