package com.swiftsoft.colossus.mobileoil.view;

import java.io.ByteArrayOutputStream;

import com.swiftsoft.colossus.mobileoil.CrashReporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MySignature extends View
{
    private Bitmap bitmap;
    private Canvas  mCanvas;    
    private Path    mPath;    
    private Paint   mBitmapPaint;    
    private Paint   mPaint;    
    private float   mX, mY;

    private static final float TOUCH_TOLERANCE = 0;

    public MySignature(Context c)
	{
    	super(c);
		init();
    }
    
    public MySignature(Context c, AttributeSet attr)
	{
    	super(c, attr);
		init();   
    }
    
	public MySignature(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

    private void init()
    {
    	try
    	{
	    	mPaint = new Paint();

	    	mPaint.setAntiAlias(true);
	    	mPaint.setDither(true);
	    	mPaint.setColor(0xFF000000);
	    	mPaint.setStyle(Paint.Style.STROKE);
	    	mPaint.setStrokeJoin(Paint.Join.ROUND);
	    	mPaint.setStrokeCap(Paint.Cap.ROUND);
	    	mPaint.setStrokeWidth(7);
	
	    	mPath = new Path();

	    	mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) 
    {
    	super.onSizeChanged(w, h, oldw, oldh);
    
    	try
    	{
	    	bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	    	mCanvas = new Canvas(bitmap);
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    @Override    
    protected void onDraw(Canvas canvas) 
    {
    	try
    	{
	    	canvas.drawColor(0xFFFFFFFF);        
	    	canvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
	    	canvas.drawPath(mPath, mPaint);    
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
        
    @Override    
    public boolean onTouchEvent(MotionEvent event) 
    {        
    	try
    	{
	    	float x = event.getX();        
	    	float y = event.getY();        
	    	
	    	switch (event.getAction())
			{
	    		case MotionEvent.ACTION_DOWN:                
	    			touch_start(x, y);                
	    			invalidate();                
	    			break;            
	    			
	    		case MotionEvent.ACTION_MOVE:                
	    			touch_move(x, y);                
	    			invalidate();                
	    			break;            
	    			
	    		case MotionEvent.ACTION_UP:                
	    			touch_up();                
	    			invalidate();                
	    			break;        
	    		}        
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    	
    	return true;    
    }    
    
    private void touch_start(float x, float y) 
    {        
    	try
    	{
	    	mPath.reset();        
	    	mPath.moveTo(x, y);        
	    	mX = x;        
	    	mY = y;    
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }    
    
    private void touch_move(float x, float y) 
    {
    	try
    	{
	    	float dx = Math.abs(x - mX);        
	    	float dy = Math.abs(y - mY);  
	    	
	    	if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) 
	    	{
	    		mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
	    		mX = x;
	    		mY = y;
	    	}    
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }    
    
    private void touch_up() 
    {  
    	try
    	{
	    	if (mPath.isEmpty())
	    	{
	    		// Draw a dot.
	    		mPath.addCircle(mX, mY, 3, Direction.CW);
	    	}
	    	else
	    	{    		
	    		mPath.lineTo(mX, mY);        
	    	}
	    	
	    	// commit the path to our bitmap        
	    	mCanvas.drawPath(mPath, mPaint);        
	    	
	    	// kill this so we don't double draw        
	    	mPath.reset();    
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }    
    
    public void ResetSignature() 
    {
    	try
    	{
    		if (bitmap != null)
	    	{
    			// Clear bitmap.
	    		bitmap.eraseColor(0);
	    		
	    		// Clear path.
	    		mPath.reset();
	    		
	    		// Redraw.
	    		invalidate();
	    	}
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);
		}
    }
    
    public byte[] toByteArray()
    {
    	try
    	{
			CrashReporter.leaveBreadcrumb("MySignature : toByteArray - Started");

	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();

			CrashReporter.leaveBreadcrumb("MySignature : toByteArray - Compressing to PNG");

			// Compress to PNG in the ByteArrayOutputStream
			bitmap.compress(CompressFormat.PNG, 0, bos);

			CrashReporter.leaveBreadcrumb("MySignature : toByteArray - Converting PNG to byte array");

			return bos.toByteArray();
		}
		catch (Exception e)
		{
			CrashReporter.logHandledException(e);

			return null;
		}
    }
}
