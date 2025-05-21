package com.puppycrawl.tools.checkstyle.grammar.javadoc;

import com.puppycrawl.tools.checkstyle.AbstractTreeTestSupport;
import org.junit.jupiter.api.Test;

public class JavadocCommentsAstRegressionTest extends AbstractTreeTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/grammar/javadoc/";
    }

    @Test
    public void testEmptyJavadoc() throws Exception {
        verifyJavadocTree(getPath("ExpectedEmptyJavadoc.txt"),
                getPath("InputEmptyJavadoc.javadoc"));
    }

    @Test
    public void testEmptyJavadocWithTabs() throws Exception {
        verifyJavadocTree(getPath("ExpectedEmptyJavadocWithTabs.txt"),
                getPath("InputEmptyJavadocWithTabs.javadoc"));
    }

    @Test
    public void testEmptyJavadocStartsWithNewline() throws Exception {
        verifyJavadocTree(getPath("ExpectedEmptyJavadocStartsWithNewline.txt"),
                getPath("InputEmptyJavadocStartsWithNewline.javadoc"));
    }

    @Test
    public void testJavadocWithText() throws Exception {
        verifyJavadocTree(getPath("ExpectedJavadocWithText.txt"),
                getPath("InputJavadocWithText.javadoc"));
    }
}
