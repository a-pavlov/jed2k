package org.jed2k.exception;

public enum SearchCode implements BaseErrorCode {
    EMPTY_OPEN_CLOSE_PAREN(51, "Empty open/close parenthesis"),
    UNCLOSED_QUOTATION_MARK(52, "Unclosed quotation mark"),
    OPERATOR_AT_END_OF_EXPRESSION(53, "Operator at the end of expression"),
    OPERATOR_AT_BEGIN_OF_EXPRESSION(54, "Operator at the begin of expression"),
    INCORRECT_PARENS_COUNT(55, "Close and open parenthesis count is not match");

    private final int code;
    private final String descr;

    private SearchCode(int c, String descr) {
        this.code = c;
        this.descr = descr;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return descr;
    }
}
