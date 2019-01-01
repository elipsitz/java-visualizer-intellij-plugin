package com.aegamesi.java_visualizer.model;

import java.util.ArrayList;
import java.util.List;

public class HeapList extends HeapEntity {
	public List<Value> items = new ArrayList<>();

	@Override
	public boolean hasSameStructure(HeapEntity other) {
		if (other instanceof HeapList) {
			return items.size() == ((HeapList) other).items.size();
		}
		return false;
	}
}
