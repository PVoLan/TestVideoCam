package com.example.testvideocam;

import java.io.IOException;

import com.example.testvideocam.CapturePreview.ErrorInfo;

import ru.pvolan.event.CustomEventListener;
import ru.pvolan.event.ParametrizedCustomEventListener;

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

	//Camera camera;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        previewCamera = (CapturePreview) findViewById(R.id.previewCamera);
        buttonRec = (Button) findViewById(R.id.buttonRec);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        
        
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
        
        previewCamera.onVideoCaptureInfo.addListener(new ParametrizedCustomEventListener<CapturePreview.ErrorInfo>() {
			
			@Override
			public void onEvent(final ErrorInfo param) {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						onVideoCaptureInfo(param);
					}
				});
			}
		});
        
        
        previewCamera.onVideoCaptureError.addListener(new ParametrizedCustomEventListener<CapturePreview.ErrorInfo>() {
			
			@Override
			public void onEvent(final ErrorInfo param) {
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
    
    protected void onVideoCaptureError(ErrorInfo param) 
    {
    	Toast.makeText(this, String.format("Error: What %d, Extra %", param.getWhat(), param.getExtra()), Toast.LENGTH_SHORT).show();	
	}


	protected void onVideoCaptureInfo(ErrorInfo param) 
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
