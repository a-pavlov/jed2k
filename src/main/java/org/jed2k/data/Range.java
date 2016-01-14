package org.jed2k.data;

import org.jed2k.Pair;

public class Range extends Pair<Long, Long> {
	public Range(Long left, Long right) {
		super(left, right);
		assert(right > left);
	}
	
	public static Range make(Long left, Long right) {
		return new Range(left, right);
	}
}
