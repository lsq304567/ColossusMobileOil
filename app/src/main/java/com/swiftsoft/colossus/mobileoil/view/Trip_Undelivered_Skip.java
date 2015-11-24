package com.swiftsoft.colossus.mobileoil.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.Trip;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;

public class Trip_Undelivered_Skip extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;

	private Button btnNext;

    private RadioGroup groupReason;

    private EditText etCustomReason;

    private int failureToDeliverReason = -1;

	public Trip_Undelivered_Skip(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Skip(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_skip, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_skip_infoview);

            groupReason = (RadioGroup)this.findViewById(R.id.trip_undelivered_skip_reason);

            // Loop through each of the contained Radio Buttons and set the listener
            for (int buttonIndex = 0; buttonIndex < groupReason.getChildCount(); buttonIndex++)
            {
                RadioButton rb = (RadioButton)groupReason.getChildAt(buttonIndex);

                rb.setOnClickListener(onRadioButtonClicked);
            }

            failureToDeliverReason = -1;

			Button btnBack = (Button)this.findViewById(R.id.trip_undelivered_skip_back);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_skip_next);

            btnNext.setEnabled(false);
			
			btnBack.setOnClickListener(onClickListener);
			btnNext.setOnClickListener(onClickListener);

            etCustomReason = (EditText)this.findViewById(R.id.trip_undelivered_skip_custom_reason);

            etCustomReason.setVisibility(View.GONE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@Override
	public boolean resumeView() 
	{
		try
		{
			// Resume updating.
			infoview.resume();

            for (int buttonIndex = 0; buttonIndex < groupReason.getChildCount(); buttonIndex++)
            {
                RadioButton rb = (RadioButton)groupReason.getChildAt(buttonIndex);

                rb.setChecked(false);
            }

            btnNext.setEnabled(false);

            etCustomReason.setVisibility(View.GONE);

            return true;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return false;
		}
	}

	@Override
	public void pauseView()
	{
		try
		{
			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	public void updateUI() 
	{
		try
		{
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: updateUI");

			// Update the UI.
			dbProduct lineProduct = Active.vehicle.getHosereelProduct();
			
			// Order no.
			infoview.setDefaultTv1("Order " + Active.order.InvoiceNo);
			
			// Line.
            infoview.setDefaultTv2(lineProduct == null ? "Line: None" : "Line: " + lineProduct.Desc);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    private final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onClickListener");

                switch (view.getId())
                {
                    case R.id.trip_undelivered_skip_back:

                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onClickListener - Back button clicked");

                        // Mark the Active.order as OnMobile again.
                        trip.orderStopped();

                        // Switch to order list view.
                        trip.selectView(Trip.ViewUndeliveredSummary, -1);

                        break;

                    case R.id.trip_undelivered_skip_next:

                        trip.orderSkipped(failureToDeliverReason, etCustomReason.getText().toString());

                        // Return to the View
                        trip.selectView(Trip.ViewUndeliveredList, -1);

                        break;
                }
            }
            catch (Exception e)
            {
                CrashReporter.logHandledException(e);
            }
        }
    };

    private final OnClickListener onRadioButtonClicked = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked");

                RadioButton button = (RadioButton)view;

                etCustomReason.setVisibility(View.GONE);

                switch (button.getId())
                {
                    case R.id.trip_undelivered_skip_gate_locked:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Gate Locked");
                        failureToDeliverReason = 0;
                        break;
                    case R.id.trip_undelivered_skip_car_block:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Car Blocking Entrance");
                        failureToDeliverReason = 1;
                        break;
                    case R.id.trip_undelivered_skip_tank_locked:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Tank Locked");
                        failureToDeliverReason = 2;
                        break;
                    case R.id.trip_undelivered_skip_dog_in_garden:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Dog In Garden");
                        failureToDeliverReason = 3;
                        break;
                    case R.id.trip_undelivered_skip_requires_payment:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Requires Payment");
                        failureToDeliverReason = 4;
                        break;
                    case R.id.trip_undelivered_skip_no_access:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason No Access");
                        failureToDeliverReason = 5;
                        break;
                    case R.id.trip_undelivered_skip_other:
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Skip: onRadioButtonClicked - Reason Other");
                        failureToDeliverReason = 6;
                        etCustomReason.setVisibility(View.VISIBLE);
                        break;

                    default:
                        failureToDeliverReason = -1;
                        break;
                }

                btnNext.setEnabled(true);
            }
            catch (Exception e)
            {
                CrashReporter.logHandledException(e);
            }
        }
    };
}
