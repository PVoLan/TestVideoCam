package com.example.testvideocam;

import java.io.IOException;

import com.example.testvideocam.CapturePreview.*;

import ru.pvolan.event.*;
import ru.pvolan.trace.*;

import android.os.*;
import android.app.*;
import android.content.res.*;
import android.hardware.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {

	
	CapturePreviewWrapper previewCamera;
	Button buttonRec;
	Button buttonStop;
	Button buttonSwitch;
	private int currentCameraNo;
	private boolean isRecordingStarted;

	//Camera camera;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Trace.Print("----------------------------- MainActivity onCreate");
        
        setContentView(R.layout.activity_main);
        
        //TODO No camera, no sd...
        
        //TODO Async calls?
        
        //TODO Front camera exist?
        
        previewCamera = (CapturePreviewWrapper) findViewById(R.id.previewCamera);
        buttonRec = (Button) findViewById(R.id.buttonRec);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonSwitch = (Button) findViewById(R.id.buttonSwitch);
        
        
        previewCamera.getPreview().onCreated.addListener(new CustomEventListener() {			
			@Override
			public void onEvent() {
				//camera = previewCamera.getCamera();				
			}
		});
        
        previewCamera.getPreview().onVideoCaptureStarted.addListener(new CustomEventListener() {			
			@Override
			public void onEvent() {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						onVideoCaptureStarted();	
					}
				});								
			}
		});  
        
        previewCamera.getPreview().onVideoCaptureStopped.addListener(new CustomEventListener() {			
			@Override
			public void onEvent() {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						onVideoCaptureStopped();
					}
				});					
			}
		});  
        
        previewCamera.getPreview().onVideoCaptureInfo.addListener(new ParametrizedCustomEventListener<MediaRecorderInfo>() {
			
			@Override
			public void onEvent(final MediaRecorderInfo param) {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						onVideoCaptureInfo(param);
					}
				});
			}
		});
        
        
        previewCamera.getPreview().onVideoCaptureError.addListener(new ParametrizedCustomEventListener<Throwable>() {
			
			@Override
			public void onEvent(final Throwable param) {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						onVideoCaptureError(param);
					}
				});
			}
		});
       
        
        buttonRec.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onButtonRecClick();
			}
		});
        
        buttonStop.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onButtonStopClick();
			}
		});
        
        buttonSwitch.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onButtonSwitchClick();
			}
		});
    }

    @Override
    protected void onResume() 
    {
    	Trace.Print("----------------------------- MainActivity onResume");
    	super.onResume();
    	
    }
    
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	Trace.Print("----------------------------- MainActivity onPause");
    	
    	super.onPause();
    	
    }
    
    @Override
    protected void onDestroy()
    {
    	Trace.Print("----------------------------- MainActivity onDestroy");
    	super.onDestroy();
    }
       
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    //*****************************************************************************


	


	private void onButtonRecClick() 
    {
		previewCamera.getPreview().startVideoCapture();
	}
    
    
    private void onButtonStopClick() 
    {
		previewCamera.getPreview().stopVideoCapture();
	}
    
    private void onButtonSwitchClick() 
    {
    	boolean needRestartCapture = isRecordingStarted;
    	
    	if(needRestartCapture)
		{
			previewCamera.getPreview().stopVideoCapture();			
		}
		
		currentCameraNo++;
		if(currentCameraNo > previewCamera.getPreview().getMaxCameraNo())
		{
			currentCameraNo=0;
		}
		
		
				
		previewCamera.recreatePreview(currentCameraNo);
		
		if(needRestartCapture)
		{
			Trace.Print("++++++++++++++++++++++++once listener added");
			previewCamera.getPreview().onCreated.addListener(new CustomEventListener() {
				
				@Override
				public void onEvent()
				{
					Trace.Print("++++++++++++++++++++++++once listener call");
					previewCamera.getPreview().startVideoCapture();
					previewCamera.getPreview().onCreated.removeListener(this);
				}
			});
		}
		
			
		
	}
    
    protected void onVideoCaptureError(Throwable param) 
    {
    	
    	if(param instanceof CaptureException)
    	{
    		MediaRecorderInfo info = ((CaptureException)param).getInfo();
    		Toast.makeText(this, String.format("Error: What %d, Extra %", info.getWhat(), info.getExtra()), Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
    		Toast.makeText(this, param.getMessage(), Toast.LENGTH_SHORT).show();
    	}
    	
	}


	protected void onVideoCaptureInfo(MediaRecorderInfo param) 
	{
		Toast.makeText(this, String.format("Info: What %d, Extra %", param.getWhat(), param.getExtra()), Toast.LENGTH_SHORT).show();		
	}
	
	protected void onVideoCaptureStopped() 
	{
		buttonRec.setVisibility(View.VISIBLE);
		buttonStop.setVisibility(View.GONE);	
		isRecordingStarted = false;
	}

	protected void onVideoCaptureStarted() 
	{
		buttonRec.setVisibility(View.GONE);
		buttonStop.setVisibility(View.VISIBLE);
		isRecordingStarted = true;
	}
    
    
    
    
}
