package com.aegamesi.java_visualizer.model;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Frame implements Serializable {
	public String name;
	public boolean internal;
	public Map<String, Value> locals = new TreeMap<>();
}
