package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.math.BigDecimal;

public class Trip_Undelivered_Summary extends MyFlipperView
{
	private Trip trip;

    private MyInfoView1Line infoview;
	private TextView tvCustomer;
	private TextView tvDelAddressLabel;
	private TextView tvDelAddress;
	private TextView tvPhoneNos;
	private TextView tvRequiredBy;
	private TextView tvProducts;
	private TextView tvTerms;
	private TextView tvNotes;

    public Trip_Undelivered_Summary(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Summary(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_summary, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_summary_infoview);
			tvCustomer = (TextView)this.findViewById(R.id.trip_undelivered_summary_customer);
			tvDelAddressLabel = (TextView)this.findViewById(R.id.trip_undelivered_summary_delAddress_label);
			tvDelAddress = (TextView)this.findViewById(R.id.trip_undelivered_summary_delAddress);
			tvPhoneNos = (TextView)this.findViewById(R.id.trip_undelivered_summary_phone_nos);
			tvRequiredBy = (TextView)this.findViewById(R.id.trip_undelivered_summary_requiredBy);
			tvProducts = (TextView)this.findViewById(R.id.trip_undelivered_summary_products);
			tvTerms = (TextView)this.findViewById(R.id.trip_undelivered_summary_terms);
			tvNotes = (TextView)this.findViewById(R.id.trip_undelivered_summary_notes);
            Button btnBack = (Button) this.findViewById(R.id.trip_undelivered_summary_back);
            Button btnSkip = (Button) this.findViewById(R.id.trip_undelivered_summary_skip);
            Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_summary_next);
			
			// btnSkip.setVisibility(View.INVISIBLE);
			
			btnBack.setOnClickListener(buttonClick);
			btnSkip.setOnClickListener(buttonClick);
			btnNext.setOnClickListener(buttonClick);
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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: resumeView");

			// Resume updating.
			infoview.resume();
			
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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: pauseView");

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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: updateUI");

			// Order no.
			infoview.setDefaultTv1("Order " + Active.order.InvoiceNo);
			
			// Line.
            infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

			String customer = Active.order.CustomerName + "\n" + Active.order.CustomerAddress;
			String delAddress = Active.order.DeliveryName + "\n" + Active.order.DeliveryAddress;
	
			// Check if customer & delivery address are the same.
			if (customer.equals(delAddress))
			{
				// Hide the delivery address.
				tvDelAddressLabel.setVisibility(View.GONE);
				tvDelAddress.setVisibility(View.GONE);
				
				// Set customer address (inc. postcode).
                tvCustomer.setText(String.format("%s (%s)\n%s\n%s", Active.order.CustomerName, Active.order.CustomerCode, Active.order.CustomerAddress, Active.order.CustomerPostcode));
			}
			else
			{
				// Show the delivery address.
				tvDelAddressLabel.setVisibility(View.VISIBLE);
				tvDelAddress.setVisibility(View.VISIBLE);

                // Show the customer address
                tvCustomer.setText(String.format("%s (%s)\n%s", Active.order.CustomerName, Active.order.CustomerCode, Active.order.CustomerAddress));

                // Set the delivery address
                tvDelAddress.setText(String.format("%s\n%s\n%s", Active.order.DeliveryName, Active.order.DeliveryAddress, Active.order.DeliveryPostcode));
			}

			tvPhoneNos.setText(Active.order.PhoneNos);
			tvRequiredBy.setText(Active.order.RequiredBy);
			tvProducts.setText(Active.order.getProductsOrdered("\n"));
			tvTerms.setText(Active.order.getTerms());
			tvNotes.setText(Active.order.Notes);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final OnClickListener buttonClick = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			try
			{
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: buttonClick");

				switch (view.getId())
				{
					case R.id.trip_undelivered_summary_back:

						CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: buttonClick - Back button clicked");

						// Mark the Active.order as OnMobile again.
						trip.orderStopped();

						// Switch to order list view.
						trip.selectView(Trip.ViewUndeliveredList, -1);

						break;

					case R.id.trip_undelivered_summary_skip:

						CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: buttonClick - Skip button clicked");

						// Switch to the Skip screen
						trip.selectView(Trip.ViewUndeliveredSkip, +1);

						break;

					case R.id.trip_undelivered_summary_next:

						CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: buttonClick - Next button clicked");

                        if (Active.order == null)
                        {
                            Active.order = dbTripOrder.load(dbTripOrder.class, trip.OrderId);
                        }

						// Check if COD 'Before Delivery' on this order.
						if (Active.order.CodPoint == 2 && Active.order.getCodBeforeDeliveryValue().compareTo(BigDecimal.ZERO) > 0)
						{
							// Switch to COD view.
							trip.selectView(Trip.ViewUndeliveredCOD, +1);
						}
						else
						{
							// Switch to Products view.
							trip.selectView(Trip.ViewUndeliveredProducts, +1);
						}

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
