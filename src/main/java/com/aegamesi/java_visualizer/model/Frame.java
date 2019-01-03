package com.aegamesi.java_visualizer.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class Frame {
	public String name;
	public boolean internal;
	public Map<String, Value> locals = new TreeMap<>();

	JSONObject toJson() {
		JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("local_names", locals.keySet());
		o.put("local_vals", locals.values().stream().map(Value::toJson).toArray());
		return o;
	}

	static Frame fromJson(JSONObject o) {
		Frame f = new Frame();
		f.name = o.getString("name");
		JSONArray jsonLocalNames = o.getJSONArray("local_names");
		JSONArray jsonLocalVals = o.getJSONArray("local_vals");
		for (int i = 0; i < jsonLocalNames.length(); i++) {
			f.locals.put(jsonLocalNames.getString(i), Value.fromJson(jsonLocalVals.getJSONArray(i)));
		}
		return f;
	}
}
