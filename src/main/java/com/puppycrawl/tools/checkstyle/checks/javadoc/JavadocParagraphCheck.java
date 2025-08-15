///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.javadoc;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.DetailNodeTreeStringPrinter;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocCommentsTokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

/**
 * <div>
 * Checks the Javadoc paragraph.
 * </div>
 *
 * <p>
 * Checks that:
 * </p>
 * <ul>
 * <li>There is one blank line between each of two paragraphs.</li>
 * <li>Each paragraph but the first has &lt;p&gt; immediately
 * before the first word, with no space after.</li>
 * <li>The outer most paragraph tags should not precede
 * <a href="https://www.w3schools.com/html/html_blocks.asp">HTML block-tag</a>.
 * Nested paragraph tags are allowed to do that. This check only supports following block-tags:
 * &lt;address&gt;,&lt;blockquote&gt;
 * ,&lt;div&gt;,&lt;dl&gt;
 * ,&lt;h1&gt;,&lt;h2&gt;,&lt;h3&gt;,&lt;h4&gt;,&lt;h5&gt;,&lt;h6&gt;,&lt;hr&gt;
 * ,&lt;ol&gt;,&lt;p&gt;,&lt;pre&gt;
 * ,&lt;table&gt;,&lt;ul&gt;.
 * </li>
 * </ul>
 *
 * <p><b>ATTENTION:</b></p>
 *
 * <p>This Check ignores HTML comments.</p>
 *
 * <p>The Check ignores all the nested paragraph tags,
 * it will not give any kind of violation if the paragraph tag is nested.</p>
 * <ul>
 * <li>
 * Property {@code allowNewlineParagraph} - Control whether the &lt;p&gt; tag
 * should be placed immediately before the first word.
 * Type is {@code boolean}.
 * Default value is {@code true}.
 * </li>
 * <li>
 * Property {@code violateExecutionOnNonTightHtml} - Control when to print violations
 * if the Javadoc being examined by this check violates the tight html rules defined at
 * <a href="https://checkstyle.org/writingjavadocchecks.html#Tight-HTML_rules">
 * Tight-HTML Rules</a>.
 * Type is {@code boolean}.
 * Default value is {@code false}.
 * </li>
 * </ul>
 *
 * <p>
 * Parent is {@code com.puppycrawl.tools.checkstyle.TreeWalker}
 * </p>
 *
 * <p>
 * Violation Message Keys:
 * </p>
 * <ul>
 * <li>
 * {@code javadoc.missed.html.close}
 * </li>
 * <li>
 * {@code javadoc.paragraph.line.before}
 * </li>
 * <li>
 * {@code javadoc.paragraph.misplaced.tag}
 * </li>
 * <li>
 * {@code javadoc.paragraph.preceded.block.tag}
 * </li>
 * <li>
 * {@code javadoc.paragraph.redundant.paragraph}
 * </li>
 * <li>
 * {@code javadoc.paragraph.tag.after}
 * </li>
 * <li>
 * {@code javadoc.parse.rule.error}
 * </li>
 * <li>
 * {@code javadoc.unclosedHtml}
 * </li>
 * <li>
 * {@code javadoc.wrong.singleton.html.tag}
 * </li>
 * </ul>
 *
 * @since 6.0
 */
