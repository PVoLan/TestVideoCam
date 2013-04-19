package ru.pvolan.event;


import java.util.*;

public class CustomEvent {
	ArrayList<CustomEventListener> listeners;
	
	public CustomEvent() {
		listeners = new ArrayList<CustomEventListener>();		
	}
	
	Object sync = new Object();
	
	public void addListener(CustomEventListener listener){
		synchronized (sync) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(CustomEventListener listener){
		synchronized (sync) {
			listeners.remove(listener);
		}
	}
	
	public void fire(){
		synchronized (sync) {
			for (CustomEventListener listener : listeners) {
				listener.onEvent();			
			}	
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		synchronized (sync) {
			listeners.clear();
		}
		super.finalize();
	}
}
