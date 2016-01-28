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

			btnChange.setOnClickListener(onClickListener);
			btnNext.setOnClickListener(onClickListener);
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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: resumeView");

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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: pauseView");

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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: updateUI");

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

	private final OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			try
			{
				switch (view.getId())
                {
                    case R.id.trip_undelivered_lc_duringdelivery_line_product_change:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: onClick - Change");

                        // Check there are products.
                        if (!products.isEmpty())
                        {
                            int productIndex = -1;

                            if (lineProduct != null)
                            {
                                // Find currently selected product.
                                for (int i = 0; i < products.size(); i++)
                                {
                                    if (products.get(i).getId().equals(lineProduct.getId()))
                                    {
                                        productIndex = i;

                                        break;
                                    }
                                }
                            }

                            // Move to next product.
                            productIndex++;

                            // Change to next product.
                            lineProduct = productIndex != products.size() ? products.get(productIndex) : products.get(0);

                            // Reflect changes on UI.
                            tvLineProduct.setText(lineProduct.Desc);
                        }

                        break;

                    case R.id.trip_undelivered_lc_duringdelivery_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change_DuringDelivery: MeterMate onClick -Next");

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