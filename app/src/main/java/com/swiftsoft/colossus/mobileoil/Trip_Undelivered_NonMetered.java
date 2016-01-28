package com.swiftsoft.colossus.mobileoil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Trip_Undelivered_NonMetered extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;
	private TextView tvProduct;
	private TextView tvOrdered;
	private MyEditText etDelivered;
	private Button btnOK;
	private Button btnCancel;
	
	private DecimalFormat decimalFormat;

	public Trip_Undelivered_NonMetered(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_NonMetered(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_NonMetered: init");
			
			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Setup a standard decimal format.
			decimalFormat = new DecimalFormat("#,##0");

			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_nonmetered, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_nonmetered_infoview);
			tvProduct = (TextView)this.findViewById(R.id.trip_undelivered_nonmetered_product);
			tvOrdered = (TextView)this.findViewById(R.id.trip_undelivered_nonmetered_ordered_qty);
			etDelivered = (MyEditText)this.findViewById(R.id.trip_undelivered_nonmetered_delivered_qty);
			etDelivered.addTextChangedListener(onDeliveredChanged);
			
			btnOK = (Button)this.findViewById(R.id.trip_undelivered_nonmetered_ok);
			btnOK.setOnClickListener(onOK);
			btnCancel = (Button)this.findViewById(R.id.trip_undelivered_nonmetered_cancel);
			btnCancel.setOnClickListener(onCancel);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@SuppressLint("SetTextI18n")
	@Override
	public boolean resumeView() 
	{
		try
		{
			// Resume updating.
			infoview.resume();
			
			// Clear litres & focus.
			etDelivered.setText("");
			etDelivered.requestFocus();
	
			// Reset buttons.
			btnOK.setEnabled(false);
			btnCancel.setText("Close");
			btnCancel.setEnabled(true);
			
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
			infoview.setDefaultTv1("Non-Metered Product");
			infoview.setDefaultTv2("");
			
			tvProduct.setText(Active.orderLine.Product.Desc);
			tvOrdered.setText(String.format("%d", Active.orderLine.OrderedQty));
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    private final TextWatcher onDeliveredChanged = new TextWatcher()
	{
		@Override
		public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
		{
		}
		
		@Override
		public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
		{
		}
		
		@Override
		public void afterTextChanged(Editable paramEditable)
		{
			validate();
		}
	};
	    
	private final OnClickListener onOK = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			String errorMessage = "";
			int quantity = 0;

			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_NonMetered: onOK");

				// Check quantity has been entered.
				if (etDelivered.getText().length() == 0)
				{
					errorMessage = "Quantity missing";
					return;
				}

				// Get quantity.
				try
				{
					quantity = decimalFormat.parse(etDelivered.getText().toString()).intValue();
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
		
				// Check quantity is valid.
				if (quantity < 0)
				{
					errorMessage = "Quantity invalid";
					return;
				}
				
				// Update orderline.
				Active.orderLine.delivered(quantity);
				
				// Update stock locally.
				Active.vehicle.recordDelivery();
				
				// Update stock on server.
				trip.sendVehicleStock();
				
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
			finally
			{
				// Show error message?
				if (errorMessage.length() > 0)
				{
					Toast t = Toast.makeText(trip, errorMessage, Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				}
			}			
		}
	};
	
	private final OnClickListener onCancel = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_NonMetered: onCancel");
				
				if (btnCancel.getText().equals("Close"))
				{
					// Close view.
					btnCancel.setEnabled(false);
					
					// Switch back to previous view.
					trip.selectView(Trip.ViewUndeliveredProducts, -1);
				}
				else
				{
					// Cancel input.
					etDelivered.setText("");
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	@SuppressLint("SetTextI18n")
	private void validate()
    {
		try
		{
	    	int quantity = -1;
	    	
	    	// Check if value is valid.
	    	try {quantity = decimalFormat.parse(etDelivered.getText().toString()).intValue();}
	    	catch (ParseException e) {e.printStackTrace();}
	    	   	
	    	if (quantity < 0)
	    	{
	    		btnOK.setEnabled(false);
	    		btnCancel.setText("Close");
	    	}
	    	else
	    	{
	    		btnOK.setEnabled(true);
	    		btnCancel.setText("Cancel");
	    	}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}
