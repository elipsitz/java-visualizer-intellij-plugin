package com.aegamesi.java_visualizer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExecutionTrace implements Serializable {
	public List<Frame> frames = new ArrayList<>();
	public Map<Long, HeapEntity> heap = new TreeMap<>();
	public Map<String, Value> statics = new TreeMap<>();
}
