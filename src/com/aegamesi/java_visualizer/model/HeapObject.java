package com.aegamesi.java_visualizer.model;

import java.util.Map;
import java.util.TreeMap;

public class HeapObject extends HeapEntity {
	public Map<String, Value> fields = new TreeMap<>();

	@Override
	public boolean hasSameStructure(HeapEntity other) {
		if (other instanceof HeapObject) {
			return fields.size() == ((HeapObject) other).fields.size();
		}
		return false;
	}
}
