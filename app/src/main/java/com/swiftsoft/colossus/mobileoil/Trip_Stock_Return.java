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
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Stock_Return extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private dbProduct product;
	private int litres;

	private MyInfoView1Line infoview;
	private TextView tvProduct;
	private Button btnChange;	
	private RadioButton rbMetered;
	private TextView tvPreset;
	private MyEditText etPreset;
	private RadioButton rbUnmetered;
	private TextView tvLitres;
	private MyEditText etLitres;
	private Button btnOK;
	private Button btnCancel;
	
	private List<dbProduct> products;

	private DecimalFormat decf;
	private String previousViewName;
	
	public Trip_Stock_Return(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Stock_Return(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Return: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Setup a standard decimal format.
			decf = new DecimalFormat("#,##0");
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_return, this, true);
			
	    	// Find UI controls.
	    	infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_return_infoview);
			tvProduct = (TextView)this.findViewById(R.id.trip_stock_return_product);
			btnChange = (Button)this.findViewById(R.id.trip_stock_return_change);
			rbMetered = (RadioButton)this.findViewById(R.id.trip_stock_return_metered);
			tvPreset = (TextView)this.findViewById(R.id.trip_stock_return_preset_label);
			etPreset = (MyEditText)this.findViewById(R.id.trip_stock_return_preset);
			rbUnmetered = (RadioButton)this.findViewById(R.id.trip_stock_return_unmetered);
			tvLitres = (TextView)this.findViewById(R.id.trip_stock_return_litres_label);
			etLitres = (MyEditText)this.findViewById(R.id.trip_stock_return_litres);
			btnOK = (Button)this.findViewById(R.id.trip_stock_return_ok);
			btnCancel = (Button)this.findViewById(R.id.trip_stock_return_cancel);
	
			btnChange.setOnClickListener(onChange);
			rbMetered.setOnCheckedChangeListener(onRadioButtonCheckChanged);
			rbMetered.setOnClickListener(onRadioButtonClicked);
			etPreset.addTextChangedListener(onLitresChanged);
			rbUnmetered.setOnCheckedChangeListener(onRadioButtonCheckChanged);
			rbUnmetered.setOnClickListener(onRadioButtonClicked);
			etLitres.addTextChangedListener(onLitresChanged);
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

			// Default to metered.
	    	rbMetered.setChecked(true);
	    	rbUnmetered.setChecked(false);
			
			// Clear litres & focus.
	   		etPreset.setText("");
			etPreset.requestFocus();
	    	etLitres.setText("");
	
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
			infoview.setDefaultTv1("Return product");
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

    private void validate()
    {
    	try
    	{
	    	litres = 0;
	    	
			if (rbMetered.isChecked())
			{
				tvPreset.setVisibility(View.VISIBLE);
				etPreset.setVisibility(View.VISIBLE);
				etPreset.requestFocus();
				
				// Check if preset value is valid.
				try {litres = decf.parse(etPreset.getText().toString()).intValue();}
				catch (ParseException e) {e.printStackTrace();}
			}
			else
			{
				tvPreset.setVisibility(View.INVISIBLE);
				etPreset.setVisibility(View.INVISIBLE);
			}
	
			if (rbUnmetered.isChecked())
			{
				tvLitres.setVisibility(View.VISIBLE);
				etLitres.setVisibility(View.VISIBLE);
				etLitres.requestFocus();
				
				// Check if unmetered value is valid.
				try {litres = decf.parse(etLitres.getText().toString()).intValue();}
				catch (ParseException e) {e.printStackTrace();}
			}
			else
			{
				tvLitres.setVisibility(View.INVISIBLE);
				etLitres.setVisibility(View.INVISIBLE);
			}
	
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

	OnClickListener onRadioButtonClicked = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onRadioButtonClicked");
				
				// Update other radiobutton.
				if (v == rbMetered)
					rbUnmetered.setChecked(false);
				else
					rbMetered.setChecked(false);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

   	OnCheckedChangeListener onRadioButtonCheckChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			validate();
		}
	};

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
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onChange");
				
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
					{
						product = products.get(idx);
						
						if (product.MobileOil == 1) // Metered
						{
							rbMetered.setChecked(true);
							rbMetered.setEnabled(true);
							
							rbUnmetered.setChecked(false);
						}
						
						if (product.MobileOil == 2) // Non-Metered
						{
							rbMetered.setChecked(false);
							rbMetered.setEnabled(false);
							
							rbUnmetered.setChecked(true);
						}
					}
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
			
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onOK");

				// Determine is metered.
				boolean isMetered = rbMetered.isChecked();

				//
				// Validation.
				//

				// Check product has been selected.
				if (product == null)
				{
					errorMessage = "No product selected";
					return;
				}
				
				if (isMetered)
				{
					if (litres <= 0)
					{
						errorMessage = "Preset value is missing";
						return;
					}

					// Switch to MeterMate view.
					trip.setMeterMateCallbacks(callbacks);
					trip.selectView(Trip.ViewUndeliveredMeterMate, +1);
				}
				else
				{
					// Complete product return.
					stockReturnComplete(litres, false);
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
	
	Trip_MeterMate_Callbacks callbacks = new Trip_MeterMate_Callbacks()
	{
		@Override
		public int getLitres()
		{
			return litres;
		}

		@Override
		public dbProduct getProduct()
		{
			return product;
		}

		@Override
		public void onTicketComplete()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: MeterMate onTicketComplete");
				
				int litres;

				litres = MeterMate.getTicketAt15Degrees() ? (int) MeterMate.getTicketNetVolume() : (int) MeterMate.getTicketGrossVolume();
	
				// Update database.
				stockReturnComplete(litres, true);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
		
		@Override
		public void onNextClicked()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: MeterMate onNextClicked");
				
				// Show view.
				trip.selectView(previousViewName, -1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	private void stockReturnComplete(int litres, boolean viaMeterMate)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Stock_Return: stockReturnComplete");
			
			// Update stock locally.
			Active.vehicle.recordReturn(product, litres, viaMeterMate);
			
			// Update stock on server.
			trip.sendVehicleStock();
			
			// Reset UI.
			etPreset.setText("");
			etLitres.setText("");
			
			// Notify user.
			Toast t = Toast.makeText(trip, litres + " returned", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	OnClickListener onCancel = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onCancel");

				// Close or cancel?
				if (btnCancel.getText().equals("Close"))
				{
					// Close view.
					btnCancel.setEnabled(false);
					
					trip.selectView(previousViewName, -1);
				}
				else
				{
					// Cancel input.
					etPreset.setText("");
					etLitres.setText("");
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
