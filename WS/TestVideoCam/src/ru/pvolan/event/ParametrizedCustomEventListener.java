package ru.pvolan.event;

public interface ParametrizedCustomEventListener<T> {
	public void onEvent(T param);
}
