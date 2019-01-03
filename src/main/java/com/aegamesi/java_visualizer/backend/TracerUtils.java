package com.aegamesi.java_visualizer.backend;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

class TracerUtils {
	static com.sun.jdi.Value invokeSimple(ThreadReference thread, ObjectReference r, String name) {
		try {
			return r.invokeMethod(thread, r.referenceType().methodsByName(name).get(0), Collections.emptyList(), 0);
		} catch (Exception e) {
			return null;
		}
	}

	static Iterator<Value> getIterator(ThreadReference thread, ObjectReference obj) {
		ObjectReference i = (ObjectReference) invokeSimple(thread, obj, "iterator");
		return new Iterator<com.sun.jdi.Value>() {
			@Override
			public boolean hasNext() {
				return ((BooleanValue) invokeSimple(thread, i, "hasNext")).value();
			}

			@Override
			public com.sun.jdi.Value next() {
				return invokeSimple(thread, i, "next");
			}
		};
	}

	static boolean doesImplementInterface(ObjectReference obj, String iface) {
		if (obj.referenceType() instanceof ClassType) {
			Queue<InterfaceType> queue = new LinkedList<>(((ClassType) obj.referenceType()).interfaces());
			while (!queue.isEmpty()) {
				InterfaceType t = queue.poll();
				if (t.name().equals(iface)) {
					return true;
				}
				queue.addAll(t.superinterfaces());
			}
		}
		return false;
	}

	// TODO clean up!!
	static String displayNameForType(ObjectReference obj) {
		String fullName = obj.referenceType().name();
		if (fullName.indexOf("$") > 0) {
			// inner, local, anonymous or lambda class
			if (fullName.contains("$$Lambda")) {
				fullName = "&lambda;" + fullName.substring(fullName.indexOf("$$Lambda") + 9); // skip $$lambda$
				try {
					String interf = ((ClassType) obj.referenceType()).interfaces().get(0).name();
					if (interf.startsWith("java.util.function."))
						interf = interf.substring(19);

					fullName += " [" + interf + "]";
				} catch (Exception e) {
				}
			}
			// more cases here?
			else {
				fullName = fullName.substring(1 + fullName.indexOf('$'));
				if (fullName.matches("[0-9]+"))
					fullName = "anonymous class " + fullName;
				else if (fullName.substring(0, 1).matches("[0-9]+"))
					fullName = "local class " + fullName.substring(1);
			}
		}
		return fullName;
	}
}
