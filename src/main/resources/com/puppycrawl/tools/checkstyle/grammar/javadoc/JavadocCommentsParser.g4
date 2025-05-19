parser grammar JavadocCommentsParser;

options {
  tokenVocab = JavadocCommentsLexer;
}

javadoc
    : NEWLINE* mainDescription EOF;

mainDescription: javadocLine*;

javadocLine
    :  LEADING_ASTERISK (NEWLINE)*
    ;
