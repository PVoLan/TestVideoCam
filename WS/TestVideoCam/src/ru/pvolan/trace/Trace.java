package ru.pvolan.trace;

import android.util.Log;


//Чуть более удобный Log.i()
public class Trace {
	private static String tag = "TestVideoCam";
	
	public static void Print(Throwable t){
		if(t == null) Log.i(tag, "null");
		else Log.i(tag, "Exception", t);
	}
	
	public static void Print(Object anything){
		if(anything == null) Log.i(tag, "null");
		else Log.i(tag, anything.toString());
	}
	
	public static void Print(int i){
		Log.i(tag, Integer.toString(i));
	}
	
	public static void Print(long i){
		Log.i(tag, Long.toString(i));
	}
	
	public static void Print(float i){
		Log.i(tag, Float.toString(i));
	}
	
	public static void Print(double i){
		Log.i(tag, Double.toString(i));
	}
	
	public static void Print(boolean i){
		Log.i(tag, Boolean.toString(i));
	}
}
