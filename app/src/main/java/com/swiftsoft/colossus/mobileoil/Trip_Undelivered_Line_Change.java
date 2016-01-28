package com.swiftsoft.colossus.mobileoil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftsoft.colossus.mobileoil.bluetooth.MeterMate;
import com.swiftsoft.colossus.mobileoil.database.DbUtils;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Trip_Undelivered_Line_Change extends MyFlipperView
{
	private Trip trip;

    private final int lcSourceCompartmentIdx = 0;
	private final int lcReturnCompartmentIdx = 0;

	private MyInfoView1Line infoview;
	private TableLayout tlByCompartment;
	private TextView tvSourceCompartment;
	private TextView tvSourceOnboard;
	private TextView tvReturnCompartment;
	private TextView tvReturnUllage;
	private TableLayout tlNotByCompartment;
	private TextView tvFromProduct;
	private TextView tvToProduct;
	private MyEditText etPreset;

    private DecimalFormat decimalFormat;
	private int litres;

	public Trip_Undelivered_Line_Change(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Line_Change(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_line_change, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_lc_infoview);
			tlByCompartment = (TableLayout)this.findViewById(R.id.trip_undelivered_lc_bycompartment);
			tvSourceCompartment = (TextView)this.findViewById(R.id.trip_undelivered_lc_from_no);
			tvSourceOnboard = (TextView)this.findViewById(R.id.trip_undelivered_lc_from_onboard);
			tvReturnCompartment = (TextView)this.findViewById(R.id.trip_undelivered_lc_to_no);
			tvReturnUllage = (TextView)this.findViewById(R.id.trip_undelivered_lc_to_ullage);
			tlNotByCompartment = (TableLayout)this.findViewById(R.id.trip_undelivered_lc_notbycompartment);
			tvFromProduct = (TextView)this.findViewById(R.id.trip_undelivered_lc_from_product);
			tvToProduct = (TextView)this.findViewById(R.id.trip_undelivered_lc_to_product);
			etPreset = (MyEditText)this.findViewById(R.id.trip_undelivered_lc_preset);
            Button btnBack = (Button) this.findViewById(R.id.trip_undelivered_lc_back);
            Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_lc_next);
			
			btnBack.setOnClickListener(onBack);
			btnNext.setOnClickListener(onNext);
	
			// Setup standard decimal format.
			decimalFormat = new DecimalFormat("#,##0");
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
			
	    	// Focus on Preset.
	    	etPreset.setText(decimalFormat.format(Active.vehicle.getHosereelCapacity()));
	    	etPreset.requestFocus();
	
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
			infoview.setDefaultTv1("Line change");

			// Line.
			infoview.setDefaultTv2(DbUtils.getInfoviewLineProduct(Active.vehicle.getHosereelProduct()));

			if (Active.vehicle.StockByCompartment)
			{
				// Update UI for StockByCompartment.
				dbProduct sourceProduct = Active.vehicle.getCompartmentProduct(lcSourceCompartmentIdx);

				int sourceCompartment = Active.vehicle.getCompartmentNo(lcSourceCompartmentIdx);
				int sourceOnboard = Active.vehicle.getCompartmentOnboard(lcSourceCompartmentIdx);
	
				dbProduct returnProduct = Active.vehicle.getCompartmentProduct(lcReturnCompartmentIdx);

				int returnCompartment = Active.vehicle.getCompartmentNo(lcReturnCompartmentIdx);
				int returnCapacity = Active.vehicle.getCompartmentCapacity(lcReturnCompartmentIdx);
				int returnOnboard = Active.vehicle.getCompartmentOnboard(lcReturnCompartmentIdx);
				int returnUllage = (returnCapacity - returnOnboard);
	
				// From section.
				tvSourceCompartment.setText(String.format("%d", sourceCompartment));

                tvSourceOnboard.setText(sourceProduct == null ? decimalFormat.format(sourceOnboard) + " l" : decimalFormat.format(sourceOnboard) + " l of " + sourceProduct.Desc);
				
				// To section
				tvReturnCompartment.setText(String.format("%d", returnCompartment));

				tvReturnUllage.setText(String.format("%s 1%s", decimalFormat.format(returnUllage), returnProduct == null ? "" : " of " + returnProduct.Desc));

				tlByCompartment.setVisibility(View.VISIBLE);
				tlNotByCompartment.setVisibility(View.GONE);
			}
			else
			{
				// Update UI for not StockByCompartment.
				tvFromProduct.setText(Active.vehicle.getHosereelProduct().Desc);
				tvToProduct.setText(Active.lineChangeProduct.Desc);
	
				tlByCompartment.setVisibility(View.GONE);
				tlNotByCompartment.setVisibility(View.VISIBLE);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}


	private final OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change: onBack");

				// Switch views.
				trip.selectView(Trip.ViewUndeliveredProducts, -1);
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
			String errorMessage = "";
			dbProduct lineProduct;
			litres = 0;
			
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change: onNext");

				// Find line product.
				lineProduct = Active.vehicle.getHosereelProduct();
				
				// Find preset value. 
				try
                {
                    litres = decimalFormat.parse(etPreset.getText().toString()).intValue();
                }
				catch (ParseException e)
                {
                    e.printStackTrace();
                }

				//
				// Validation.
				//
				
				if (Active.vehicle.StockByCompartment)
				{
					dbProduct sourceProduct = Active.vehicle.getCompartmentProduct(lcSourceCompartmentIdx);
					dbProduct returnProduct = Active.vehicle.getCompartmentProduct(lcReturnCompartmentIdx);

					int sourceCompartment = Active.vehicle.getCompartmentNo(lcSourceCompartmentIdx);
					int sourceOnboard = Active.vehicle.getCompartmentOnboard(lcSourceCompartmentIdx);
					int returnCapacity = Active.vehicle.getCompartmentCapacity(lcReturnCompartmentIdx);
					int returnOnboard = Active.vehicle.getCompartmentOnboard(lcReturnCompartmentIdx);
					int returnUllage = returnCapacity - returnOnboard;
					
					// Check 'From' and 'To' are different.
                    //noinspection ConstantConditions
                    if (lcSourceCompartmentIdx == lcReturnCompartmentIdx)
					{
						errorMessage = "Compartment numbers can't be the same";
						return;
					}
					
					// Check there is product in the 'Source' compartment.
					if (sourceProduct == null)
					{
						errorMessage = "No product in compartment " + sourceCompartment;
						return;
					}
		
					// Check 'line' product is different from 'Source' compartment's product.
					if (lineProduct != null)
					{
						if (lineProduct.getId().equals(sourceProduct.getId()))
						{
							errorMessage = "Line already contains " + lineProduct.Desc + " !!";
							return;
						}
					}
					
					// Check 'line' product is the same as 'Return' compartment's product.
					if (lineProduct != null && returnProduct != null)
					{
						if (!lineProduct.getId().equals(returnProduct.getId()))
						{
							errorMessage = "Line contains " + lineProduct.Desc + " !!";
							return;
						}
					}
		
					// Check there is enough product in 'Source' compartment.
					if (litres > sourceOnboard)
					{
						errorMessage = "Litres greater than onboard";
						return;
					}
		
					// Check there is enough ullage in 'Return' compartment.
					if (litres > returnUllage)
					{
						errorMessage = "Litres greater than ullage";
						return;
					}				
				}
				
				if (litres <= 0)
				{
					errorMessage = "Preset value is missing";
					return;
				}

				// Switch to MeterMate view.
				trip.setMeterMateCallbacks(callbacks);
				trip.selectView(Trip.ViewUndeliveredMeterMate, +1);
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
			return Active.lineChangeProduct;
		}

		@Override
		public void onTicketComplete()
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change: MeterMate onTicketComplete");

				// Read ticket details.
				int litres = MeterMate.getTicketAt15Degrees() ? (int) MeterMate.getTicketNetVolume() : (int) MeterMate.getTicketGrossVolume();
				
				// Update stock locally.
				Active.vehicle.recordLineChange(Active.lineChangeProduct, litres, MeterMate.getTicketNo());
				
				// Update stock on server.
				trip.sendVehicleStock();
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
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Line_Change: MeterMate onNextClicked");

				// Switch views.
				trip.selectView(Trip.ViewUndeliveredProducts, -1);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
