package com.example.testvideocam;

import java.io.File;
import java.io.IOException;
import java.util.*;

import ru.pvolan.event.ParametrizedCustomEvent;
import ru.pvolan.trace.Trace;

import android.app.*;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.*;
import android.view.*;

public class CameraManager
{
	private Camera currentCamera;
	private int currentCameraIndex = 0;
	private List<Camera> cameras = new ArrayList<Camera>();
	
	private MediaRecorder mediaRecorder;
	private File outputFile;
	
	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();
	
	public CameraManager()
	{
		super();
	}
	
	public void createCameras(SurfaceHolder holder)
	{
		int maxCameras = Camera.getNumberOfCameras();
		Trace.Print("Total cameras: " + maxCameras);
		
		for (int i = 0; i < maxCameras; i++)
		{
			Camera c = null;
			try
			{
				c = Camera.open(i);
				//c.setPreviewDisplay(holder);
				cameras.add(c);
				Trace.Print("Camera created: " + i);
			}
			catch (Exception exception)
			{
				Trace.Print(exception);
				if(c != null)
				{
					c.release();
					c = null;
				}

				onVideoCaptureError.fire(exception);
			}
		}
		
		currentCamera = cameras.get(0);		
	}
	
	



	public void destroyCameras()
	{
		for (Camera c : cameras)
		{
			c.stopPreview();
			
			try
			{
				c.unlock();
			}
			catch(Exception ex){}
			
			c.setPreviewCallback(null);
			c.release();
			c = null;
		}
	}



	public void updateCameraSizeApproximately(SurfaceHolder holder, int w, int h)
	{
		for (Camera c : cameras)
		{
			Camera.Parameters parameters = c.getParameters();
	
			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
			Camera.Size max = c.new Size(0, 0);
	
			for (Camera.Size size : previewSizes)
			{
				if (size.width <= w && size.height <= h
						&& (max.width < size.width || max.height < size.height))
				{
					max = size;
				}
	
			}
	
			Trace.Print("max " + max.width + "x" + max.height);
	
			parameters.setPreviewSize(max.width, max.height);
			parameters.setRotation(90);
			parameters.set("orientation", "portrait");
			c.setDisplayOrientation(90);
			
			c.setParameters(parameters);
			try
			{
				c.setPreviewDisplay(holder);
			}
			catch (IOException e)
			{
				Trace.Print(e);
			}
			
			//setCameraDisplayOrientation(activity, i, c);
		}
	}



	public File startRecording(SurfaceHolder holder) throws IOException
	{
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setCamera(currentCamera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		outputFile = MediaFileManager.getOutputVideoFile();
		if (outputFile == null)
		{
			throw new RuntimeException("Cannot create output file");
		}

		mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
		mediaRecorder.setPreviewDisplay(holder.getSurface());
		
		mediaRecorder.setOrientationHint(90);
		
		mediaRecorder.setOnErrorListener(new OnErrorListener() 
		{
			@Override
			public void onError(MediaRecorder mr, int what, int extra)
			{
				onVideoCaptureError.fire(new CaptureException(mr, what, extra));
			}
		});
		
		mediaRecorder.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra)
			{
				onVideoCaptureInfo.fire(new MediaRecorderInfo(mr, what, extra));
			}
		});

		mediaRecorder.prepare();

		Trace.Print("#######################START RECORD#####################################");
		mediaRecorder.start();
		
		return outputFile;
	}



	public void stopRecording()
	{
		Trace.Print("#######################STOP RECORD#####################################");
		try
		{
			mediaRecorder.stop();
		}
		catch (RuntimeException e)
		{
			if (outputFile != null)
			{
				if (outputFile.exists())
					outputFile.delete();
			}
		}

		mediaRecorder.reset();
		mediaRecorder.release();
		mediaRecorder = null;
	}



	public void startPreview(boolean lockRequired)
	{
		if (lockRequired)
			currentCamera.lock();
		currentCamera.startPreview();
	}



	public void stopPreview(boolean unlockRequired)
	{
		currentCamera.stopPreview();
		if (unlockRequired)
			currentCamera.unlock();
	}
	
	public void switchCurrentCamera()
	{
		currentCameraIndex++;
		
		if(cameras.size() >= currentCameraIndex)
		{
			currentCameraIndex = 0;
		}
		
		currentCamera = cameras.get(currentCameraIndex);
		Trace.Print("Switched to camera " + currentCameraIndex);
	}
	
	
	
	// privates ***********************************
	/*
	private void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) 
	{
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	 }*/
}
