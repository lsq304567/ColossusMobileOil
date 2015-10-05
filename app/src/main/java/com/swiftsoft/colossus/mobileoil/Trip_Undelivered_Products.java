package com.swiftsoft.colossus.mobileoil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import org.json.JSONObject;

public class Trip_Undelivered_Products extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private TableLayout tlProductsTable;
	private TextView tvDeliveryMethod;
	private Button btnDeliveryMethodChange;
	private TextView tvLineProduct;
	private Button btnLineProductChange;
	private LinearLayout llProductsLineChange;
	private TextView tvTerms;
	private TextView tvNotes;
	private Button btnBack;
	private Button btnNext;

	private dbProduct lineProduct = null;
	
	public Trip_Undelivered_Products(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Products(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_products, this, true);

			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_products_infoview);
			tlProductsTable = (TableLayout)this.findViewById(R.id.trip_undelivered_products_table);
			tvDeliveryMethod = (TextView)this.findViewById(R.id.trip_undelivered_products_delivery_method);
			btnDeliveryMethodChange = (Button)this.findViewById(R.id.trip_undelivered_products_delivery_method_change);
			tvLineProduct = (TextView)this.findViewById(R.id.trip_undelivered_products_line_product);
			btnLineProductChange = (Button)this.findViewById(R.id.trip_undelivered_products_line_product_change);
			llProductsLineChange = (LinearLayout)this.findViewById(R.id.trip_undelivered_products_line_change);
			tvTerms = (TextView)this.findViewById(R.id.trip_undelivered_products_terms);
			tvNotes = (TextView)this.findViewById(R.id.trip_undelivered_products_notes);
	
			btnBack = (Button)this.findViewById(R.id.trip_undelivered_products_back);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_products_next);
			
			btnDeliveryMethodChange.setOnClickListener(onDeliveryMethodChange);
			btnLineProductChange.setOnClickListener(onLineProductChange);
			btnBack.setOnClickListener(onBack);
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
			
			// Initialise UI.
			tvDeliveryMethod.setText("Hose");
			llProductsLineChange.setVisibility(View.GONE);
			btnBack.setEnabled(true);
			
			if (Active.order.getUndeliveredCount() > 0)
				btnNext.setEnabled(false);
			else
				btnNext.setEnabled(true);
			
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
			infoview.setDefaultTv1("Product delivery");
			
			// Line.
			if (lineProduct == null)
			{
				infoview.setDefaultTv2("Line: None");
				tvLineProduct.setText("(none)");
			}
			else
			{
				infoview.setDefaultTv2("Line: " + lineProduct.Desc);
				tvLineProduct.setText(lineProduct.Desc);
			}
	
			// Remove any old order lines.
			while (tlProductsTable.getChildCount() > 1)
				tlProductsTable.removeViewAt(1);
			
			if (Active.order != null)
			{		
				// Create a TableRow for each order line.
				for (dbTripOrderLine line : Active.order.GetTripOrderLines())
				{
					// Skip non-deliverable products e.g. credit card fee?
					if (line.Product.MobileOil == 3)
						continue;
					
					TableRow tr = (TableRow)inflater.inflate(R.layout.trip_undelivered_products_row, null);
					tr.setTag(line);
		
					RadioButton rb = (RadioButton) tr.findViewById(R.id.trip_undelivered_products_row_radiobutton);
					rb.setText(line.Product.Desc);
					rb.setOnClickListener(onRbClick);
					
					TextView tvOrderedQty = (TextView) tr.findViewById(R.id.trip_undelivered_products_row_ordered);
					tvOrderedQty.setText(Integer.toString(line.OrderedQty));
					
					TextView tvDeliveredQty = (TextView) tr.findViewById(R.id.trip_undelivered_products_row_delivered);
					if (line.DeliveredQty != 0)
						tvDeliveredQty.setText(Integer.toString(line.DeliveredQty));
					
					// Check if already delivered.
					if (line.Delivered)
						rb.setEnabled(false);
					
					// Add the TableRow to the TableLayout
			        tlProductsTable.addView(tr);
				}
		
				tvTerms.setText(Active.order.getTerms());
				tvNotes.setText(Active.order.Notes);
			}
			
			// If just a title row and one product, default to it.
			if (tlProductsTable.getChildCount() == 2)
			{
				TableRow tr = (TableRow) tlProductsTable.getChildAt(1);
	
				RadioButton rb = (RadioButton) tr.findViewById(R.id.trip_undelivered_products_row_radiobutton);

				if (!rb.isChecked())
				{
					rb.performClick();
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	OnClickListener onRbClick = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: onRbClick");

				// Find TableRow.
				TableRow myTr = (TableRow) paramView.getParent().getParent();
				
				// Set all other RadioButtons to unchecked.
				for (int row = 1; row < tlProductsTable.getChildCount(); row++)
				{
					TableRow tr = (TableRow) tlProductsTable.getChildAt(row);

					if (!tr.equals(myTr))
					{
						RadioButton rb = (RadioButton) tr.findViewById(R.id.trip_undelivered_products_row_radiobutton);
						rb.setChecked(false);
					}
				}
	
				checkIfLineChangedRequired();
	
				// Enabled Next.
				btnNext.setEnabled(true);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	private void checkIfLineChangedRequired()
	{
		// Check if any product is selected.
		dbTripOrderLine orderLine = getSelectedOrderLine();
		
		// Assume line change will not be required.
		llProductsLineChange.setVisibility(View.GONE);
		btnLineProductChange.setEnabled(false);
			
		// Is bulk, then ignore line change logic.
		if (tvDeliveryMethod.getText().equals("Bulk"))
			return;
		
		// Is a line change necessary?
		// dbProduct lineProduct = Active.vehicle.getHosereelProduct();
		if (lineProduct != null && orderLine != null)
		{
			if (!lineProduct.getId().equals(orderLine.Product.getId()))
			{
				if (orderLine.Product.MobileOil == 1)
				{
					llProductsLineChange.setVisibility(View.VISIBLE);
					btnLineProductChange.setEnabled(true);
				}
			}
		}
	}
	
	// Return the current selected dbTripOrderLine.
	private dbTripOrderLine getSelectedOrderLine()
	{
		try
		{
			for (int row = 1; row < tlProductsTable.getChildCount(); row++)
			{
				final TableRow tr = (TableRow) tlProductsTable.getChildAt(row);
				
				RadioButton rb = (RadioButton) tr.findViewById(R.id.trip_undelivered_products_row_radiobutton);
				if (rb.isChecked())
				{
					dbTripOrderLine orderLine = (dbTripOrderLine) tr.getTag();
					return orderLine;
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
		
		return null;
	}
	
	OnClickListener onDeliveryMethodChange = new OnClickListener()
	{		
		@Override
		public void onClick(View v)
		{
			if (tvDeliveryMethod.getText().equals("Bulk"))
				tvDeliveryMethod.setText("Hose");
			else
				tvDeliveryMethod.setText("Bulk");
			
			checkIfLineChangedRequired();
		}
	};
	
	OnClickListener onLineProductChange = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: onChange");
			
				// Display msgPerformChecklist message and finish activity.
				AlertDialog.Builder builder = new AlertDialog.Builder(trip);
				builder.setTitle("Line product correction");
				builder.setMessage("This should ONLY be used if correcting an error. Do you wish to proceed?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{	
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (getSelectedOrderLine() != null)
						{
							lineProduct = getSelectedOrderLine().Product; 

							// Update UI.
							updateUI();
						}
					}
				});
				builder.setNegativeButton("No", null);
				builder.show();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: onBack");

				if (Active.order.CodPoint == 2 && Active.order.getCodBeforeDeliveryValue() > 0)
				{
					// Switch views.
					trip.selectView(Trip.ViewUndeliveredCOD, -1);
				}
				else
				{
					// Switch views.
					trip.selectView(Trip.ViewUndeliveredSummary, -1);
				}				
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
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
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: onNext");

				// Record line change
				if (lineProduct == null)
				{
					CrashReporter.leaveBreadcrumb("lineProduct is null!!");
				}
				
				if (Active.vehicle.getHosereelProduct() == null)
				{
					CrashReporter.leaveBreadcrumb("getHostreelProduct is null!!");
				}
				
				if (!Active.vehicle.getHosereelProduct().getId().equals(lineProduct.getId()))
				{
					Active.vehicle.recordLineChange(lineProduct, Active.vehicle.C0_Capacity, "C");
				}

				// Check if all products are now delivered.
				if (Active.order.getUndeliveredCount() > 0)
				{
					// Determine if line change is required.
					if (llProductsLineChange.getVisibility() == View.VISIBLE)
					{
						// Perform line change.
						Active.orderLine = null;
						Active.lineChangeProduct = getSelectedOrderLine().Product; 
	
						trip.selectView(Trip.ViewUndeliveredLineChange, +1);
					}
					else
					{
						// Delivery selected product.
						Active.orderLine = getSelectedOrderLine();
						
						if (Active.orderLine.Product.MobileOil == 1)	// Metered product.
						{						
							trip.setMeterMateCallbacks(callbacks);
							trip.selectView(Trip.ViewUndeliveredMeterMate, +1);
						}
						
						if (Active.orderLine.Product.MobileOil == 2)	// Non-metered product.
						{
							trip.selectView(Trip.ViewUndeliveredNonMetered, +1);
						}
					}
				}
				else
				{
					// All delivered; Switch to delivery note.
					trip.selectView(Trip.ViewUndeliveredDeliveryNote, +1);
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	Trip_MeterMate_Callbacks callbacks = new Trip_MeterMate_Callbacks()
	{
		@Override
		public int getLitres()
		{
			return Active.orderLine.OrderedQty;
		}

		@Override
		public dbProduct getProduct()
		{
			return Active.orderLine.Product;
		}

		@Override
		public void onTicketComplete()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: MeterMate onTicketComplete");

				int litres;

				litres = MeterMate.getTicketAt15Degrees() ? (int) MeterMate.getTicketNetVolume() : (int) MeterMate.getTicketGrossVolume();
	
				// Record product delivery.
				Active.orderLine.delivered(litres);
				Active.orderLine.ticketNo = MeterMate.getTicketNo();
				Active.orderLine.ticketProductDesc = MeterMate.getTicketProductDesc();
				Active.orderLine.ticketStartTime = MeterMate.getTicketStartTime();
				Active.orderLine.ticketFinishTime = MeterMate.getTicketFinishTime();
				Active.orderLine.ticketStartTotaliser = MeterMate.getTicketStartTotaliser();
				Active.orderLine.ticketEndTotaliser = MeterMate.getTicketEndTotaliser();
				Active.orderLine.ticketGrossVolume = MeterMate.getTicketGrossVolume();
				Active.orderLine.ticketNetVolume = MeterMate.getTicketNetVolume();
				Active.orderLine.ticketTemperature = MeterMate.getTicketTemperature();
				Active.orderLine.ticketAt15Degrees = MeterMate.getTicketAt15Degrees();
				Active.orderLine.save();
				
				// Update stock locally.
				Active.vehicle.recordDelivery();
				
				// Update stock on server.
				trip.sendVehicleStock();

                // Save MeterMate data to Colossus Server
                saveMeterMateData();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}

        private void saveMeterMateData() throws Exception
        {
            Intent i = new Intent(getContext(), ColossusIntentService.class);

            // Set the type of the data being stored
            i.putExtra("Type", "MeterMate_Data");

            // Construct the data to be stored as JSON
            JSONObject json = new JSONObject();

            json.put("Number", MeterMate.getTicketNo());
            json.put("ProductDescrition", MeterMate.getTicketProductDesc());
            json.put("StartTime", MeterMate.getTicketStartTime());
            json.put("FinishTime", MeterMate.getTicketFinishTime());
            json.put("StartTotaliser", MeterMate.getTicketStartTotaliser());
            json.put("EndTotaliser", MeterMate.getTicketEndTotaliser());
            json.put("GrossVolume", MeterMate.getTicketGrossVolume());
            json.put("NetVolume", MeterMate.getTicketNetVolume());
            json.put("Temperature", MeterMate.getTicketTemperature());
            json.put("At15Degress", MeterMate.getTicketAt15Degrees());

            // Set the content
            i.putExtra("Content", json.toString());

            // Perform the storage
            getContext().startService(i);
        }

		@Override
		public void onNextClicked()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Products: MeterMate onNextClicked");

				if (tvDeliveryMethod.getText().equals("Hose"))
				{
					// Check if line changed occurred during delivery.
					trip.selectView(Trip.ViewUndeliveredLineChangeDuringDelivery, +1);
				}
				else
				{
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
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
