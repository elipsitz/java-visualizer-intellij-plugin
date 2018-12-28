package com.aegamesi.java_visualizer.model;

import java.util.ArrayList;
import java.util.List;

public class HeapMap extends HeapEntity{
	public List<Pair> pairs = new ArrayList<>();

	public static class Pair {
		public Value key;
		public Value val;
	}
}
