package com.swiftsoft.colossus.mobileoil.view;

import java.text.DecimalFormat;
import java.text.ParseException;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MyStockSummary extends LinearLayout
{
	private LayoutInflater inflater;
	private DecimalFormat decf;
	private MyTankerImageView tanker;
	private TableLayout byProductTable;
	private TextView tvByCompartment;
	private TableLayout tlByCompartmentTable;
	
	public MyStockSummary(Context context)
	{
		super(context);
		init();
	}

	public MyStockSummary(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		try
		{
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.stock_summary, this, true);
	
			if (!isInEditMode())
			{
				// Setup a standard decimal format.
				decf = new DecimalFormat("#,##0");
		
				// Find UI controls.
				tanker = (MyTankerImageView)findViewById(R.id.stock_summary_tanker);
				byProductTable = (TableLayout)findViewById(R.id.stock_summary_by_product_table);
				tvByCompartment = (TextView)findViewById(R.id.stock_summary_by_compartment);
				tlByCompartmentTable = (TableLayout)findViewById(R.id.stock_summary_by_compartment_table);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void UpdateStock()
	{
		try
		{
			//
			// Step 1 - Clear existing data.
			// 
			
			// Clear 'by product' table.
			while (byProductTable.getChildCount() > 1)
			{
				byProductTable.removeViewAt(1);
			}
	
	    	// Clear 'by compartment' table.
			while (tlByCompartmentTable.getChildCount() > 1)
			{
				tlByCompartmentTable.removeViewAt(1);
			}
	
			//
			// Step 2 - Add any compartment stock.
			//
			
			if (Active.vehicle.StockByCompartment)
			{
				tvByCompartment.setVisibility(View.VISIBLE);
				tlByCompartmentTable.setVisibility(View.VISIBLE);
				
				// Add hosereel product.
				if (Active.vehicle.getHasHosereel())
				{
					dbProduct product = Active.vehicle.getHosereelProduct();

					int capacity = Active.vehicle.getHosereelCapacity();
					int onboard = capacity;
		
					// Add product to 'by product' table.
		    		AddByProduct(product, onboard);
		    		
		    		// Add product to 'by compartment' table.
		   			AddByCompartment(product, 0, capacity, onboard);
				}
				
				// Set number of compartments on tanker image.
				tanker.setCompartmentCount(Active.vehicle.getCompartmentCount());
				
		    	// Set product types and levels on tanker image.
		    	for (int tankerIdx = 0, compartmentIdx = Active.vehicle.getCompartmentStartIdx(); compartmentIdx < Active.vehicle.getCompartmentEndIdx(); tankerIdx++, compartmentIdx++)
		    	{
					dbProduct product = Active.vehicle.getCompartmentProduct(compartmentIdx);
					int no = Active.vehicle.getCompartmentNo(compartmentIdx);
					int capacity = Active.vehicle.getCompartmentCapacity(compartmentIdx);
					int onboard = Active.vehicle.getCompartmentOnboard(compartmentIdx);
					int colour = Active.vehicle.getCompartmentColour(compartmentIdx);
					
					// Update compartment on vehicle image.
					tanker.setCompartmentNo(tankerIdx, no);
					tanker.setCompartmentCapacity(tankerIdx, capacity);
		   			tanker.setCompartmentOnboard(tankerIdx, onboard);
		   			tanker.setCompartmentColour(tankerIdx, colour);
					
					// Add product to 'by product' table.
		    		AddByProduct(product, onboard);
		    		
		    		// Add product to 'by compartment' table.
		    		if (Active.vehicle.StockByCompartment)
					{
						AddByCompartment(product, no, capacity, onboard);
					}
		    	}
			}
	
	    	//
	    	// Step 3 - Add any non-compartment stock.
	    	//
	    	
			for (dbVehicleStock vs : dbVehicleStock.FindAllNonCompartmentStock(Active.vehicle))
			{
				AddByProduct(vs.Product, vs.CurrentStock);
			}
			
			if (!Active.vehicle.StockByCompartment)
			{
				tvByCompartment.setVisibility(View.GONE);
				tlByCompartmentTable.setVisibility(View.GONE);
	
				if (Active.vehicle.getHasHosereel())
				{
					// Add product to 'by product' table.
		    		AddByProduct(Active.vehicle.getHosereelProduct(), Active.vehicle.getHosereelCapacity());
				}
			}
			
	    	//
	    	// Step 4 - find orders on this trip, and updated required.
	    	//
	    	
			// Find all undelivered orders on this trip.
	    	if (Active.trip != null)
	    	{
				for (dbTripOrder order : Active.trip.GetUndelivered())
				{
					// Find all order lines.
					for (dbTripOrderLine orderLine : order.GetTripOrderLines())
					{
						TableRow myTr = null;
	
						// Skip null products.
						// Skip non-deliverable products. e.g. credit card fees
						if (orderLine.Product == null || orderLine.Product.MobileOil == 3)
						{
							continue;
						}
						
						// Check if product already exists.
						for (int row = 1; row < byProductTable.getChildCount(); row++)
						{
							TableRow tr = (TableRow) byProductTable.getChildAt(row);

							if (tr.getTag().equals(orderLine.Product))
							{
								myTr = tr;

								break;
							}
						}
						
						// Create new row, if not exists.
						if (myTr == null)
						{
							myTr = CreateByProductRow(orderLine.Product, 0);
						}
	
						// Find previous required.
						TextView tvRequired = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_required);
	
						int prevRequired = 0;

						try
						{
							prevRequired = decf.parse((String) tvRequired.getText()).intValue();
						}
						catch (ParseException e)
						{
							e.printStackTrace();
						}
	
						// Update required to include this order line.
						int required = prevRequired + orderLine.OrderedQty; 
						tvRequired.setText(decf.format(required));
	
						// Find total onboard.
						TextView tvOnboard = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_onboard);
						
						int onboard = 0;

						try
						{
							onboard = decf.parse((String) tvOnboard.getText()).intValue();
						}
						catch (ParseException e)
						{
							e.printStackTrace();
						}
						
						// Update spare.
						TextView tvSpare = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_spare);
						tvSpare.setText(decf.format(onboard - required));
					}
				}
	    	}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    private void AddByProduct(dbProduct product, int onboard)
    {
    	try
    	{
    		// Check product is valid.
	    	if (product == null)
			{
				return;
			}
	    	
	    	// Check existing rows for this product.
			for (int row = 1; row < byProductTable.getChildCount(); row++)
			{
				TableRow tr = (TableRow) byProductTable.getChildAt(row);

				if (tr.getTag().equals(product))
				{
					TextView tvOnboard = (TextView)tr.findViewById(R.id.stock_summary_by_product_tablerow_onboard);
					TextView tvSpare = (TextView)tr.findViewById(R.id.stock_summary_by_product_tablerow_spare);
	
					// Update quantity onboard.
					int prevOnboard = 0;

					try
					{
						prevOnboard = decf.parse((String) tvOnboard.getText()).intValue();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
					
					tvOnboard.setText(decf.format(onboard + prevOnboard));
					
					// Update quantity spare.
					int prevSpare = 0;

					try
					{
						prevSpare = decf.parse((String) tvSpare.getText()).intValue();
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
					
					tvSpare.setText(decf.format(onboard + prevSpare));
					
					return;
				}
			}
	
			CreateByProductRow(product, onboard);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    private TableRow CreateByProductRow(dbProduct product, int onboard)
    {
    	try
    	{
			// Create new row.
			TableRow tr = (TableRow)inflater.inflate(R.layout.stock_summary_by_product_tablerow, null);
			tr.setTag(product);
			
			TextView tvProduct = (TextView)tr.findViewById(R.id.stock_summary_by_product_tablerow_product);
			tvProduct.setText(product.Desc);
			
			TextView tvOnboard = (TextView)tr.findViewById(R.id.stock_summary_by_product_tablerow_onboard);
			tvOnboard.setText(decf.format(onboard));
	
			TextView tvSpare = (TextView)tr.findViewById(R.id.stock_summary_by_product_tablerow_spare);
			tvSpare.setText(decf.format(onboard));
	
			byProductTable.addView(tr);
			return tr;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return null;
		}
    }
    
    private void AddByCompartment(dbProduct product, int compartment, int capacity, int onboard)
    {
    	try
    	{
			TableRow tr = (TableRow)inflater.inflate(R.layout.stock_summary_by_compartment_tablerow, null);
			
			TextView tvNo = (TextView)tr.findViewById(R.id.stock_summary_by_compartment_tablerow_no);

			tvNo.setText(compartment == 0 ? "Line" : "#" + compartment);
			
			TextView tvProduct = (TextView)tr.findViewById(R.id.stock_summary_by_compartment_tablerow_product);

			if (product != null)
			{
				tvProduct.setText(product.Desc);
			}
	
			TextView tvCapacity = (TextView)tr.findViewById(R.id.stock_summary_by_compartment_tablerow_capacity);

			if (capacity != 0)
			{
				tvCapacity.setText(decf.format(capacity));
			}
	
			TextView tvOnboard = (TextView)tr.findViewById(R.id.stock_summary_by_compartment_tablerow_onboard);

			if (onboard != 0)
			{
				tvOnboard.setText(decf.format(onboard));
			}
			
			tlByCompartmentTable.addView(tr);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}