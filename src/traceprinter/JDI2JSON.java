/*****************************************************************************

 traceprinter: a Java package to print traces of Java programs
 David Pritchard (daveagp@gmail.com), created May 2013

 The contents of this directory are released under the GNU Affero
 General Public License, versions 3 or later. See LICENSE or visit:
 http://www.gnu.org/licenses/agpl.html

 See README for documentation on this package.

 ******************************************************************************/

package traceprinter;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
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
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;

public class JDI2JSON {
	public static boolean showVoid = true;
	public static boolean showRealType = true;
	public static String[] builtin_packages = {"java", "javax", "sun", "jdk", "com.sun", "traceprinter", "com.intellij", "org.junit", "jh61b.junit", "jh61b"};
	public List<ReferenceType> staticListable = new ArrayList<>();
	public ReferenceType stdinRT = null;
	boolean showStringsAsValues = true;
	boolean showAllFields = false;
	List<String> wrapperTypes =
			new ArrayList<String>
					(Arrays.asList
							("Byte Short Integer Long Float Double Character Boolean".split(" ")));
	private JsonObject last_ep = null;
	private TreeMap<Long, ObjectReference> heap;
	private TreeSet<Long> heap_done;
	/*    private ArrayList<Long> frame_stack = new ArrayList<Long>();*/
	private long frame_ticker = 0;
	private JsonArray convertVoid = jsonArray("VOID");

	private ThreadReference thread;

	static JsonValue jsonInt(long l) {
		return Json.createArrayBuilder().add(l).build().getJsonNumber(0);
	}

	static JsonValue jsonReal(double d) {
		return Json.createArrayBuilder().add(d).build().getJsonNumber(0);
	}

	static JsonValue jsonString(String S) {
		return Json.createArrayBuilder().add(S).build().getJsonString(0);
	}