@StatelessCheck
public class JavadocParagraphCheck extends AbstractJavadocCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_TAG_AFTER = "javadoc.paragraph.tag.after";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_LINE_BEFORE = "javadoc.paragraph.line.before";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_REDUNDANT_PARAGRAPH = "javadoc.paragraph.redundant.paragraph";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_MISPLACED_TAG = "javadoc.paragraph.misplaced.tag";

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_PRECEDED_BLOCK_TAG = "javadoc.paragraph.preceded.block.tag";

    /**
     * Set of block tags supported by this check.
     */
    private static final Set<String> BLOCK_TAGS =
            Set.of("address", "blockquote", "div", "dl",
                   "h1", "h2", "h3", "h4", "h5", "h6", "hr",
                   "ol", "p", "pre", "table", "ul");

    /**
     * Control whether the &lt;p&gt; tag should be placed immediately before the first word.
     */
    private boolean allowNewlineParagraph = true;

    /**
     * Setter to control whether the &lt;p&gt; tag should be placed
     * immediately before the first word.
     *
     * @param value value to set.
     * @since 6.9
     */
    public void setAllowNewlineParagraph(boolean value) {
        allowNewlineParagraph = value;
    }

    @Override
    public int[] getDefaultJavadocTokens() {
        return new int[] {
            JavadocCommentsTokenTypes.NEWLINE,
            JavadocCommentsTokenTypes.HTML_ELEMENT,
        };
    }

    @Override
    public int[] getRequiredJavadocTokens() {
        return getAcceptableJavadocTokens();
    }

    @Override
    public void visitJavadocToken(DetailNode ast) {
        if (ast.getLineNumber() == 87) {
            System.out.println(DetailNodeTreeStringPrinter.printTree(ast.getParent().getParent(), "", ""));
        }
        if (ast.getType() == JavadocCommentsTokenTypes.NEWLINE && isEmptyLine(ast)) {
            checkEmptyLine(ast);
        }
        else if (isParagraphTag(ast)) {
            checkParagraphTag(ast);
        }
    }

    private static boolean isParagraphTag(DetailNode detailNode) {
        return detailNode.getType() == JavadocCommentsTokenTypes.HTML_ELEMENT
                && detailNode.getFirstChild().getType() == JavadocCommentsTokenTypes.HTML_TAG_START
                && JavadocUtil.findFirstToken(detailNode.getFirstChild(), JavadocCommentsTokenTypes.TAG_NAME)
                .getText().equalsIgnoreCase("p");
    }

    /**
     * Determines whether or not the next line after empty line has paragraph tag in the beginning.
     *
     * @param newline NEWLINE node.
     */
    private void checkEmptyLine(DetailNode newline) {
        final DetailNode nearestToken = getNearestNode(newline);
        if (nearestToken.getType() == JavadocCommentsTokenTypes.TEXT
                && !CommonUtil.isBlank(nearestToken.getText())) {
            log(newline.getLineNumber(), newline.getColumnNumber(), MSG_TAG_AFTER);
        }
    }

    /**
     * Determines whether or not the line with paragraph tag has previous empty line.
     *
     * @param tag html tag.
     */
    private void checkParagraphTag(DetailNode tag) {

        if (tag.getLineNumber() == 58) {
           // System.out.println(DetailNodeTreeStringPrinter.printTree(tag, "", ""));
        }
        if (!isNestedParagraph(tag)) {
            final DetailNode newLine = getNearestEmptyLine(tag);
            if (isRedundantParagraphTag(tag)) {
                log(tag.getLineNumber(), tag.getColumnNumber(), MSG_REDUNDANT_PARAGRAPH);
            }
            else if (newLine == null || tag.getLineNumber() - newLine.getLineNumber() != 1) {
                log(tag.getLineNumber(), tag.getColumnNumber(), MSG_LINE_BEFORE);
            }

            final String blockTagName = findFollowedBlockTagName(tag);
            if (blockTagName != null) {
                log(tag.getLineNumber(), tag.getColumnNumber(),
                        MSG_PRECEDED_BLOCK_TAG, blockTagName);
            }

            if (!allowNewlineParagraph && isImmediatelyFollowedByNewLine(tag)) {
                log(tag.getLineNumber(), tag.getColumnNumber(), MSG_MISPLACED_TAG);
            }
            if (isNotImmediatelyFollowedByText(tag)) {
                log(tag.getLineNumber(), tag.getColumnNumber(), MSG_MISPLACED_TAG);
            }
        }
    }

    /**
     * Determines whether the paragraph tag is nested.
     *
     * @param tag html tag.
     * @return true, if the paragraph tag is nested.
     */
    private static boolean isNestedParagraph(DetailNode tag) {
        boolean nested = false;
        DetailNode parent = tag.getParent();

        while (parent != null) {
            if (parent.getType() == JavadocCommentsTokenTypes.HTML_ELEMENT) {
                nested = true;
                break;
            }
            parent = parent.getParent();
        }

        return nested;
    }

    /**
     * Determines whether or not the paragraph tag is followed by block tag.
     *
     * @param tag html tag.
     * @return block tag if the paragraph tag is followed by block tag or null if not found.
     */
    @Nullable
    private static String findFollowedBlockTagName(DetailNode tag) {
        final Optional<DetailNode> htmlElement = findFirstHtmlElementAfter(tag);
        return htmlElement
                .map(element -> JavadocUtil.findFirstToken(element, JavadocCommentsTokenTypes.HTML_TAG_START))
                .map(start -> JavadocUtil.findFirstToken(start, JavadocCommentsTokenTypes.TAG_NAME))
                .map(DetailNode::getText)
                .filter(BLOCK_TAGS::contains)
                .orElse(null);
    }

    /**
     * Finds and returns first html element after the tag.
     *
     * @param tag html tag.
     * @return first html element after the paragraph tag or null if not found.
     */
    private static Optional<DetailNode> findFirstHtmlElementAfter(DetailNode tag) {

        if (tag.getLineNumber() == 58) {
            //System.out.println(DetailNodeTreeStringPrinter.printTree(tag, "", ""));
        }

        return JavadocUtil.findFirstTokenByPredicate(tag,
                        node -> node.getType() == JavadocCommentsTokenTypes.HTML_CONTENT)
                .map(content -> JavadocUtil.findFirstToken(content, JavadocCommentsTokenTypes.HTML_ELEMENT));
    }

    /**
     * Returns nearest node.
     *
     * @param node DetailNode node.
     * @return nearest node.
     */
    private static DetailNode getNearestNode(DetailNode node) {
        DetailNode currentNode = node;
        while (currentNode.getType() == JavadocCommentsTokenTypes.LEADING_ASTERISK
                || currentNode.getType() == JavadocCommentsTokenTypes.NEWLINE) {
            currentNode = currentNode.getNextSibling();
        }
        return currentNode;
    }

    /**
     * Determines whether or not the line is empty line.
     *
     * @param newLine NEWLINE node.
     * @return true, if line is empty line.
     */
    private static boolean isEmptyLine(DetailNode newLine) {
        boolean result = false;
        DetailNode previousSibling = newLine.getPreviousSibling();

        if (previousSibling != null && isInJavadocContent(previousSibling)) {

            // Skip over blank TEXT nodes
            if (previousSibling.getType() == JavadocCommentsTokenTypes.TEXT
                    && CommonUtil.isBlank(previousSibling.getText())) {
                previousSibling = previousSibling.getPreviousSibling();
            }

            result = previousSibling != null
                    && previousSibling.getType() == JavadocCommentsTokenTypes.LEADING_ASTERISK;
        } else if (!isInJavadocContent(newLine) && !isInParagraphTag(newLine)) {
            result = false;
        } else {
            result = previousSibling != null
                    && previousSibling.getType() == JavadocCommentsTokenTypes.LEADING_ASTERISK;
        }

        return result;
    }



    private static boolean isInJavadocContent(DetailNode node) {
        final boolean result;
        final DetailNode parent = node.getParent();

        if (parent != null) {
            if (parent.getType() == JavadocCommentsTokenTypes.JAVADOC_CONTENT) {
                result = true;
            }
            else if (parent.getType() == JavadocCommentsTokenTypes.HTML_ELEMENT) {
                result = false;
            }
            else {
                result = isInJavadocContent(parent);
            }
        } else {
            result = false;
        }

        return result;
    }

    private static boolean isInParagraphTag(DetailNode node) {
        boolean result = false;
        DetailNode parent = node.getParent();

        if (parent != null) {
            if (isParagraphTag(parent)) {
                result = true;
            }
            else if (parent.getType() == JavadocCommentsTokenTypes.JAVADOC_CONTENT) {
                result = false;
            }
            else {
                result = isInParagraphTag(parent);
            }
        }

        return result;
    }




    /**
     * Determines whether or not the line with paragraph tag is first line in javadoc.
     *
     * @param paragraphTag paragraph tag.
     * @return true, if line with paragraph tag is first line in javadoc.
     */
    private static boolean isRedundantParagraphTag(DetailNode paragraphTag) {

        if (paragraphTag.getLineNumber() == 35) {
            System.out.println(DetailNodeTreeStringPrinter.printTree(paragraphTag, "", ""));
        }

        final DetailNode parent = paragraphTag.getParent();

        final boolean isFirstParagraphTag = JavadocUtil.findFirstTokenByPredicate(parent,
                        node -> node.getType() == JavadocCommentsTokenTypes.HTML_ELEMENT)
                .filter(c -> c.equals(paragraphTag))
                .isPresent();

        // If there is no content, we would log a different message
        final boolean hasEmbeddedText = JavadocUtil.findFirstTokenByPredicate(paragraphTag,
                        node -> node.getType() == JavadocCommentsTokenTypes.HTML_CONTENT)
                .isPresent();

        // If there is no text before the paragraph tag, it is redundant
        final boolean hasTextBeforeParagraphTag = JavadocUtil.findFirstTokenByPredicate(parent,
                node -> node.getType() == JavadocCommentsTokenTypes.TEXT
                        && !CommonUtil.isBlank(node.getText()))
                .filter(text -> text.getLineNumber() < paragraphTag.getLineNumber()
                        || (text.getLineNumber() == paragraphTag.getLineNumber()
                        && text.getColumnNumber() < paragraphTag.getColumnNumber()))
                .isPresent();

        return isFirstParagraphTag && hasEmbeddedText && !hasTextBeforeParagraphTag;
    }
    
    /**
     * Finds and returns nearest empty line in javadoc.
     *
     * @param node DetailNode node.
     * @return Some nearest empty line in javadoc.
     */
    private static DetailNode getNearestEmptyLine(DetailNode node) {
        DetailNode newLine = node;
        while (newLine != null) {
            final DetailNode previousSibling = newLine.getPreviousSibling();
            if (newLine.getType() == JavadocCommentsTokenTypes.NEWLINE && isEmptyLine(newLine)) {
                break;
            }
            newLine = previousSibling;
        }
        return newLine;
    }

    /**
     * Tests whether the paragraph tag is immediately followed by the text.
     *
     * @param tag html tag.
     * @return true, if the paragraph tag is immediately followed by the text.
     */
    private static boolean isNotImmediatelyFollowedByText(DetailNode tag) {

        // TODO: extract to it's own condition AND reconsider EOF
        // TODO: create some utility for finding html text?
        return tag.getNextSibling() == null
                || tag.getNextSibling() != null
                && tag.getNextSibling().getType() == JavadocCommentsTokenTypes.TEXT
                && tag.getNextSibling().getText().startsWith(" ")
                || tag.getFirstChild() != null
                && tag.getFirstChild().getNextSibling() != null
                && tag.getFirstChild().getNextSibling().getFirstChild() != null
                && tag.getFirstChild().getNextSibling().getFirstChild().getType() == JavadocCommentsTokenTypes.TEXT
                && tag.getFirstChild().getNextSibling().getFirstChild().getText().startsWith(" ")
                ;
    }

    /**
     * Tests whether the paragraph tag is immediately followed by the new line.
     *
     * @param tag html tag.
     * @return true, if the paragraph tag is immediately followed by the new line.
     */
    private static boolean isImmediatelyFollowedByNewLine(DetailNode tag) {
        return tag.getFirstChild() != null
                && tag.getFirstChild().getNextSibling() != null
                && tag.getFirstChild().getNextSibling().getType() == JavadocCommentsTokenTypes.NEWLINE
                || tag.getNextSibling() != null
                && tag.getNextSibling().getType() == JavadocCommentsTokenTypes.NEWLINE
                && tag.getFirstChild() != null
                // There is nothing else besides the tag on the line
                && tag.getFirstChild().getNextSibling() == null;

    }

    /**
     * Custom getNextSibling method to handle different types of paragraph tag.
     * It works for both {@code <p>} and {@code <p></p>} tags.
     *
     * @param tag HTML_ELEMENT tag.
     * @return next sibling of the tag.
     */
    private static DetailNode getNextSibling(DetailNode tag) {
        DetailNode nextSibling;

        if (JavadocUtil.getFirstChild(tag) != null
                && isParagraphTag(tag)) {
            final DetailNode paragraphToken = JavadocUtil.getFirstChild(tag);
            final DetailNode paragraphStartTagToken = JavadocUtil.getFirstChild(paragraphToken);
            nextSibling = JavadocUtil.getNextSibling(paragraphStartTagToken);
        }
        else {
            nextSibling = JavadocUtil.getNextSibling(tag);
        }

        if (nextSibling != null && nextSibling.getType() == JavadocCommentsTokenTypes.HTML_COMMENT) {
            nextSibling = JavadocUtil.getNextSibling(nextSibling);
        }

        return nextSibling;
    }
}
