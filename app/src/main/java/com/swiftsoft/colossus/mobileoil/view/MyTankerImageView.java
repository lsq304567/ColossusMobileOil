package com.swiftsoft.colossus.mobileoil.view;

import com.swiftsoft.colossus.mobileoil.CrashReporter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyTankerImageView extends ImageView
{
	Paint paint = new Paint();

	public MyTankerImageView(Context context)
	{
		super(context);
		init();
	}

	public MyTankerImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public MyTankerImageView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		try
		{
			if (isInEditMode() == false)
			{
				// Initialise compartments.
				compartments = new Compartment[tankCompartments];

				for (int i = 0; i < tankCompartments; i++)
				{
					compartments[i] = new Compartment();
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	// Properties.
	public void setCompartmentCount(int compartments)
	{
		try
		{
			tankCompartments = compartments;
			
			init();
			invalidate();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	public void setCompartmentNo(int compartmentIdx, int no)
	{
		try
		{
			compartments[compartmentIdx].no = no;
			invalidate();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void setCompartmentCapacity(int compartmentIdx, int capacity)
	{
		try
		{
			compartments[compartmentIdx].capacity = capacity;
			invalidate();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	public void setCompartmentOnboard(int compartmentIdx, int onboard)
	{
		try
		{
			compartments[compartmentIdx].onboard = onboard;
			invalidate();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	public void setCompartmentColour(int compartmentIdx, int colour)
	{
		try
		{
			compartments[compartmentIdx].colour = colour;
			invalidate();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		try
		{
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.BLACK);
			paint.setAntiAlias(true);
			paint.setTextSize(20);
	
			if (tankCompartments > 0)
			{
				// Calculate size of each compartment.
				int compartmentSize = tankWidth / tankCompartments;
		
				// Since tankWidth divided by tankCompartments is not always an integer,
				// we need to centre the compartments to make things look better.
				int tankPadding = (tankWidth - (compartmentSize * tankCompartments)) / 2;
		
				// Draw compartments.
				for (int i = 0; i < tankCompartments; i++)
				{
					// Calculate compartment location and size.
					int x = tankPadding + tankLeft + (i * compartmentSize);

					// Get the compartment
					Compartment currentCompartment = compartments[i];

					// Draw product onboard.
					if (currentCompartment.onboard > 0)
					{
						// Calculate height.
						int height = (int) (tankHeight * (currentCompartment.onboard / (float) currentCompartment.capacity));

						paint.setColor(currentCompartment.colour);
						paint.setStyle(Paint.Style.FILL);

						canvas.drawRect(x, tankTop + (tankHeight - height), x + compartmentSize, tankTop + tankHeight, paint);
					}
					
					// Draw compartment outline.
					paint.setColor(Color.GRAY);
					paint.setStyle(Paint.Style.STROKE);

					canvas.drawRect(x, tankTop, x + compartmentSize, tankTop + tankHeight, paint);
		
					// Centre label.
					String label = String.format("#%d", currentCompartment.no);

					float labelWidth = paint.measureText(label);
					int labelX = x + (int)((compartmentSize - labelWidth) / 2);
					int labelY = tankTop - 6;
					
					paint.setColor(Color.BLUE);
					canvas.drawText(label, labelX, labelY, paint);
				}
			}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
	}

	private int tankLeft = 140;
	private int tankTop = 26;
	private int tankWidth = 357;
	private int tankHeight = 100;
	private int tankCompartments = 0;

	private Compartment[] compartments;
	
	private class Compartment
	{
		int no;
		int capacity;
		int onboard;
		int colour;
	}
}