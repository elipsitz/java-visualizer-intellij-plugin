package com.aegamesi.java_visualizer.model;

public class HeapPrimitive extends HeapEntity {
	public Value value;

	@Override
	public boolean hasSameStructure(HeapEntity other) {
		return other instanceof HeapPrimitive;
	}
}
