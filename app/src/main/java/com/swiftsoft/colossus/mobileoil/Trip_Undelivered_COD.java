package com.swiftsoft.colossus.mobileoil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.swiftsoft.colossus.mobileoil.view.MyFlipperView;
import com.swiftsoft.colossus.mobileoil.view.MyInfoView1Line;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Trip_Undelivered_COD extends MyFlipperView
{
	private Trip trip;

	private MyInfoView1Line infoview;
	private TextView tvAmountAgreed;
	private TableRow trCashRow;
	private TextView tvCashAmount;
	private TableRow trChequeRow;
	private TextView tvChequeAmount;
	private TableRow trVoucherRow;
	private TextView tvVoucherAmount;
	private TextView tvAmountOutstanding;

	private DecimalFormat decimalFormat;
	
	public Trip_Undelivered_COD(Context context)
	{
		super(context);
		init(context);
	}

	public Trip_Undelivered_COD(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context)
	{
		try
		{
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: init");

			// Store reference to Trip activity.
			trip = (Trip)context;
	
			// Inflate layout.
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.trip_undelivered_cod, this, true);
			
			infoview = (MyInfoView1Line)this.findViewById(R.id.trip_undelivered_cod_infoview);
			tvAmountAgreed = (TextView)this.findViewById(R.id.trip_undelivered_cod_amount_agreed);
			trCashRow = (TableRow)this.findViewById(R.id.trip_undelivered_cod_amount_cash);
			tvCashAmount = (TextView)this.findViewById(R.id.trip_undelivered_cod_amount_cash_received);
			trChequeRow = (TableRow)this.findViewById(R.id.trip_undelivered_cod_amount_cheque);
			tvChequeAmount = (TextView)this.findViewById(R.id.trip_undelivered_cod_amount_cheque_received);
			trVoucherRow = (TableRow)this.findViewById(R.id.trip_undelivered_cod_amount_voucher);
			tvVoucherAmount = (TextView)this.findViewById(R.id.trip_undelivered_cod_amount_voucher_received);
			tvAmountOutstanding = (TextView)this.findViewById(R.id.trip_undelivered_cod_amount_outstanding);
			Button btnBack = (Button) this.findViewById(R.id.trip_undelivered_cod_back);
			Button btnPayment = (Button) this.findViewById(R.id.trip_undelivered_cod_payment);
			Button btnNext = (Button) this.findViewById(R.id.trip_undelivered_cod_next);
			
			btnBack.setOnClickListener(onClickListener);
			btnPayment.setOnClickListener(onClickListener);
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
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: resumeView");

			// Resume updating.
			infoview.resume();
			
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
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: pauseView");

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
			// Leave breadcrumb.
			CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: updateUI");

			infoview.setDefaultTv1("C.O.D.");
			infoview.setDefaultTv2("");
			
			tvAmountAgreed.setText(decimalFormat.format(Active.order.getCodBeforeDeliveryValue()));
			trCashRow.setVisibility(Active.order.getCashReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvCashAmount.setText(decimalFormat.format(Active.order.getCashReceived()));
			trChequeRow.setVisibility(Active.order.getChequeReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvChequeAmount.setText(decimalFormat.format(Active.order.getChequeReceived()));
			trVoucherRow.setVisibility(Active.order.getVoucherReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvVoucherAmount.setText(decimalFormat.format(Active.order.getVoucherReceived()));
			tvAmountOutstanding.setText(decimalFormat.format(Active.order.getCodBeforeDeliveryValue().subtract(Active.order.getPaidDriver())));
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
                    case R.id.trip_undelivered_cod_back:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onClick - Back");

                        // Switch views.
                        trip.selectView(Trip.ViewUndeliveredSummary, -1);

                        break;

                    case R.id.trip_undelivered_cod_payment:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onClick - Payment");

                        // Show payment dialog.
                        trip.acceptPayment(true);

                        break;

                    case R.id.trip_undelivered_cod_next:

                        // Leave breadcrumb.
                        CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onClick - Next");

                        // Calculate unpaid COD.
                        BigDecimal unpaidCod = Active.order.getCodBeforeDeliveryValue().subtract(Active.order.getPaidDriver());

                        if (unpaidCod.compareTo(BigDecimal.ZERO) > 0)
                        {
                            // Warn driver COD is not fully paid.
                            AlertDialog.Builder builder = new AlertDialog.Builder(trip);
                            builder.setTitle("COD outstanding");
                            builder.setMessage(decimalFormat.format(unpaidCod) + " outstanding.\n\nAre you sure you wish to continue?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    // Switch views.
                                    trip.selectView(Trip.ViewUndeliveredProducts, +1);
                                }

                            });

                            builder.setNegativeButton("No", null);
                            builder.show();
                        }
                        else
                        {
                            // Switch views.
                            trip.selectView(Trip.ViewUndeliveredProducts, +1);
                        }

                        break;
                }
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}