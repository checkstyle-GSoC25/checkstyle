parser grammar JavadocCommentsParser;

options {
  tokenVocab = JavadocCommentsLexer;
}

javadoc:
    EOF
    ;
