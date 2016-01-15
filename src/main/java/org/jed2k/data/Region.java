package org.jed2k.data;

import java.util.LinkedList;

public class Region {
	private LinkedList<Range>	segments = new LinkedList<Range>(); 
	
	public Region(Range seg) {
		segments.add(seg);
	}
	
	public Region(Range[] ranges) {
		for(Range r: ranges) {
			segments.add(r);
		}
	}
	
	public boolean empty() {
		return segments.isEmpty();
	}
	
	public Region sub(final Range seq) {
		LinkedList<Range> res = new LinkedList<Range>();
		for (Range range : segments) {
			res.addAll(sub(range, seq));
		}
		
        segments = res;
        return this;
	}
	
	// TODO - check this method - uses for compression
    //public void shrink_end(long size) {
    //    assert(segments.size() == 1);
    //    segments.get(0).right = segments.get(0).left + size;
    //}
	
    @Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Region) {
			Region x = (Region)obj;
			return x.segments.equals(segments);
		}
		
		return false;
	}

	public Long begin() 
    {
        assert(segments.size() == 1);
        return segments.get(0).left;
    }
    
    private static LinkedList<Range> sub(final Range seg1, final Range seg2) {        
        LinkedList<Range> res = new LinkedList<Range>();

        // [   seg1    )
        //   [ seg2 )    -> [ )   [ )
        if (seg1.left < seg2.left && seg1.right > seg2.right) {
            res.add(Range.make(seg1.left, seg2.left));
            res.add(Range.make(seg2.right, seg1.right));
        }
        // [ seg1 )
        //          [ seg2 ) -> [   )
        else if (seg1.right <= seg2.left || seg2.right <= seg1.left)  {
            res.add(seg1);
        }
        // [ seg1 )
        //    [ seg2 ) -> [  )
        else if (seg2.left > seg1.left && seg2.left < seg1.right) {
            res.add(Range.make(seg1.left, seg2.left));
        }
        //     [ seg1 )
        // [ seg2 )     -> [  )
        else if (seg2.right > seg1.left && seg2.right < seg1.right) {
            res.add(Range.make(seg2.right, seg1.right));
        }

        return res;
    }
}
