package org.dkf.jed2k.protocol.server.search;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.exception.SearchCode;
import org.dkf.jed2k.protocol.ByteContainer;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import static org.dkf.jed2k.protocol.Unsigned.uint16;

public class SearchRequest implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SearchRequest.class);

    static byte SEARCH_TYPE_BOOL       = 0x00;
    static byte SEARCH_TYPE_STR        = 0x01;
    static byte SEARCH_TYPE_STR_TAG    = 0x02;
    static byte SEARCH_TYPE_UINT32     = 0x03;
    static byte SEARCH_TYPE_UINT64     = 0x08;

    private static final int SEARCH_REQ_ELEM_LENGTH    = 20;
    private static final int SEARCH_REQ_QUERY_LENGTH   = 450;
    private static final int SEARCH_REQ_ELEM_COUNT     = 30;

    // Media values for FT_FILETYPE
    public static final String ED2KFTSTR_AUDIO = "Audio";
    public static final String ED2KFTSTR_VIDEO = "Video";
    public static final String ED2KFTSTR_IMAGE = "Image";
    public static final String ED2KFTSTR_DOCUMENT = "Doc";
    public static final String ED2KFTSTR_PROGRAM = "Pro";
    public static final String ED2KFTSTR_ARCHIVE = "Arc";  // *Mule internal use only
    public static final String ED2KFTSTR_CDIMAGE = "Iso";  // *Mule internal use only
    public static final String ED2KFTSTR_EMULECOLLECTION = "EmuleCollection";
    public static final String ED2KFTSTR_FOLDER  = "Folder"; // Value for eD2K tag FT_FILETYPE
    public static final String ED2KFTSTR_USER = "User"; // eMule internal use only

    // Additional media meta data tags from eDonkeyHybrid (note also the uppercase/lowercase)
    private static final String FT_ED2K_MEDIA_ARTIST = "Artist";    // <string>
    private static final String FT_ED2K_MEDIA_ALBUM = "Album";     // <string>
    private static final String FT_ED2K_MEDIA_TITLE = "Title";     // <string>
    private static final String FT_ED2K_MEDIA_LENGTH = "length";    // <string> !!!
    private static final String FT_ED2K_MEDIA_BITRATE = "bitrate";   // <uint32>
    private static final String FT_ED2K_MEDIA_CODEC = "codec";    // <string>

    public enum FileType {
        ED2KFT_ANY               (0),
        ED2KFT_AUDIO             (1),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_VIDEO             (2),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_IMAGE             (3),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_PROGRAM           (4),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_DOCUMENT          (5),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_ARCHIVE           (6),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_CDIMAGE           (7),    // ED2K protocol value (eserver 17.6+)
        ED2KFT_EMULECOLLECTION   (8);

        public final byte value;

        private FileType(int value) {
            this.value = (byte)value;
        }
    }

    public enum Operator {
        ED2K_SEARCH_OP_EQUAL(0),
        ED2K_SEARCH_OP_GREATER(1),
        ED2K_SEARCH_OP_LESS(2),
        ED2K_SEARCH_OP_GREATER_EQUAL(3),
        ED2K_SEARCH_OP_LESS_EQUAL(4),
        ED2K_SEARCH_OP_NOTEQUAL(5);

        public final byte value;

        private Operator(int value) {
            this.value = (byte)value;
        }
    }

    private final ArrayDeque<Serializable> value;

    SearchRequest(ArrayDeque<Serializable> value) {
        this.value = value;
        log.debug(dbgString(value));
    }

    public static Serializable makeEntry(BooleanEntry.Operator value) {
        return new BooleanEntry(value);
    }

    private static BooleanEntry.Operator string2Operator(String value) {
        if (value.compareTo("AND") == 0) return BooleanEntry.Operator.OPER_AND;
        if (value.compareTo("OR") == 0)  return BooleanEntry.Operator.OPER_OR;
        if (value.compareTo("NOT") == 0) return BooleanEntry.Operator.OPER_NOT;
        return null;
    }

    private static boolean isOperator(Serializable value) {
        return value instanceof BooleanEntry ||
                value instanceof OpenParen ||
                value instanceof CloseParen;

    }

    private static void appendItem(final ArrayList<Serializable> dst, final Serializable sre) throws JED2KException {
        if (!(sre instanceof BooleanEntry))
        {
            if (!dst.isEmpty())
            {
                if ((!isOperator(dst.get(dst.size()-1)) && !isOperator(sre)) ||           // xxx xxx
                (!isOperator(dst.get(dst.size()-1)) && sre instanceof OpenParen) ||       // xxx (
                (dst.get(dst.size()-1) instanceof CloseParen && !isOperator(sre)) ||      // ) xxx
                (dst.get(dst.size()-1) instanceof CloseParen && sre instanceof OpenParen))// ) (
                {
                    dst.add(makeEntry(BooleanEntry.Operator.OPER_AND));
                }

                if (dst.get(dst.size()-1) instanceof OpenParen && sre instanceof CloseParen)
                {
                    throw new JED2KException(SearchCode.EMPTY_OPEN_CLOSE_PAREN);
                }
            }
        }

        dst.add(sre);
    }

    private static ArrayList<Serializable> string2Entries(
            long minSize,
            long maxSize,
            int sourcesCount,
            int completeSourcesCount,
            String fileType,
            String fileExtension,
            String codec,
            int mediaLength,
            int mediaBitrate,
            String value) throws JED2KException {

        if (fileType.length() > SEARCH_REQ_ELEM_LENGTH) {
            throw new JED2KException(SearchCode.FILE_TYPE_TOO_LONG);
        }

        if (fileExtension.length() > SEARCH_REQ_ELEM_LENGTH) {
            throw new JED2KException(SearchCode.FILE_EXT_TOO_LONG);
        }

        if (codec.length() > SEARCH_REQ_ELEM_LENGTH) {
            throw new JED2KException(SearchCode.CODEC_TOO_LONG);
        }

        if (value.length() > SEARCH_REQ_QUERY_LENGTH) {
            throw new JED2KException(SearchCode.QUERY_TOO_LONG);
        }

        if (value.isEmpty()) throw new JED2KException(SearchCode.QUERY_EMPTY);


        boolean verbatim = false;
        StringBuilder item = new StringBuilder();
        ArrayList<Serializable> res = new ArrayList<Serializable>();

        if (fileType.compareTo(ED2KFTSTR_FOLDER) == 0) // for folders we search emule collections exclude ed2k links - user brackets to correct expr
        {
            appendItem(res, new OpenParen());
            appendItem(res, makeEntry(null, Tag.FT_FILETYPE, ED2KFTSTR_EMULECOLLECTION));
            appendItem(res, makeEntry(BooleanEntry.Operator.OPER_NOT));
            appendItem(res, makeEntry("ED2K:\\"));
            appendItem(res, new CloseParen());
        }
        else
        {
            if (!fileType.isEmpty())
            {
                if ((fileType.compareTo(ED2KFTSTR_ARCHIVE) == 0) || (fileType.compareTo(ED2KFTSTR_CDIMAGE) == 0))
                {
                    appendItem(res, makeEntry(null, Tag.FT_FILETYPE, ED2KFTSTR_PROGRAM));
                }
                else
                {
                    appendItem(res, makeEntry(null, Tag.FT_FILETYPE, fileType)); // I don't check this value!
                }
            }

            // if type is not folder - process file parameters now
            if (fileType.compareTo(ED2KFTSTR_EMULECOLLECTION) != 0)
            {
                if (minSize != 0)
                    appendItem(res, makeEntry(null, Tag.FT_FILESIZE, Operator.ED2K_SEARCH_OP_GREATER.value, minSize));

                if (maxSize != 0)
                    appendItem(res, makeEntry(null, Tag.FT_FILESIZE, Operator.ED2K_SEARCH_OP_LESS.value, maxSize));

                if (sourcesCount != 0)
                    appendItem(res, makeEntry(null, Tag.FT_SOURCES, Operator.ED2K_SEARCH_OP_GREATER.value, sourcesCount));

                if (completeSourcesCount != 0)
                    appendItem(res, makeEntry(null, Tag.FT_COMPLETE_SOURCES, Operator.ED2K_SEARCH_OP_GREATER.value, completeSourcesCount));

                if (!fileExtension.isEmpty())
                    appendItem(res, makeEntry(null, Tag.FT_FILEFORMAT, fileExtension)); // I don't check this value!

                if (!codec.isEmpty())
                    appendItem(res, makeEntry(null, Tag.FT_MEDIA_CODEC, codec)); // I don't check this value!

                if (mediaLength != 0)
                    appendItem(res, makeEntry(null, Tag.FT_MEDIA_LENGTH, Operator.ED2K_SEARCH_OP_GREATER_EQUAL.value, mediaLength)); // I don't check this value!

                if (mediaBitrate != 0)
                    appendItem(res, makeEntry(null, Tag.FT_MEDIA_BITRATE, Operator.ED2K_SEARCH_OP_GREATER_EQUAL.value, mediaBitrate)); // I don't check this value!
            }
        }

        int beforeCount = res.size();

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
                        BooleanEntry.Operator so = string2Operator(item.toString());

                        if (so != null)
                        {
                            // add boolean operator
                            if (res.isEmpty() || (res.get(res.size()-1) instanceof BooleanEntry) || (c == ')'))
                            {
                                // operator in begin, operator before previous operator and operator before close bracket is error
                                throw new JED2KException(SearchCode.OPERATOR_AT_BEGIN_OF_EXPRESSION);
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
                        appendItem(res, new OpenParen());
                    }

                    if (c == ')') {
                        appendItem(res, new CloseParen());
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
            throw new JED2KException(SearchCode.UNCLOSED_QUOTATION_MARK);
        }

        if (item.length() != 0)
        {
            // add last item - check it is not operator
            BooleanEntry.Operator so = string2Operator(item.toString());

            if (so != null)
            {
                throw new JED2KException(SearchCode.OPERATOR_AT_END_OF_EXPRESSION);
            }
            else
            {
                appendItem(res, makeEntry(item.toString().replace("\"", "")));
            }
        }

        if (res.size() - beforeCount > SEARCH_REQ_ELEM_COUNT) throw new JED2KException(SearchCode.QUERY_TOO_COMPLEX);

        return res;
    }

    private static ArrayDeque<Serializable> packRequest(final ArrayList<Serializable> source) throws JED2KException {
        ArrayDeque<Serializable> res = new ArrayDeque<Serializable>();
        Stack<Serializable> operators_stack = new Stack<Serializable>();

        for(int i = source.size() - 1; i >= 0; --i) {
            Serializable entry = source.get(i);
            if (isOperator(entry)) {

                if (entry instanceof OpenParen) {
                    if (operators_stack.empty()) {
                        throw new JED2KException(SearchCode.INCORRECT_PARENS_COUNT);
                    }

                    // roll up
                    while(!(operators_stack.peek() instanceof CloseParen)) {
                        res.addFirst(operators_stack.pop());

                        if (operators_stack.empty()) {
                            throw new JED2KException(SearchCode.INCORRECT_PARENS_COUNT);
                        }
                    }

                    // pull close bracket entry
                    operators_stack.pop();
                    continue;
                }

                // we have normal operator and on stack top we have normal operator
                // prepare result - move operator from top to result and replace top
                if ((entry instanceof BooleanEntry) &&
                        !operators_stack.empty() &&
                        (operators_stack.peek() instanceof BooleanEntry))
                {
                    res.addFirst(operators_stack.pop());
                }

                operators_stack.push(entry);
            }
            else
            {
                res.addFirst(entry);
            }
        }

        if (!operators_stack.empty())
        {
            if (operators_stack.peek() instanceof OpenParen || operators_stack.peek() instanceof CloseParen)
            {
                throw new JED2KException(SearchCode.INCORRECT_PARENS_COUNT);
            }

            res.addFirst(operators_stack.pop());
        }

        return res;
    }

    private static ByteContainer<UInt16> generateTag(String name, byte id) throws JED2KException {
        ByteContainer<UInt16> tag;
        if (name != null) {
            tag = ByteContainer.fromString16(name);
        } else {
            byte[] nm = {id};
            tag = new ByteContainer<UInt16>(uint16(1), nm);
        }

        return tag;
    }

    public static Serializable makeEntry(String value) throws JED2KException {
            return new StringEntry(ByteContainer.fromString16(value), null);
    }

    public static Serializable makeEntry(String name, byte id, String value) throws JED2KException {
            return new StringEntry(ByteContainer.fromString16(value), generateTag(name, id));
    }

    public static Serializable makeEntry(String name, byte id, byte operator, long value) throws JED2KException {
        return new NumericEntry(value, operator, generateTag(name, id));
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        assert(false);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(value != null);
        Iterator<Serializable> itr = value.iterator();
        while(itr.hasNext()) {
            Serializable s = itr.next();
            s.put(dst);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        int res = 0;

        Iterator<Serializable> itr = value.iterator();
        while(itr.hasNext()) {
            res += itr.next().bytesCount();
        }

        return res;
    }

    public int count() {
        assert(value != null);
        return value.size();
    }

    public Serializable entry(int index) {
        assert(value != null);
        assert(value.size() > index);
        //assert(value.get(index) != null);
        Iterator<Serializable> itr = value.iterator();
        int current = 0;
        while(itr.hasNext()) {
            Serializable s = itr.next();
            if (current == index) return s;
            ++current;
        }

        return null;
    }

    public static SearchRequest makeRequest(
            long minSize,
            long maxSize,
            int sourcesCount,
            int completeSourcesCount,
            String fileType,
            String fileExtension,
            String codec,
            int mediaLength,
            int mediaBitrate,
            String value) throws JED2KException {
        ArrayList<Serializable> a;
        a = string2Entries(minSize, maxSize, sourcesCount, completeSourcesCount,
                fileType, fileExtension, codec, mediaLength, mediaBitrate, value);
        log.debug(dbgString(a));
        return new SearchRequest(packRequest(string2Entries(minSize, maxSize, sourcesCount, completeSourcesCount,
                fileType, fileExtension, codec, mediaLength, mediaBitrate, value)));
    }

    public static SearchRequest makeRelatedSearchRequest(Hash value) throws JED2KException {
        ArrayDeque<Serializable> ival = new ArrayDeque<Serializable>();
        ival.add(makeEntry("related::" + value.toString()));
        return new SearchRequest(ival);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Serializable> itr = value.iterator();
        while(itr.hasNext()) {
            sb.append(" ").append(itr.next());
        }

        return sb.toString();
    }

    public static String dbgString(Iterable<Serializable> a) {
        StringBuilder sb = new StringBuilder();
        Iterator<Serializable> itr = a.iterator();
        while(itr.hasNext()) {
            Serializable s = itr.next();
            sb.append(" ").append(s.toString());
        }
        return sb.toString();
    }
}
