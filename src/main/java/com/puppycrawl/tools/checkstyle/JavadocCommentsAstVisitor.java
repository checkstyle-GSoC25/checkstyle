package com.puppycrawl.tools.checkstyle;

import com.puppycrawl.tools.checkstyle.api.JavadocCommentsTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocNodeImpl;
import com.puppycrawl.tools.checkstyle.grammar.javadoc.JavadocCommentsLexer;
import com.puppycrawl.tools.checkstyle.grammar.javadoc.JavadocCommentsParser;
import com.puppycrawl.tools.checkstyle.grammar.javadoc.JavadocCommentsParserBaseVisitor;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class JavadocCommentsAstVisitor extends JavadocCommentsParserBaseVisitor<JavadocNodeImpl> {

    private final BufferedTokenStream tokens;

    public JavadocCommentsAstVisitor(CommonTokenStream tokens) {
        this.tokens = tokens;
    }


    @Override
    public JavadocNodeImpl visitJavadoc(JavadocCommentsParser.JavadocContext ctx) {
        final JavadocNodeImpl javadocNode;
        final boolean isEmptyJavadoc = ctx.children.size() == 1;
        if (isEmptyJavadoc) {
            javadocNode = null;
        }
        else {
            javadocNode =  createImaginary(JavadocCommentsTokenTypes.JAVADOC);
            processChildren(javadocNode, ctx.children.subList(0, ctx.children.size() - 1));
        }
        return javadocNode;
    }

    @Override
    public JavadocNodeImpl visitMainDescription(JavadocCommentsParser.MainDescriptionContext ctx) {
        return flattenedTree(ctx);
    }

    private JavadocNodeImpl flattenedTree(ParserRuleContext ctx) {
        final JavadocNodeImpl dummyNode = new JavadocNodeImpl();
        processChildren(dummyNode, ctx.children);
        return (JavadocNodeImpl) JavadocUtil.getFirstChild(dummyNode);
    }

    private void processChildren(JavadocNodeImpl parent, List<? extends ParseTree> children) {
        if (children != null) {
            children.forEach(child -> {
                if (child instanceof TerminalNode) {
                    TerminalNode terminalNode = (TerminalNode) child;
                    Token token = (Token) terminalNode.getPayload();
                    addTokenAndHiddenAsterisks(token, parent);
                } else {
                    parent.addChild(visit(child));
                }
            });
        }
    }

    private void addTokenAndHiddenAsterisks(Token token, JavadocNodeImpl parent) {
        int tokenIndex = token.getTokenIndex();
        final List<Token> tokensToLeft =
                tokens.getHiddenTokensToLeft(tokenIndex, JavadocCommentsLexer.LEADING_ASTERISKS);

        if (tokensToLeft != null) {
            for (Token asterisk : tokensToLeft) {
                parent.addChild(create(asterisk));
            }
        }

        parent.addChild(create(token));
    }

    private JavadocNodeImpl create(Token token) {
        final JavadocNodeImpl javadocNode = new JavadocNodeImpl();
        javadocNode.initialize(token);
        return javadocNode;
    }

    private JavadocNodeImpl createImaginary(int tokenType) {
        final JavadocNodeImpl node = new JavadocNodeImpl();
        node.setType(tokenType);
        node.setText(JavadocUtil.getJavadocTokenName(tokenType));
        return node;
    }
}
