package traceprinter;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import java.util.Collections;
import java.util.Iterator;

public class JDIUtils {
	public static Value invokeSimple(ThreadReference thread, ObjectReference r, String name) {
		try {
			return r.invokeMethod(thread, r.referenceType().methodsByName(name).get(0), Collections.emptyList(), 0);
		} catch (Exception e) {
			return null;
		}
	}

	public static Iterator<Value> getIterator(ThreadReference thread, ObjectReference obj) {
		ObjectReference i = (ObjectReference) JDIUtils.invokeSimple(thread, obj, "iterator");
		return new Iterator<Value>() {
			@Override
			public boolean hasNext() {
				return ((BooleanValue) JDIUtils.invokeSimple(thread, i, "hasNext")).value();
			}

			@Override
			public Value next() {
				return JDIUtils.invokeSimple(thread, i, "next");
			}
		};
	}
}
