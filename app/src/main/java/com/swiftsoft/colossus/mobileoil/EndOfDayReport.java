package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.database.model.dbDriver;
import com.swiftsoft.colossus.mobileoil.database.model.dbEndOfDay;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class EndOfDayReport extends Activity
{
    private MyInfoView1Line infoView;

    private Button btnFinish;

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

            // Find the navigation buttons at the bottom of the screen
            Button btnBack = (Button) findViewById(R.id.eod_report_back);
            btnFinish = (Button)findViewById(R.id.eod_report_finish);

            // Find the print & change printer buttons
            Button btnPrint = (Button) findViewById(R.id.eod_report_print);
            Button btnChangePrinter = (Button) findViewById(R.id.eod_report_change);

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
            try
            {
                CrashReporter.leaveBreadcrumb("EndOfDayReport: onClick");

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

                        // Send EoD report to Colossus
                        sendEndOfDayReport();

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
            catch (Exception e)
            {
                CrashReporter.logHandledException(e);
            }
        }
    };

    private void sendEndOfDayReport() throws Exception
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: sendEndOfDayReport");

        Context context = getApplicationContext();

        JSONObject json = new JSONObject();

        json.put("Vehicle", getVehicleDetails());
        json.put("Driver", getDriverDetails());
        json.put("Date", "/Date(" + Utils.getCurrentTime() + ")/");
        json.put("TripIDs", getTripIds());
        json.put("Products", getProducts());
        json.put("Payments", getPayments());

        // Create Intent object to send message to Colossus
        Intent intent = new Intent(context, ColossusIntentService.class);

        intent.putExtra("Type", "EndOfDayReport");
        intent.putExtra("Content", json.toString());

        // Send the report
        context.startService(intent);
    }

    private JSONObject getVehicleDetails() throws Exception
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: getVehicleDetails");

        // Get vehicle details
        dbVehicle vehicle = dbVehicle.FindByNo(Active.vehicle.No);

        JSONObject json = new JSONObject();

        json.put("ID", vehicle.No);
        json.put("Reg", vehicle.Reg);

        return json;
    }

    private JSONObject getDriverDetails() throws Exception
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: getDriverDetails");

        // Get driver details
        dbDriver driver = dbDriver.FindByNo(Active.driver.No);

        JSONObject json = new JSONObject();

        json.put("ID", driver.No);
        json.put("Reg", driver.Name);

        return json;
    }

    private JSONArray getTripIds()
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: getTripIds");

        JSONArray array = new JSONArray();

        for (Integer tripId : dbEndOfDay.getUniqueTripIds())
        {
            array.put(tripId);
        }

        return array;
    }

    private JSONArray getProducts() throws Exception
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: getProducts");

        JSONArray array = new JSONArray();

        for (dbProduct product : dbEndOfDay.getUniqueProducts())
        {
            JSONObject json = new JSONObject();

            json.put("ID", product.ColossusID);
            json.put("Starting", dbEndOfDay.getStartingQuantity(product));
            json.put("Loaded", dbEndOfDay.getLoadedQuantity(product));
            json.put("Delivery", dbEndOfDay.getDeliveredQuantity(product));
            json.put("Return", dbEndOfDay.getReturnedQuantity(product));
            json.put("Finishing", dbEndOfDay.getFinishingQuantity(product));

            array.put(json);
        }

        return array;
    }

    private JSONArray getPayments() throws Exception
    {
        CrashReporter.leaveBreadcrumb("EndOfDayReport: getPayments");

        DecimalFormat format2dp = new DecimalFormat("###0.00");

        JSONArray array = new JSONArray();

        BigDecimal cashPayment = dbEndOfDay.getCashPayments();
        BigDecimal chequePayment = dbEndOfDay.getChequePayments();
        BigDecimal voucherPayment = dbEndOfDay.getVoucherPayments();

        if (cashPayment.compareTo(BigDecimal.ZERO) > 0)
        {
            JSONObject json = new JSONObject();

            json.put("Type", "Cash");
            json.put("Amount", format2dp.format(cashPayment));

            array.put(json);
        }

        if (chequePayment.compareTo(BigDecimal.ZERO) > 0)
        {
            JSONObject json = new JSONObject();

            json.put("Type", "Cheque");
            json.put("Amount", format2dp.format(chequePayment));

            array.put(json);
        }

        if (voucherPayment.compareTo(BigDecimal.ZERO) > 0)
        {
            JSONObject json = new JSONObject();

            json.put("Type", "Voucher");
            json.put("Amount", format2dp.format(voucherPayment));

            array.put(json);
        }

        return array;
    }
}
