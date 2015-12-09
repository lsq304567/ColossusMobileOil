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
	private LayoutInflater inflater;
	
	private MyInfoView1Line infoview;
	private TextView tvAmountAgreed;
	private TableRow trCashRow;
	private TextView tvCashAmount;
	private TableRow trChequeRow;
	private TextView tvChequeAmount;
	private TableRow trVoucherRow;
	private TextView tvVoucherAmount;
	private TextView tvAmountOutstanding;
	private Button btnBack;
	private Button btnPayment;
	private Button btnNext;
	
	private DecimalFormat decf2;
	
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
			inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			btnBack = (Button)this.findViewById(R.id.trip_undelivered_cod_back);
			btnPayment = (Button)this.findViewById(R.id.trip_undelivered_cod_payment);
			btnNext = (Button)this.findViewById(R.id.trip_undelivered_cod_next);
			
			btnBack.setOnClickListener(onBack);
			btnPayment.setOnClickListener(onPayment);
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
			infoview.setDefaultTv1("C.O.D.");
			infoview.setDefaultTv2("");
			
			tvAmountAgreed.setText(decf2.format(Active.order.getCodBeforeDeliveryValue()));
			trCashRow.setVisibility(Active.order.getCashReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvCashAmount.setText(decf2.format(Active.order.getCashReceived()));
			trChequeRow.setVisibility(Active.order.getChequeReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvChequeAmount.setText(decf2.format(Active.order.getChequeReceived()));
			trVoucherRow.setVisibility(Active.order.getVoucherReceived().compareTo(BigDecimal.ZERO) == 0 ? View.GONE : View.VISIBLE);
			tvVoucherAmount.setText(decf2.format(Active.order.getVoucherReceived()));
			tvAmountOutstanding.setText(decf2.format(Active.order.getCodBeforeDeliveryValue().subtract(Active.order.getPaidDriver())));
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	OnClickListener onBack = new OnClickListener()
	{
		@Override
		public void onClick(View paramView)
		{
			try
			{
				// Leave breadcrumb.
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onBack");

				// Switch views.
				trip.selectView(Trip.ViewUndeliveredSummary, -1);
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
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onPayment");

				// Show payment dialog.
				trip.acceptPayment(true);
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
				CrashReporter.leaveBreadcrumb("Trip_Undelivered_COD: onNext");

				// Calculate unpaid COD.
				BigDecimal unpaidCod = Active.order.getCodBeforeDeliveryValue().subtract(Active.order.getPaidDriver());
				
				if (unpaidCod.compareTo(BigDecimal.ZERO) > 0)
				{
					// Warn driver COD is not fully paid.
					AlertDialog.Builder builder = new AlertDialog.Builder(trip);
					builder.setTitle("COD outstanding");
					builder.setMessage(decf2.format(unpaidCod) + " outstanding.\n\nAre you sure you wish to continue?");
					
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
			}
			catch (Exception e)
			{
				CrashReporter.logHandledException(e);
			}
		}
	};
}
