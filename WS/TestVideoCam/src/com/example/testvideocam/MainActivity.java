package com.example.testvideocam;

import java.io.IOException;

import com.example.testvideocam.CapturePreview.*;

import ru.pvolan.event.*;

import android.os.*;
import android.app.*;
import android.content.res.*;
import android.hardware.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {

	
	CapturePreview previewCamera;
	Button buttonRec;
	Button buttonStop;
	Button buttonSwitch;

	//Camera camera;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //TODO No camera, no sd...
        
        //TODO Async calls?
        
        //TODO Front camera exist?
        
        previewCamera = (CapturePreview) findViewById(R.id.previewCamera);
        buttonRec = (Button) findViewById(R.id.buttonRec);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonSwitch = (Button) findViewById(R.id.buttonSwitch);
        
        
        previewCamera.onCreated.addListener(new CustomEventListener() {			
			@Override
			public void onEvent() {
				//camera = previewCamera.getCamera();				
			}
		});
        
        previewCamera.onVideoCaptureStarted.addListener(new CustomEventListener() {			
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
        
        previewCamera.onVideoCaptureStopped.addListener(new CustomEventListener() {			
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
        
        previewCamera.onVideoCaptureInfo.addListener(new ParametrizedCustomEventListener<MediaRecorderInfo>() {
			
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
        
        
        previewCamera.onVideoCaptureError.addListener(new ParametrizedCustomEventListener<Throwable>() {
			
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
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    }
    
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	
    	
    	super.onPause();
    	
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
		previewCamera.startVideoCapture();
	}
    
    
    private void onButtonStopClick() 
    {
		previewCamera.stopVideoCapture();
	}
    
    private void onButtonSwitchClick() 
    {
		previewCamera.switchCamera();
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
	}

	protected void onVideoCaptureStarted() 
	{
		buttonRec.setVisibility(View.GONE);
		buttonStop.setVisibility(View.VISIBLE);		
	}
    
    
    
    
}
