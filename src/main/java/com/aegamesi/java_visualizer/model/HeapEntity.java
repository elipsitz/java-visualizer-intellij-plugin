package com.aegamesi.java_visualizer.model;

import org.json.JSONObject;

public abstract class HeapEntity {
	public long id;
	public Type type;
	public String label;

	public enum Type {
		LIST, SET, MAP, OBJECT, PRIMITIVE
	}

	public abstract boolean hasSameStructure(HeapEntity other);

	JSONObject toJson() {
		JSONObject o = new JSONObject();
		o.put("id", id);
		o.put("type", type.name());
		o.put("label", label);
		return o;
	}

	static HeapEntity fromJson(JSONObject o) {
		Type type = Type.valueOf(o.getString("type"));
		HeapEntity e = null;
		switch (type) {
			case LIST:
			case SET:
				e = HeapList.fromJson(o);
				break;
			case OBJECT:
				e = HeapObject.fromJson(o);
				break;
			case MAP:
				e = HeapMap.fromJson(o);
				break;
			case PRIMITIVE:
				e = HeapPrimitive.fromJson(o);
				break;
		}
		e.id = o.getLong("id");
		e.type = type;
		e.label = o.getString("label");
		return e;
	}
}
