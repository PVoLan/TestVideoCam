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
		SurfaceHolder.Callback {
	
	SurfaceHolder holder;
	Camera camera;
	MediaRecorder mediaRecorder;
	File outputFile;

	public CustomEvent onCreated = new CustomEvent();
	
	public ParametrizedCustomEvent<ErrorInfo> onVideoCaptureError = new ParametrizedCustomEvent<CapturePreview.ErrorInfo>();
	public ParametrizedCustomEvent<ErrorInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<CapturePreview.ErrorInfo>();
	public CustomEvent onVideoCaptureStarted = new CustomEvent();
	public CustomEvent onVideoCaptureStopped = new CustomEvent();
	

	public CapturePreview(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
		init();
	}

	CapturePreview(Context context) {
		super(context);

		init();
	}

	private void init() {
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Trace.Print("surfaceCreated");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.

		try {
			camera = Camera.open();
			camera.setPreviewDisplay(holder);
		} catch (Exception exception) {
			Trace.Print(exception);
			camera.release();
			camera = null;

			if (getContext() instanceof Activity) {
				((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity) getContext()).finish();
			}
		}

		onCreated.fire();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Trace.Print("surfaceDestroyed");
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		try {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		} catch (Exception e) {
			Trace.Print(e);

			if (getContext() instanceof Activity) {
				((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity) getContext()).finish();
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Trace.Print("surfaceChanged");
		// Now that the size is known, set up the camera parameters and begin
		// the preview.

		try {
			Camera.Parameters parameters = camera.getParameters();

			List<Camera.Size> previewSizes = parameters
					.getSupportedPreviewSizes();
			Camera.Size max = camera.new Size(0, 0);

			for (Camera.Size size : previewSizes) {

				if (size.width <= w && size.height <= h
						&& (max.width < size.width || max.height < size.height)) {
					max = size;
				}

			}

			Trace.Print("max " + max.width + "x" + max.height);

			parameters.setPreviewSize(max.width, max.height);
			camera.setParameters(parameters);
			camera.startPreview();
		} catch (Exception e) {
			Trace.Print(e);

			if (getContext() instanceof Activity) {
				((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity) getContext()).finish();
			}
		}
	}

	/*
	public Camera getCamera() {
		return mCamera;
	}*/
	
	
	
	public void startVideoCapture()
	{
		try
		{
			camera.stopPreview();
			camera.unlock();
			
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setCamera(camera);
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
			
			outputFile = MediaFileManager.getOutputVideoFile();
			if(outputFile == null)
			{
				//TODO Error handle - move out of here
				Toast.makeText(getContext(), "Cannot create output file", Toast.LENGTH_SHORT).show();
				releaseRecorderResources();
				return;
			}
			
			mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
			mediaRecorder.setPreviewDisplay(holder.getSurface());
			
			mediaRecorder.prepare();
			
			Trace.Print("#######################START RECORD#####################################");
			mediaRecorder.start();
			
			
			
			//TODO move out of here
			Toast.makeText(getContext(), "Recording to " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
		}
		catch(Exception ex)
		{
			//TODO Error handle - move out of here
			Trace.Print(ex);
			Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
			releaseRecorderResources();
			return;
		}
		
		
		onVideoCaptureStarted.fire();
	}
	
	
	public void stopVideoCapture()
	{
		releaseRecorderResources();
		
		onVideoCaptureStopped.fire();
	}
	
	private void releaseRecorderResources()
	{
		try
		{
			Trace.Print("#######################STOP RECORD#####################################");
			mediaRecorder.stop();
		}
		catch (RuntimeException e) 
		{
			if(outputFile != null){
				//if(outputFile.exists()) outputFile.delete();
			}
		}
		mediaRecorder.reset();
		mediaRecorder.release();
		camera.lock();
		camera.startPreview();
	}
	
	
	
	public static class ErrorInfo{
		private MediaRecorder mr;
		private int what;
		private int extra;
		
		
		public ErrorInfo(MediaRecorder mr, int what, int extra) {
			super();
			this.mr = mr;
			this.what = what;
			this.extra = extra;
		}


		public MediaRecorder getMediaRecorder() {
			return mr;
		}


		public int getWhat() {
			return what;
		}


		public int getExtra() {
			return extra;
		}
		
		
	}
}
