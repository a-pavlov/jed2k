package org.jed2k.protocol.search.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.jed2k.protocol.Unsigned.uint8;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.search.SearchRequest;
import org.junit.Test;

public class SearchTest {
    
    @Test
    public void testTrvialParse() throws JED2KException {
        String bracket_expr[] = {
            "(a b)c d",
            "(a AND b) AND c d",
            "(a b) c AND d",
            "(((a b)))c d",
            "(((a b)))(c)(d)",
            "(((a AND b)))AND((c))AND((d))",
            "(((\"a\" AND \"b\")))AND((c))AND((\"d\"))",
            "   (   (  (  a    AND b   )  )   )  AND  ((c  )  )    AND (  (  d  )   )"
        };
        
        for(int i = 0; i < bracket_expr.length; ++i) {
            SearchRequest sr = SearchRequest.makeRequest(0,0,0,0,"", "", "", 0, 0, bracket_expr[i]);            
        }
    }   
}