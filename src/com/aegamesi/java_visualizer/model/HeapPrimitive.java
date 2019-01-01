package com.aegamesi.java_visualizer.model;

import java.util.Arrays;
import java.util.List;

public class HeapPrimitive extends HeapEntity {
	public Value value;

	@Override
	public List<Value> getContainedValues() {
		return Arrays.asList(value);
	}
}
