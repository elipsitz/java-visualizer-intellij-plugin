package com.aegamesi.java_visualizer.model;

public class HeapEntity {
	public long id;
	public Type type;
	public String label;

	public enum Type {
		LIST, SET, MAP, OBJECT, PRIMITIVE
	}
}
