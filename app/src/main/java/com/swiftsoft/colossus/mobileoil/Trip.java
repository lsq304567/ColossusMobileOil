package com.swiftsoft.colossus.mobileoil;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.swiftsoft.colossus.mobileoil.bluetooth.Bluetooth;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyNumericKeypad;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Date;

public class Trip extends Activity
{
	public static final String ViewStockStart                          = "Trip.StockStart";
	public static final String ViewStockLoad                           = "Trip.StockLoad";
	public static final String ViewStockReturn                         = "Trip.StockReturn";
	public static final String ViewTransportDoc                        = "Trip.TransportDoc";
	public static final String ViewUndeliveredList                     = "Trip.UndeliveredList";
	public static final String ViewUndeliveredSummary                  = "Trip.UndeliveredSummary";
	public static final String ViewUndeliveredCOD                      = "Trip.UndeliveredCOD";
	public static final String ViewUndeliveredProducts                 = "Trip.UndeliveredProducts";
	public static final String ViewUndeliveredLineChange               = "Trip.UndeliveredLineChange";
	public static final String ViewUndeliveredMeterMate                = "Trip.UndeliveredMeterMate";
	public static final String ViewUndeliveredNonMetered               = "Trip.UndeliveredNonMetered";
	public static final String ViewUndeliveredLineChangeDuringDelivery = "Trip.UndeliveredLineChangeDuringDelivery";
	public static final String ViewUndeliveredDeliveryNote             = "Trip.UndeliveredDeliveryNote";
	public static final String ViewUndeliveredTicket                   = "Trip.UndeliveredTicket";
	public static final String ViewStockEnd                            = "Trip.StockEnd";
	public static final String ViewTripReport                          = "Trip.Report";

	private final int ViewStockStartIdx                          = 0;
	private final int ViewStockLoadIdx                           = 1;
	private final int ViewStockReturnIdx                         = 2;
	private final int ViewTransportDocIdx                        = 3;
	private final int ViewUndeliveredListIdx                     = 4;
	private final int ViewUndeliveredSummaryIdx                  = 5;
	private final int ViewUndeliveredCODIdx                      = 6;
	private final int ViewUndeliveredProductsIdx                 = 7;
	private final int ViewUndeliveredLineChangeIdx               = 8;
	private final int ViewUndeliveredMeterMateIdx                = 9;
	private final int ViewUndeliveredNonMeteredIdx               = 10;
	private final int ViewUndeliveredLineChangeDuringDeliveryIdx = 11;
	private final int ViewUndeliveredDeliveryNoteIdx             = 12;
	private final int ViewUndeliveredTicketIdx                   = 13;
	private final int ViewStockEndIdx                            = 14;
	private final int ViewTripReportIdx                          = 15;
	
	private Trip_Stock_Start                            tripStockStart;
	private Trip_Stock_Load                             tripStockLoad;
	private Trip_Stock_Return                           tripStockReturn;
	private Trip_Transport_Doc                          tripTransportDoc;
	private Trip_Undelivered_List                       tripUndeliveredList;
	private Trip_Undelivered_Summary                    tripUndeliveredSummary;
	private Trip_Undelivered_COD                        tripUndeliveredCOD;
	private Trip_Undelivered_Products                   tripUndeliveredProducts;
    private Trip_Undelivered_Line_Change                tripUndeliveredLineChange;	
	private Trip_Undelivered_MeterMate                  tripUndeliveredMeterMate;
	private Trip_Undelivered_NonMetered                 tripUndeliveredNonMetered;
	private Trip_Undelivered_Line_Change_DuringDelivery tripUndeliveredLineChangeDuringDelivery;
	private Trip_Undelivered_Delivery_Note              tripUndeliveredDeliveryNote;
	private Trip_Undelivered_Ticket                     tripUndeliveredTicket;
	private Trip_Stock_End                              tripStockEnd;
	private Trip_Report                                 tripReport;

	private ViewFlipper vf;
	private String currentViewName;
	private MyFlipperView currentView;
	private DecimalFormat decf2;
	private Trip_Delivered deliveredDialog;
	private myReceiver receiver;
	
	class myReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip: onReceive");
				
				if (intent.getAction().equals(ColossusIntentService.BroadcastTripsChanged))
				{
					if (tripUndeliveredList != null)
					{
						tripUndeliveredList.updateUI();
					}
				}
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: onCreate");

