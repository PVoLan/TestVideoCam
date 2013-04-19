package com.example.testvideocam;

import android.media.MediaRecorder;


public class CaptureException extends Exception
{

	private MediaRecorderInfo info;



	public CaptureException(MediaRecorder mr, int what, int extra)
	{
		super();
		info = new MediaRecorderInfo(mr, what, extra);
	}



	public MediaRecorderInfo getInfo()
	{
		return info;
	}

}