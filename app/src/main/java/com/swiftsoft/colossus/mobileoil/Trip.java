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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.swiftsoft.colossus.mobileoil.bluetooth.Bluetooth;
import com.swiftsoft.colossus.mobileoil.database.model.dbEndOfDay;
import com.swiftsoft.colossus.mobileoil.database.model.dbProduct;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripStock;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicle;
import com.swiftsoft.colossus.mobileoil.database.model.dbVehicleStock;
import com.swiftsoft.colossus.mobileoil.service.ColossusIntentService;
import com.swiftsoft.colossus.mobileoil.view.MyEditText;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyNumericKeypad;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
	public static final String ViewUndeliveredSkip                     = "Trip.UndeliveredSkip";

	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewStockStartIdx                          = 0;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewStockLoadIdx                           = 1;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewStockReturnIdx                         = 2;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewTransportDocIdx                        = 3;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredListIdx                     = 4;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredSummaryIdx                  = 5;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredCODIdx                      = 6;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredProductsIdx                 = 7;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredLineChangeIdx               = 8;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredMeterMateIdx                = 9;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredNonMeteredIdx               = 10;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredLineChangeDuringDeliveryIdx = 11;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredDeliveryNoteIdx             = 12;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredTicketIdx                   = 13;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewStockEndIdx                            = 14;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewTripReportIdx                          = 15;
	@SuppressWarnings("FieldCanBeLocal")
	private final int ViewUndeliveredSkipIdx                     = 16;
	
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
	private Trip_Undelivered_Skip                       tripUndeliveredSkip;

	private ViewFlipper vf;
	private String currentViewName;
	private MyFlipperView currentView;
	private DecimalFormat formatMoney;
	private Trip_Delivered deliveredDialog;
	private myReceiver receiver;

	public long OrderId;
	
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
			formatMoney = new DecimalFormat("#,##0.00");
	
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
            tripUndeliveredSkip                     = new Trip_Undelivered_Skip(this);
			
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
            vf.addView(tripUndeliveredSkip);                        // Index 16
			
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

            if (newName.equals(ViewUndeliveredSkip))
            {
                newView = tripUndeliveredSkip;
                newIdx = ViewUndeliveredSkipIdx;
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
					tvOrderedQty.setText(String.format("%d", orderLine.OrderedQty));
					tvOrderedPrice.setText(formatMoney.format(orderLine.getOrderedPrice()));
					tvOrderedSurcharge.setText(formatMoney.format(orderLine.getSurcharge()));
					
					tvDeliveredQty.setText(String.format("%d", orderLine.DeliveredQty));
					
					BigDecimal newPrice = getEditTextAmount(etNewPrice);

					tvDeliveredPrice.setText(newPrice.compareTo(BigDecimal.ZERO) == 0 ? "" : formatMoney.format(newPrice));
					
					tvDeliveredSurcharge.setText(formatMoney.format(orderLine.getSurcharge()));
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

						BigDecimal value = getEditTextAmount(et);

						et.setText(value.compareTo(BigDecimal.ZERO) == 0 ? "" : formatMoney.format(value));
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
					BigDecimal newPrice = getEditTextAmount(etNewPrice);
					
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
			if (Active.orderLine.getDeliveredPrice().compareTo(BigDecimal.ZERO) == 0)
			{
				etNewPrice.setText("");
			}
			else
			{
				etNewPrice.setText(String.format("%f", Active.orderLine.getDeliveredPrice()));
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
					tvPaidDriver.setText(formatMoney.format(getEditTextAmount(etCash).add(getEditTextAmount(etCheque)).add(getEditTextAmount(etVoucher))));
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

						BigDecimal value = getEditTextAmount(et);

						et.setText(value.compareTo(BigDecimal.ZERO) == 0 ? "" : formatMoney.format(value));
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
					Active.order.setCashReceived(getEditTextAmount(etCash));
					Active.order.setChequeReceived(getEditTextAmount(etCheque));
					Active.order.setVoucherReceived(getEditTextAmount(etVoucher));
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
			if (Active.order.getCashReceived().compareTo(BigDecimal.ZERO) != 0)
			{
				etCash.setText(formatMoney.format(Active.order.getCashReceived()));
			}
			
			if (Active.order.getChequeReceived().compareTo(BigDecimal.ZERO) != 0)
			{
				etCheque.setText(formatMoney.format(Active.order.getChequeReceived()));
			}
			
			if (Active.order.getVoucherReceived().compareTo(BigDecimal.ZERO) != 0)
			{
				etVoucher.setText(formatMoney.format(Active.order.getVoucherReceived()));
			}
	
			if (Active.order.getPrepaidAmount().compareTo(BigDecimal.ZERO) != 0)
			{
				trPaidOffice.setVisibility(View.VISIBLE);
				tvPaidOffice.setText(formatMoney.format(Active.order.getPrepaidAmount()));
			}
			else
			{
				trPaidOffice.setVisibility(View.GONE);
			}
			
			BigDecimal nett = Active.order.getDeliveredNettValue();
			BigDecimal vat = Active.order.getDeliveredVatValue();
			BigDecimal surcharge = Active.order.getDeliveredSurchargeValue();
			BigDecimal accBalance = Active.order.getCodAccBalance();

			BigDecimal cashTotal = nett.add(vat).add(accBalance);

			BigDecimal paidOffice = Active.order.getPrepaidAmount();
			BigDecimal payDriver = cashTotal.subtract(paidOffice);
			BigDecimal surchargeVatAmount = Active.order.getSurchargeVat();
			
			if (beforeDelivery)
			{
				payDriver = Active.order.getCodBeforeDeliveryValue();
			}
			
			String message = "Pay driver " + formatMoney.format(payDriver.subtract(surchargeVatAmount));

			if (surcharge.compareTo(BigDecimal.ZERO) != 0)
			{
				message += " for cash discount of " + formatMoney.format(surcharge.add(surchargeVatAmount));
			}
			
			tvMessage.setText(message);

            tvMessage.setVisibility(Active.order.HidePrices ? View.GONE : View.VISIBLE);

			tvTerms.setText(String.format("Terms: %s", Active.order.getTerms()));
	
			dialog.show();
			
			// Default to cash.
			etCash.setSelection(etCash.getText().length(), etCash.getText().length());
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	private BigDecimal getEditTextAmount(EditText amount)
	{
		BigDecimal value = BigDecimal.ZERO;
		
		// Convert text to value.
		try
		{
			value = new BigDecimal(amount.getText().toString());

			value = Utils.truncate(value, 2);
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

		final CheckBox unattendedDelivery;

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
            unattendedDelivery = (CheckBox)dialog.findViewById(R.id.signature_name_unattended);
	
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
					captureSignature("Customer", name.getText().toString(), unattendedDelivery.isChecked());
					
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
			Utils.showKeyboard(name);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

    public void captureSignature(String type, String name)
    {
        captureSignature(type, name, false);
    }
	
	public void captureSignature(String type, String name, boolean isChecked)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: captureSignature (" + type + ")");

			// Proceed to customer signature activity.
			Intent intent = new Intent(this, Signature.class);

			intent.putExtra("SignatureType", type);
			intent.putExtra("SignatureName", name);
            intent.putExtra("SignatureUnattended", isChecked);

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
                    boolean unattendedDelivery = data.getExtras().getBoolean("SignatureUnattended");
	
					// Store customer signature.
					if (signatureType.equals("Customer"))
					{
						Log.d("Signature", "Got customer sig");
						
						Active.order.CustomerSignature = true;
						Active.order.CustomerSignatureName = signatureName;
						Active.order.CustomerSignatureImage = signatureImage;
						Active.order.CustomerSignatureDateTime = Utils.getCurrentTime();
                        Active.order.UnattendedSignature = unattendedDelivery;
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
						Active.order.DriverSignatureDateTime = Utils.getCurrentTime();
                        Active.order.UnattendedSignature = unattendedDelivery;
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

    public void orderSkipped(int nonDeliveryReason, String customReason)
    {
        try
        {
            CrashReporter.leaveBreadcrumb("Trip: orderSkipped");

            // Send message to MobileIn showing that order is undeliverable

            JSONObject json = new JSONObject();

            json.put("TripID", Active.trip.ColossusID);
            json.put("OrderID", Active.order.ColossusID);
            json.put("Reason", nonDeliveryReason);

            if (nonDeliveryReason == 6)
            {
                json.put("CustomReason", customReason);
            }

            addIntent("Order_Undelivered", json.toString());

            // Delete all order lines
            for (dbTripOrderLine line : Active.order.GetTripOrderLines())
            {
                // Delete order line.
                line.delete();
            }

            // Delete order from local db
            Active.order.delete();
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

			JSONObject json = new JSONObject();
			json.put("TripID", Active.trip.ColossusID);
			json.put("OrderID", Active.order.ColossusID);
			json.put("DeliveryDate", "/Date(" + Active.order.DeliveryDate + ")/");
			json.put("CustomerSignature", Active.order.CustomerSignature);
			json.put("CustomerSignatureName", Active.order.CustomerSignatureName);
			json.put("CustomerSignatureImage", encodeSignature(Active.order.CustomerSignature, Active.order.CustomerSignatureImage)); // customerSig);
			json.put("CustomerSignatureDateTime", "/Date(" + Active.order.CustomerSignatureDateTime + ")/");
			json.put("CashReceived", Active.order.getCashReceived());
			json.put("ChequeReceived", Active.order.getChequeReceived());
			json.put("VoucherReceived", Active.order.getVoucherReceived());
			json.put("Discount", Active.order.getDiscount());
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

	private void tripComplete()
	{
        CrashReporter.leaveBreadcrumb("Trip: tripComplete");

        // Get the list of vehicle stocks
        List<dbVehicleStock> vehicleStocks = dbVehicleStock.GetStockByProduct(dbVehicle.FindByNo(Active.trip.Vehicle.No));

        // Get the list of stock transactions
        List<dbTripStock> transactions = Active.trip.GetStockTrans();

        // Get the list of products that are on lorry
        List<dbProduct> products = getUniqueProducts(transactions, vehicleStocks);

        // Store the start, load, deliver, return & finish quantities
        for (dbProduct product : products)
        {
            // Starting quantities
            dbEndOfDay eod = new dbEndOfDay();

            eod.Type = "Start";
            eod.TripId = Active.trip.No;
            eod.Product = product;
            eod.Quantity = getStartingVolume(vehicleStocks, product);

            eod.save();

            // Loaded quantities
            eod = new dbEndOfDay();

            eod.Type = "Load";
            eod.TripId = Active.trip.No;
            eod.Product = product;
            eod.Quantity = getLoadedVolume(transactions, product);

            eod.save();

            // Delivered quantities
            eod = new dbEndOfDay();

            eod.Type = "Deliver";
            eod.TripId = Active.trip.No;
            eod.Product = product;
            eod.Quantity = getDeliveredVolume(transactions, product);

            eod.save();

            // Returned quantities
            eod = new dbEndOfDay();

            eod.Type = "Return";
            eod.TripId = Active.trip.No;
            eod.Product = product;
            eod.Quantity = getReturnedVolume(transactions, product);

            eod.save();

            // Finishing quantities
            eod = new dbEndOfDay();

            eod.Type = "Finish";
            eod.TripId = Active.trip.No;
            eod.Product = product;
            eod.Quantity = getFinishingVolume(vehicleStocks, product);

            eod.save();
        }

        // Now we need to save payment details for the trip ...
		for (dbTripStock transaction : transactions)
		{
			if (transaction.Type.equals("Payment"))
            {
				// Get all payment information in the transaction
				String[] paymentLines = (transaction.Description + "\n" + transaction.Notes).split("\n");

				for (String paymentLine : paymentLines)
				{
                    if (isCashChequeVoucherPayment(paymentLine))
                    {
                        int idx = paymentLine.indexOf(":") + 1;

                        // Remove all commas from the value field so that parsing does not fail
                        String strValue = paymentLine.substring(idx).replace(",", "").trim();

                        dbEndOfDay eod = new dbEndOfDay();

                        if (paymentLine.startsWith("Cash payment:"))
                        {
                            eod.Type = "Payment_Cash";
                        }

                        if (paymentLine.startsWith("Cheque payment:"))
                        {
                            eod.Type = "Payment_Cheque";
                        }

                        if (paymentLine.startsWith("Voucher payment:"))
                        {
                            eod.Type = "Payment_Voucher";
                        }

                        eod.TripId = Active.trip.No;
                        eod.Product = null;
                        BigDecimal amount = new BigDecimal(strValue);
                        eod.setValue(amount);
                        eod.Quantity = 0;

                        eod.save();
                    }
				}
            }
		}
    }

    private boolean isCashChequeVoucherPayment(String line)
    {
        if (line.startsWith("Cash payment:") || line.startsWith("Cheque payment:") || line.startsWith("Voucher payment:"))
        {
            return true;
        }

        return false;
    }

    private static int getDeliveredVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Trip: getDeliveredVolume");

        int quantity = 0;

        // Loop through all 'Delivery' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Delivery"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static int getReturnedVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Trip: getReturnedVolume");

        int quantity = 0;

        // Loop through all 'Return' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Return"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static int getLoadedVolume(List<dbTripStock> stockTransactions, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Trip: getLoadedVolume");

        int quantity = 0;

        // Loop through all 'Load' transactions calculating the total for the product
        for (dbTripStock stockTransaction : stockTransactions)
        {
            if (stockTransaction.Type.equals("Load"))
            {
                if (stockTransaction.Product.ColossusID == product.ColossusID)
                {
                    quantity += stockTransaction.Quantity;
                }
            }
        }

        return quantity;
    }

    private static int getFinishingVolume(List<dbVehicleStock> stockList, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Trip: getFinishingVolume");

        int quantity = 0;

        // Go through all stock until we find a matching product
        // and return the quantity
        for (dbVehicleStock stock : stockList)
        {
            if (stock != null && stock.Product.ColossusID == product.ColossusID)
            {
                quantity = stock.CurrentStock;

                CrashReporter.leaveBreadcrumb(String.format("Printing: getFinishingVolume - Product [%s] : %d litres", product.Desc, quantity));

                break;
            }
        }

        return quantity;
    }

    private static int getStartingVolume(List<dbVehicleStock> stockList, dbProduct product)
    {
        CrashReporter.leaveBreadcrumb("Trip: getStartingVolume");

        int quantity = 0;

        // Go through all stock until we find a matching product
        // and return the quantity
        for (dbVehicleStock stock : stockList)
        {
            if (stock != null && stock.Product.ColossusID == product.ColossusID)
            {
                quantity = stock.OpeningStock;

                CrashReporter.leaveBreadcrumb(String.format("Printing: getStartingVolume - Product [%s] : %d litres", product.Desc, quantity));

                break;
            }
        }

        return quantity;
    }

    private static List<dbProduct> getUniqueProducts(List<dbTripStock> transactions, List<dbVehicleStock> stocks)
    {
        CrashReporter.leaveBreadcrumb("Trip: getUniqueProducts");

        // Create object to hold list of unique product in stock & transactions
        // to be returned
        List<dbProduct> products =  new ArrayList<dbProduct>();

        for (dbTripStock transaction : transactions)
        {
            if (transaction.Type.equals("Load") || transaction.Type.equals("Return") || transaction.Type.equals("Delivery"))
            {
                if (!products.contains(transaction.Product))
                {
                    CrashReporter.leaveBreadcrumb(String.format("Printing: getUniqueProducts - Adding Product %s", transaction.Product.Desc));

                    products.add(transaction.Product);
                }
            }
        }

        for (dbVehicleStock stock : stocks)
        {
            if (!products.contains(stock.Product))
            {
                CrashReporter.leaveBreadcrumb(String.format("Printing: getUniqueProducts - Adding Product %s", stock.Product.Desc));

                products.add(stock.Product);
            }
        }

        return products;
    }


    // User has finished the trip.
    public void tripDelivered()
    {
		// This needs some improving!!!
		// Perhaps the trip, order etc should now be deleted.

    	try
    	{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip: tripDelivered");

            // Store details of the trip in dbEndOfDay
            tripComplete();

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