			// Setup view.
			setContentView(R.layout.trip);
			
			// Initialise Bluetooth.
			Bluetooth.bluetooth = BluetoothAdapter.getDefaultAdapter();
	
			// Setup standard decimal formats.
			decf2 = new DecimalFormat("#,##0.00");
	
			// Create views.
			tripStockStart                          = new Trip_Stock_Start(this);
			tripStockLoad                           = new Trip_Stock_Load(this);
			tripStockReturn                         = new Trip_Stock_Return(this);
			tripTransportDoc                        = new Trip_Transport_Doc(this);
			tripUndeliveredList                     = new Trip_Undelivered_List(this);
			tripUndeliveredSummary                  = new Trip_Undelivered_Summary(this);
			tripUndeliveredCOD                      = new Trip_Undelivered_COD(this);
			tripUndeliveredProducts                 = new Trip_Undelivered_Products(this);
			tripUndeliveredLineChange               = new Trip_Undelivered_Line_Change(this);
			tripUndeliveredMeterMate                = new Trip_Undelivered_MeterMate(this);
			tripUndeliveredNonMetered               = new Trip_Undelivered_NonMetered(this);
			tripUndeliveredLineChangeDuringDelivery = new Trip_Undelivered_Line_Change_DuringDelivery(this);
			tripUndeliveredDeliveryNote             = new Trip_Undelivered_Delivery_Note(this);
			tripUndeliveredTicket                   = new Trip_Undelivered_Ticket(this);
			tripStockEnd                            = new Trip_Stock_End(this);
			tripReport                              = new Trip_Report(this);
			
			vf = (ViewFlipper) findViewById(R.id.trip_flipper);
			vf.removeAllViews();
			vf.addView(tripStockStart);					            // Index 0
			vf.addView(tripStockLoad);                              // Index 1
			vf.addView(tripStockReturn);                            // Index 2
			vf.addView(tripTransportDoc);				            // Index 3
			vf.addView(tripUndeliveredList);                        // Index 4
			vf.addView(tripUndeliveredSummary);                     // Index 5
			vf.addView(tripUndeliveredCOD);                         // Index 6
			vf.addView(tripUndeliveredProducts);		            // Index 7
			vf.addView(tripUndeliveredLineChange);                  // Index 8
			vf.addView(tripUndeliveredMeterMate);                   // Index 9
			vf.addView(tripUndeliveredNonMetered);		            // Index 10
			vf.addView(tripUndeliveredLineChangeDuringDelivery);    // Index 11
			vf.addView(tripUndeliveredDeliveryNote);                // Index 12
			vf.addView(tripUndeliveredTicket);                      // Index 13
			vf.addView(tripStockEnd);                               // Index 14
			vf.addView(tripReport);                                 // Index 15
			
