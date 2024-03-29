package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;
import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;
import com.swiftsoft.colossus.mobileoil.view.MyNumericKeypad;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;

public class Trip_Stock_Return extends MyFlipperView
{
	private Trip trip;

	private dbProduct product;
	private int litres;

	private MyInfoView1Line infoview;
	private TextView tvProduct;
	private RadioButton rbMetered;
	private TextView tvPreset;
	private EditText etPreset;
	private RadioButton rbUnmetered;
	private TextView tvLitres;
	private EditText etLitres;
	private Button btnOK;
	private Button btnCancel;

	// Radio Buttons for return destination
	private RadioButton rbReturnTank;
	private RadioButton rbReturnVehicle;
	private RadioButton rbReturnOther;

	// Edit Text for return detail
	private EditText etReturnDetail;
	
	private List<dbProduct> products;

	private DecimalFormat decimalFormat;
	private String previousViewName;

	private Hashtable<String, Integer> requiredProducts;
	private Hashtable<String, Integer> stockLevels;

    MyNumericKeypad keypad;

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
			decimalFormat = new DecimalFormat("#,##0");
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_stock_return, this, true);
			
	    	// Find UI controls.
	    	infoview = (MyInfoView1Line)this.findViewById(R.id.trip_stock_return_infoview);
			tvProduct = (TextView)this.findViewById(R.id.trip_stock_return_product);
			Button btnChange = (Button) this.findViewById(R.id.trip_stock_return_change);
			rbMetered = (RadioButton)this.findViewById(R.id.trip_stock_return_metered);
			tvPreset = (TextView)this.findViewById(R.id.trip_stock_return_preset_label);
			etPreset = (EditText)this.findViewById(R.id.trip_stock_return_preset);
			rbUnmetered = (RadioButton)this.findViewById(R.id.trip_stock_return_unmetered);
			tvLitres = (TextView)this.findViewById(R.id.trip_stock_return_litres_label);
			etLitres = (EditText)this.findViewById(R.id.trip_stock_return_litres);
			btnOK = (Button)this.findViewById(R.id.trip_stock_return_ok);
			btnCancel = (Button)this.findViewById(R.id.trip_stock_return_cancel);

            rbReturnTank = (RadioButton)this.findViewById(R.id.trip_stock_return_location_tank);
            rbReturnVehicle = (RadioButton)this.findViewById(R.id.trip_stock_return_location_vehicle);
            rbReturnOther = (RadioButton)this.findViewById(R.id.trip_stock_return_location_other);

            etReturnDetail = (EditText)this.findViewById(R.id.trip_stock_return_location_details);
	
			btnChange.setOnClickListener(onClickListener);
			rbMetered.setOnCheckedChangeListener(onRadioButtonCheckChanged);
			rbMetered.setOnClickListener(onClickListener);
			etPreset.addTextChangedListener(onTextChanged);
			rbUnmetered.setOnCheckedChangeListener(onRadioButtonCheckChanged);
			rbUnmetered.setOnClickListener(onClickListener);
			etLitres.addTextChangedListener(onTextChanged);
			btnOK.setOnClickListener(onClickListener);
			btnCancel.setOnClickListener(onClickListener);

            rbReturnTank.setOnCheckedChangeListener(onRadioButtonCheckChanged);
            rbReturnVehicle.setOnCheckedChangeListener(onRadioButtonCheckChanged);
            rbReturnOther.setOnCheckedChangeListener(onRadioButtonCheckChanged);

            etReturnDetail.addTextChangedListener(onDetailsChanged);

