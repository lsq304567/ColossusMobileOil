package com.swiftsoft.colossus.mobileoil.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.Active;
import com.swiftsoft.colossus.mobileoil.CrashReporter;
import com.swiftsoft.colossus.mobileoil.R;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;

import java.text.DecimalFormat;
import java.text.ParseException;

public class MyStockSummary extends LinearLayout
{
	private LayoutInflater inflater;
	private DecimalFormat formatDecimal;
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
				formatDecimal = new DecimalFormat("#,##0");
		
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

    /**
     * Removes all items from a table layout on screen
     * @param tableLayout Table that is to be cleared
     */
	private static void clearTable(TableLayout tableLayout)
	{
		while (tableLayout.getChildCount() > 1)
		{
			tableLayout.removeViewAt(1);
		}
	}
	
	public void updateStock()
	{
		try
		{
            CrashReporter.leaveBreadcrumb("MyStockSummary : updateStock");

			//
			// Step 1 - Clear existing data.
			// 
			
			// Clear 'by product' table.
			clearTable(byProductTable);

	    	// Clear 'by compartment' table.
			clearTable(tlByCompartmentTable);

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
		    		addByProduct(product, onboard);
		    		
		    		// Add product to 'by compartment' table.
		   			addByCompartment(product, 0, capacity, onboard);
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
		    		addByProduct(product, onboard);
		    		
		    		// Add product to 'by compartment' table.
		    		if (Active.vehicle.StockByCompartment)
					{
						addByCompartment(product, no, capacity, onboard);
					}
		    	}
			}
	
	    	//
	    	// Step 3 - Add any non-compartment stock.
	    	//
	    	
			for (dbVehicleStock vs : dbVehicleStock.FindAllNonCompartmentStock(Active.vehicle))
			{
				addByProduct(vs.Product, vs.CurrentStock);
			}
			
			if (!Active.vehicle.StockByCompartment)
			{
				tvByCompartment.setVisibility(View.GONE);
				tlByCompartmentTable.setVisibility(View.GONE);
	
				if (Active.vehicle.getHasHosereel())
				{
					// Add product to 'by product' table.
		    		addByProduct(Active.vehicle.getHosereelProduct(), Active.vehicle.getHosereelCapacity());
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
							myTr = createByProductRow(orderLine.Product, 0);
						}
	
						// Find previous required.
						TextView tvRequired = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_required);
	
						int prevRequired = 0;

						try
						{
							prevRequired = formatDecimal.parse((String) tvRequired.getText()).intValue();
						}
						catch (ParseException e)
						{
							e.printStackTrace();
						}
	
						// Update required to include this order line.
						int required = prevRequired + orderLine.OrderedQty; 
						tvRequired.setText(formatDecimal.format(required));
	
						// Find total onboard.
						TextView tvOnboard = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_onboard);
						
						int onboard = 0;

						try
						{
							onboard = formatDecimal.parse((String) tvOnboard.getText()).intValue();
						}
						catch (ParseException e)
						{
							e.printStackTrace();
						}
						
						// Update the 'To Load' column.
						TextView toLoad = (TextView)myTr.findViewById(R.id.stock_summary_by_product_tablerow_to_load);

						if (required > onboard)
						{
							toLoad.setText(formatDecimal.format(required - onboard));
						}
						else
						{
							toLoad.setText(formatDecimal.format(0));
						}
					}
				}
	    	}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    private static TableRow findProductTableRow(TableLayout tableLayout, dbProduct product)
    {
        // Get the number of rows in the table
        int rowCount = tableLayout.getChildCount();

        // Loop through the rows in the table searching for the one
        // with the matching product
        for (int rowIndex = 1; rowIndex < rowCount; rowIndex++)
        {
            // Get the row at the index
            TableRow row = (TableRow)tableLayout.getChildAt(rowIndex);

            // Test if the tag matches - if it does return the row
            if (row.getTag().equals(product))
            {
                return row;
            }
        }

        // Row was not found therefore return null
        return null;
    }

    private void addByProduct(dbProduct product, int onboard)
    {
    	try
    	{
            CrashReporter.leaveBreadcrumb("MyStockSummary : addByProduct");

    		// Check product is valid.
	    	if (product == null)
			{
				return;
			}

            // Attempt to find the table row showing the product
            TableRow row = findProductTableRow(byProductTable, product);

            if (row != null)
            {
                // Get reference to table cell holding the amount of product onboard
                TextView productOnboard = (TextView)row.findViewById(R.id.stock_summary_by_product_tablerow_onboard);

                // Update quantity onboard.
                int prevOnboard = 0;

                try
                {
                    prevOnboard = formatDecimal.parse(productOnboard.getText().toString()).intValue();
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                productOnboard.setText(formatDecimal.format(onboard + prevOnboard));

                // Get reference to table cell holding the amount of surplus product
                TextView productSurplus = (TextView)row.findViewById(R.id.stock_summary_by_product_tablerow_to_load);

                // Update quantity spare.
                int prevSpare = 0;

                try
                {
                    prevSpare = formatDecimal.parse((String) productSurplus.getText()).intValue();
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                productSurplus.setText(formatDecimal.format(onboard + prevSpare));
            }
            else
            {
                // If this point is reached then the row containing the product
                // could not be found in the table - therefore create a row for it.
                createByProductRow(product, onboard);
            }
        }
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    private TableRow createByProductRow(dbProduct product, int onboard)
    {
    	try
    	{
            CrashReporter.leaveBreadcrumb("MyStockSummary : createByProductRow");

			// Create new row to show product stock on truck.
			TableRow row = (TableRow)inflater.inflate(R.layout.stock_summary_by_product_tablerow, null);
			row.setTag(product);

            // In the row set the cell showing the name of the product
			TextView productName = (TextView)row.findViewById(R.id.stock_summary_by_product_tablerow_product);
			productName.setText(product.Desc);

            // In the row set the cell showing the amount of product on board the truck
			TextView productOnboard = (TextView)row.findViewById(R.id.stock_summary_by_product_tablerow_onboard);
			productOnboard.setText(formatDecimal.format(onboard));

            // In the row set the cell showing the volume of surplus product
			TextView surplusProduct = (TextView)row.findViewById(R.id.stock_summary_by_product_tablerow_to_load);
			surplusProduct.setText(formatDecimal.format(0));

            // Add the newly created row to the table
			byProductTable.addView(row);

            return row;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
			return null;
		}
    }
    
    private void addByCompartment(dbProduct product, int compartment, int capacity, int onboard)
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
				tvCapacity.setText(formatDecimal.format(capacity));
			}
	
			TextView tvOnboard = (TextView)tr.findViewById(R.id.stock_summary_by_compartment_tablerow_onboard);

			if (onboard != 0)
			{
				tvOnboard.setText(formatDecimal.format(onboard));
			}
			
			tlByCompartmentTable.addView(tr);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
}