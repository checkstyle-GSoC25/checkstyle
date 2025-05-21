lexer grammar JavadocCommentsLexer;

channels {
    LEADING_ASTERISKS
}

tokens {
    JAVADOC, LEADING_ASTERISK, NEWLINE, TEXT
}

@lexer::members {
    private int previousTokenType = 0;
    private Token previousToken = null;
    private boolean afterNewline = true;

    public boolean isAfterNewline() {
        return afterNewline;
    }

    public void setAfterNewline() {
        afterNewline = true;
    }

    @Override
    public void emit(Token token) {
        super.emit(token);
        previousTokenType = token.getType();
        previousToken = token;
        if (previousTokenType != NEWLINE) {
            afterNewline = false;
        }
    }
}

LEADING_ASTERISK
    : [ \t]* '*' {isAfterNewline()}? -> channel(LEADING_ASTERISKS)
    ;

NEWLINE
    : '\r'? '\n' {setAfterNewline();}
    ;

TEXT
    : ~[@*{}\r\n]+
    ;
