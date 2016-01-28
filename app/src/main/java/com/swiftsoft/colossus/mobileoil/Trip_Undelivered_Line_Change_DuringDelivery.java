package com.swiftsoft.colossus.mobileoil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.util.List;

public class Trip_Undelivered_Line_Change_DuringDelivery extends MyFlipperView
{
	private Trip trip;

	private dbProduct lineProduct = null;
	private List<dbProduct> products;

	private MyInfoView1Line infoview;
	private TextView tvLineProduct;

	public Trip_Undelivered_Line_Change_DuringDelivery(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Line_Change_DuringDelivery(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_line_change_duringdelivery, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_lc_duringdelivery_infoview);
			tvLineProduct = (TextView)this.findViewById(R.id.trip_undelivered_lc_duringdelivery_line_product);
			Button btnChange = (Button) this.findViewById(R.id.trip_undelivered_lc_duringdelivery_line_product_change);
			Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_lc_duringdelivery_next);

			btnChange.setOnClickListener(onChange);
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
			
			// Initialise lineProduct.
			lineProduct = Active.vehicle.getHosereelProduct();
			
			// Load products.
			products = dbProduct.GetAllMetered();

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

	@SuppressLint("SetTextI18n")
	@Override
	public void updateUI() 
	{
		try
		{
			// Update the UI.
			infoview.setDefaultTv1("Line changed?");

			dbProduct lineProduct = Active.vehicle.getHosereelProduct();

			infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(lineProduct));
			
			// Line.
			if (lineProduct == null)
			{
				tvLineProduct.setText("(none)");
			}
			else
			{
				tvLineProduct.setText(lineProduct.Desc);
			}	
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private final OnClickListener onChange = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: onChange");
				
				// Check there are products.
				if (!products.isEmpty())
				{
					int idx = -1;
	
					if (lineProduct != null)
					{
						// Find currently selected product.
						for (int i = 0; i < products.size(); i++)
						{
							if (products.get(i).getId().equals(lineProduct.getId()))
							{
								idx = i;
								break;
							}
						}
					}
	
					// Move to next product.
					idx++;
					
					// Change to next product.
					if (idx != products.size())
						lineProduct = products.get(idx);
					else
						lineProduct = products.get(0);
					
					// Reflect changes on UI.
					tvLineProduct.setText(lineProduct.Desc);
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private final OnClickListener onNext = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: MeterMate onNextClicked");

				// Record line change
				if (!Active.vehicle.getHosereelProduct().getId().equals(lineProduct.getId()))
				{
					Active.vehicle.recordLineChange(lineProduct, Active.vehicle.C0_Capacity, "0");
				}
			
				if (Active.order.getUndeliveredCount() > 0)
				{
					// Deliver next product.
					trip.selectView(Trip.ViewUndeliveredProducts, -1);
				}
				else
				{
					// Move to Delivery Note. 
					trip.selectView(Trip.ViewUndeliveredDeliveryNote, +1);
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
