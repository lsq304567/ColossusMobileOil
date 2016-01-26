package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Undelivered_Ticket extends MyFlipperView
{
	private Trip trip;
	private dbTripOrder order;

	private MyInfoView1Line infoview;
	private Button btnBack;
	private Button btnFinish;

	public Trip_Undelivered_Ticket(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Ticket(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_ticket, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_ticket_infoview);
			Button btnPrint = (Button) this.findViewById(R.id.trip_undelivered_ticket_print);
			Button btnChange = (Button) this.findViewById(R.id.trip_undelivered_ticket_change);
			btnBack = (Button)this.findViewById(R.id.trip_undelivered_ticket_back);
			btnFinish = (Button)this.findViewById(R.id.trip_undelivered_ticket_finish);
			
			btnPrint.setOnClickListener(onClickListener);
			btnChange.setOnClickListener(onClickListener);
			btnBack.setOnClickListener(onClickListener);
			btnFinish.setOnClickListener(onClickListener);
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
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: resumeView");

			// Resume updating.
			infoview.resume();

			// Enable 'Back' button.
			btnBack.setEnabled(true);
			
			// Disable 'Finish' button.
			btnFinish.setEnabled(false);
		
			// Store another reference to the active order.
			// As the trip.orderDelivered will set it to null,
			// and we need it for re-printing.
			order = Active.order;
		
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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: pauseView");

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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_ticket: updateUI");

			// Set order no.
			infoview.setDefaultTv1("Order " + order.InvoiceNo);
			infoview.setDefaultTv2("");
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
                switch (view.getId())
                {
                    case R.id.trip_undelivered_ticket_print:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: onClick - print");

                        // Print ticket.
                        Printing.ticket(trip, order);

                        // Mark order as delivered.
                        if (Active.order != null)
                        {
                            trip.orderDelivered();
                        }

                        btnBack.setEnabled(false);
                        btnFinish.setEnabled(true);

                        break;

                    case R.id.trip_undelivered_ticket_change:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: onClick - change printer");

                        // Show setting activity.
                        trip.changeSettings();

                        break;

                    case R.id.trip_undelivered_ticket_back:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: onClick - back");

                        // Switch views.
                        trip.selectView(Trip.ViewUndeliveredDeliveryNote, -1);

                        break;

                    case R.id.trip_undelivered_ticket_finish:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Ticket: onClick - finish");

                        // Finally order is delivered!!
                        trip.selectView(Trip.ViewUndeliveredList, +1);

                        break;
                }
            }
            catch (Exception e)
            {
                CrashReporter.logHandledException(e);
            }
        }
    };
}
