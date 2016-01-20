package com.swiftsoft.colossus.mobileoil;

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

import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;

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

    private Hashtable<String, Integer> requiredProducts;
    private Hashtable<String, Integer> stockLevels;
	
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

	private void getRequiredProducts()
	{
        CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getRequiredProducts");

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
		CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getStockLevels");

        // Create the Hashtable if it does not already exist
		if (stockLevels == null)
		{
			CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getStockLevels - Creating stock levels hashtable ...");

			stockLevels = new Hashtable<String, Integer>();
		}

        CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getStockLevels - Clearing stock levels hashtable ...");

        // Empty it
		stockLevels.clear();

		for (dbVehicleStock vehicleStock : dbVehicleStock.FindAllNonCompartmentStock(Active.vehicle))
		{
			String productName = vehicleStock.Product.Desc;
			int stockLevel = vehicleStock.CurrentStock;

			if (!stockLevels.containsKey(productName))
			{
                CrashReporter.leaveBreadcrumb(String.format("Trip_Stock_Load: getStockLevels - Adding entry for %s to hashtable ...", productName));

				stockLevels.put(productName, 0);
			}

            CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getStockLevels - Updating stock level ...");

			stockLevels.put(productName, stockLevels.get(productName) + stockLevel);
		}

		if (Active.vehicle.getHasHosereel())
		{
            CrashReporter.leaveBreadcrumb("Trip_Stock_Load: getStockLevels - Adding hosereel product to stock");

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
            CrashReporter.leaveBreadcrumb("Trip_Stock_Load: resumeView");

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
            CrashReporter.leaveBreadcrumb("Trip_Stock_Load: pauseView");

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
            CrashReporter.leaveBreadcrumb("Trip_Stock_Load : updateUI");

			// Update the UI.
			infoview.setDefaultTv1("Load product");

			// Set the Line product in the title bar
            infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

            if (product == null)
            {
                tvProduct.setText("None");
            }
            else
            {
                int stockLevel = 0;

                if (stockLevels.containsKey(product.Desc))
                {
                    stockLevel = stockLevels.get(product.Desc);
                }

                int requiredAmount = 0;

                if (requiredProducts.containsKey(product.Desc))
                {
                    requiredAmount = requiredProducts.get(product.Desc);
                }

                if (requiredAmount > stockLevel)
                {
                    int toLoad = requiredAmount - stockLevel;

                    // Load product.
                    tvProduct.setText(product.Desc + " " + toLoad + " litres");
                }
                else
                {
                    // Load product.
                    tvProduct.setText(product.Desc + " 0 litres");
                }
            }
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
					int index = -1;

					if (product != null)
					{
						// Find currently selected product.
						for (int i = 0; i < products.size(); i++)
						{
							if (products.get(i).getId().equals(product.getId()))
							{
								index = i;

								break;
							}
						}
					}
	
					// Move to next product.
					index++;
					
					// Change to next product.
                    product = index != products.size() ? products.get(index) : null;

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
            CrashReporter.leaveBreadcrumb("Trip_Stock_Load: validate");

	    	int litres = 0;
	    	
	    	// Check if value is valid.
	    	try
            {
                String text = etLoaded.getText().toString();

                if (text.length() > 0)
                {
                    litres = decf.parse(text).intValue();
                }
            }
	    	catch (ParseException e)
            {
                e.printStackTrace();
            }

            btnOK.setEnabled(litres <= 0 ? false : true);
            btnCancel.setText(litres <= 0 ? "Close" : "Cancel");
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}
