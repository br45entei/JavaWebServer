package com.gmail.br45entei.server.data;

public final class Property<T> {
	private final String				name;
	private volatile T					value;
	private volatile boolean			isLocked	= false;
	
	private String						description	= "";
	
	public Property(String name) {
		this.name = name;
	}
	
	public Property(String name, T value) {
		this(name);
		this.value = value;
	}
	
	public final String getName() {
		return this.name;
	}
	
	public final T getValue() {
		return this.value;
	}
	
	public final Property<T> setValue(T value) {
		if(!this.isLocked) {
			this.value = value;
		}
		return this;
	}
	
	public final Property<T> lockValue() {
		this.isLocked = true;
		return this;
	}
	
	public final String getDescription() {
		return this.description;
	}
	
	public final Property<T> setDescription(String description) {
		this.description = description;
		return this;
	}
	
}