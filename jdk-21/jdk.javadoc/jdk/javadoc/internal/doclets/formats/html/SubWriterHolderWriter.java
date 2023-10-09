/*
 * Copyright (c) 1997, 2022, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.javadoc.internal.doclets.formats.html;

import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.markup.BodyContents;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;

/**
 * This abstract class exists to provide functionality needed in the
 * the formatting of member information.  Since AbstractSubWriter and its
 * subclasses control this, they would be the logical place to put this.
 * However, because each member type has its own subclass, subclassing
 * can not be used effectively to change formatting.  The concrete
 * class subclass of this class can be subclassed to change formatting.
 *
 * @see AbstractMemberWriter
 * @see ClassWriterImpl
 */
public abstract class SubWriterHolderWriter extends HtmlDocletWriter {

    /**
     * The HTML builder for the body contents.
     */
    protected BodyContents bodyContents = new BodyContents();

    public SubWriterHolderWriter(HtmlConfiguration configuration, DocPath filename) {
        super(configuration, filename);
    }

    /**
     * Add the summary header.
     *
     * @param mw the writer for the member being documented
     * @param memberContent the content to which the summary header will be added
     */
    public void addSummaryHeader(AbstractMemberWriter mw, Content memberContent) {
        mw.addSummaryLabel(memberContent);
    }

    /**
     * Add the inherited summary header.
     *
     * @param mw the writer for the member being documented
     * @param typeElement the te to be documented
     * @param inheritedContent the content to which the inherited summary header will be added
     */
    public void addInheritedSummaryHeader(AbstractMemberWriter mw, TypeElement typeElement,
            Content inheritedContent) {
        mw.addInheritedSummaryLabel(typeElement, inheritedContent);
    }

    /**
     * Add the index comment.
     *
     * @param member the member being documented
     * @param content the content to which the comment will be added
     */
    protected void addIndexComment(Element member, Content content) {
        List<? extends DocTree> tags = utils.getFirstSentenceTrees(member);
        addIndexComment(member, tags, content);
    }

    /**
     * Add the index comment.
     *
     * @param member the member being documented
     * @param firstSentenceTags the first sentence tags for the member to be documented
     * @param tdSummaryContent the content to which the comment will be added
     */
    protected void addIndexComment(Element member, List<? extends DocTree> firstSentenceTags,
            Content tdSummaryContent) {
        addPreviewSummary(member, tdSummaryContent);
        List<? extends DeprecatedTree> deprs = utils.getDeprecatedTrees(member);
        Content div;
        if (utils.isDeprecated(member)) {
            var deprLabel = HtmlTree.SPAN(HtmlStyle.deprecatedLabel, getDeprecatedPhrase(member));
            div = HtmlTree.DIV(HtmlStyle.block, deprLabel);
            if (!deprs.isEmpty()) {
                addSummaryDeprecatedComment(member, deprs.get(0), div);
            }
            tdSummaryContent.add(div);
            return;
        } else {
            Element te = member.getEnclosingElement();
            if (te != null &&  utils.isTypeElement(te) && utils.isDeprecated(te)) {
                var deprLabel = HtmlTree.SPAN(HtmlStyle.deprecatedLabel, getDeprecatedPhrase(te));
                div = HtmlTree.DIV(HtmlStyle.block, deprLabel);
                tdSummaryContent.add(div);
            }
        }
        addSummaryComment(member, firstSentenceTags, tdSummaryContent);
    }

    /**
     * Add the summary link for the member.
     *
     * @param member the member to be documented
     * @param content the content to which the link will be added
     */
    public void addSummaryLinkComment(Element member, Content content) {
        List<? extends DocTree> tags = utils.getFirstSentenceTrees(member);
        addSummaryLinkComment(member, tags, content);
    }

    /**
     * Add the summary link comment.
     *
     * @param member the member being documented
     * @param firstSentenceTags the first sentence tags for the member to be documented
     * @param tdSummaryContent the content to which the comment will be added
     */
    public void addSummaryLinkComment(Element member, List<? extends DocTree> firstSentenceTags, Content tdSummaryContent) {
        addIndexComment(member, firstSentenceTags, tdSummaryContent);
    }

