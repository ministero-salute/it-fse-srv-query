package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class OptionalUtility {

	public static <In> In first(List<In> in) { 
		return getIndex(in, 0); 
	}	
	
	public static <In> In first(Collection<In> in) { 
		if (in == null) return null;
		return in
				.iterator()
				.next();
	}

	public static <In> In last(List<In> in) { 
		if (in == null) return null;
		return getIndex(in, in.size()-1); 
	}	

	public static <In> In getIndex(List<In> in, int index) {
		try { 
			return Optional
				.ofNullable(in.get(index))
				.orElse(null); 
		}
		catch (IndexOutOfBoundsException e) { return null; }
	}	
	
	public static <In, Out> Out getValue(In in, Function<In, Out> mapper) {
		return Optional
				.ofNullable(in)
				.map(mapper)
				.orElse(null);
	}
	
	public static <In, Out> Out getValueOrDefault(In in, Function<In, Out> mapper, Out defaultValue) {
		return Optional
				.ofNullable(in)
				.map(mapper)
				.orElse(defaultValue);
	}
	
	public static <In, Out1, Out2> Out2 getValue(In in, Function<In, Out1> mapper1, Function<Out1, Out2> mapper2) {
		return Optional
				.ofNullable(in)
				.map(mapper1)
				.map(mapper2)
				.orElse(null);
	}
	
	public static <In, Out1, Out2> Out2 getValueOrDefault(In in, Function<In, Out1> mapper1, Function<Out1, Out2> mapper2, Out2 defaultValue) {
		return Optional
				.ofNullable(in)
				.map(mapper1)
				.map(mapper2)
				.orElse(defaultValue);
	}
	
	public static <In, Out1, Out2, Out3> Out3 getValue(In in, Function<In, Out1> mapper1, Function<Out1, Out2> mapper2, Function<Out2, Out3> mapper3) {
		return Optional
				.ofNullable(in)
				.map(mapper1)
				.map(mapper2)
				.map(mapper3)
				.orElse(null);
	}
	
	public static <In, Out1, Out2, Out3> Out3 getValueOrDefault(In in, Function<In, Out1> mapper1, Function<Out1, Out2> mapper2, Function<Out2, Out3> mapper3, Out3 defaultValue) {
		return Optional
				.ofNullable(in)
				.map(mapper1)
				.map(mapper2)
				.map(mapper3)
				.orElse(defaultValue);
	}
	
}