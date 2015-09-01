package com.swiftsoft.colossus.mobileoil;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbSetting;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Setup_Vehicle_Line extends MyFlipperView
{
	private Setup setup;
	private LayoutInflater inflater;

	private MyInfoView1Line infoview; 
	private TextView tvLineProduct;
	private Button btnChange;
	private Button btnNext;
	
	private dbProduct lineProduct = null;
	private List<dbProduct> products;
	
	public Setup_Vehicle_Line(Context context)
	{
		super(context);
		init(context);
	}

	public Setup_Vehicle_Line(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public void saveState(Bundle state)
	{
        state.putString("Vehicle.Line.Info.TV1", infoview.getDefaultTv1());
        state.putString("Vehicle.Line.Info.TV2", infoview.getDefaultTv2());

        state.putString("Vehicle.Line.Product.Text", tvLineProduct.getText().toString());
        state.putBoolean("Vehicle.Line.Product.Enabled", tvLineProduct.isEnabled());
        state.putInt("Vehicle.Line.Product.Visibility", tvLineProduct.getVisibility());

        state.putString("Vehicle.Line.Change.Text", btnChange.getText().toString());
        state.putBoolean("Vehicle.Line.Change.Enabled", btnChange.isEnabled());
        state.putInt("Vehicle.Line.Change.Visibility", btnChange.getVisibility());

        state.putString("Vehicle.Line.Next.Text", btnNext.getText().toString());
        state.putBoolean("Vehicle.Line.Next.Enabled", btnNext.isEnabled());
        state.putInt("Vehicle.Line.Next.Visibility", btnNext.getVisibility());
	}

	@SuppressWarnings("ResourceType")
    public void restoreState(Bundle state)
	{
        infoview.setDefaultTv1(state.getString("Vehicle.Line.Info.TV1"));
        infoview.setDefaultTv2(state.getString("Vehicle.Line.Info.TV2"));

        tvLineProduct.setText(state.getString("Vehicle.Line.Product.Text"));
        tvLineProduct.setEnabled(state.getBoolean("Vehicle.Line.Product.Enabled"));
        tvLineProduct.setVisibility(state.getInt("Vehicle.Line.Product.Visibility"));

        btnChange.setText(state.getString("Vehicle.Line.Change.Text"));
        btnChange.setEnabled(state.getBoolean("Vehicle.Line.Change.Enabled"));
        btnChange.setVisibility(state.getInt("Vehicle.Line.Change.Visibility"));

        btnNext.setText(state.getString("Vehicle.Line.Next.Text"));
        btnNext.setEnabled(state.getBoolean("Vehicle.Line.Next.Enabled"));
        btnNext.setVisibility(state.getInt("Vehicle.Line.Next.Visibility"));
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: init");
			
			// Store reference to Startup activity.
			setup = (Setup)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.setup_vehicle_line, this, true);
		
			infoview = (MyInfoView1Line)this.findViewById(R.id.setup_vehicle_line_infoview);
			tvLineProduct = (TextView)this.findViewById(R.id.setup_vehicle_line_product);
			btnChange = (Button)this.findViewById(R.id.setup_vehicle_line_change);
			btnNext = (Button)this.findViewById(R.id.setup_vehicle_line_next);
	
			btnChange.setOnClickListener(onChange);
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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: resumeView");
			
			// Resume updating.
			infoview.resume();
			
			// Load products.
			products = dbProduct.GetAllMetered();
			
			// Setup UI.
			infoview.setDefaultTv1("App Setup");
			infoview.setDefaultTv2("Line product");
			btnNext.setEnabled(false);
			
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
			CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: pauseView");

			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onChange = new OnClickListener()
	{		
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: onChange");
				
				// Check there are products.
				if (!products.isEmpty())
				{
					int idx = -1;
	
					if (lineProduct != null)
					{
						// Find currently selected product.
						for (int i = 0; i < products.size(); i++)
						{
							if (products.get(i).getId().equals(lineProduct.getId()))
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
						lineProduct = products.get(idx);
					else
						lineProduct = products.get(0);
					
					// Reflect changes on UI.
					tvLineProduct.setText(lineProduct.Desc);
					btnNext.setEnabled(true);
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
				CrashReporter.leaveBreadcrumb("Setup_Vehicle_Line: onNext");

				dbSetting setting = dbSetting.FindByKey("VehicleRegistered");
		    	if (setting != null)
		    	{
		    		dbVehicle vehicle = dbVehicle.FindByNo(setting.IntValue);
		    		if (vehicle != null)
		    		{
		    			// Save line product.
		    			vehicle.updateCompartmentProduct(0, lineProduct);
		    			vehicle.updateCompartmentOnboard(0, vehicle.C0_Capacity);
		    			
		    			// Update stock on server.
		    			setup.sendVehicleStock(vehicle);
		    		}
		    	}
				
				setup.isSetupComplete();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
