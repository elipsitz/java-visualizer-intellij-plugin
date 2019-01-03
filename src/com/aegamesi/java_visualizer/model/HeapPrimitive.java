package com.aegamesi.java_visualizer.model;

import org.json.JSONObject;

public class HeapPrimitive extends HeapEntity {
	public Value value;

	@Override
	public boolean hasSameStructure(HeapEntity other) {
		return other instanceof HeapPrimitive;
	}

	@Override
	JSONObject toJson() {
		JSONObject o = super.toJson();
		o.put("val", value.toJson());
		return o;
	}

	static HeapPrimitive fromJson(JSONObject o) {
		HeapPrimitive e = new HeapPrimitive();
		e.value = Value.fromJson(o.getJSONArray("val"));
		return e;
	}
}
