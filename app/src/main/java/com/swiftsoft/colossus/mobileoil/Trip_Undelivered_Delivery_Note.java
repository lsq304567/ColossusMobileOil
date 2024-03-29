package com.swiftsoft.colossus.mobileoil;

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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

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

    private LinearLayout llPaymentMessages;
    private TextView tvTableValueHeader;

	private DecimalFormat decimalFormat;

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
            Button btnSignature = (Button) this.findViewById(R.id.trip_undelivered_delivery_note_signature);
            Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_delivery_note_next);

            llPaymentMessages = (LinearLayout)this.findViewById(R.id.trip_undelivered_delivery_note_payments);
            tvTableValueHeader = (TextView)this.findViewById(R.id.trip_undelivered_delivery_note_table_header_value);
			
			btnPayment.setOnClickListener(onClickListener);
			btnSignature.setOnClickListener(onClickListener);
			btnNext.setOnClickListener(onClickListener);
			
			// Setup standard decimal format.
			decimalFormat = new DecimalFormat("#,##0.00");
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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: resumeView");

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

			if (Active.order.HidePrices)
			{
				btnPayment.setEnabled(false);
			}
			else
			{
				btnPayment.setEnabled(!Active.order.Terms.equals("Paying by Card"));
			}


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
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: pauseView");

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
            // Leave breadcrumb.
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: updateUI");

            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			
			// InfoView.
			infoview.setDefaultTv1("Delivery note");
			infoview.setDefaultTv2("");
			
			// Remove any old order lines.
			while (tlTable.getChildCount() > 1)
			{
				tlTable.removeViewAt(1);
			}

            // Hide or show the value column of the tale
            // depending on whether or not prices are to be shown
            tvTableValueHeader.setVisibility(Active.order.HidePrices ? View.GONE : View.VISIBLE);
			
			// Create a TableRow for each order line.
			if (Active.order != null)
			{
				for (dbTripOrderLine line : Active.order.GetTripOrderLines())
				{
					@SuppressLint("InflateParams")
					TableRow tr = (TableRow)inflater.inflate(R.layout.trip_undelivered_delivery_note_tablerow, null);
		
					TextView tvDesc = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_desc);
					tvDesc.setText(line.Product.Desc);
					
					TextView tvDeliveredQty = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_delivered);
					tvDeliveredQty.setText(String.format("%d", line.DeliveredQty));

					TextView tvValue = (TextView) tr.findViewById(R.id.trip_undelivered_delivery_note_tablerow_value);

					tvValue.setText(line.getDeliveredQtyVariesFromOrdered() && line.getDeliveredPrice().compareTo(BigDecimal.ZERO) == 0 ? "" : "" + decimalFormat.format(line.getDeliveredNettValue().add(line.getDeliveredSurchargeValue())));

                    tvValue.setVisibility(Active.order.HidePrices ? View.GONE : View.VISIBLE);

					// Add the TableRow to the TableLayout.
					tlTable.addView(tr);
					
					// If delivered qty varies too much from the ordered qty, then allow driver to reprice.
					if (line.getDeliveredQtyVariesFromOrdered())
					{
						@SuppressLint("InflateParams")
                        TableRow tr2 = (TableRow)inflater.inflate(R.layout.trip_undelivered_delivery_note_tablerow2, null);
						
						Button btnNewPrice = (Button)tr2.findViewById(R.id.trip_undelivered_delivery_note_tablerow2_button);

						btnNewPrice.setOnClickListener(onClickListener);
						btnNewPrice.setTag(line);

						btnNewPrice.setText(line.getDeliveredPrice().compareTo(BigDecimal.ZERO) == 0 ? "Ordered " + line.OrderedQty + " - tap to price" : "Ordered " + line.OrderedQty);
	
						tlTable.addView(tr2);
					}
				}
		
				BigDecimal vat = Active.order.getDeliveredVatValue();
				BigDecimal surcharge = Active.order.getDeliveredSurchargeValue();
				BigDecimal accBalance = Active.order.getCodAccBalance();
				BigDecimal creditTotal = Active.order.getCreditTotal();
				BigDecimal cashTotal = Active.order.getCashTotal();
				BigDecimal paidOffice = Active.order.getAmountPrepaid();
				BigDecimal paidDriver = Active.order.getPaidDriver();
                Active.order.calculateDiscount();
                BigDecimal discount = Active.order.getDiscount();
				BigDecimal outstanding = Active.order.getOutstanding();
				BigDecimal surchargeVatAmount = Active.order.getSurchargeVat();

                llPaymentMessages.setVisibility(Active.order.HidePrices ? View.GONE : View.VISIBLE);
				
				// Update view.
				tvVat.setText(decimalFormat.format(vat));
				
				if (accBalance.compareTo(BigDecimal.ZERO) == 0)
				{
					trAccBalanceRow.setVisibility(View.GONE);
				}
				else
				{
					trAccBalanceRow.setVisibility(View.VISIBLE);
					tvAccBalance.setText(decimalFormat.format(accBalance));
				}
				
				tvTotal.setText(decimalFormat.format(creditTotal));
				
				if (paidOffice.compareTo(BigDecimal.ZERO) == 0)
				{
					trPaidOfficeRow.setVisibility(View.GONE);
				}
				else
				{
					trPaidOfficeRow.setVisibility(View.VISIBLE);
					tvPaidOffice.setText(decimalFormat.format(paidOffice));
				}
				
				if (paidDriver.compareTo(BigDecimal.ZERO) == 0)
				{
					trPaidDriverRow.setVisibility(View.GONE);
				}
				else
				{
					trPaidDriverRow.setVisibility(View.VISIBLE);
					tvPaidDriver.setText(decimalFormat.format(paidDriver));
				}
				
				if (discount.compareTo(BigDecimal.ZERO) == 0)
				{
					trDiscountRow.setVisibility(View.GONE);
				}
				else
				{
					trDiscountRow.setVisibility(View.VISIBLE);
					tvDiscount.setText(decimalFormat.format(discount));
				}

				if (paidOffice.compareTo(BigDecimal.ZERO) == 0 && paidDriver.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) == 0)
				{
					trSubtotalRow.setVisibility(View.GONE);
					trOutstandingRow.setVisibility(View.GONE);
				}
				else
				{
					trSubtotalRow.setVisibility(View.VISIBLE);
					trOutstandingRow.setVisibility(View.VISIBLE);
					tvOutstanding.setText(decimalFormat.format(outstanding));
				}

				if (surcharge.compareTo(BigDecimal.ZERO) != 0 && outstanding.compareTo(BigDecimal.ZERO) > 0)
				{
                    llCashDiscountMsg.setVisibility(Active.order.HidePrices ? View.GONE : View.VISIBLE);

                    tvCashDiscountMsg1.setText(String.format("Please pay driver %s", decimalFormat.format(cashTotal.subtract(paidOffice).subtract(surchargeVatAmount))));
					tvCashDiscountMsg2.setText(String.format("to receive a cash discount of %s", decimalFormat.format(surcharge.add(surchargeVatAmount))));
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

    private final OnClickListener onClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                switch (view.getId())
                {
                    case R.id.trip_undelivered_delivery_note_tablerow2_button:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onClick - NewPrice");

                        // Set Active.orderLine
                        Active.orderLine = (dbTripOrderLine)view.getTag();

                        // Show amend price dialog.
                        trip.amendPrice();

                        break;

                    case R.id.trip_undelivered_delivery_note_payment:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onClick - Payment");

                        // Show payment dialog.
                        trip.acceptPayment(false);

                        break;

                    case R.id.trip_undelivered_delivery_note_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onClick - Next");

                        // Order now complete.
                        finishOrder();

                        break;

                    case R.id.trip_undelivered_delivery_note_signature:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: onClick - Signature");

                        // Capture signature.
                        trip.captureCustomersName();

                        break;
                }
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
            // Leave breadcrumb.
            CrashReporter.leaveBreadcrumb("Trip_Undelivered_Delivery_Note: finishOrder");

            // If customer has not paid all, and has not signed, ask for their signature now.
			if (Active.order.getOutstanding().compareTo(BigDecimal.ZERO) > 0 && !Active.order.CustomerSignature)
			{
				trip.captureCustomersName();

				return;
			}			
			
			// If driver has taken a payment, ensure he signs for it.
			if (Active.order.getPaidDriver().compareTo(BigDecimal.ZERO) != 0 && !Active.order.DriverSignature)
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