package com.aegamesi.java_visualizer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeapMap extends HeapEntity{
	public List<Pair> pairs = new ArrayList<>();

	public static class Pair implements Serializable {
		public Value key;
		public Value val;
	}

	@Override
	public boolean hasSameStructure(HeapEntity other) {
		if (other instanceof HeapMap) {
			return pairs.size() == ((HeapMap) other).pairs.size();
		}
		return false;
	}
}
