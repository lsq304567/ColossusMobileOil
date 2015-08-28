package com.swiftsoft.colossus.mobileoil;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

public class Trip_Undelivered_Delivery_Note extends MyFlipperView
{
	private Trip trip;
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private TableLayout tlTable; 
	private TextView tvVat;
	private TableRow trAccBalanceRow;
	private TextView tvAccBalance;
	private TextView tvTotal;
	private TableRow trPaidOfficeRow;
	private TextView tvPaidOffice;
	private TableRow trPaidDriverRow;
	private TextView tvPaidDriver;
	private TableRow trDiscountRow;
	private TextView tvDiscount;
	private TableRow trSubtotalRow;
	private TableRow trOutstandingRow;
	private TextView tvOutstanding;
	private LinearLayout llCashDiscountMsg;
	private TextView tvCashDiscountMsg1;
	private TextView tvCashDiscountMsg2;
	private TextView tvTerms;
	private TableLayout tlSignature;
	private ImageView ivSignatureImage;
	private TextView tvSignatureName;
	private TextView tvSignatureDateTime;
	private Button btnPayment;
	private Button btnSignature;
	private Button btnNext;

	private DecimalFormat decf2;

	public Trip_Undelivered_Delivery_Note(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_Delivery_Note(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_delivery_note, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_delivery_note_infoview);
			tlTable = (TableLayout)this.findViewById(R.id.trip_undelivered_delivery_note_table);
			tvVat = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_vat);
			trAccBalanceRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_acc_balance_row);
			tvAccBalance = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_acc_balance);
			tvTotal = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_total);
			trPaidOfficeRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_paid_office_row);
			tvPaidOffice = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_paid_office);
			trPaidDriverRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_paid_driver_row);
			tvPaidDriver = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_paid_driver);
			trPaidDriverRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_paid_driver_row);
			tvPaidDriver = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_paid_driver);
			trDiscountRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_discount_row);
			tvDiscount = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_discount);
			trSubtotalRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_subtotal_row);
			trOutstandingRow = (TableRow)this.findViewById(R.id.trip_undelivered_delivery_note_outstanding_row);
			tvOutstanding = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_outstanding);
	    	llCashDiscountMsg = (LinearLayout)this.findViewById(R.id.trip_undelivered_delivery_note_cash_discount_msg);
	    	tvCashDiscountMsg1 = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_cash_discount_msg1);
	    	tvCashDiscountMsg2 = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_cash_discount_msg2);
			tvTerms = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_terms);
			tlSignature = (TableLayout)this.findViewById(R.id.trip_undelivered_delivery_note_signature_table);
			ivSignatureImage = (ImageView)this.findViewById(R.id.trip_undelivered_delivery_note_signature_image);
			tvSignatureName = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_signature_name);
			tvSignatureDateTime = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_signature_datetime);
			btnPayment = (Button)this.findViewById(R.id.trip_undelivered_delivery_note_payment);
			btnSignature = (Button)this.findViewById(R.id.trip_undelivered_delivery_note_signature);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_delivery_note_next);
			
			btnPayment.setOnClickListener(onPayment);
			btnSignature.setOnClickListener(onSignature);
			btnNext.setOnClickListener(onNext);
			
			// Setup standard decimal format.
			decf2 = new DecimalFormat("#,##0.00");
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
			
			// Mark all non-deliverable products as delivered.
			for (dbTripOrderLine line : Active.order.GetTripOrderLines())
			{
				if (line.Product.MobileOil == 3)
				{
					line.delivered(line.OrderedQty);
				}
			}					

			// Update discount - important if COD paid before delivery.
			Active.order.calculateDiscount();
			Active.order.save();

			btnPayment.setEnabled(Active.order.Terms.equals("Paying by Card") ? false : true);
			
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
	@SuppressLint("SimpleDateFormat")
	public void updateUI() 
	{
		try
		{
			DateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			
			// InfoView.
			infoview.setDefaultTv1("Delivery note");
			infoview.setDefaultTv2("");
			
			// Remove any old order lines.
			while (tlTable.getChildCount() > 1)
			{
				tlTable.removeViewAt(1);
			}
			
			// Create a TableRow for each order line.
			if (Active.order != null)
			{
				for (dbTripOrderLine line : Active.order.GetTripOrderLines())
				{
					TableRow tr = (TableRow)inflater.inflate(R.layout.trip_undelivered_delivery_note_tablerow, null);
		
					TextView tvDesc = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_desc);
					tvDesc.setText(line.Product.Desc);
					
					TextView tvDeliveredQty = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_delivered);
					tvDeliveredQty.setText(Integer.toString(line.DeliveredQty));
					
					TextView tvValue = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_value);

					tvValue.setText(line.getDeliveredQtyVariesFromOrdered() && line.DeliveredPrice == 0 ? "" : "" + decf2.format(line.getDeliveredNettValue() + line.getDeliveredSurchargeValue()));
				
					// Add the TableRow to the TableLayout.
					tlTable.addView(tr);
					
					// If delivered qty varies too much from the ordered qty, then allow driver to reprice.
					if (line.getDeliveredQtyVariesFromOrdered())
					{
						TableRow tr2 = (TableRow)inflater.inflate(R.layout.trip_undelivered_delivery_note_tablerow2, null);
						
						Button btnNewPrice = (Button)tr2.findViewById(R.id.trip_undelivered_delivery_note_tablerow2_button);
						btnNewPrice.setOnClickListener(onNewPrice);
						btnNewPrice.setTag(line);

						btnNewPrice.setText(line.DeliveredPrice == 0 ? "Ordered " + line.OrderedQty + " - tap to price" : "Ordered " + line.OrderedQty);
	
						tlTable.addView(tr2);
					}
				}
		
				double vat = Active.order.getDeliveredVatValue();
				double surcharge = Active.order.getDeliveredSurchargeValue();
				double accBalance = Active.order.getCodAccBalance();
				double creditTotal = Active.order.getCreditTotal();
				double cashTotal = Active.order.getCashTotal();
				double paidOffice = Active.order.getPrepaidAmount();
				double paidDriver = Active.order.getPaidDriver();
				double discount = Active.order.Discount;
				double outstanding = Active.order.getOutstanding();
				
				// Update view.
				tvVat.setText(decf2.format(vat));
				
				if (accBalance == 0)
				{
					trAccBalanceRow.setVisibility(View.GONE);
				}
				else
				{
					trAccBalanceRow.setVisibility(View.VISIBLE);
					tvAccBalance.setText(decf2.format(accBalance));
				}
				
				tvTotal.setText(decf2.format(creditTotal));
				
				if (paidOffice == 0)
				{
					trPaidOfficeRow.setVisibility(View.GONE);
				}
				else
				{
					trPaidOfficeRow.setVisibility(View.VISIBLE);
					tvPaidOffice.setText(decf2.format(paidOffice));
				}
				
				if (paidDriver == 0)
				{
					trPaidDriverRow.setVisibility(View.GONE);
				}
				else
				{
					trPaidDriverRow.setVisibility(View.VISIBLE);
					tvPaidDriver.setText(decf2.format(paidDriver));
				}
				
				if (discount == 0)
				{
					trDiscountRow.setVisibility(View.GONE);
				}
				else
				{
					trDiscountRow.setVisibility(View.VISIBLE);
					tvDiscount.setText(decf2.format(discount));
				}

				if (paidOffice == 0 && paidDriver == 0 && discount == 0)
				{
					trSubtotalRow.setVisibility(View.GONE);
					trOutstandingRow.setVisibility(View.GONE);
				}
				else
				{
					trSubtotalRow.setVisibility(View.VISIBLE);
					trOutstandingRow.setVisibility(View.VISIBLE);
					tvOutstanding.setText(decf2.format(outstanding));
				}

				if (surcharge != 0 && outstanding > 0)
				{
					llCashDiscountMsg.setVisibility(View.VISIBLE);
					tvCashDiscountMsg1.setText("Please pay driver " + decf2.format(cashTotal - paidOffice));
					tvCashDiscountMsg2.setText("to receive a cash discount of " + decf2.format(surcharge));
				}
				else
				{
					llCashDiscountMsg.setVisibility(View.GONE);
				}
				
				tvTerms.setText(Active.order.getTerms());
		
				// Show / hide signature.
				if (!Active.order.CustomerSignature)
				{
					tlSignature.setVisibility(View.GONE);
				}
				else
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray(Active.order.CustomerSignatureImage, 0, Active.order.CustomerSignatureImage.length);
					
					tvSignatureName.setText(Active.order.CustomerSignatureName);
					ivSignatureImage.setImageBitmap(bitmap);
					tvSignatureDateTime.setText(df.format(Active.order.CustomerSignatureDateTime));
					tlSignature.setVisibility(View.VISIBLE);
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	OnClickListener onNewPrice = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onNewPrice");

				// Set Active.orderLine
				Active.orderLine = (dbTripOrderLine)paramView.getTag();
				
				// Show amend price dialog.
				trip.amendPrice();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onPayment = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onPayment");

				// Show payment dialog.
				trip.acceptPayment(false);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	OnClickListener onSignature = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onSignature");

				// Capture signature.
				trip.captureCustomersName();
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
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onNext");
				
				// Order now complete.
				finishOrder();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};

	public void finishOrder()
	{
		try
		{
			// If customer has not paid all, and has not signed, ask for their signature now.
			if (Active.order.getOutstanding() > 0 && !Active.order.CustomerSignature)
			{
				trip.captureCustomersName();

				return;
			}			
			
			// If driver has taken a payment, ensure he signs for it.
			if (Active.order.getPaidDriver() != 0 && !Active.order.DriverSignature)
			{
				// Ask driver to sign for payment received.
				trip.captureSignature("Driver", Active.driver.Name);
			}
			else
			{
				// Switch to ticket printing view.
				trip.selectView(Trip.ViewUndeliveredTicket, +1);
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
}