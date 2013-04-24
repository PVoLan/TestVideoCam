package com.example.testvideocam;

import java.io.*;

import ru.pvolan.event.*;
import ru.pvolan.trace.*;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

//Собственно вью, который показываает нам изображение с камеры, и заодно немного управляет съемкой
//При создании себя создает камеру (через менеджер, номер камеры задается извне) и запускает превью автоматически.
//Запись запускается по запросу.
public class CapturePreview extends SurfaceView implements
		SurfaceHolder.Callback
{

	// private fields *************************
	
	//Очевидно
	private CameraManager manager;

	//Очевидно
	private SurfaceHolder holder;
	private int previewWidth = 0;
	private int previewHeight = 0;
	private int cameraNo = 0;
	private boolean isRecordingStarted = false;

	// public events **************************
	
	//Вью и камера создана (случается асинхронно)
	public CustomEvent onCreated = new CustomEvent(); 
	
	//Прокси ошибок MediaRecorder'a
	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();
	
	//Старт-стоп записи (по факту синхронно, но с расчетом на возможную асинхронность)
	public CustomEvent onVideoCaptureStarted = new CustomEvent();
	public CustomEvent onVideoCaptureStopped = new CustomEvent();



	// init **************************************************

	CapturePreview(Context context, int cameraNo)
	{
		super(context);
		this.cameraNo = cameraNo; //Сама камера создастся потом, пока нужно только сохранить номер
		init();
	}



	private void init()
	{
		//Создаем менеджера
		manager = new CameraManager();

		//Цепляем прокси событий MediaRecorder'a
		manager.onVideoCaptureError
				.addListener(new ParametrizedCustomEventListener<Throwable>() {
					@Override
					public void onEvent(Throwable param)
					{
						onVideoCaptureError.fire(param);
					}
				});

		manager.onVideoCaptureInfo
				.addListener(new ParametrizedCustomEventListener<MediaRecorderInfo>() {

					@Override
					public void onEvent(MediaRecorderInfo param)
					{
						onVideoCaptureInfo.fire(param);
					}
				});

		
		// Прям вот так стандартно
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}



	// SurfaceHolder.Callback implementation *******************
	// Почти стандартно

	public void surfaceCreated(SurfaceHolder holder)
	{
		Trace.Print("surfaceCreated");
		// The Surface has been created, acquire the camera and tell it where
		// to draw.

		//Ммм, кстати, тут должно что-то возвращаться, хотя бы boolean. 
		//Иначе onCreated файрится, даже если камера не создана
		manager.createCamera(cameraNo);

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
			//Ай-яй-яй, внутри все-таки нужна проверка на null, иначе несимметрично
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
			//Приостанавливаем превью на всякий случай
			manager.stopPreview(false); //Обращаю внимание, лок не делается
			
			//Выствляем размер
			previewWidth = w;
			previewHeight = h;
			manager.updateCameraSizeApproximately(holder, previewWidth,
					previewHeight);
			
			//Запускам превью
			manager.startPreview(false); //Обращаю внимание, лок не делается
		}
		catch (Exception e)
		{
			Trace.Print(e);
			onVideoCaptureError.fire(e);
		}
	}



	// publics *********************************************

	//Запуск записи видео
	public void startVideoCapture()
	{
		try
		{
			//Останавливаем превью
			manager.stopPreview(true); //Лок!
			
			//Запускаем запись
			File outputFile = manager.startRecording(holder);

			//Делаем тост. Вообще, тост должна делать активити, но было не до того
			// TODO move out of here
			Toast.makeText(getContext(),
					"Recording to " + outputFile.getAbsolutePath(),
					Toast.LENGTH_LONG).show();
		}
		catch (Exception ex)
		{
			Trace.Print(ex);
			onVideoCaptureError.fire(ex);
			
			//Если не получилось, возвращаемся на исходную.
			manager.stopRecording();
			manager.startPreview(true);
			return;
		}

		//Если получилось, отмечаемся
		isRecordingStarted = true;
		onVideoCaptureStarted.fire();
	}


	//Остановка записи
	public void stopVideoCapture()
	{
		manager.stopRecording();
		manager.startPreview(true); //Лок!

		isRecordingStarted = false;
		onVideoCaptureStopped.fire();
	}


	//Тривиально
	public int getMaxCameraNo()
	{
		return manager.getMaxCameraNo();
	}

}
