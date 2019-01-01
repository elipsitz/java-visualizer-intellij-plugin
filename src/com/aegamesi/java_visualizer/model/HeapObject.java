package com.aegamesi.java_visualizer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HeapObject extends HeapEntity {
	public Map<String, Value> fields = new TreeMap<>();

	@Override
	public List<Value> getContainedValues() {
		List<Value> l = new ArrayList<>();
		l.addAll(fields.values());
		return l;
	}
}
