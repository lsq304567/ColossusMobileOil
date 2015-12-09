package com.swiftsoft.colossus.mobileoil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrder;
import com.swiftsoft.colossus.mobileoil.database.model.dbTripOrderLine;
import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Trip_Delivered_Order extends MyFlipperView
{
	private Trip trip;
	private Trip_Delivered tripDelivered;
	private LayoutInflater inflater;

	private TextView tvOrderNo;
	private TextView tvOrderCustomer;
	private TextView tvOrderDelAddress;
	private TableLayout tlOrderProductTable;
	private TextView tvOrderVat;
	private TableRow trOrderAccBalanceRow;
	private TextView tvOrderAccBalance;
	private TextView tvOrderTotal;
	private TableRow trOrderPaidOfficeRow;
	private TextView tvOrderPaidOffice;
	private TableRow trOrderPaidDriverRow;
	private TextView tvOrderPaidDriver;
	private TableRow trOrderDiscountRow;
	private TextView tvOrderDiscount;
	private TableRow trOrderSubtotalRow;
	private TableRow trOrderOutstandingRow;
	private TextView tvOrderOutstanding;
	private TextView tvOrderTerms;
	private TextView tvOrderNotes;
	private LinearLayout llOrderSignature;
	private ImageView ivOrderSignatureImage;
	private TextView tvOrderSignatureName;
	private TextView tvOrderSignatureDateTime;

    private DecimalFormat formatMoney;

	private dbTripOrder selectedOrder;

	public Trip_Delivered_Order(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Delivered_Order(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public Trip_Delivered_Order(Context context, Trip_Delivered tripDelivered)
	{
		super(context);
		init(context);

		// Store reference to Trip_Delivered dialog.
		this.tripDelivered = tripDelivered;
	}

	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Delivered_Order: init");

			// Store reference to Trip activity.
			trip = (Trip)context;

			// Inflate layout.
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_delivered_order, this, true);

			tvOrderNo = (TextView)this.findViewById(R.id.trip_delivered_order_no);
			tvOrderCustomer = (TextView)this.findViewById(R.id.trip_delivered_order_customer);
			tvOrderDelAddress = (TextView)this.findViewById(R.id.trip_delivered_order_delAddress);
			tlOrderProductTable = (TableLayout)this.findViewById(R.id.trip_delivered_order_product_table);
			tvOrderVat = (TextView)this.findViewById(R.id.trip_delivered_order_vat);
			trOrderAccBalanceRow = (TableRow)this.findViewById(R.id.trip_delivered_order_acc_balance_row);
			tvOrderAccBalance = (TextView)this.findViewById(R.id.trip_delivered_order_acc_balance);
			tvOrderTotal = (TextView)this.findViewById(R.id.trip_delivered_order_total);
			trOrderPaidOfficeRow = (TableRow)this.findViewById(R.id.trip_delivered_order_paid_office_row);
			tvOrderPaidOffice = (TextView)this.findViewById(R.id.trip_delivered_order_paid_office);
			trOrderPaidDriverRow = (TableRow)this.findViewById(R.id.trip_delivered_order_paid_driver_row);
			tvOrderPaidDriver = (TextView)this.findViewById(R.id.trip_delivered_order_paid_driver);
			trOrderDiscountRow = (TableRow)this.findViewById(R.id.trip_delivered_order_discount_row);
			tvOrderDiscount = (TextView)this.findViewById(R.id.trip_delivered_order_discount);
			trOrderSubtotalRow = (TableRow)this.findViewById(R.id.trip_delivered_order_subtotal_row);
			trOrderOutstandingRow = (TableRow)this.findViewById(R.id.trip_delivered_order_outstanding_row);
			tvOrderOutstanding = (TextView)this.findViewById(R.id.trip_delivered_order_outstanding);
			tvOrderTerms = (TextView)this.findViewById(R.id.trip_delivered_order_terms);
			tvOrderNotes = (TextView)this.findViewById(R.id.trip_delivered_order_notes);
			llOrderSignature = (LinearLayout)this.findViewById(R.id.trip_delivered_order_signature);
			ivOrderSignatureImage = (ImageView)this.findViewById(R.id.trip_delivered_order_signature_image);
			tvOrderSignatureName = (TextView)this.findViewById(R.id.trip_delivered_order_signature_name);
			tvOrderSignatureDateTime = (TextView)this.findViewById(R.id.trip_delivered_order_signature_datetime);
            Button btnBack = (Button) this.findViewById(R.id.trip_delivered_order_back);
            Button btnReprint = (Button) this.findViewById(R.id.trip_delivered_order_reprint);

			btnBack.setOnClickListener(onBack);
			btnReprint.setOnClickListener(onReprint);

			// Setup standard decimal format.
			formatMoney = new DecimalFormat("#,##0.00");
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	public void setOrder(dbTripOrder order)
	{
		selectedOrder = order;
	}

	@SuppressLint("SetTextI18n")
    @Override
	public void updateUI()
	{
		try
		{
			if (selectedOrder != null)
			{
				DateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

				String customer = selectedOrder.CustomerName + "\n" + selectedOrder.CustomerAddress;
				String delAddress = selectedOrder.DeliveryName + "\n" + selectedOrder.DeliveryAddress;

				tvOrderNo.setText(selectedOrder.InvoiceNo);
				tvOrderCustomer.setText(customer);
				tvOrderDelAddress.setText(String.format("%s\n%s", delAddress, selectedOrder.DeliveryPostcode));

				// If delivery address is different, show it in bold.
				tvOrderDelAddress.setTypeface(null, customer.equals(delAddress) ? Typeface.NORMAL : Typeface.BOLD);

				// Remove any old order lines.
				while (tlOrderProductTable.getChildCount() > 1)
				{
					tlOrderProductTable.removeViewAt(1);
				}

				for (dbTripOrderLine line : selectedOrder.GetTripOrderLines())
				{
					@SuppressLint("InflateParams")
					TableRow tr = (TableRow)inflater.inflate(R.layout.trip_delivered_order_tablerow, null);
					tr.setTag(line);

					TextView tvDesc = (TextView) tr.findViewById(R.id.trip_delivered_order_tablerow_desc);
					tvDesc.setText(line.Product.Desc);

					TextView tvDeliveredQty = (TextView) tr.findViewById(R.id.trip_delivered_order_tablerow_delivered);
					tvDeliveredQty.setText(String.format("%d", line.DeliveredQty));

					TextView tvValue = (TextView) tr.findViewById(R.id.trip_delivered_order_tablerow_value);
					tvValue.setText("" + formatMoney.format(line.getDeliveredNettValue().add(line.getDeliveredSurchargeValue())));

					// Add the TableRow to the TableLayout.
					tlOrderProductTable.addView(tr);
				}

				BigDecimal vat = selectedOrder.getDeliveredVatValue();
				BigDecimal accBalance = selectedOrder.getCodAccBalance();
				BigDecimal creditTotal = selectedOrder.getCreditTotal();
				BigDecimal paidOffice = selectedOrder.getPrepaidAmount();
				BigDecimal paidDriver = selectedOrder.getPaidDriver();
				BigDecimal discount = selectedOrder.getDiscount();
				BigDecimal outstanding = selectedOrder.getOutstanding();

				// Update view.
				tvOrderVat.setText(formatMoney.format(vat));

				if (accBalance.compareTo(BigDecimal.ZERO) == 0)
				{
					trOrderAccBalanceRow.setVisibility(View.GONE);
				}
				else
				{
					trOrderAccBalanceRow.setVisibility(View.VISIBLE);
					tvOrderAccBalance.setText(formatMoney.format(accBalance));
				}

				tvOrderTotal.setText(formatMoney.format(creditTotal));

				if (paidOffice.compareTo(BigDecimal.ZERO) == 0)
				{
					trOrderPaidOfficeRow.setVisibility(View.GONE);
				}
				else
				{
					trOrderPaidOfficeRow.setVisibility(View.VISIBLE);
					tvOrderPaidOffice.setText(formatMoney.format(paidOffice));
				}

				if (paidDriver.compareTo(BigDecimal.ZERO) == 0)
				{
					trOrderPaidDriverRow.setVisibility(View.GONE);
				}
				else
				{
					trOrderPaidDriverRow.setVisibility(View.VISIBLE);
					tvOrderPaidDriver.setText(formatMoney.format(paidDriver));
				}

				if (discount.compareTo(BigDecimal.ZERO) == 0)
				{
					trOrderDiscountRow.setVisibility(View.GONE);
				}
				else
				{
					trOrderDiscountRow.setVisibility(View.VISIBLE);
					tvOrderDiscount.setText(formatMoney.format(discount));
				}

				if (paidOffice.compareTo(BigDecimal.ZERO) == 0 && paidDriver.compareTo(BigDecimal.ZERO) == 0 && discount.compareTo(BigDecimal.ZERO) == 0)
				{
					trOrderSubtotalRow.setVisibility(View.GONE);
					trOrderOutstandingRow.setVisibility(View.GONE);
				}
				else
				{
					trOrderSubtotalRow.setVisibility(View.VISIBLE);
					trOrderOutstandingRow.setVisibility(View.VISIBLE);
					tvOrderOutstanding.setText(formatMoney.format(outstanding));
				}
				
				tvOrderTerms.setText(selectedOrder.getTerms());
		
				// Show / hide signature.
				if (!selectedOrder.CustomerSignature)
				{
					llOrderSignature.setVisibility(View.GONE);
				}
				else
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray(selectedOrder.CustomerSignatureImage, 0, selectedOrder.CustomerSignatureImage.length);
					
					tvOrderSignatureName.setText(selectedOrder.CustomerSignatureName);
					ivOrderSignatureImage.setImageBitmap(bitmap);
					tvOrderSignatureDateTime.setText(df.format(selectedOrder.CustomerSignatureDateTime));
					llOrderSignature.setVisibility(View.VISIBLE);
				}
	
				tvOrderTerms.setText(selectedOrder.getTerms());
				tvOrderNotes.setText(selectedOrder.Notes);
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
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Delivered_Order: onBack");
				
				// Switch back to the delivered list.
				tripDelivered.showList();
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
	
	private final OnClickListener onReprint = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Delivered_Order: onReprint");
				
				// Reprint the ticket.
				Printing.ticket(trip, selectedOrder);
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}