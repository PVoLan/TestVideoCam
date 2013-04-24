package com.example.testvideocam;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ru.pvolan.event.ParametrizedCustomEvent;
import ru.pvolan.trace.Trace;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.*;
import android.view.SurfaceHolder;



/**
 * CameraManager - собственно, класс, который менеджит управление камерой. Единственный класс, имеющий доступ к android.hardware.Camera
 * Умеет создавать-уничтожать камеру по её номеру, настраивать размер, запускать-останавливать превью, запускать-устанавливать съемку.
 * а также оповещать кого-нибудь об ошибках записи
 * Единовременно работает только с одной камерой
 */
public class CameraManager
{
	//Камера и её номер. Актуальны только в момент, когда камера существует
	private Camera camera;
	private int cameraNo;

	//Рекордер и выходной файл для записи. Актуальны только в момент, когда ведется запись
	private MediaRecorder mediaRecorder;
	private File outputFile;

	//Евенты об ошибках записи и информационных сообщениях. (см ru.pvolan.event) В случае ошибки передается Throwable
	//в случае инфо - контейнер MediaRecorderInfo (внутри содержит what и extra)
	//onVideoCaptureError надо бы переименовать, ибо он кидает ошибки не только Capture
	public ParametrizedCustomEvent<Throwable> onVideoCaptureError = new ParametrizedCustomEvent<Throwable>();
	public ParametrizedCustomEvent<MediaRecorderInfo> onVideoCaptureInfo = new ParametrizedCustomEvent<MediaRecorderInfo>();



	public CameraManager()
	{
		super();
	}


	//Просто создаем камеру по её номеру
	public void createCamera(int cameraNo)
	{
		try
		{
			this.cameraNo = cameraNo;
			camera = Camera.open(cameraNo);
			Trace.Print("Camera created " + cameraNo);
		}
		catch (Exception exception)
		{
			Trace.Print(exception);
			camera.release();
			camera = null;

			onVideoCaptureError.fire(exception);
		}
	}


	//Уничтожаем камеру. Предполагается, что камера к тому моменту была создана, хотя проверка на null тут не помешала бы
	public void destroyCamera()
	{
		camera.stopPreview();
		camera.setPreviewCallback(null);
		camera.release();
		camera = null;
		Trace.Print("Camera destroyed " + cameraNo);
	}


	//Приблизительно подгоняем размер превью под заданные. Заодно прицепляем камеру к SurfaceView через холдер
	//Метод надо бы переименовать
	public void updateCameraSizeApproximately(SurfaceHolder holder, int w, int h)
	{
		Camera.Parameters parameters = camera.getParameters();

		//Собственно, получаем досутпгые размеры
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size max = camera.new Size(0, 0);

		//Ищем подходящий размер
		for (Camera.Size size : previewSizes)
		{

			Trace.Print("Camera " + cameraNo + " Available size " + max.width
					+ "x" + max.height);

			if (size.width <= w && size.height <= h
					&& (max.width < size.width || max.height < size.height))
			{
				max = size;
			}

		}

		Trace.Print("max " + max.width + "x" + max.height);

		// TODO Excepional devices

		//Устанавливаем поворот
		parameters.setRotation(90);
		parameters.set("orientation", "portrait");
		camera.setDisplayOrientation(90);
		
		//Устанавливаем размер
		parameters.setPreviewSize(max.width, max.height);
		
		
		camera.setParameters(parameters);

		//Устанавливаем SurfaceView
		try
		{
			camera.setPreviewDisplay(holder);
		}
		catch (IOException exception)
		{
			Trace.Print(exception);
			camera.release();
			camera = null;

			onVideoCaptureError.fire(exception);
		}
	}


	//Метод начала записи. Хм, а почему он кидает ексепшн? Нехорошо, должен кидать onVideoCaptureError
	public File startRecording(SurfaceHolder holder) throws IOException
	{
		//Куча стандартных действий...
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setCamera(camera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setProfile(CamcorderProfile.get(cameraNo,
				CamcorderProfile.QUALITY_HIGH));

		//Определение выходного файла
		outputFile = MediaFileManager.getOutputVideoFile();
		if (outputFile == null)
		{
			throw new RuntimeException("Cannot create output file");
		}

		mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
		
		//Выставляем Surface
		mediaRecorder.setPreviewDisplay(holder.getSurface());

		// Выставляем поворот
		// TODO Exceptional devices
		mediaRecorder.setOrientationHint(90);

		// Выставляем размер. Грубо выставляем
		mediaRecorder.setVideoSize(640, 480); // TODO ????

		//Прицепляем обработчики системных сообщений
		mediaRecorder.setOnErrorListener(new OnErrorListener() {
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

		
		//Далее стандартно
		mediaRecorder.prepare();

		Trace.Print("#######################START RECORD#####################################");
		mediaRecorder.start();

		return outputFile;
	}


	//Остановка записи
	public void stopRecording()
	{
		Trace.Print("#######################STOP RECORD#####################################");
		try
		{
			mediaRecorder.stop();
		}
		catch (RuntimeException e)
		{
			//Удаляем файл, если в него ничего не записалось
			if (outputFile != null)
			{
				if (outputFile.exists())
					outputFile.delete();
			}
		}

		//Освобождаем ресурсы
		mediaRecorder.reset();
		mediaRecorder.release();
		mediaRecorder = null;
	}


	//Тривиально. Т.к. лок требуется не всегда, передаем его через параметр
	public void startPreview(boolean lockRequired)
	{
		if (lockRequired)
			camera.lock();
		camera.startPreview();
	}


	//Тривиально. Т.к. лок требуется не всегда, передаем его через параметр
	public void stopPreview(boolean unlockRequired)
	{
		camera.stopPreview();
		if (unlockRequired)
			camera.unlock();
	}


	//Тривиально.
	public int getMaxCameraNo()
	{
		return Camera.getNumberOfCameras() - 1;
	}
}
