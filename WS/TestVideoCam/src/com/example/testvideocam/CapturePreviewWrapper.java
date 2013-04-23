package com.example.testvideocam;

import ru.pvolan.event.*;
import android.content.*;
import android.util.*;
import android.widget.*;

public class CapturePreviewWrapper extends FrameLayout
{
	private CapturePreview preview;
	
	public CustomEvent onCreated = new CustomEvent();
	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();
	public CustomEvent onVideoCaptureStarted = new CustomEvent();
	public CustomEvent onVideoCaptureStopped = new CustomEvent();
	
	public CustomEventListener onCreatedListener;
	public ParametrizedCustomEventListener<Throwable> onVideoCaptureErrorListener;
	public ParametrizedCustomEventListener<MediaRecorderInfo> onVideoCaptureInfoListener;
	public CustomEventListener onVideoCaptureStartedListener;
	public CustomEventListener onVideoCaptureStoppedListener;
	

	public CapturePreviewWrapper(Context context, AttributeSet attrs,
			int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public CapturePreviewWrapper(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public CapturePreviewWrapper(Context context)
	{
		super(context);
		init();
	}
	
	private void init()
	{
		onCreatedListener = new CustomEventListener() {
			
			@Override
			public void onEvent()
			{
				onCreated.fire();
			}
		};
		
		
		onVideoCaptureErrorListener = new ParametrizedCustomEventListener<Throwable>() {
			
			@Override
			public void onEvent(Throwable param)
			{
				onVideoCaptureError.fire(param);
				
			}
		};
		
		
		onVideoCaptureInfoListener = new ParametrizedCustomEventListener<MediaRecorderInfo>() {
			
			@Override
			public void onEvent(MediaRecorderInfo param)
			{
				onVideoCaptureInfo.fire(param);
			}
		};
		
		onVideoCaptureStartedListener = new CustomEventListener() {
			
			@Override
			public void onEvent()
			{
				onVideoCaptureStarted.fire();
			}
		};
		
		onVideoCaptureStoppedListener = new CustomEventListener() {
			
			@Override
			public void onEvent()
			{
				onVideoCaptureStopped.fire();
			}
		};
		
		
		createPreview(0);
		
	}

	
	
	public void recreatePreview(int cameraNo)
	{
		removePreview();
		createPreview(cameraNo);
	}

	
	
	
	
	
	
	private void removePreview()
	{
		removePreviewListeners();
		removeAllViews();
	}
	
	private void createPreview(int cameraNo)
	{
		preview = new CapturePreview(getContext(), cameraNo);
		preview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
		addView(preview);
		addPreviewListeners();
	}
	
	private void addPreviewListeners(){
		preview.onCreated.addListener(onCreatedListener);
		preview.onVideoCaptureError.addListener(onVideoCaptureErrorListener);
		preview.onVideoCaptureInfo.addListener(onVideoCaptureInfoListener);
		preview.onVideoCaptureStarted.addListener(onVideoCaptureStartedListener);
		preview.onVideoCaptureStopped.addListener(onVideoCaptureStoppedListener);
	}
	
	private void removePreviewListeners(){
		preview.onCreated.removeListener(onCreatedListener);
		preview.onVideoCaptureError.removeListener(onVideoCaptureErrorListener);
		preview.onVideoCaptureInfo.removeListener(onVideoCaptureInfoListener);
		preview.onVideoCaptureStarted.removeListener(onVideoCaptureStartedListener);
		preview.onVideoCaptureStopped.removeListener(onVideoCaptureStoppedListener);
	}

	public void startVideoCapture()
	{
		preview.startVideoCapture();
	}

	public void stopVideoCapture()
	{
		preview.stopVideoCapture();
	}

	public int getMaxCameraNo()
	{
		return preview.getMaxCameraNo();
	}
}
