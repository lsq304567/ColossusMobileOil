package com.swiftsoft.colossus.mobileoil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;

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

import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Stock_Load extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private dbProduct product = null;
	private MyInfoView1Line infoview;
	private MyEditText etLoaded;
	private TextView tvProduct;
	private Button btnChange;	
	private Button btnOK;
	private Button btnCancel;
	
	private List<dbProduct> products;

	private DecimalFormat decf;
	private String previousViewName;
	
	public Trip_Stock_Load(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Stock_Load(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Load: init");
			
			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Setup a standard decimal format.
			decf = new DecimalFormat("#,##0");
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_load, this, true);
			
	    	// Find UI controls.
	    	infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_load_infoview);
			etLoaded = (MyEditText)this.findViewById(R.id.trip_stock_load_litres);
			tvProduct = (TextView)this.findViewById(R.id.trip_stock_load_product);
			btnChange = (Button)this.findViewById(R.id.trip_stock_load_change);
			btnOK = (Button)this.findViewById(R.id.trip_stock_load_ok);
			btnCancel = (Button)this.findViewById(R.id.trip_stock_load_cancel);
	
			etLoaded.addTextChangedListener(onLitresChanged);
			btnChange.setOnClickListener(onChange);
			btnOK.setOnClickListener(onOK);
			btnCancel.setOnClickListener(onCancel);
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
			// Clear product.
			product = null;
			
			// Load products.
			products = dbProduct.GetAllMeteredAndNonMetered();
	
			// Resume updating.
			infoview.resume();
			
			// Clear litres & focus.
			etLoaded.setText("");
			etLoaded.requestFocus();
	
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
	public void setPreviousView(String name) 
	{
		// Store previous view.
		previousViewName = name;
	}
	
	@Override
	public void updateUI() 
	{
		try
		{
			// Update the UI.
			infoview.setDefaultTv1("Load product");
			dbProduct lineProduct = Active.vehicle.getHosereelProduct();
			
			// Line.
			if (lineProduct == null)
				infoview.setDefaultTv2("Line: None");
			else
				infoview.setDefaultTv2("Line: " + lineProduct.Desc);
	
			// Load product.
			if (product == null)
				tvProduct.setText("None");
			else
				tvProduct.setText(product.Desc);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    TextWatcher onLitresChanged = new TextWatcher()
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
	    
    OnClickListener onChange = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Load: onChange");
				
				// Check there are products.
				if (!products.isEmpty())
				{
					int idx = -1;
	
					if (product != null)
					{
						// Find currently selected product.
						for (int i = 0; i < products.size(); i++)
						{
							if (products.get(i).getId().equals(product.getId()))
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
						product = products.get(idx);
					else
						product = null;
					
					// Validate.
					validate();
					
					// Reflect changes on UI.
					updateUI();
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onOK = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			String errorMessage = "";
			int litres = 0;

			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Load: onOK");
				
				// Check for product.
				if (product == null)
				{
					errorMessage = "Please select a product";
					return;
				}
			
				// Check loaded quantity.
				try {litres = decf.parse(etLoaded.getText().toString()).intValue();}
				catch (ParseException e) {e.printStackTrace();}
		
				if (litres <= 0)
				{
					errorMessage = "Litres missing";
					return;
				}
				
				// Update stock locally.
				Active.vehicle.recordLoad(product, litres);
				
				// Update stock on server.
				trip.sendVehicleStock();
				
				// Reset UI.
				etLoaded.setText("");

				// Notify user.
				Toast t = Toast.makeText(trip, litres + " loaded", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
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
	
	OnClickListener onCancel = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Load: onCancel");
				
				if (btnCancel.getText().equals("Close"))
				{
					// Close view.
					btnCancel.setEnabled(false);
					
					// Switch back to previous view.
					trip.selectView(previousViewName, -1);
				}
				else
				{
					// Cancel input.
					etLoaded.setText("");
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private void validate()
    {
		try
		{
	    	int litres = 0;
	    	
	    	// Check if value is valid.
	    	try {litres = decf.parse((String) etLoaded.getText().toString()).intValue();}
	    	catch (ParseException e) {e.printStackTrace();}
	    	   	
	    	if (litres <= 0)
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
