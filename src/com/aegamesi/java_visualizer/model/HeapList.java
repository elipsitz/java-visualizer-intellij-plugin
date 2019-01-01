package com.aegamesi.java_visualizer.model;

import java.util.ArrayList;
import java.util.List;

public class HeapList extends HeapEntity {
	public List<Value> items = new ArrayList<>();

	@Override
	public List<Value> getContainedValues() {
		return items;
	}
}
