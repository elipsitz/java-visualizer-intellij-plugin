package com.aegamesi.java_visualizer.model;

import java.io.Serializable;
import java.util.List;

public abstract class HeapEntity implements Serializable {
	public long id;
	public Type type;
	public String label;

	public enum Type {
		LIST, SET, MAP, OBJECT, PRIMITIVE
	}

	public abstract List<Value> getContainedValues();
}
