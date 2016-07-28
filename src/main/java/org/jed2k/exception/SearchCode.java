package org.jed2k.exception;

public enum SearchCode implements BaseErrorCode {
    EMPTY_OPEN_CLOSE_PAREN(51),
    UNCLOSED_QUOTATION_MARK(52),
    OPERATOR_AT_END_OF_EXPRESSION(53),
    OPERATOR_AT_BEGIN_OF_EXPRESSION(54),
    INCORRECT_PARENS_COUNT(55);

    private final int value;

    private SearchCode(int value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }
}