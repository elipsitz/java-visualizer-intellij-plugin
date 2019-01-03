package com.aegamesi.java_visualizer.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExecutionTrace {
	public List<Frame> frames = new ArrayList<>();
	public Map<Long, HeapEntity> heap = new TreeMap<>();
	public Map<String, Value> statics = new TreeMap<>();

	public String toJsonString() {
		JSONObject obj = new JSONObject();
		obj.put("frames", frames.stream().map(Frame::toJson).toArray());
		obj.put("heap", heap.values().stream().map(HeapEntity::toJson).toArray());
		return obj.toString();
	}

	public static ExecutionTrace fromJsonString(String str) {
		JSONObject o = new JSONObject(str);
		ExecutionTrace trace = new ExecutionTrace();
		for (Object s : o.getJSONArray("frames")) {
			trace.frames.add(Frame.fromJson((JSONObject) s));
		}
		for (Object s : o.getJSONArray("heap")) {
			HeapEntity e = HeapEntity.fromJson((JSONObject) s);
			trace.heap.put(e.id, e);
		}
		return trace;
	}
}
