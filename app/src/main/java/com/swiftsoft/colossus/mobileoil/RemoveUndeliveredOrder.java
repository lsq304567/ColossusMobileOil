package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RemoveUndeliveredOrder extends Activity
{
    private Button btnNo;
    private Button btnYes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            CrashReporter.leaveBreadcrumb("RemoveUndeliveredOrder : onCreate");

            // Find UI controls
            btnNo = (Button) findViewById(R.id.remove_undelivered_no);
            btnNo.setOnClickListener(onNoClicked);
            btnYes = (Button) findViewById(R.id.remove_undelivered_yes);
            btnYes.setOnClickListener(onYesClicked);

            setContentView(R.layout.activity_remove_undelivered_order);
        }
        catch (Exception e)
        {
            CrashReporter.logHandledException(e);
        }
    }

    OnClickListener onNoClicked = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {

        }
    };

    OnClickListener onYesClicked = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {

        }
    };
}
