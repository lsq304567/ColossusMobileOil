package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.database.model.dbEndOfDay;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class EndOfDayReport extends Activity
{
    private MyInfoView1Line infoView;

    private Button btnFinish;
    private Button btnBack;

    private Button btnPrint;
    private Button btnChangePrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            CrashReporter.leaveBreadcrumb("EndOfDayReport: onCreate");

            setContentView(R.layout.end_of_day_report);

            // Find the infoview line
            infoView = (MyInfoView1Line)findViewById(R.id.eod_report_infoview);

            // Find the navigation buttons at the botto of the screen
            btnBack = (Button)findViewById(R.id.eod_report_back);
            btnFinish = (Button)findViewById(R.id.eod_report_finish);

            // Find the print & change printer buttons
            btnPrint = (Button)findViewById(R.id.eod_report_print);
            btnChangePrinter = (Button)findViewById(R.id.eod_report_change);

            // Setup handlers for the buttons
            btnBack.setOnClickListener(buttonClick);
            btnChangePrinter.setOnClickListener(buttonClick);
            btnPrint.setOnClickListener(buttonClick);
            btnFinish.setOnClickListener(buttonClick);
        }
        catch (Exception e)
        {
            CrashReporter.logHandledException(e);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            // Leave breadcrumb.
            CrashReporter.leaveBreadcrumb("EndOfDayReport: onResume");

            updateUI();
        }
        catch (Exception e)
        {
            CrashReporter.logHandledException(e);
        }
    }

    private void updateUI()
    {
        try
        {
            CrashReporter.leaveBreadcrumb("EndOfDayReport: updateUI");

            // Update trip no.
            infoView.setDefaultTv1("EOD Report");
            infoView.setDefaultTv2(Active.driver.Name);

            btnFinish.setEnabled(false);
        }
        catch (Exception e)
        {
            CrashReporter.logHandledException(e);
        }
    }

    private final View.OnClickListener buttonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            // Take action appropriate to the button clicked
            switch (view.getId())
            {
                case R.id.eod_report_back:

                    CrashReporter.leaveBreadcrumb("EndOfDayReport: onClick - Move back");

                    // Start the trips activity.
                    Intent intentBack = new Intent(getApplicationContext(), Trips.class);
                    startActivity(intentBack);

                    break;

                case R.id.eod_report_finish:

                    CrashReporter.leaveBreadcrumb("EndOfDayReport: onClick - Finish report");

                    // Delete all End of Day records from database
                    dbEndOfDay.deleteAll();

                    // Go back to the Trips activity.
                    finish();

                    break;
                
                case R.id.eod_report_print:

                    CrashReporter.leaveBreadcrumb("EndOfDayReport: onClick - Print report");

                    Printing.endOfDayReport(EndOfDayReport.this);

                    btnFinish.setEnabled(true);

                    break;

                case R.id.eod_report_change:

                    // Leave breadcrumb.
                    CrashReporter.leaveBreadcrumb("EndOfDayReport: onClick - Change Printer");

                    // Show settings activity.
                    Intent i = new Intent(getApplicationContext(), Settings.class);

                    startActivity(i);

                    break;
            }
        }
    };
}
