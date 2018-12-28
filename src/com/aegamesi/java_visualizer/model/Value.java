package com.aegamesi.java_visualizer.model;

import java.io.Serializable;

public class Value implements Serializable {
	// primitive or reference
	public Type type;
	public long longValue;
	public double doubleValue;
	public boolean booleanValue;
	public String stringValue;
	public char charValue;
	public long reference;

	@Override
	public String toString() {
		switch (type) {
			case NULL:
				return "null";
			case STRING:
				return "\"" + stringValue + "\"";
			case LONG:
				return Long.toString(longValue);
			case DOUBLE:
				return Double.toString(doubleValue);
			case BOOLEAN:
				return Boolean.toString(booleanValue);
			case CHAR:
				return "'" + charValue + "'";
			case REFERENCE:
				return "*REF*";
			default:
				return "<?>";
		}
	}

	public enum Type {
		NULL, VOID, LONG, DOUBLE, BOOLEAN, STRING, CHAR, REFERENCE;
	}
}
