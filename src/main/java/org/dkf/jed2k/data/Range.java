package org.dkf.jed2k.data;

public class Range implements Comparable<Range> {
	public long left;
    public long right;

	public Range(long l, long r) {
		assert(r > l);
        this.left = l;
        this.right = r;
	}

	public static Range make(long l, long r) {
		return new Range(l, r);
	}

	@Override
    public String toString() {
        return String.format("range [%d..%d]", left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Range) {
            return ((Range) o).left == left &&
                    ((Range) o).right == right;
        }

        return false;
    }

    @Override
    public int compareTo(Range o) {
        if (left != o.left) {
            if (left < o.left) return -1;
            if (left > o.left) return 1;
        }

        if (right < o.right) return -1;
        if (right > o.right) return 1;
        return 0;
    }
}
