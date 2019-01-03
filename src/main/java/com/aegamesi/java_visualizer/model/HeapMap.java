package com.aegamesi.java_visualizer.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HeapMap extends HeapEntity{
	public List<Pair> pairs = new ArrayList<>();

	public static class Pair {
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

	@Override
	JSONObject toJson() {
		JSONObject o = super.toJson();
		o.put("keys", pairs.stream().map(p -> p.key.toJson()).toArray());
		o.put("vals", pairs.stream().map(p -> p.val.toJson()).toArray());
		return o;
	}

	static HeapMap fromJson(JSONObject o) {
		HeapMap e = new HeapMap();

		JSONArray keys = o.getJSONArray("keys");
		JSONArray vals = o.getJSONArray("vals");
		for (int i = 0; i < keys.length(); i++) {
			Pair p = new Pair();
			p.key = Value.fromJson(keys.getJSONArray(i));
			p.val = Value.fromJson(vals.getJSONArray(i));
			e.pairs.add(p);
		}
		return e;
	}
}
