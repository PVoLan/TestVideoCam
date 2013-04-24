package ru.pvolan.event;


import java.util.*;

//То же, что CustomEvent, но с параметром
public class ParametrizedCustomEvent<T> {
	ArrayList<ParametrizedCustomEventListener<T>> listeners;
	
	public ParametrizedCustomEvent() {
		listeners = new ArrayList<ParametrizedCustomEventListener<T>>();		
	}
	
	Object sync = new Object();
	
	public void addListener(ParametrizedCustomEventListener<T> listener){
		synchronized (sync) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(ParametrizedCustomEventListener<T> listener){
		synchronized (sync) {
			listeners.remove(listener);
		}
	}
	
	public void fire(T param){
		synchronized (sync) {
			for (ParametrizedCustomEventListener<T> listener : listeners) {
				listener.onEvent(param);			
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
