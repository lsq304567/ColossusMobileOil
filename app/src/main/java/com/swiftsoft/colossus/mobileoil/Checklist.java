package com.swiftsoft.colossus.mobileoil;

import java.util.Date;

import org.json.JSONObject;

import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklist;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSection;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleChecklistSectionItem;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.utilities.ControlSaver;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Checklist extends PreferenceActivity
{
	MyInfoView1Line infoview;
	LinearLayout ll1;
	LinearLayout ll2;
	TextView message;
	EditText issues;
	Button button1;
	Button button2;

	private static final String msgChecklistCompleted = "Have you completed\nyour vehicle checklist?";
	private static final String msgPerformChecklist = "Please complete your vehicle checklist, before starting deliveries";
	private static final String msgEverythingSatisfactory = "Are all items\nsatisfactory?";
	private static final String msgReportIssues = "Describe unsatisfactory item(s) for transport manager";
	private static final String msgCompleteDefectSheet = "Please also complete a defect sheet";

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
        ControlSaver.save(infoview, "Checklist.Info", outState);
        ControlSaver.save(message, "Checklist.Message", outState);
        ControlSaver.save(issues, "Checklist.Issues", outState);
        ControlSaver.save(button1, "Checklist.Button1", outState);
        ControlSaver.save(button2, "Checklist.Button2", outState);
        ControlSaver.save(ll1, "Checklist.Layout1", outState);
        ControlSaver.save(ll2, "Checklist.Layout2", outState);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state)
	{
		super.onRestoreInstanceState(state);

        ControlSaver.restore(infoview, "Checklist.Indo", state);
        ControlSaver.restore(message, "Checklist.Message", state);
        ControlSaver.restore(issues, "Checklist.Issues", state);
        ControlSaver.restore(button1, "Checklist.Button1", state);
        ControlSaver.restore(button2, "Checklist.Button2", state);
        ControlSaver.restore(ll1, "Checklist.Layout1", state);
        ControlSaver.restore(ll2, "Checklist.Layout2", state);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: onCreate");
	
			// Build checklist for vehicle.
			setPreferenceScreen(createPreferenceHierarchy());
			setContentView(R.layout.checklist);
			
			// Find UI controls.
			infoview = (MyInfoView1Line) findViewById(R.id.checklist_infoview);
			ll1 = (LinearLayout) findViewById(R.id.checklist_layout1);
			ll2 = (LinearLayout) findViewById(R.id.checklist_layout2);
			issues = (EditText) findViewById(R.id.checklist_issues);
			message = (TextView) findViewById(R.id.checklist_message);
			button1 = (Button) findViewById(R.id.checklist_button1);
			button2 = (Button) findViewById(R.id.checklist_button2);

			// Initialise infoview.
			infoview.setDefaultTv1("Vehicle Checklist");
			infoview.setDefaultTv2("");
			
			issues.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (hasFocus)
					{
						InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Service.INPUT_METHOD_SERVICE);
						imm.showSoftInput(issues, 0);
					}
					else
					{
						InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Service.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(issues.getWindowToken(), 0);
					}
				}
			});
	
			// Initialise UI.
			showPrompt1();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// Prevent the Back button from exiting the application.
	@Override
	public void onBackPressed()
	{
		return;
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: onResume");
			
			// Update Active.activity
			Active.activity = this;

			// Resume updating.
			infoview.resume();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: onPause");
			
			// Pause updating.
			infoview.pause();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private PreferenceScreen createPreferenceHierarchy() 
	{        
		PreferenceScreen root = null;
		dbVehicleChecklist newestChecklist = null;
				
		try
		{
			// Create root i.e. PreferenceScreen
			root = getPreferenceManager().createPreferenceScreen(this);
			
			for (dbVehicleChecklist checklist : Active.vehicle.GetVehicleChecklists())
			{
				if (newestChecklist == null)
					newestChecklist = checklist;
				else
				{
					if (checklist.Version > newestChecklist.Version)
					{
						// Delete old checklists?
						newestChecklist = checklist;
					}
				}
			}
			
			if (newestChecklist != null)
			{
				// Find all sections in this checklist.
				for (dbVehicleChecklistSection checklistSection : newestChecklist.GetVehicleChecklistSections())
				{
					// Create section i.e. PreferenceCategory
					PreferenceCategory pc = new PreferenceCategory(this);
					pc.setTitle(checklistSection.Title);
					root.addPreference(pc);
					
					// Find all items in this section.
					for (dbVehicleChecklistSectionItem checklistSectionItem : checklistSection.GetVehicleChecklistSectionItems())
					{
						Preference p = new Preference(this);
						p.setTitle(checklistSectionItem.Title);
						p.setSummary(checklistSectionItem.Summary);
						pc.addPreference(p);
					}
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
		
		return root;    
	}
	
	// Yes/OK button handler.
	public void onButton1Clicked(View button)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: onButton1Clicked");

			// Get current DateTime.
			long now = new Date().getTime();
			
			if (message.getText().equals(msgChecklistCompleted))
			{
				showPrompt2();
			}
			else if (message.getText().equals(msgEverythingSatisfactory))
			{
				// Create content.
				JSONObject json = new JSONObject();
				json.put("VehicleID", Active.vehicle.ColossusID);
				json.put("DriverID", Active.driver.ColossusID);
				json.put("DateTime", "/Date(" + now + ")/");

				// Call ColossusIntentService.
				Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
				i.putExtra("Type", "Checklist_OK");
				i.putExtra("Content", json.toString());
				startService(i);
	
				// Start trips activity.
				startTripsActivity();
				
				// Close this activity.
				finish();
			}
			else
			{
				// Create content.
				JSONObject json = new JSONObject();
				json.put("VehicleID", Active.vehicle.ColossusID);
				json.put("DriverID", Active.driver.ColossusID);
				json.put("Issues", issues.getText().toString());
				json.put("DateTime", "/Date(" + now + ")/");
	
				// Call ColossusIntentService.
				Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
				i.putExtra("Type", "Checklist_NOK");
				i.putExtra("Content", json.toString());
				startService(i);
	
				// Display msgCompleteDefectSheet message.
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Vehicle Defect Sheet required");
				builder.setMessage(msgCompleteDefectSheet);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// Start trips activity.
						startTripsActivity();

						// Close this activity.
						finish();
					}
				});
				builder.show();
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// No/Cancel button handler.
	public void onButton2Clicked(View button)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: onButton2Clicked");

			if (message.getText().equals(msgChecklistCompleted))
			{
				// Display msgPerformChecklist message and finish activity.
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Vehicle Checklist required");
				builder.setMessage(msgPerformChecklist);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
	
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// Call ColossusIntentService.
						Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);
						i.putExtra("Type", "Shift_End");
						i.putExtra("Content", "");
						startService(i);
	
						// Close this activity.
						finish();
					}
				});
				builder.show();
			}
			else if (message.getText().equals(msgEverythingSatisfactory))
			{
				// Display msgReportIssues message and ask for input.
				showPrompt3();
			}
			else
			{
				// Back to the start!
				showPrompt1();
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	void showPrompt1()
	{
		ll1.setVisibility(View.VISIBLE);
		ll2.setVisibility(View.GONE);
		message.setText(msgChecklistCompleted);
		button1.setText("Yes");
		button2.setText("No");
	}

	void showPrompt2()
	{
		ll1.setVisibility(View.VISIBLE);
		ll2.setVisibility(View.GONE);
		message.setText(msgEverythingSatisfactory);
		button1.setText("Yes");
		button2.setText("No");
	}

	void showPrompt3()
	{
		ll1.setVisibility(View.GONE);
		ll2.setVisibility(View.VISIBLE);
		message.setText(msgReportIssues);
		button1.setText("OK");
		button2.setText("Cancel");
		
		issues.requestFocus();
	}

	void startTripsActivity()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Checklist: startTripsActivity");
	
			// Start the Trips activity.
			Intent intent = new Intent(this, Trips.class);
			startActivity(intent);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}