//            etReturnDetail.setOnClickListener(onClickListener);
//            etPreset.setOnClickListener(onClickListener);
//            etLitres.setOnClickListener(onClickListener);
//
//            keypad = (MyNumericKeypad)this.findViewById(R.id.trip_stock_return_numeric_keypad);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void getRequiredProducts()
	{
		CrashReporter.leaveBreadcrumb("Trip_Stock_Return: getRequiredProducts");

		// Create the Hashtable if it does not already exist
		if (requiredProducts == null)
		{
			requiredProducts = new Hashtable<String, Integer>();
		}

		// Empty it
		requiredProducts.clear();

		if (Active.trip != null)
		{
			// Get all undelivered orders in the trip
			for (dbTripOrder order : Active.trip.GetUndelivered())
			{
				// Get all the order lines in each order
				for (dbTripOrderLine orderLine : order.GetTripOrderLines())
				{
					if (orderLine.Product == null || orderLine.Product.MobileOil == 3)
					{
						continue;
					}

					String productName = orderLine.Product.Desc;
					int orderQuantity = orderLine.OrderedQty;

					if (!requiredProducts.containsKey(productName))
					{
						requiredProducts.put(productName, 0);
					}

					requiredProducts.put(productName, requiredProducts.get(productName) + orderQuantity);
				}
			}
		}
	}

	private void getStockLevels()
	{
		CrashReporter.leaveBreadcrumb("Trip_Stock_Return: getStockLevels");

		// Create the Hashtable if it does not already exist
		if (stockLevels == null)
		{
			stockLevels = new Hashtable<String, Integer>();
		}

		// Empty it
		stockLevels.clear();

		for (dbVehicleStock vs : dbVehicleStock.FindAllNonCompartmentStock(Active.vehicle))
		{
			String productName = vs.Product.Desc;
			int stockLevel = vs.CurrentStock;

			if (!stockLevels.containsKey(productName))
			{
				stockLevels.put(productName, 0);
			}

			stockLevels.put(productName, stockLevels.get(productName) + stockLevel);
		}

		if (Active.vehicle.getHasHosereel())
		{
			String productName = Active.vehicle.getHosereelProduct().Desc;
			int hosereelCapacity = Active.vehicle.getHosereelCapacity();

			if (!stockLevels.containsKey(productName))
			{
				stockLevels.put(productName, 0);
			}

			stockLevels.put(productName, stockLevels.get(productName) + hosereelCapacity);
		}
	}

	@Override
	public boolean resumeView() 
	{
		try
		{
			CrashReporter.leaveBreadcrumb("Trip_Stock_Return: resumeView");

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

			// Make sure that the required products Hashtable is populated
			getRequiredProducts();

			// Make sure that the stock levels Hashtable is populated
			getStockLevels();

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
			CrashReporter.leaveBreadcrumb("Trip_Stock_Return: pauseView");

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
		// Leave breadcrumb.
		CrashReporter.leaveBreadcrumb("Trip_Stock_Return: setPreviousView");

		// Store previous view.
		previousViewName = name;
	}

    private int getStockLevel(String productName)
    {
		// Leave breadcrumb.
		CrashReporter.leaveBreadcrumb("Trip_Stock_Return: getStockLevel");

		if (stockLevels.containsKey(productName))
        {
            return stockLevels.get(productName);
        }

        return 0;
    }

    private int getRequiredAmount(String productName)
    {
		// Leave breadcrumb.
		CrashReporter.leaveBreadcrumb("Trip_Stock_Return: getRequiredAmount");

		if (requiredProducts.containsKey(productName))
        {
            return requiredProducts.get(productName);
        }

        return 0;
    }
	
	@Override
	public void updateUI() 
	{
		try
		{
            CrashReporter.leaveBreadcrumb("Trip_Stock_Return: updateUI");

			// Update the UI.
			infoview.setDefaultTv1("Return product");

			// Set the line product in the title bar.
            infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

            // Set the Product name with maximum amount of product that can be returned
            if (product == null)
            {
                tvProduct.setText("None");
            }
            else
            {
                // Get the stock level of the product
                int stockLevel = getStockLevel(product.Desc);

                // Get the required amount of the product
                int requiredAmount = getRequiredAmount(product.Desc);

                if (stockLevel > requiredAmount)
                {
                    int toReturn = stockLevel - requiredAmount;

                    // Return product.
                    tvProduct.setText(String.format("%s %d litres", product.Desc, toReturn));
                }
                else
                {
                    // Return product.
                    tvProduct.setText(String.format("%s 0 litres", product.Desc));
                }
            }
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
            CrashReporter.leaveBreadcrumb("Trip_Stock_Return: validate");

            boolean isValid = true;

	    	litres = 0;
	    	
			if (rbMetered.isChecked())
			{
				tvPreset.setVisibility(View.VISIBLE);
				etPreset.setVisibility(View.VISIBLE);
				//etPreset.requestFocus();
				
				// Check if preset value is valid.
				try
                {
                    String text = etPreset.getText().toString();

                    if (text.length() > 0)
                    {
                        litres = decimalFormat.parse(text).intValue();
                    }
                }
				catch (ParseException e)
                {
                    e.printStackTrace();
                }
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
				//etLitres.requestFocus();
				
				// Check if unmetered value is valid.
				try
                {
                    String text = etLitres.getText().toString();

                    if (text.length() > 0)
                    {
                        litres = decimalFormat.parse(text).intValue();
                    }
                }
				catch (ParseException e)
                {
                    e.printStackTrace();
                }
			}
			else
			{
				tvLitres.setVisibility(View.INVISIBLE);
				etLitres.setVisibility(View.INVISIBLE);
			}

            // If litres is more than zero then valid
            isValid = litres > 0;

            if (isValid)
            {
                if (rbReturnTank.isChecked() || rbReturnVehicle.isChecked() || rbReturnOther.isChecked())
                {
                    if (etReturnDetail.getText().length() <= 0)
                    {
                        isValid = false;
                    }
                }
                else
                {
                    isValid = false;
                }
            }

            btnOK.setEnabled(isValid);
            btnCancel.setText(isValid ? "Cancel" : "Close");

//            btnOK.setEnabled(litres > 0);
//            btnCancel.setText(litres <= 0 ? "Close" : "Cancel");
    	}
    	catch (Exception e)
    	{
    		CrashReporter.logHandledException(e);
    	}
    }


	private final OnCheckedChangeListener onRadioButtonCheckChanged = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			validate();
		}
	};

	private final TextWatcher onTextChanged = new TextWatcher()
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

	private final TextWatcher onDetailsChanged = new TextWatcher()
	{
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{

		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
		{

		}

		@Override
		public void afterTextChanged(Editable editable)
		{
			validate();
		}
	};

	private final OnClickListener onClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			try
            {
                switch (view.getId())
                {
//                    case R.id.trip_stock_return_litres:
//                    case R.id.trip_stock_return_preset:
//
//                        keypad.setVisibility(VISIBLE);
//
//                        break;
//
//                    case R.id.trip_stock_return_location_details:
//
//                        keypad.setVisibility(INVISIBLE);
//
//                        break;
//
                    case R.id.trip_stock_return_metered:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onClick - RB Metered");

                        rbUnmetered.setChecked(false);

                        break;

                    case R.id.trip_stock_return_unmetered:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onClick - RB Unmetered");

                        rbMetered.setChecked(false);

                        break;

                    case R.id.trip_stock_return_change:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onClick - Change");

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

                        break;

                    case R.id.trip_stock_return_ok:

                        String errorMessage = "";

                        try
                        {
                            // Leave breadcrumb.
                            CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onClick - OK");

                            //
                            // Validation.
                            //

                            // Check product has been selected.
                            if (product == null)
                            {
                                errorMessage = "No product selected";
                                return;
                            }

                            if (rbMetered.isChecked())
                            {
                                if (litres <= 0)
                                {
                                    errorMessage = "Preset value is missing";

                                    return;
                                }

                                if (!rbReturnTank.isChecked() && !rbReturnVehicle.isChecked() && !rbReturnOther.isChecked())
                                {
                                    errorMessage = "Return destination not chosen";

                                    return;
                                }

                                if (etReturnDetail.getText().length() <= 0)
                                {
                                    errorMessage = "No return details completed";

                                    return;
                                }

                                // Switch to MeterMate view.
                                trip.setMeterMateCallbacks(callbacks);
                                trip.selectView(Trip.ViewUndeliveredMeterMate, +1);
                            }
                            else
                            {
                                if (!rbReturnTank.isChecked() && !rbReturnVehicle.isChecked() && !rbReturnOther.isChecked())
                                {
                                    errorMessage = "Return destination not chosen";

                                    return;
                                }

                                if (etReturnDetail.getText().length() <= 0)
                                {
                                    errorMessage = "No return details completed";

                                    return;
                                }

                                // Complete product return.
                                stockReturnComplete(litres, false);
                            }
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

                        break;

                    case R.id.trip_stock_return_cancel:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Stock_Return: onClick - Cancel");

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
                            etReturnDetail.setText("");

                            rbReturnTank.setChecked(false);
                            rbReturnVehicle.setChecked(false);
                            rbReturnOther.setChecked(false);
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

	private final Trip_MeterMate_Callbacks callbacks = new Trip_MeterMate_Callbacks()
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

            // Construct the note to print
            StringBuilder builder = new StringBuilder();

            if (rbReturnTank.isChecked())
            {
                builder.append("Returned to Tank ");
            }
            else if (rbReturnVehicle.isChecked())
            {
                builder.append("Returned to Vehicle ");
            }
            else if (rbReturnOther.isChecked())
            {
                builder.append("Returned to Other location ");
            }

            builder.append(etReturnDetail.getText().toString());
			
			// Update stock locally.
			Active.vehicle.recordReturn(product, litres, viaMeterMate, builder.toString());
			
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
}