			// Select the initial view.
			selectView(Trip.ViewStockStart, 0);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	protected void onResume() 
	{
    	super.onResume();
        
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: onResume");
			
			// Update Active.activity
			Active.activity = this;
			
			// Create IntentFilter for broadcast we are interested in.
			IntentFilter filter = new IntentFilter();
			filter.addAction(ColossusIntentService.BroadcastTripsChanged);
	
			// Create the BroadcastReceiver.
			receiver = new myReceiver();
			
			// Register BroadcastReceiver.
			registerReceiver(receiver, filter);
	
			// Refresh the list of orders.
			if (tripUndeliveredList != null)
			{
				tripUndeliveredList.updateUI();
			}
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
			CrashReporter.leaveBreadcrumb("Trip: onPause");
			
			// Unregister BroadcastReceiver.
			unregisterReceiver(receiver);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    // Prevent Back button.
    @Override
    public void onBackPressed()
	{
	}

    // Change the current view.
    public void selectView(String newName, int direction)
    {
    	try
    	{
	    	// Setup animation.
			if (direction < 0)
			{
	    		vf.setInAnimation(AnimationHelper.inFromLeftAnimation());
	    		vf.setOutAnimation(AnimationHelper.outToRightAnimation());
			}
			
			if (direction > 0)
			{
	    		vf.setInAnimation(AnimationHelper.inFromRightAnimation());
	    		vf.setOutAnimation(AnimationHelper.outToLeftAnimation());
			}
				
			MyFlipperView newView = null;	
			int newIdx = -1;
			
			// Switch to specified view.
			if (newName.equals(ViewStockStart))
			{
				newView = tripStockStart;
				newIdx = ViewStockStartIdx;
			}
			
			if (newName.equals(ViewStockLoad))
			{
				newView = tripStockLoad;
				newIdx = ViewStockLoadIdx;
			}
			
			if (newName.equals(ViewStockReturn))
			{
				newView = tripStockReturn;
				newIdx = ViewStockReturnIdx;
			}
			
			if (newName.equals(ViewTransportDoc))
			{
				newView = tripTransportDoc;
				newIdx = ViewTransportDocIdx;
			}
			
			if (newName.equals(ViewUndeliveredList))
			{
				newView = tripUndeliveredList;
				newIdx = ViewUndeliveredListIdx;
			}
			
			if (newName.equals(ViewUndeliveredSummary))
			{
				newView = tripUndeliveredSummary;
				newIdx = ViewUndeliveredSummaryIdx;
			}
			
			if (newName.equals(ViewUndeliveredCOD))
			{
				newView = tripUndeliveredCOD;
				newIdx = ViewUndeliveredCODIdx;
			}
			
			if (newName.equals(ViewUndeliveredProducts))
			{
				newView = tripUndeliveredProducts;
				newIdx = ViewUndeliveredProductsIdx;
			}
	
			if (newName.equals(ViewUndeliveredLineChange))
			{
				newView = tripUndeliveredLineChange;
				newIdx = ViewUndeliveredLineChangeIdx;
			}
			
			if (newName.equals(ViewUndeliveredMeterMate))
			{
				newView = tripUndeliveredMeterMate;
				newIdx = ViewUndeliveredMeterMateIdx;
			}
			
			if (newName.equals(ViewUndeliveredNonMetered))
			{
				newView = tripUndeliveredNonMetered;
				newIdx = ViewUndeliveredNonMeteredIdx;
			}
			
			if (newName.equals(ViewUndeliveredLineChangeDuringDelivery))
			{
				newView = tripUndeliveredLineChangeDuringDelivery;
				newIdx = ViewUndeliveredLineChangeDuringDeliveryIdx;
			}
	
			if (newName.equals(ViewUndeliveredDeliveryNote))
			{
				newView = tripUndeliveredDeliveryNote;
				newIdx = ViewUndeliveredDeliveryNoteIdx;
			}
			
			if (newName.equals(ViewUndeliveredTicket))
			{
				newView = tripUndeliveredTicket;
				newIdx = ViewUndeliveredTicketIdx;
			}
			
			if (newName.equals(ViewStockEnd))
			{
				newView = tripStockEnd;
				newIdx = ViewStockEndIdx;
			}
			
			if (newName.equals(ViewTripReport))
			{
				newView = tripReport;
				newIdx = ViewTripReportIdx;
			}
			
			// Switch to new view.
			if (newView != null)
			{
				// Pause currentView.
				if (currentView != null)
				{
					currentView.pauseView();
				}
				
				// Initialise the view.
				if (newView.resumeView())
				{
					if (direction > 0)
					{
						newView.setPreviousView(currentViewName);
					}
					
					newView.updateUI();
					vf.setDisplayedChild(newIdx);
	
					currentView = newView;
					currentViewName = newName;
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    // Start the settings activity.
    public void changeSettings()
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: changeSettings");
			
    		// Show settings activity.
			Intent i = new Intent(this, Settings.class);

			startActivity(i);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }

    public void setMeterMateCallbacks(Trip_MeterMate_Callbacks callbacks)
    {
    	tripUndeliveredMeterMate.callbacks = callbacks;
    }
    
    // --- Amend price (this is a temporary manual process) ---
    
	public void amendPrice()
	{
		final Dialog dialog;
		final TextView tvOrderedQty;
		final TextView tvOrderedPrice;
		final TextView tvOrderedSurcharge;
		final TextView tvDeliveredQty;
		final TextView tvDeliveredPrice;
		final TextView tvDeliveredSurcharge;
		final MyEditText etNewPrice;
		final Button ok_button;
		final Button cancel_button;
		final MyNumericKeypad keypad;

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: amendPrice");

			// Create custom dialog.
			dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
			dialog.setContentView(R.layout.trip_undelivered_delivery_note_amendprice);
	
			tvOrderedQty = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_orderedqty);
			tvOrderedPrice = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_orderedprice);
			tvOrderedSurcharge = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_orderedsurcharge);
			tvDeliveredQty = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_deliveredqty);
			tvDeliveredPrice = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_deliveredprice);
			tvDeliveredSurcharge = (TextView)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_deliveredsurcharge);
			etNewPrice = (MyEditText)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_newprice);
			ok_button = (Button)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_ok);
			cancel_button = (Button)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_cancel);
			keypad = (MyNumericKeypad)dialog.findViewById(R.id.trip_undelivered_delivery_note_amendprice_keypad);
			keypad.setDialog(dialog);
	
			// Set dialog width & height to 'Fill parent'.
	        dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT); 
	
	        // Update 'ToPay' as text changes.
	        TextWatcher tw = new TextWatcher()
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
					dbTripOrderLine orderLine = Active.orderLine;

					// Update UI
					tvOrderedQty.setText(Integer.toString(orderLine.OrderedQty));
					tvOrderedPrice.setText(decf2.format(orderLine.OrderedPrice));
					tvOrderedSurcharge.setText(decf2.format(orderLine.Surcharge));
					
					tvDeliveredQty.setText(Integer.toString(orderLine.DeliveredQty));
					
					double newPrice = getEditTextAmount(etNewPrice);

					tvDeliveredPrice.setText(newPrice == 0 ? "" : decf2.format(newPrice));
					
					tvDeliveredSurcharge.setText(decf2.format(orderLine.Surcharge));
				}
			};
	
			etNewPrice.addTextChangedListener(tw);
			
			// Format values to 2 dec places when focus leaves.
			OnFocusChangeListener fcl = new OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (!hasFocus)
					{
						EditText et = (EditText)v;
						double value = getEditTextAmount(et);

						et.setText(value == 0 ? "" : decf2.format(value));
					}
				}
			};
			
			etNewPrice.setOnFocusChangeListener(fcl);
	
			// OK click event.
			ok_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					// Update price.
					double newPrice = getEditTextAmount(etNewPrice);
					
					Active.orderLine.setDeliveredPricePrice(newPrice);
					
					// Update UI.
					currentView.updateUI();
				
					dialog.dismiss();
				}
			});
	
			// Cancel click event.
			cancel_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					dialog.dismiss();
				}
			});
	
			// Initialise dialog - triggers onTextChanged.
			if (Active.orderLine.DeliveredPrice == 0)
			{
				etNewPrice.setText("");
			}
			else
			{
				etNewPrice.setText("" + Active.orderLine.DeliveredPrice);
				etNewPrice.setSelection(etNewPrice.getText().length(), etNewPrice.getText().length());
			}
			
			dialog.show();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// --- Accept payment ---
    
	public void acceptPayment(boolean beforeDelivery)
	{
		final Dialog dialog;
		final MyEditText etCash;
		final MyEditText etCheque;
		final MyEditText etVoucher;
		final TextView tvPaidDriver;
		final TableRow trPaidOffice;
		final TextView tvPaidOffice;
		final TextView tvMessage;
		final TextView tvTerms;
		final Button ok_button;
		final Button cancel_button;
		final MyNumericKeypad keypad;

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: acceptPayment");

			// Create custom dialog.
			dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
			dialog.setContentView(R.layout.trip_undelivered_payment);
	
			etCash = (MyEditText)dialog.findViewById(R.id.trip_undelivered_payment_cash_received);
			etCheque = (MyEditText)dialog.findViewById(R.id.trip_undelivered_payment_cheque_received);
			etVoucher = (MyEditText)dialog.findViewById(R.id.trip_undelivered_payment_voucher_received);
			tvPaidDriver = (TextView)dialog.findViewById(R.id.trip_undelivered_payment_paid_driver);
			trPaidOffice = (TableRow)dialog.findViewById(R.id.trip_undelivered_payment_paid_office_row);
			tvPaidOffice = (TextView)dialog.findViewById(R.id.trip_undelivered_payment_paid_office);
			tvMessage = (TextView)dialog.findViewById(R.id.trip_undelivered_payment_message);
			tvTerms = (TextView)dialog.findViewById(R.id.trip_undelivered_payment_terms);
			ok_button = (Button)dialog.findViewById(R.id.trip_undelivered_payment_ok);
			cancel_button = (Button)dialog.findViewById(R.id.trip_undelivered_payment_cancel);
			keypad = (MyNumericKeypad)dialog.findViewById(R.id.trip_undelivered_payment_keypad);
			keypad.setDialog(dialog);
	
			// Set dialog width & height to 'Fill parent'.
	        dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT); 
	
	        // Update 'ToPay' as text changes.
	        TextWatcher tw = new TextWatcher()
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
					// Update total paid to driver.
					tvPaidDriver.setText(decf2.format(getEditTextAmount(etCash) + getEditTextAmount(etCheque) + getEditTextAmount(etVoucher)));
				}
			};
	
			// Format values to 2 dec places when focus leaves.
			etCash.addTextChangedListener(tw);
			etCheque.addTextChangedListener(tw);
			etVoucher.addTextChangedListener(tw);
			
			OnFocusChangeListener fcl = new OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View v, boolean hasFocus)
				{
					if (!hasFocus)
					{
						EditText et = (EditText)v;
						double value = getEditTextAmount(et);

						et.setText(value == 0 ? "" : decf2.format(value));
					}
				}
			};
			
			etCash.setOnFocusChangeListener(fcl);
			etCheque.setOnFocusChangeListener(fcl);
			etVoucher.setOnFocusChangeListener(fcl);
	
			// OK click event.
			ok_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					// Add payment to order.
					Active.order.CashReceived = getEditTextAmount(etCash); 
					Active.order.ChequeReceived = getEditTextAmount(etCheque);
					Active.order.VoucherReceived = getEditTextAmount(etVoucher);
					Active.order.calculateDiscount();
					Active.order.save();
					
					// Update UI.
					currentView.updateUI();
					
					dialog.dismiss();
				}
			});
	
			// Cancel click event.
			cancel_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					dialog.dismiss();
				}
			});
	
			// Initialise dialog - triggers onTextChanged.
			etCash.setText("");
			etCheque.setText("");
			etVoucher.setText("");
			
			// Show current values.
			if (Active.order.CashReceived != 0)
			{
				etCash.setText(decf2.format(Active.order.CashReceived));
			}
			
			if (Active.order.ChequeReceived != 0)
			{
				etCheque.setText(decf2.format(Active.order.ChequeReceived));
			}
			
			if (Active.order.VoucherReceived != 0)
			{
				etVoucher.setText(decf2.format(Active.order.VoucherReceived));
			}
	
			if (Active.order.PrepaidAmount != 0)
			{
				trPaidOffice.setVisibility(View.VISIBLE);
				tvPaidOffice.setText(decf2.format(Active.order.PrepaidAmount));
			}
			else
			{
				trPaidOffice.setVisibility(View.GONE);
			}
			
			double nett = Active.order.getDeliveredNettValue();
			double vat = Active.order.getDeliveredVatValue();
			double surcharge = Active.order.getDeliveredSurchargeValue();
			double accBalance = Active.order.getCodAccBalance();
			double cashTotal = (double) Math.round((nett + vat + accBalance) * 100) / 100;
			double paidOffice = Active.order.PrepaidAmount;
			double payDriver = cashTotal - paidOffice;
			double surchargeVatAmount = Active.order.getSurchargeVat();
			
			if (beforeDelivery)
			{
				payDriver = Active.order.getCodBeforeDeliveryValue();
			}
			
			String message = "Pay driver " + decf2.format(payDriver - surchargeVatAmount);

			if (surcharge != 0)
			{
				message += " for cash discount of " + decf2.format(surcharge + surchargeVatAmount);
			}
			
			tvMessage.setText(message);
			tvTerms.setText("Terms: " + Active.order.getTerms());
	
			dialog.show();
			
			// Default to cash.
			etCash.setSelection(etCash.getText().length(), etCash.getText().length());
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private double getEditTextAmount(EditText amount)
	{
		double value = 0;
		
		// Convert text to value.
		try
		{
			value = Double.parseDouble(amount.getText().toString());
			value = Utils.Truncate(value, 2);
		}
		catch (Exception e)
		{
			// We don't want to CrashReport this exception.
		}

		return value;
	}

	// --- Capture name & signature ---
	
	public void captureCustomersName()
	{
		final Dialog dialog;
		final EditText name;
		final Button ok_button;
		final Button cancel_button;

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: captureCustomersName");

			// Create custom dialog.
			dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
			dialog.setContentView(R.layout.signature_name);
	
			name = (EditText)dialog.findViewById(R.id.signature_name_name);
			ok_button = (Button)dialog.findViewById(R.id.signature_name_ok);
			cancel_button = (Button)dialog.findViewById(R.id.signature_name_cancel);
	
			// Set dialog width & height to 'Fill parent'.
	        dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT); 
	        
			name.setOnKeyListener(new View.OnKeyListener()
			{
				@Override
				public boolean onKey(View paramView, int paramInt, KeyEvent paramKeyEvent)
				{
					// Prevent 'DONE' key hiding keyboard.
					// Instead simulate a click on the 'OK' button.
					if ((paramKeyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (paramKeyEvent.getAction() == KeyEvent.ACTION_UP))
					{
						ok_button.performClick();

						return true;
					}

					return false;
				}
			});
			
			name.addTextChangedListener(new TextWatcher() 
			{
				@Override
				public void afterTextChanged(Editable s)
				{
				}
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
				}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{
					// Enable/disable OK button.
					ok_button.setEnabled(name.getText().toString().trim().length() > 0);
				}
			});
			
			ok_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					// Capture customer signature.
					captureSignature("Customer", name.getText().toString());
					
					dialog.dismiss();
				}
			});
	
			cancel_button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View paramView)
				{
					dialog.dismiss();
				}
			});
			
			dialog.show();
	
			// Start with 'OK' button disabled.
			ok_button.setEnabled(false);
	
			// Show soft keyboard.
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			Utils.ShowKeyboard(name);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void captureSignature(String type, String name)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: captureSignature (" + type + ")");

			// Proceed to customer signature activity.
			Intent intent = new Intent(this, Signature.class);

			intent.putExtra("SignatureType", type);
			intent.putExtra("SignatureName", name);

	    	startActivityForResult(intent, Signature.REQUEST_SIGNATURE);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: onActivityResult");
			
			if (requestCode == Signature.REQUEST_SIGNATURE)
			{
				if (resultCode == RESULT_OK)
				{
					String signatureType = data.getExtras().getString("SignatureType");
					String signatureName = data.getExtras().getString("SignatureName");
					byte[] signatureImage = data.getExtras().getByteArray("SignatureImage");
	
					// Store customer signature.
					if (signatureType.equals("Customer"))
					{
						Log.d("Signature", "Got customer sig");
						
						Active.order.CustomerSignature = true;
						Active.order.CustomerSignatureName = signatureName;
						Active.order.CustomerSignatureImage = signatureImage;
						Active.order.CustomerSignatureDateTime = new Date().getTime();
						Active.order.save();
						
						// Update UI with signature.
						tripUndeliveredDeliveryNote.updateUI();
					}
	
					// Store driver signature.
					if (signatureType.equals("Driver"))
					{
						Log.d("Signature", "Got driver sig");
						
						Active.order.DriverSignature = true;
						Active.order.DriverSignatureName = signatureName;
						Active.order.DriverSignatureImage = signatureImage;
						Active.order.DriverSignatureDateTime = new Date().getTime();
						Active.order.save();
						
						// Try finishing the order again.
						tripUndeliveredDeliveryNote.finishOrder();
					}
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// Start the Active order.
	public void orderStarted()
	{
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: orderStarted");

    		// Create content.
			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
			json.put("OrderID", Active.order.ColossusID);
	
			// Call ColossusIntentService.
			addIntent("Order_Started", json.toString());

    		// Update database.
    		Active.order.start();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	// User has backed out of the order.
	public void orderStopped()
	{
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: orderStopped");

    		// Create content.
			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
			json.put("OrderID", Active.order.ColossusID);
	
			// Call ColossusIntentService.
			addIntent("Order_Stopped", json.toString());

    		// Update database.
    		Active.order.stop();
    		Active.order = null;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private static String encodeSignature(boolean signaturePresent, byte[] signatureImage)
	{
		if (signaturePresent)
		{
			return Base64.encodeToString(signatureImage, Base64.DEFAULT);
		}

		return null;
	}
	
	// User has finished the order.
	public void orderDelivered()
	{
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: orderDelivered");

    		// Update database.
			Active.vehicle.recordPayments();
			Active.order.delivered();
			
			// Prepare data for Colossus.
			JSONArray lines = new JSONArray();

			for (dbTripOrderLine line : Active.order.GetTripOrderLines())
			{
				JSONObject jsonLine = new JSONObject();
				jsonLine.put("OrderLineID", line.ColossusID);
				jsonLine.put("DeliveredQty", line.DeliveredQty);
				jsonLine.put("SellingPrice", line.DeliveredPrice);
				
				lines.put(jsonLine);
			}

//			JSONArray customerSig = null;
//
//			if (Active.order.CustomerSignature)
//			{
//				customerSig = new JSONArray();
//
//				int signatureImageLength = Active.order.CustomerSignatureImage.length;
//
//				for (int i = 0; i < signatureImageLength; i++)
//				{
//					customerSig.put(Active.order.CustomerSignatureImage[i] & 0xff);
//				}
//			}
//
//			JSONArray driverSig = null;
//
//			if (Active.order.DriverSignature)
//			{
//				driverSig = new JSONArray();
//
//				int signatureImageLength = Active.order.DriverSignatureImage.length;
//
//				for (int i = 0; i < signatureImageLength; i++)
//				{
//					driverSig.put(Active.order.DriverSignatureImage[i] & 0xff);
//				}
//			}

			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
			json.put("OrderID", Active.order.ColossusID);
			json.put("DeliveryDate", "/Date(" + Active.order.DeliveryDate + ")/");
			json.put("CustomerSignature", Active.order.CustomerSignature);
			json.put("CustomerSignatureName", Active.order.CustomerSignatureName);
			json.put("CustomerSignatureImage", encodeSignature(Active.order.CustomerSignature, Active.order.CustomerSignatureImage)); // customerSig);
			json.put("CustomerSignatureDateTime", "/Date(" + Active.order.CustomerSignatureDateTime + ")/");
			json.put("CashReceived", Active.order.CashReceived);
			json.put("ChequeReceived", Active.order.ChequeReceived);
			json.put("VoucherReceived", Active.order.VoucherReceived);
			json.put("Discount", Active.order.Discount);
			json.put("DriverSignature", Active.order.DriverSignature);
			json.put("DriverSignatureName", Active.order.DriverSignatureName);
			json.put("DriverSignatureImage", encodeSignature(Active.order.DriverSignature, Active.order.DriverSignatureImage)); // driverSig);
			json.put("DriverSignatureDateTime", "/Date(" + Active.order.DriverSignatureDateTime + ")/");
			json.put("Lines", lines);

			// Call ColossusIntentService.
			addIntent("Order_Delivered", json.toString());

    		// Clear reference to order.
    		Active.order = null;
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
    // User has backed out of the trip.
    public void tripStopped()
    {
    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: tripStopped");

	    	// Send 'Trip_Stopped'
			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
	
			// Call ColossusIntentService.
			addIntent("Trip_Stopped", json.toString());

    		// Update database.
    		Active.trip.stop();
    		Active.trip = null;

    		// Pause currentView.
    		if (currentView != null)
			{
				currentView.pauseView();
			}
    		
			// Close Trip activity.
	    	finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }

    // User has finished the trip.
    public void tripDelivered()
    {
		// This needs some improving!!!
		// Perhap the trip, order etc should now be deleted.

    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: tripDelivered");

    		// Create content.
			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
	
			// Call ColossusIntentService.
			addIntent("Trip_Delivered", json.toString());

	    	// Delete any zero vehicle stock records.
	    	dbVehicleStock.RemoveZeroProducts(Active.vehicle);
	    	
			// Mark trip as delivered.
	    	Active.trip.delivered();
	    	
	    	// Clear reference to trip.
			Active.trip = null;
	    	
			// Pause currentView.
			if (currentView != null)
			{
				currentView.pauseView();
			}

			// Finish the Activity.
			finish();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
	// --- Show delivered orders ---
    
	public void showDelivered()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: showDelivered");

    		// Create content.
			// Show delivered dialog.
			deliveredDialog = new Trip_Delivered(this);
			deliveredDialog.show();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void hideDelivered()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: hideDelivered");

    		// Create content.
			// Hide delivered dialog.
			if (deliveredDialog != null)
			{
				deliveredDialog.dismiss();
				deliveredDialog = null;
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	// --- Update stock on server ---
	
	public void sendVehicleStock()
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: sendVehicleStock");

			// Call ColossusIntentService.
			addIntent("Stock", Active.vehicle.buildStock());
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private void addIntent(String type, String content)
	{
		Intent i = new Intent(getApplicationContext(), ColossusIntentService.class);

		i.putExtra("Type", type);
		i.putExtra("Content", content);

		startService(i);
	}
}