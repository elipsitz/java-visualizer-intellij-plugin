package com.aegamesi.java_visualizer.model;

import org.json.JSONArray;
import org.json.JSONObject;

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

	@Override
	JSONObject toJson() {
		JSONObject o = super.toJson();
		o.put("items", items.stream().map(Value::toJson).toArray());
		return o;
	}

	static HeapList fromJson(JSONObject o) {
		HeapList e = new HeapList();
		for (Object item : o.getJSONArray("items")) {
			e.items.add(Value.fromJson((JSONArray) item));
		}
		return e;
	}
}
