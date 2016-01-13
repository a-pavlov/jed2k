package org.jed2k;

import org.jed2k.data.PieceBlock;

public interface Policy {   
    public int priority(final PieceBlock pb);
}
