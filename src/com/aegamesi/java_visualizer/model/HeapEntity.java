package com.aegamesi.java_visualizer.model;

import java.io.Serializable;

public class HeapEntity implements Serializable {
	public long id;
	public Type type;
	public String label;

	public enum Type {
		LIST, SET, MAP, OBJECT, PRIMITIVE
	}
}
