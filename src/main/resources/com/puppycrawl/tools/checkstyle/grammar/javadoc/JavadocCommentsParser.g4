parser grammar JavadocCommentsParser;

options {
  tokenVocab = JavadocCommentsLexer;
}

javadoc
    : mainDescription EOF;

mainDescription: (NEWLINE | TEXT)*;
