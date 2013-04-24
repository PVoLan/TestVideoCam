package com.example.testvideocam;

import android.media.MediaRecorder;

//Обертка над Info, которые возвращает MediaRecorder
public class MediaRecorderInfo
{
	private MediaRecorder mr;
	private int what;
	private int extra;



	public MediaRecorderInfo(MediaRecorder mr, int what, int extra)
	{
		super();
		this.mr = mr;
		this.what = what;
		this.extra = extra;
	}



	public MediaRecorder getMediaRecorder()
	{
		return mr;
	}



	public int getWhat()
	{
		return what;
	}



	public int getExtra()
	{
		return extra;
	}

}