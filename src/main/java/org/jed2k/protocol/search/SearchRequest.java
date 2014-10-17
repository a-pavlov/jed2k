package org.jed2k.protocol.search;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.search.SearchEntry.Operator;

import static org.jed2k.protocol.Unsigned.uint16;

public class SearchRequest {
    static byte SEARCH_TYPE_BOOL       = 0x00;
    static byte SEARCH_TYPE_STR        = 0x01;
    static byte SEARCH_TYPE_STR_TAG    = 0x02;
    static byte SEARCH_TYPE_UINT32     = 0x03;
    static byte SEARCH_TYPE_UINT64     = 0x08;
    
    
    public static SearchEntry makeEntry(Operator value) {
        return new BooleanEntry(value);
    }
    
    private static SearchEntry.Operator string2Operator(String value) {
        if (value == "AND") return Operator.OPER_AND;
        if (value == "OR")  return Operator.OPER_OR;
        if (value == "NOT") return Operator.OPER_NOT;
        return Operator.OPER_NONE;
    }
    
    private static boolean isQuote(char value) {
        return (value == '"');
    }
    
    private static void appendItem(ArrayList<SearchEntry> dst, final SearchEntry sre) {
        if (!(sre instanceof BooleanEntry))
        {
            if (!dst.isEmpty())
            {
                if ((!dst.get(dst.size()-1).isOperator() && !sre.isOperator()) ||                                                              // xxx xxx
                (!dst.get(dst.size()-1).isOperator() && sre.getOperator() == Operator.OPER_OPEN_PAREN) ||                                 // xxx (
                (dst.get(dst.size()-1).getOperator() == Operator.OPER_CLOSE_PAREN && !sre.isOperator()) ||                                 // ) xxx
                (dst.get(dst.size()-1).getOperator() == Operator.OPER_CLOSE_PAREN && sre.getOperator() == Operator.OPER_OPEN_PAREN))  // ) (
                {
                    dst.add(makeEntry(Operator.OPER_AND));
                }

                //if (dst.back().getOperator() == search_request_entry::SRE_OBR && sre.getOperator() == search_request_entry::SRE_CBR)
                //{
                //    throw libed2k_exception(errors::empty_brackets);
                //}
            }
        }

        dst.add(sre);
    }
    
    private static ArrayList<SearchEntry> string2Entries(String value) throws ProtocolException {        
        boolean verbatim = false;        
        StringBuilder item = new StringBuilder();
        ArrayList<SearchEntry> res = new ArrayList<SearchEntry>();

        for (int i = 0; i < value.length(); ++i)
        {
            char c = value.charAt(i);

            switch(c)
            {
                case ' ':
                case '(':
                case ')':
                {
                    if (verbatim)
                    {
                        item.append(c);
                    }
                    else if (item.length() != 0)
                    {                        
                        SearchEntry.Operator so = string2Operator(item.toString());

                        if (so != Operator.OPER_NONE)
                        {
                            // add boolean operator
                            if (res.isEmpty() || (res.get(res.size()-1) instanceof BooleanEntry) || (c == ')'))
                            {
                                // operator in begin, operator before previous operator and operator before close bracket is error
                                throw new ProtocolException("Operator incorrect place");                                
                            }
                            else
                            {
                                appendItem(res, makeEntry(so));
                            }
                        }
                        else
                        {                            
                            appendItem(res, makeEntry(item.toString().replace("\"", "")));
                        }

                        item.setLength(0);

                    }

                    if (c == '(') {
                        appendItem(res, makeEntry(Operator.OPER_OPEN_PAREN));
                    }

                    if (c == ')') {
                        appendItem(res, makeEntry(Operator.OPER_CLOSE_PAREN));
                    }

                    break;
                }
                case '"':
                    verbatim = !verbatim; // change verbatim status and add this character
                default:
                    item.append(c);
                    break;
            }
        }

        // check unclosed quotes
        if (verbatim) {
            throw new ProtocolException("Unclosed quotation mark");
        }

        if (item.length() != 0)
        {
            // add last item - check it is not operator
            SearchEntry.Operator so = string2Operator(item.toString());

            if (so != Operator.OPER_NONE)
            {
                throw new ProtocolException("Operator on end of expression");
            }
            else
            {                
                appendItem(res, makeEntry(item.toString().replace("\"", "")));
            }
        }
        
        return res;
    }
    
    private static ArrayList<SearchEntry> packRequest(ArrayList<SearchEntry> source) throws ProtocolException {
        ArrayList<SearchEntry> res = new ArrayList<SearchEntry>();
        Stack<SearchEntry> operators_stack = new Stack<SearchEntry>();
        
        for(int i = source.size() - 1; i >= 0; --i) {
            SearchEntry entry = source.get(i);
            if (entry.isOperator()) {
                
                if (entry.getOperator() == Operator.OPER_OPEN_PAREN) {
                    if (operators_stack.empty()) {
                        throw new ProtocolException("Incorrect brackets count");
                    }

                    // roll up
                    while(operators_stack.peek().getOperator() != Operator.OPER_CLOSE_PAREN) {
                        res.add(operators_stack.pop());
                        
                        if (operators_stack.empty()) {
                            throw new ProtocolException("Incorrect brackets count");
                        }
                    }

                    // pull close bracket entry
                    operators_stack.pop();
                    continue;
                }

                // we have normal operator and on stack top we have normal operator
                // prepare result - move operator from top to result and replace top
                if ((entry.getOperator().value <= Operator.OPER_NOT.value) &&
                        !operators_stack.empty() &&
                        (operators_stack.peek().getOperator().value <= Operator.OPER_NOT.value))
                {
                    res.add(operators_stack.pop());                    
                }

                operators_stack.push(entry);
            }
            else
            {
                res.add(entry);
            }
        }

        if (!operators_stack.empty())
        {
            if (operators_stack.peek().getOperator().value > Operator.OPER_NOT.value)
            {
                throw new ProtocolException("Incorrect brackets count");
            }

            res.add(operators_stack.pop());
        }
        
        return res;
    }
    
    private static ByteContainer<UInt16> generateTag(String name, byte id) throws ProtocolException {
        ByteContainer<UInt16> tag;
        if (name != null) {
            try {
                byte[] content = name.getBytes("UTF-8");
                tag = new ByteContainer<UInt16>(uint16(content.length), content);
            } catch(UnsupportedEncodingException e) {
                throw new ProtocolException(e);
            }
                        
        } else {
            byte[] nm = {id};
            tag = new ByteContainer<UInt16>(uint16(1), nm);
        }
        
        return tag;
    }
    
    public static SearchEntry makeEntry(String value) throws ProtocolException {
        try {
            return new StringEntry(new ByteContainer<UInt16>(uint16(), value.getBytes("UTF-8")), null);
        } catch(UnsupportedEncodingException e) {
            throw new ProtocolException(e);
        }
    }
    
    public static SearchEntry makeEntry(String name, byte id, String value) throws ProtocolException {                
        try {
            return new StringEntry(new ByteContainer<UInt16>(uint16(), value.getBytes("UTF-8")), generateTag(name, id));
        } catch(UnsupportedEncodingException e) {
            throw new ProtocolException(e);
        }
    }
    
    public static SearchEntry makeEntry(String name, byte id, byte operator, long value) throws ProtocolException {
        return new NumericEntry(value, operator, generateTag(name, id));
    }
}
