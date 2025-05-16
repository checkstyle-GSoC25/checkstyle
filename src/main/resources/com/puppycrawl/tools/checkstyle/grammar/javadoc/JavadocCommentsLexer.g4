lexer grammar JavadocCommentsLexer;

tokens {
    JAVADOC, LEADING_ASTERISK
}

LEADING_ASTERISK
    :  '*'
    ;
