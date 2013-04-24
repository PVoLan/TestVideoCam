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

/**
 * Просто активити. Содержит вьюху-камеру и три кнопки - запись-останов, перключение камеры
 * Запись-останов видимы попеременно.
 * Также активити помнит текщий номер камеры и сама определяет, куда переключаться по switch'у
 */
public class MainActivity extends Activity
{

	//Обернутая SurfaceView
	CapturePreviewWrapper previewCamera;
	
	//Кнопки
	Button buttonRec;
	Button buttonStop;
	Button buttonSwitch;
	
	//Состояние записи
	private int currentCameraNo;
	private boolean isRecordingStarted;



	// Camera camera;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Trace.Print("----------------------------- MainActivity onCreate");
		
		// TODO No camera, no sd...

		// TODO Async calls?

		// TODO Front camera exist?
		
		//Инициализация лейаута

		setContentView(R.layout.activity_main);

		previewCamera = (CapturePreviewWrapper) findViewById(R.id.previewCamera);
		buttonRec = (Button) findViewById(R.id.buttonRec);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		buttonSwitch = (Button) findViewById(R.id.buttonSwitch);

		
		//Цепляем обработчики к сообщениям с камеры
		
		previewCamera.onCreated.addListener(new CustomEventListener() {
			@Override
			public void onEvent()
			{
				// camera = previewCamera.getCamera();
			}
		});

		previewCamera.onVideoCaptureStarted
				.addListener(new CustomEventListener() {
					@Override
					public void onEvent()
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								onVideoCaptureStarted();
							}
						});
					}
				});

		previewCamera.onVideoCaptureStopped
				.addListener(new CustomEventListener() {
					@Override
					public void onEvent()
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								onVideoCaptureStopped();
							}
						});
					}
				});

		previewCamera.onVideoCaptureInfo
				.addListener(new ParametrizedCustomEventListener<MediaRecorderInfo>() {

					@Override
					public void onEvent(final MediaRecorderInfo param)
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								onVideoCaptureInfo(param);
							}
						});
					}
				});

		previewCamera.onVideoCaptureError
				.addListener(new ParametrizedCustomEventListener<Throwable>() {

					@Override
					public void onEvent(final Throwable param)
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run()
							{
								onVideoCaptureError(param);
							}
						});
					}
				});

		
		//Цепляем обработчи к кнопкам
		
		buttonRec.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				onButtonRecClick();
			}
		});

		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				onButtonStopClick();
			}
		});

		buttonSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				onButtonSwitchClick();
			}
		});
	}


	// Несколько переопределений чисто для цели логгирования

	@Override
	protected void onResume()
	{
		Trace.Print("----------------------------- MainActivity onResume");
		super.onResume();

	}



	@Override
	protected void onPause()
	{
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


	//Атавизм от шаблона, созданного эклипсом

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	// *****************************************************************************

	//Обработчики кнопок примитивны
	
	private void onButtonRecClick()
	{
		previewCamera.startVideoCapture();
	}



	private void onButtonStopClick()
	{
		previewCamera.stopVideoCapture();
	}



	//Перелючение камеры чуть сложнее
	private void onButtonSwitchClick()
	{
		boolean needRestartCapture = isRecordingStarted;

		//Приостанавливаем запись
		if (needRestartCapture)
		{
			previewCamera.stopVideoCapture();
		}

		//Переключаем камеру
		currentCameraNo++;
		if (currentCameraNo > previewCamera.getMaxCameraNo())
		{
			currentCameraNo = 0;
		}

		//Пересоздаем SurfaceView (вместе с ним пересоздается CameraManager и сама камера)
		previewCamera.recreatePreview(currentCameraNo);

		//Запускаем запись обратно, если нужно. Асинхронно. Возможно, надо поднять этот вызов над recreatePreview.
		if (needRestartCapture)
		{
			Trace.Print("++++++++++++++++++++++++once listener added");
			previewCamera.onCreated.addListener(new CustomEventListener() {

				@Override
				public void onEvent()
				{
					Trace.Print("++++++++++++++++++++++++once listener call");
					previewCamera.startVideoCapture();
					previewCamera.onCreated.removeListener(this);
				}
			});
		}

	}


	//Тосты по ошибкам/сообщениям
	protected void onVideoCaptureError(Throwable param)
	{

		if (param instanceof CaptureException)
		{
			MediaRecorderInfo info = ((CaptureException) param).getInfo();
			Toast.makeText(
					this,
					String.format("Error: What %d, Extra %", info.getWhat(),
							info.getExtra()), Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, param.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}



	protected void onVideoCaptureInfo(MediaRecorderInfo param)
	{
		Toast.makeText(
				this,
				String.format("Info: What %d, Extra %", param.getWhat(),
						param.getExtra()), Toast.LENGTH_SHORT).show();
	}



	//Переключение кнопок по старту-стопу записи.
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
