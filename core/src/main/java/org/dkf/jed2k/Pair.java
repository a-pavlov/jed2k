package org.dkf.jed2k;

import lombok.Data;
import lombok.ToString;

// C++ like pair template
@ToString(exclude={"cleft", "cright"})
@Data
public class Pair<L,R> implements Comparable<Pair<L,R>> {
    public final L left;
    public final R right;
    private boolean cleft;
    private boolean cright;

    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
        cleft = (this.left instanceof Comparable<?>);
        cright = (this.right instanceof Comparable<?>);
    }

    public static <L, R> Pair<L, R> make(final L left, final R right) {
        return new Pair<L, R>(left, right);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public int compareTo(Pair<L, R> pair) {
        assert(cleft || cright);
        if (cleft) {
            final int k = ((Comparable<L>)left).compareTo(pair.left);
            if (k > 0) return 1;
            if (k < 0) return -1;
        }

        if (cright) {
            final int k = ((Comparable<R>)right).compareTo(pair.right);
            if (k > 0) return 1;
            if (k < 0) return -1;
        }

        return 0;
    }

    @Override
    public boolean equals(
            Object obj)
    {
        if (obj instanceof Pair) {
            final Pair<?,?> o = (Pair<?,?>) obj;
            return (left.equals(o.left) && right.equals(o.right));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (left != null?left.hashCode():0) + ((right !=null)?right.hashCode():0);
    }

}
