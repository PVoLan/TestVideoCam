package com.example.testvideocam;

import android.content.*;
import android.util.*;
import android.widget.*;

public class CapturePreviewWrapper extends FrameLayout
{
	private CapturePreview preview;

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
		createPreview(0);
	}

		
	public CapturePreview getPreview()
	{
		return preview;
	}
	
	public void recreatePreview(int cameraNo)
	{
		removeAllViews();
		createPreview(cameraNo);
	}
	
	
	private void createPreview(int cameraNo)
	{
		preview = new CapturePreview(getContext(), cameraNo);
		preview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
		addView(preview);
	}
}
