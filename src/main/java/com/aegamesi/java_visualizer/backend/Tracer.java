package com.aegamesi.java_visualizer.backend;

import com.aegamesi.java_visualizer.model.ExecutionTrace;
import com.aegamesi.java_visualizer.model.Frame;
import com.aegamesi.java_visualizer.model.HeapEntity;
import com.aegamesi.java_visualizer.model.HeapList;
import com.aegamesi.java_visualizer.model.HeapMap;
import com.aegamesi.java_visualizer.model.HeapObject;
import com.aegamesi.java_visualizer.model.HeapPrimitive;
import com.aegamesi.java_visualizer.model.Value;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VoidValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.aegamesi.java_visualizer.backend.TracerUtils.displayNameForType;
import static com.aegamesi.java_visualizer.backend.TracerUtils.doesImplementInterface;
import static com.aegamesi.java_visualizer.backend.TracerUtils.getIterator;
import static com.aegamesi.java_visualizer.backend.TracerUtils.invokeSimple;

/**
 * Some code from traceprinter, written by David Pritchard (daveagp@gmail.com)
 */
public class Tracer {
	private static final String[] INTERNAL_PACKAGES = {
			"java.",
			"javax.",
			"sun.",
			"jdk.",
			"com.sun.",
			"com.intellij.",
			"com.aegamesi.java_visualizer.",
			"org.junit.",
			"jh61b.junit.",
			"jh61b.",
	};
	private static final List<String> BOXED_TYPES = Arrays.asList("Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "Boolean");
	private static final boolean SHOW_ALL_FIELDS = false;
	private static final List<ReferenceType> STATIC_LISTABLE = new ArrayList<>();

	private ThreadReference thread;
	private ExecutionTrace model;

	/*
	Converting actual heap objects requires running code on the suspended VM thread.
	However, once we start running code on the thread, we can no longer read frame locals.
	Therefore, we have to convert all heap objects at the very end.
	*/
	private TreeMap<Long, ObjectReference> pendingConversion = new TreeMap<>();

	public Tracer(ThreadReference thread) {
		this.thread = thread;
	}

	public ExecutionTrace getModel() throws IncompatibleThreadStateException {
		model = new ExecutionTrace();

		// Convert stack frame locals
		for (StackFrame frame : thread.frames()) {
			if (shouldShowFrame(frame)) {
				model.frames.add(convertFrame(frame));
			}
		}

		// Convert (some) statics
		for (ReferenceType rt : STATIC_LISTABLE) {
			if (rt.isInitialized() && !isInternalPackage(rt.name())) {
				for (Field f : rt.visibleFields()) {
					if (f.isStatic()) {
						String name = rt.name() + "." + f.name();
						model.statics.put(name, convertValue(rt.getValue(f)));
					}
				}
			}
		}

		// Convert heap
		Set<Long> heapDone = new HashSet<>();
		while (!pendingConversion.isEmpty()) {
			Map.Entry<Long, ObjectReference> first = pendingConversion.firstEntry();
			long id = first.getKey();
			ObjectReference obj = first.getValue();
			pendingConversion.remove(id);
			if (heapDone.contains(id))
				continue;
			heapDone.add(id);
			HeapEntity converted = convertObject(obj);
			converted.id = id;
			model.heap.put(id, converted);
		}

		return model;
	}

	// TODO clean this up
	private Frame convertFrame(StackFrame sf) {
		Frame output = new Frame();
		output.name = sf.location().method().name() + ":" + sf.location().lineNumber();

		if (sf.thisObject() != null) {
			output.locals.put("this", convertValue(sf.thisObject()));
		}

		// list args first
		/* KNOWN ISSUE:
		   .arguments() gets the args which have names in LocalVariableTable,
           but if there are none, we get an IllegalArgExc, and can use .getArgumentValues()
           However, sometimes some args have names but not all. Such as within synthetic
           lambda methods like "lambda$inc$0". For an unknown reason, trying .arguments()
           causes a JDWP error in such frames. So sadly, those frames are incomplete. */

		boolean JDWPerror = false;
		try {
			sf.getArgumentValues();
		} catch (com.sun.jdi.InternalException e) {
			if (e.toString().contains("Unexpected JDWP Error:")) {
				// expect JDWP error 35
				JDWPerror = true;
			} else {
				throw e;
			}
		}

		List<LocalVariable> frame_vars, frame_args;
		boolean completed_args = false;
		try {
			// args make sense to show first
			frame_args = sf.location().method().arguments(); //throwing statement
			completed_args = !JDWPerror && frame_args.size() == sf.getArgumentValues().size();
			for (LocalVariable lv : frame_args) {
				if (lv.name().equals("args")) {
					com.sun.jdi.Value v = sf.getValue(lv);
					if (v instanceof ArrayReference && ((ArrayReference) v).length() == 0)
						continue;
				}

				try {
					output.locals.put(lv.name(), convertValue(sf.getValue(lv)));
				} catch (IllegalArgumentException exc) {
					System.out.println("That shouldn't happen!");
				}
			}
		} catch (AbsentInformationException e) {
			// ok.
		}

		// args did not have names, like a functional interface call...
		// although hopefully a future Java version will give them names!
		if (!completed_args && !JDWPerror) {
			try {
				List<com.sun.jdi.Value> anon_args = sf.getArgumentValues();
				for (int i = 0; i < anon_args.size(); i++) {
					String name = "param#" + i;
					output.locals.put(name, convertValue(anon_args.get(i)));
				}
			} catch (InvalidStackFrameException e) {
				// ok.
			}
		}

		// now non-args
		try {
            /* We're using the fact that the hashCode tells us something
               about the variable's position (which is subject to change)
               to compensate for that the natural order of variables()
               is often different from the declaration order (see LinkedList.java) */
			frame_vars = sf.location().method().variables();
			TreeMap<Integer, LocalVariable> orderByHash = null;
			int offset = 0;
			for (LocalVariable lv : frame_vars) {
				if (!lv.isArgument() && (SHOW_ALL_FIELDS || !lv.name().endsWith("$"))) {
					if (orderByHash == null) {
						offset = lv.hashCode();
						orderByHash = new TreeMap<>();
					}
					orderByHash.put(lv.hashCode() - offset, lv);
				}
			}
			if (orderByHash != null) {
				for (Map.Entry<Integer, LocalVariable> me : orderByHash.entrySet()) {
					try {
						LocalVariable lv = me.getValue();
						output.locals.put(lv.name(), convertValue(sf.getValue(lv)));
					} catch (IllegalArgumentException exc) {
						// variable not yet defined, don't list it
					}
				}
			}
		} catch (AbsentInformationException ex) {
			// ok.
		}

		return output;
	}

	private Value convertReference(ObjectReference obj) {
		// Special handling for boxed types
		if (obj.referenceType().name().startsWith("java.lang.")
				&& BOXED_TYPES.contains(obj.referenceType().name().substring(10))) {
			return convertValue(obj.getValue(obj.referenceType().fieldByName("value")));
		}

		long key = obj.uniqueID();
		pendingConversion.put(key, obj);

		// Actually create and return the reference
		Value out = new Value();
		out.type = Value.Type.REFERENCE;
		out.reference = key;
		return out;
	}

	private HeapEntity convertObject(ObjectReference obj) {
		if (obj instanceof ArrayReference) {
			ArrayReference ao = (ArrayReference) obj;
			int length = ao.length();

			HeapList out = new HeapList();
			out.type = HeapEntity.Type.LIST;
			out.label = ao.type().name();
			for (int i = 0; i < length; i++) {
				// TODO: optional feature, skip runs of zeros
				out.items.add(convertValue(ao.getValue(i)));
			}
			return out;
		} else if (obj instanceof StringReference) {
			HeapPrimitive out = new HeapPrimitive();
			out.type = HeapEntity.Type.PRIMITIVE;
			out.label = "String";
			out.value = new Value();
			out.value.type = Value.Type.STRING;
			out.value.stringValue = ((StringReference) obj).value();
			return out;
		}

		String typeName = obj.referenceType().name();
		if ((doesImplementInterface(obj, "java.util.List")
				|| doesImplementInterface(obj, "java.util.Set"))
				&& isInternalPackage(typeName)) {
			HeapList out = new HeapList();
			out.type = HeapEntity.Type.LIST; // XXX: or SET
			out.label = displayNameForType(obj);
			Iterator<com.sun.jdi.Value> i = getIterator(thread, obj);
			while (i.hasNext()) {
				out.items.add(convertValue(i.next()));
			}
			return out;
		}

		if (doesImplementInterface(obj, "java.util.Map") && isInternalPackage(typeName)) {
			HeapMap out = new HeapMap();
			out.type = HeapEntity.Type.MAP;
			out.label = displayNameForType(obj);

			ObjectReference entrySet = (ObjectReference) invokeSimple(thread, obj, "entrySet");
			Iterator<com.sun.jdi.Value> i = getIterator(thread, entrySet);
			while (i.hasNext()) {
				ObjectReference entry = (ObjectReference) i.next();
				HeapMap.Pair pair = new HeapMap.Pair();
				pair.key = convertValue(invokeSimple(thread, entry, "getKey"));
				pair.val = convertValue(invokeSimple(thread, entry, "getValue"));
				out.pairs.add(pair);
			}
			return out;
		}

		// now, arbitrary objects
		HeapObject out = new HeapObject();
		out.type = HeapEntity.Type.OBJECT;
		out.label = displayNameForType(obj);

		ReferenceType refType = obj.referenceType();

		if (shouldShowDetails(refType)) {
			// fields: -inherited -hidden +synthetic
			// visibleFields: +inherited -hidden +synthetic
			// allFields: +inherited +hidden +repeated_synthetic
			Map<Field, com.sun.jdi.Value> fields = obj.getValues(
					SHOW_ALL_FIELDS ? refType.allFields() : refType.visibleFields()
			);
			for (Map.Entry<Field, com.sun.jdi.Value> me : fields.entrySet()) {
				if (!me.getKey().isStatic() && (SHOW_ALL_FIELDS || !me.getKey().isSynthetic())) {
					String name = SHOW_ALL_FIELDS ? me.getKey().declaringType().name() + "." : "";
					name += me.getKey().name();
					Value value = convertValue(me.getValue());
					out.fields.put(name, value);
				}
			}
		}
		return out;
	}

	private Value convertValue(com.sun.jdi.Value v) {
		Value out = new Value();
		if (v instanceof BooleanValue) {
			out.type = Value.Type.BOOLEAN;
			out.booleanValue = ((BooleanValue) v).value();
		} else if (v instanceof ByteValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((ByteValue) v).value();
		} else if (v instanceof ShortValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((ShortValue) v).value();
		} else if (v instanceof IntegerValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((IntegerValue) v).value();
		} else if (v instanceof LongValue) {
			out.type = Value.Type.LONG;
			out.longValue = ((LongValue) v).value();
		} else if (v instanceof FloatValue) {
			out.type = Value.Type.DOUBLE;
			out.doubleValue = ((FloatValue) v).value();
		} else if (v instanceof DoubleValue) {
			out.type = Value.Type.DOUBLE;
			out.doubleValue = ((DoubleValue) v).value();
		} else if (v instanceof CharValue) {
			out.type = Value.Type.LONG;
			out.charValue = ((CharValue) v).value();
		} else if (v instanceof VoidValue) {
			out.type = Value.Type.VOID;
		} else if (!(v instanceof ObjectReference)) {
			out.type = Value.Type.NULL;
		} else if (v instanceof StringReference) {
			out.type = Value.Type.STRING;
			out.stringValue = ((StringReference) v).value();
		} else {
			ObjectReference obj = (ObjectReference) v;
			out = convertReference(obj);
		}
		return out;
	}

	// input format: [package.]ClassName:lineno or [package.]ClassName
	private static boolean isInternalPackage(final String name) {
		return Arrays.stream(INTERNAL_PACKAGES).anyMatch(name::startsWith);
	}

	private static boolean shouldShowFrame(StackFrame frame) {
		Location loc = frame.location();
		return !isInternalPackage(loc.toString()) && !loc.method().name().contains("$access");
	}

	private static boolean shouldShowDetails(ReferenceType type) {
		return !isInternalPackage(type.name());
	}
}
