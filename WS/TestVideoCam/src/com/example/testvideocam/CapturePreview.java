package com.example.testvideocam;

import java.io.*;
import java.util.*;

import ru.pvolan.event.*;
import ru.pvolan.trace.*;

import android.app.*;
import android.content.*;
import android.hardware.*;
import android.media.*;
import android.util.*;
import android.view.*;
import android.widget.Toast;

public class CapturePreview extends SurfaceView implements
		SurfaceHolder.Callback
{

	// private fields
	private CameraManager manager;
	
	private SurfaceHolder holder;
	private int previewWidth = 0;
	private int previewHeight = 0;

	// public events

	public CustomEvent onCreated = new CustomEvent();
	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();
	public CustomEvent onVideoCaptureStarted = new CustomEvent();
	public CustomEvent onVideoCaptureStopped = new CustomEvent();



	// init **************************************************

	public CapturePreview(Context context, AttributeSet attrs)
	{
		// TODO Auto-generated constructor stub
		super(context, attrs);
		init();
	}



	CapturePreview(Context context)
	{
		super(context);

		init();
	}



	private void init()
	{
		manager = new CameraManager();
		
		manager.onVideoCaptureError.addListener(new ParametrizedCustomEventListener<Throwable>() 
		{
			@Override
			public void onEvent(Throwable param)
			{
				onVideoCaptureError.fire(param);
			}
		});
		
		manager.onVideoCaptureInfo.addListener(new ParametrizedCustomEventListener<MediaRecorderInfo>() {
			
			@Override
			public void onEvent(MediaRecorderInfo param)
			{
				onVideoCaptureInfo.fire(param);
			}
		});
		
		
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}



	// SurfaceHolder.Callback implementation *******************

	public void surfaceCreated(SurfaceHolder holder)
	{
		Trace.Print("surfaceCreated");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.

		manager.createCamera(holder, 0);

		onCreated.fire();
	}



	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Trace.Print("surfaceDestroyed");
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		try
		{
			manager.destroyCamera();
		}
		catch (Exception e)
		{
			Trace.Print(e);
			onVideoCaptureError.fire(e);
		}
	}



	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		Trace.Print("surfaceChanged");
		// Now that the size is known, set up the camera parameters and begin
		// the preview.

		try
		{
			manager.stopPreview(false);
			previewWidth = w;
			previewHeight = h;
			manager.updateCameraSizeApproximately(previewWidth, previewHeight);
			manager.startPreview(false);
		}
		catch (Exception e)
		{
			Trace.Print(e);
			onVideoCaptureError.fire(e);
		}
	}



	// publics *********************************************

	public void startVideoCapture()
	{
		try
		{
			manager.stopPreview(true);
			File outputFile = manager.startRecording(holder);

			
			// TODO move out of here
			Toast.makeText(getContext(),
					"Recording to " + outputFile.getAbsolutePath(),
					Toast.LENGTH_LONG).show();
		}
		catch (Exception ex)
		{
			Trace.Print(ex);
			onVideoCaptureError.fire(ex);
			manager.stopRecording();
			manager.startPreview(true);
			return;
		}

		onVideoCaptureStarted.fire();
	}



	public void stopVideoCapture()
	{
		manager.stopRecording();
		manager.startPreview(true);

		onVideoCaptureStopped.fire();
	}



	


}