    /**
     * Add the inherited member summary.
     *
     * @param mw the writer for the member being documented
     * @param typeElement the class being documented
     * @param member the member being documented
     * @param isFirst true if it is the first link being documented
     * @param linksContent the content to which the summary will be added
     */
    public void addInheritedMemberSummary(AbstractMemberWriter mw,
                                          TypeElement typeElement,
                                          Element member,
                                          boolean isFirst,
                                          Content linksContent) {
        if (!isFirst) {
            linksContent.add(", ");
        }
        mw.addInheritedSummaryLink(typeElement, member, linksContent);
    }

    /**
     * {@return the document content header}
     */
    public Content getContentHeader() {
        return new ContentBuilder();
    }

    /**
     * Add the class content.
     *
     * @param source class content which will be added to the documentation
     */
    public void addClassContent(Content source) {
        bodyContents.addMainContent(source);
    }

    /**
     * Add the annotation content.
     *
     * @param source annotation content which will be added to the documentation
     */
    public void addAnnotationContent(Content source) {
        addClassContent(source);
    }

    /**
     * {@return the member header}
     */
    public Content getMemberHeader() {
        return HtmlTree.UL(HtmlStyle.blockList);
    }

    /**
     * Returns a list to be used for the list of summaries for members of a given kind.
     *
     * @return a list to be used for the list of summaries for members of a given kind
     */
    public Content getSummariesList() {
        return HtmlTree.UL(HtmlStyle.summaryList);
    }

    /**
     * Returns an item for the list of summaries for members of a given kind.
     *
     * @param content content for the item
     * @return an item for the list of summaries for members of a given kind
     */
    public Content getSummariesListItem(Content content) {
        return HtmlTree.LI(content);
    }


    /**
     * Returns a list to be used for the list of details for members of a given kind.
     *
     * @return a list to be used for the list of details for members of a given kind
     */
    public Content getDetailsList() {
        return HtmlTree.UL(HtmlStyle.detailsList);
    }

    /**
     * Returns an item for the list of details for members of a given kind.
     *
     * @param content content for the item
     * @return an item for the list of details for members of a given kind
     */
    public Content getDetailsListItem(Content content) {
        return HtmlTree.LI(content);
    }

    /**
     * {@return a list to add member items to}
     */
    public Content getMemberList() {
        return HtmlTree.UL(HtmlStyle.memberList);
    }

    /**
     * {@return a member item}
     *
     * @param member the member to represent as an item
     */
    public Content getMemberListItem(Content member) {
        return HtmlTree.LI(member);
    }

    public Content getMemberInherited() {
        return HtmlTree.DIV(HtmlStyle.inheritedList);
    }

    /**
     * Adds a section for a summary with the given CSS {@code class} and {@code id} attribute.
     *
     * @param style  the CSS class for the section
     * @param htmlId the id for the section
     * @param target the list of summary sections to which the summary will be added
     * @param source the content representing the summary
     */
    public void addSummary(HtmlStyle style, HtmlId htmlId, Content target, Content source) {
        var htmlTree = HtmlTree.SECTION(style, source)
                .setId(htmlId);
        target.add(getSummariesListItem(htmlTree));
    }

    /**
     * {@return the member content}
     *
     * @param content the content used to generate the complete member
     */
    public Content getMember(Content content) {
        return HtmlTree.LI(content);
    }

    /**
     * {@return the member summary content}
     *
     * @param memberContent the content used to generate the member summary
     */
    public Content getMemberSummary(Content memberContent) {
        return HtmlTree.SECTION(HtmlStyle.summary, memberContent);
    }

    /**
     * {@return the member details}
     *
     * @param content the content used to generate the member details
     */
    public Content getMemberDetailsContent(Content content) {
        return HtmlTree.SECTION(HtmlStyle.details, content);
    }

    /**
     * Get the member content
     *
     * @param id the id to be used for the content
     * @param style the style class to be added to the content
     * @param source the content used to generate the complete member content
     * @return the member content
     */
    public Content getMember(HtmlId id, HtmlStyle style, Content source) {
        return HtmlTree.SECTION(style, source).setId(id);
    }
}
