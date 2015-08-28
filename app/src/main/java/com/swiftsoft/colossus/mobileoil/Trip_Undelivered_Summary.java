package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Undelivered_Summary extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private TextView tvCustomer;
	private TextView tvDelAddressLabel;
	private TextView tvDelAddress;
	private TextView tvPhoneNos;
	private TextView tvRequiredBy;
	private TextView tvProducts;
	private TextView tvTerms;
	private TextView tvNotes;
	private Button btnBack;
	private Button btnSkip;
	private Button btnNext;
	
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
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			btnBack = (Button)this.findViewById(R.id.trip_undelivered_summary_back);
			btnSkip = (Button)this.findViewById(R.id.trip_undelivered_summary_skip);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_summary_next);
			
			btnSkip.setVisibility(View.INVISIBLE);
			
			btnBack.setOnClickListener(onBack);
			btnSkip.setOnClickListener(onSkip);
			btnNext.setOnClickListener(onNext);
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
			// Update the UI.
			dbProduct lineProduct = Active.vehicle.getHosereelProduct();
			
			// Order no.
			infoview.setDefaultTv1("Order " + Active.order.InvoiceNo);
			
			// Line.
			if (lineProduct == null)
				infoview.setDefaultTv2("Line: None");
			else
				infoview.setDefaultTv2("Line: " + lineProduct.Desc);
	
			String customer = Active.order.CustomerName + "\n" + Active.order.CustomerAddress;
			String delAddress = Active.order.DeliveryName + "\n" + Active.order.DeliveryAddress;
	
			// Check if customer & delivery address are the same.
			if (customer.equals(delAddress))
			{
				// Hide the delivery address.
				tvDelAddressLabel.setVisibility(View.GONE);
				tvDelAddress.setVisibility(View.GONE);
				
				// Set customer (inc. postcode).
				tvCustomer.setText(Active.order.CustomerName + " (" + Active.order.CustomerCode + ")\n" + 
								   Active.order.CustomerAddress + "\n" + 
								   Active.order.CustomerPostcode);
			}
			else
			{
				// Show the delivery address.
				tvDelAddressLabel.setVisibility(View.VISIBLE);
				tvDelAddress.setVisibility(View.VISIBLE);
				
				tvCustomer.setText(Active.order.CustomerName + " (" + Active.order.CustomerCode + ")\n" + 
								   Active.order.CustomerAddress);
				tvDelAddress.setText(Active.order.DeliveryName + "\n" + 
								     Active.order.DeliveryAddress + "\n" + 
								     Active.order.DeliveryPostcode);				
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

	OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: onBack");

				// Mark the Active.order as OnMobile again.
				trip.orderStopped();
	
				// Switch to order list view.
				trip.selectView(Trip.ViewUndeliveredList, -1);			
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onSkip = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: onSkip");
		}
	};

	OnClickListener onNext = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Summary: onNext");

				// Check if COD 'Before Delivery' on this order.
				if (Active.order.CodPoint == 2 && Active.order.getCodBeforeDeliveryValue() > 0)
				{
					// Switch to COD view.
					trip.selectView(Trip.ViewUndeliveredCOD, +1);
				}
				else
				{
					// Switch to Products view.
					trip.selectView(Trip.ViewUndeliveredProducts, +1);
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