	static JsonObject jsonModifiedObject(JsonObject obj, String S, JsonValue v) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add(S, v);
		for (Map.Entry<String, JsonValue> me : obj.entrySet()) {
			if (!S.equals(me.getKey()))
				result.add(me.getKey(), me.getValue());
		}
		return result.build();
	}

	private static String displayNameForType(ObjectReference obj) {
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

	private static boolean doesImplementInterface(ObjectReference obj, String iface) {
		if (obj.referenceType() instanceof ClassType) {
			Queue<InterfaceType> queue = new LinkedList<>();
			queue.addAll(((ClassType) obj.referenceType()).interfaces());
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

	// returns null when nothing changed since the last time
	// (or when only event type changed and new value is "step_line")
	public ArrayList<JsonObject> convertExecutionPoint(ThreadReference t) throws IncompatibleThreadStateException {
		this.thread = t;
		Location loc = t.frame(t.frameCount() - 1).location();

		ArrayList<JsonObject> results = new ArrayList<>();
		if (loc.method().name().contains("access$")) return results; // don't visualize synthetic access$000 methods

		heap_done = new TreeSet<Long>();
		heap = new TreeMap<>();

		JsonValue returnValue = null;

		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add("stdout", ""); // irrelevant
		result.add("event", "step_line");
		result.add("line", loc.lineNumber());

		JsonArrayBuilder frames = Json.createArrayBuilder();
		StackFrame lastNonUserFrame = null;

		boolean firstFrame = true;
		for (StackFrame sf : t.frames()) {
			if (!showFramesInLocation(sf.location())) {
				lastNonUserFrame = sf;
				continue;
			}

			if (lastNonUserFrame != null) {
				frame_ticker++;
				frames.add(convertFrameStub(lastNonUserFrame));
				lastNonUserFrame = null;
			}
			frame_ticker++;
			frames.add(convertFrame(sf, firstFrame, returnValue));
			firstFrame = false;
			returnValue = null;
		}

		result.add("stack_to_render", frames);

		JsonObjectBuilder statics = Json.createObjectBuilder();
		JsonArrayBuilder statics_a = Json.createArrayBuilder();
		for (ReferenceType rt : staticListable)
			if (rt.isInitialized() && !in_builtin_package(rt.name()))
				for (Field f : rt.visibleFields())
					if (f.isStatic()) {
						statics.add(rt.name() + "." + f.name(),
								convertValue(rt.getValue(f)));
						statics_a.add(rt.name() + "." + f.name());
					}
		if (stdinRT != null && stdinRT.isInitialized()) {
			int stdinPosition = ((IntegerValue) stdinRT.getValue(stdinRT.fieldByName("position"))).value();
			result.add("stdinPosition", stdinPosition);
		}

		result.add("globals", statics);
		result.add("ordered_globals", statics_a);

		result.add("func_name", loc.method().name());

		JsonObjectBuilder heapDescription = Json.createObjectBuilder();
		convertHeap(heapDescription);
		result.add("heap", heapDescription);

		JsonObject this_ep = result.build();
		if (reallyChanged(last_ep, this_ep)) {
			results.add(this_ep);
			last_ep = this_ep;
		}


		return results;
	}

	// input format: [package.]ClassName:lineno or [package.]ClassName
	public boolean in_builtin_package(String S) {
		S = S.split(":")[0];
		for (String badPrefix : builtin_packages)
			if (S.startsWith(badPrefix + "."))
				return true;
		return false;
	}

	private boolean showFramesInLocation(Location loc) {
		return (!in_builtin_package(loc.toString())
				&& !loc.method().name().contains("$access"));
		// skip synthetic accessor methods
	}

	private boolean showGuts(ReferenceType rt) {
		return (rt.name().matches("(^|\\.)Point") ||
				!in_builtin_package(rt.name()));
	}

	// issue: the frontend uses persistent frame ids but JDI doesn't provide them
	// approach 1, trying to compute them, seems intractable (esp. w/ callbacks)
	// approach 2, using an id based on stack depth, does not work w/ frontend
	// approach 3, just give each frame at each execution point a unique id,
	// is what we do. but we also want to skip animating e.p.'s where nothing changed,
	// and if only the frame ids changed, we should treat it as if nothing changed
	private boolean reallyChanged(JsonObject old_ep, JsonObject new_ep) {
		if (old_ep == null) return true;
		return !stripFrameIDs(new_ep).equals(stripFrameIDs(old_ep));
	}

	private JsonObject stripFrameIDs(JsonObject ep) {
		JsonArrayBuilder result = Json.createArrayBuilder();
		for (JsonValue frame : (JsonArray) (ep.get("stack_to_render"))) {
			result.add(jsonModifiedObject
					(jsonModifiedObject((JsonObject) frame,
							"unique_hash",
							jsonString("")),
							"frame_id",
							jsonInt(0)));
		}
		return jsonModifiedObject(ep, "stack_to_render", result.build());
	}

	private JsonObjectBuilder convertFrame(StackFrame sf, boolean highlight, JsonValue returnValue) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		JsonArrayBuilder result_ordered = Json.createArrayBuilder();
		if (sf.thisObject() != null) {
			result.add("this", convertValue(sf.thisObject()));
			result_ordered.add("this");
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

		List<LocalVariable> frame_vars = null, frame_args = null;
		boolean completed_args = false;
		try {
			// args make sense to show first
			frame_args = sf.location().method().arguments(); //throwing statement
			completed_args = !JDWPerror && frame_args.size() == sf.getArgumentValues().size();
			for (LocalVariable lv : frame_args) {
				//System.out.println(sf.location().method().getClass());
				if (lv.name().equals("args")) {
					Value v = sf.getValue(lv);
					if (v instanceof ArrayReference && ((ArrayReference) v).length() == 0) continue;
				}
				try {
					result.add(lv.name(),
							convertValue(sf.getValue(lv)));
					result_ordered.add(lv.name());
				} catch (IllegalArgumentException exc) {
					System.out.println("That shouldn't happen!");
				}
			}
		} catch (AbsentInformationException e) {
		}
		// args did not have names, like a functional interface call...
		// although hopefully a future Java version will give them names!
		if (!completed_args && !JDWPerror) {
			try {
				List<Value> anon_args = sf.getArgumentValues();
				for (int i = 0; i < anon_args.size(); i++) {
					result.add("param#" + i, convertValue(anon_args.get(i)));
					result_ordered.add("param#" + i);
				}
			} catch (InvalidStackFrameException e) {
			}
		}

		if (JDWPerror) {
			result.add("&hellip;?", jsonArray("NUMBER-LITERAL", jsonString("&hellip;?"))); // hack since number-literal is just html
			result_ordered.add("&hellip;?");
		}

		// now non-args
		try {
            /* We're using the fact that the hashCode tells us something
               about the variable's position (which is subject to change)
               to compensate for that the natural order of variables()
               is often different from the declaration order (see LinkedList.java) */
			frame_vars = sf.location().method().variables(); //throwing statement
			TreeMap<Integer, String> orderByHash = null;
			int offset = 0;
			for (LocalVariable lv : frame_vars)
				if (!lv.isArgument())
					if (showAllFields || !lv.name().endsWith("$")) { // skip for-loop synthetics (exists in Java 7, but not 8)
						try {
							result.add(lv.name(),
									convertValue(sf.getValue(lv)));
							if (orderByHash == null) {
								offset = lv.hashCode();
								orderByHash = new TreeMap<>();
							}
							orderByHash.put(lv.hashCode() - offset, lv.name());
						} catch (IllegalArgumentException exc) {
							// variable not yet defined, don't list it
						}
					}
			if (orderByHash != null) // maybe no local vars
				for (Map.Entry<Integer, String> me : orderByHash.entrySet())
					result_ordered.add(me.getValue());
		} catch (AbsentInformationException ex) {
			//System.out.println("AIE: can't list variables in " + sf.location());
		}
		if (returnValue != null && (showVoid || returnValue != convertVoid)) {
			result.add("__return__", returnValue);
			result_ordered.add("__return__");
		}
		return Json.createObjectBuilder()
				.add("func_name", sf.location().method().name() + ":" + sf.location().lineNumber())
				.add("encoded_locals", result)
				.add("ordered_varnames", result_ordered)
				.add("parent_frame_id_list", Json.createArrayBuilder())
				.add("is_highlighted", highlight)//frame_stack.size()-1)
				.add("is_zombie", false)
				.add("is_parent", false)
				.add("unique_hash", "" + frame_ticker)//frame_stack.get(level))
				.add("frame_id", frame_ticker);//frame_stack.get(level));
	}



	/* JSON utility methods */

	// used to show a single non-user frame when there is
	// non-user code running between two user frames
	private JsonObjectBuilder convertFrameStub(StackFrame sf) {
		return Json.createObjectBuilder()
				.add("func_name", "\u22EE\n" + sf.location().declaringType().name() + "." + sf.location().method().name())
				.add("encoded_locals", Json.createObjectBuilder())//.add("...", "..."))
				.add("ordered_varnames", Json.createArrayBuilder())//.add("..."))
				.add("parent_frame_id_list", Json.createArrayBuilder())
				.add("is_highlighted", false)//frame_stack.size()-1)
				.add("is_zombie", false)
				.add("is_parent", false)
				.add("unique_hash", "" + frame_ticker)//frame_stack.get(level))
				.add("frame_id", frame_ticker);//frame_stack.get(level));
	}

	void convertHeap(JsonObjectBuilder result) {
		heap_done = new java.util.TreeSet<>();
		while (!heap.isEmpty()) {
			Map.Entry<Long, ObjectReference> first = heap.firstEntry();
			ObjectReference obj = first.getValue();
			long id = first.getKey();
			heap.remove(id);
			if (heap_done.contains(id))
				continue;
			heap_done.add(id);
			result.add("" + id, convertObject(obj, true));
		}
	}

	private JsonValue convertObject(ObjectReference obj, boolean fullVersion) {
		if (showStringsAsValues && obj.referenceType().name().startsWith("java.lang.")
				&& wrapperTypes.contains(obj.referenceType().name().substring(10))) {
			return convertValue(obj.getValue(obj.referenceType().fieldByName("value")));
		}

		JsonArrayBuilder result = Json.createArrayBuilder();

		// abbreviated versions are for references to objects
		if (!fullVersion) {
			result.add("REF").add(obj.uniqueID());
			heap.put(obj.uniqueID(), obj);
			return result.build();
		}

		// full versions are for describing the objects themselves,
		// in the heap

		else if (obj instanceof ArrayReference) {
			ArrayReference ao = (ArrayReference) obj;
			int L = ao.length();
			result.add("LIST");
			if (showRealType) {
				result.add(ao.type().name());
			}
			heap_done.add(obj.uniqueID());

			class Help {
				// is it a zero integer?
				boolean isz(Value v) {
					return v instanceof IntegerValue && ((IntegerValue) v).intValue() == 0;
				}
			}
			Help help = new Help();


			for (int i = 0; i < L; i++) {
				// hack for markov
				if (help.isz(ao.getValue(i))) {
					// j is the next nonzero after i
					int j = i + 1;
					while (j < L && help.isz(ao.getValue(j)))
						j++;
					if (j - i >= 4) {
						result.add(convertValue(ao.getValue(i)));
						result.add(Json.createArrayBuilder().add("ELIDE").add(j - i - 2));
						result.add(convertValue(ao.getValue(j - 1)));
					} else for (int k = i; k < j; k++)
						result.add(convertValue(ao.getValue(k)));
					i = j - 1; // don't redo them all
				} else result.add(convertValue(ao.getValue(i)));
			}
			return result.build();
		} else if (obj instanceof StringReference) {
			return Json.createArrayBuilder()
					.add("HEAP_PRIMITIVE")
					.add("String")
					.add(jsonString(((StringReference) obj).value()))
					.build();
		}
		// do we need special cases for ClassObjectReference, ThreadReference,.... ?
		// list / set / map handling by Eli Lipsitz
		else {
			String typeName = obj.referenceType().name();
			if (doesImplementInterface(obj, "java.util.List") && in_builtin_package(typeName)) {
				heap_done.add(obj.uniqueID());
				result.add("LIST");
				if (showRealType) {
					result.add(displayNameForType(obj));
				}
				Iterator<Value> i = JDIUtils.getIterator(thread, obj);
				while (i.hasNext()) {
					Value v = i.next();
					result.add(convertValue(v));
				}
				return result.build();
			}

			if (doesImplementInterface(obj, "java.util.Set") && in_builtin_package(typeName)) {
				heap_done.add(obj.uniqueID());
				result.add("SET");
				if (showRealType) {
					result.add(displayNameForType(obj));
				}
				Iterator<Value> i = JDIUtils.getIterator(thread, obj);
				while (i.hasNext()) {
					Value v = i.next();
					result.add(convertValue(v));
				}
				return result.build();
			}

			if (doesImplementInterface(obj, "java.util.Map") && in_builtin_package(typeName)) {
				heap_done.add(obj.uniqueID());
				result.add("DICT");
				if (showRealType) {
					result.add(displayNameForType(obj));
				}
				ObjectReference entrySet = (ObjectReference) JDIUtils.invokeSimple(thread, obj, "entrySet");
				Iterator<Value> i = JDIUtils.getIterator(thread, entrySet);
				while (i.hasNext()) {
					ObjectReference entry = (ObjectReference) i.next();
					Value key = JDIUtils.invokeSimple(thread, entry, "getKey");
					Value val = JDIUtils.invokeSimple(thread, entry, "getValue");
					result.add(Json.createArrayBuilder().add(convertValue(key)).add(convertValue(val)).build());
				}
				return result.build();
			}

			if (doesImplementInterface(obj, "java.util.Queue") && in_builtin_package(typeName)) {
				heap_done.add(obj.uniqueID());
				result.add("QUEUE");
				if (showRealType) {
					result.add(displayNameForType(obj));
				}
				Iterator<Value> i = JDIUtils.getIterator(thread, obj);
				while (i.hasNext()) {
					Value v = i.next();
					result.add(convertValue(v));
				}
				return result.build();
			}

			// now deal with Objects.
			heap_done.add(obj.uniqueID());
			result.add("INSTANCE");
			if (obj.referenceType().name().startsWith("java.lang.")
					&& wrapperTypes.contains(obj.referenceType().name().substring(10))) {
				result.add(obj.referenceType().name().substring(10));
				result.add(jsonArray("___NO_LABEL!___",//jsonArray("NO-LABEL"), // don't show a label or label cell for wrapper instance field
						convertValue(obj.getValue(obj.referenceType().fieldByName("value")))));
			} else {
				result.add(displayNameForType(obj));
			}
			if (showGuts(obj.referenceType())) {
				// fields: -inherited -hidden +synthetic
				// visibleFields: +inherited -hidden +synthetic
				// allFields: +inherited +hidden +repeated_synthetic
				for (Map.Entry<Field, Value> me :
						obj.getValues
								(
										showAllFields ?
												obj.referenceType().allFields() :
												obj.referenceType().visibleFields())
								.entrySet()
						) {
					if (!me.getKey().isStatic()
							&& (showAllFields || !me.getKey().isSynthetic())
							)
						result.add(Json.createArrayBuilder()
								.add
										((
												showAllFields ?
														me.getKey().declaringType().name() + "." :
														""
										) + me.getKey().name())
								.add(convertValue(me.getValue())));
				}
			} else if (obj.referenceType().name().equals("Stopwatch")) {
				ReferenceType rt = obj.referenceType();
				Field f = rt.fieldByName("startString");
				result.add(Json.createArrayBuilder().add("started at").add(
						Json.createArrayBuilder().add("NUMBER-LITERAL").add(
								convertValue(obj.getValue(f)))));
			}
			return result.build();
		}
	}

	private JsonArray jsonArray(Object... args) {
		JsonArrayBuilder result = Json.createArrayBuilder();
		for (Object o : args) {
			if (o instanceof JsonValue)
				result.add((JsonValue) o);
			else if (o instanceof String)
				result.add((String) o);
			else throw new RuntimeException("Add more cases to JDI2JSON.jsonArray(Object...)");
		}
		return result.build();
	}

	private JsonValue convertValue(Value v) {
		if (v instanceof BooleanValue) {
			if (((BooleanValue) v).value())
				return JsonValue.TRUE;
			else
				return JsonValue.FALSE;
		} else if (v instanceof ByteValue) return jsonInt(((ByteValue) v).value());
		else if (v instanceof ShortValue) return jsonInt(((ShortValue) v).value());
		else if (v instanceof IntegerValue) return jsonInt(((IntegerValue) v).value());
			// some longs can't be represented as doubles, they won't survive the json conversion
		else if (v instanceof LongValue) return jsonArray("NUMBER-LITERAL", jsonString("" + ((LongValue) v).value()));
			// floats who hold integer values will end up as integers after json conversion
			// also, this lets us pass "Infinity" and other IEEE non-numbers
		else if (v instanceof FloatValue) return jsonArray("NUMBER-LITERAL", jsonString("" + ((FloatValue) v).value()));
		else if (v instanceof DoubleValue)
			return jsonArray("NUMBER-LITERAL", jsonString("" + ((DoubleValue) v).value()));
		else if (v instanceof CharValue) return jsonArray("CHAR-LITERAL", jsonString(((CharValue) v).value() + ""));
		else if (v instanceof VoidValue) return convertVoid;
		else if (!(v instanceof ObjectReference)) return JsonValue.NULL; //not a hack
		else if (showStringsAsValues && v instanceof StringReference)
			return jsonString(((StringReference) v).value());
		else {
			ObjectReference obj = (ObjectReference) v;
			heap.put(obj.uniqueID(), obj);
			return convertObject(obj, false);
		}
	}
}