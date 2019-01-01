package com.aegamesi.java_visualizer.model;

import java.io.Serializable;

public abstract class HeapEntity implements Serializable {
	public long id;
	public Type type;
	public String label;

	public enum Type {
		LIST, SET, MAP, OBJECT, PRIMITIVE
	}

	public abstract boolean hasSameStructure(HeapEntity other);
}
