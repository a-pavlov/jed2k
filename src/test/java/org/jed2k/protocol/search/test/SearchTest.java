package org.jed2k.protocol.search.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.server.search.BooleanEntry;
import org.jed2k.protocol.server.search.BooleanEntry.Operator;
import org.jed2k.protocol.server.search.SearchRequest;
import org.jed2k.protocol.server.search.StringEntry;
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
    
    @Test
    public void testLargeExpression() throws JED2KException {
        // check large expression
        SearchRequest sr = SearchRequest.makeRequest(0,0,0,0, "", "", "", 0, 0, "a OR (b OR c AND d OR e) OR j (x OR (y z))");
        assertEquals(17, sr.count());
        assertTrue(sr.entry(0) instanceof BooleanEntry && ((BooleanEntry)sr.entry(0)).operator() == Operator.OPER_OR);        
        assertTrue(sr.entry(1) instanceof StringEntry && sr.entry(1).toString().compareTo("a") == 0);
        assertTrue(sr.entry(2) instanceof BooleanEntry && ((BooleanEntry)sr.entry(2)).operator() == Operator.OPER_OR);
        assertTrue(sr.entry(3) instanceof BooleanEntry && ((BooleanEntry)sr.entry(3)).operator() == Operator.OPER_OR);
        assertTrue(sr.entry(4) instanceof StringEntry && sr.entry(4).toString().compareTo("b") == 0);
        assertTrue(sr.entry(5) instanceof BooleanEntry && ((BooleanEntry)sr.entry(5)).operator() == Operator.OPER_AND);
        assertTrue(sr.entry(6) instanceof StringEntry && sr.entry(6).toString().compareTo("c") == 0);
        assertTrue(sr.entry(7) instanceof BooleanEntry && ((BooleanEntry)sr.entry(7)).operator() == Operator.OPER_OR);
        assertTrue(sr.entry(8) instanceof StringEntry && sr.entry(8).toString().compareTo("d") == 0);
        assertTrue(sr.entry(9) instanceof StringEntry && sr.entry(9).toString().compareTo("e") == 0);
        assertTrue(sr.entry(10) instanceof BooleanEntry && ((BooleanEntry)sr.entry(10)).operator() == Operator.OPER_AND);
        assertTrue(sr.entry(11) instanceof StringEntry && sr.entry(11).toString().compareTo("j") == 0);
        assertTrue(sr.entry(12) instanceof BooleanEntry && ((BooleanEntry)sr.entry(12)).operator() == Operator.OPER_OR);
        assertTrue(sr.entry(13) instanceof StringEntry && sr.entry(13).toString().compareTo("x") == 0);
        assertTrue(sr.entry(14) instanceof BooleanEntry && ((BooleanEntry)sr.entry(14)).operator() == Operator.OPER_AND);
        assertTrue(sr.entry(15) instanceof StringEntry && sr.entry(15).toString().compareTo("y") == 0);
        assertTrue(sr.entry(16) instanceof StringEntry && sr.entry(16).toString().compareTo("z") == 0);
        
    }
}