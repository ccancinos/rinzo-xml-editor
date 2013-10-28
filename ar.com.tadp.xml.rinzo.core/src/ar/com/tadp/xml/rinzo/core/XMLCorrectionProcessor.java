/*****************************************************************************
 * This file is part of Rinzo
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/editorxml
 * Copyright (C): 2008, Claudio Cancinos
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.xml.rinzo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.contentassist.proposals.XMLCompletionProposal;
import ar.com.tadp.xml.rinzo.core.model.XMLAttribute;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.visitor.ToStringVisitor;
import ar.com.tadp.xml.rinzo.core.utils.Utils;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * Generates proposals for quick assistance.
 * 
 * @author ccancinos
 */
public class XMLCorrectionProcessor implements IQuickAssistProcessor {
	private final RinzoXMLEditor xmlEditor;
	private String lineSeparator = null;

	public XMLCorrectionProcessor(RinzoXMLEditor xmlEditor) {
		this.xmlEditor = xmlEditor;
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext context) {
		try {
			Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			XMLNode node = XMLTreeModelUtilities.getActiveNode(context.getSourceViewer().getDocument(),
					context.getOffset());

			this.addTagProposals(context, proposals, node);
			this.addTextProposals(context, proposals, node);

			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private void addTextProposals(IQuickAssistInvocationContext context, Collection<ICompletionProposal> proposals,
			XMLNode node) {
		if (node.isTextTag()) {
			this.addCDATAProposal(proposals, node, context);
		}
	}

	private void addTagProposals(IQuickAssistInvocationContext context, Collection<ICompletionProposal> proposals,
			XMLNode node) throws BadLocationException {
		if (node.isTag() || node.isEndTag() || node.isEmptyTag()) {
			if (node.isEndTag()) {
				node = node.getCorrespondingNode();
			}
			int offset = node.getSelectionOffset();
			int tagLength = this.getNodeFullLength(node);
			IDocument document = context.getSourceViewer().getDocument();
			String replacement = document.get(node.getOffset(), tagLength);
			String tagIndentation = this.getIndentation(document, node.getOffset());
			String indentationToken = XMLEditorPlugin.getDefault().getIndentToken();

			this.addRenameTagProposal(proposals, node, offset, tagLength, replacement, document);
			this.addDuplicateTagProposal(proposals, node, offset, tagLength, replacement, document, tagIndentation);
			this.addCutTagProposal(proposals, node, context, tagLength, replacement);
			this.addSurroundWithTagProposal(proposals, node, offset, tagLength, replacement, document, tagIndentation,
					indentationToken);
			this.addCommentTagProposal(proposals, node, offset, tagLength, replacement, document, tagIndentation,
					indentationToken);
			this.addDeleteTagProposal(proposals, node, tagLength);
			this.addDeleteSurroundingTagProposal(proposals, node, tagLength);
		}
	}

	private void addCDATAProposal(Collection<ICompletionProposal> proposals, XMLNode node,
			IQuickAssistInvocationContext context) {
		int offset = node.getOffset() + 1;
		int tagLength = node.getContent().length();
		String lineSeparator = this.xmlEditor.getLineSeparator();
		String replacement = "<![CDATA[" + node.getContent() + "]]>";
		this.addTemplate(proposals, "Sorround With CDATA", replacement, PluginImages.IMG_EDIT_INLINE, context
				.getSourceViewer().getDocument(), offset, tagLength, offset, tagLength);
	}

	private void addRenameTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int offset, int length,
			String replacement, IDocument document) {
		replacement = replacement.replace(node.getTagName(), "${" + node.getTagName() + "}");
		this.addTemplate(proposals, "Rename Tag", replacement, PluginImages.IMG_EDIT_INLINE, document, offset, length,
				offset, length);
	}

	private void addDuplicateTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int offset,
			int length, String replacement, IDocument document, String tagIndentation) {
		for (Iterator<Entry<String, XMLAttribute>> iterator = node.getAttributes().entrySet().iterator(); iterator
				.hasNext();) {
			Entry<String, XMLAttribute> attribute = iterator.next();
			XMLAttribute attr = attribute.getValue();
			replacement = replacement.replace("\"" + attr.getValue() + "\"", "\"${" + attr.getName() + "}\"");
		}
		replacement = this.getLineSeparator() + tagIndentation + replacement;
		this.addTemplate(proposals, "Duplicate Tag", replacement, PluginImages.IMG_CHANGE, document, offset + length,
				0, offset + length, length);
	}

	private void addCutTagProposal(Collection<ICompletionProposal> proposals, final XMLNode node,
			final IQuickAssistInvocationContext context, final int length, final String replacement) {
		proposals.add(new XMLCompletionProposal("", node.getOffset(), length, 0, PluginImages
				.get(PluginImages.IMG_CHANGE), "Cut Tag", null, null) {
			public void apply(IDocument document) {
				Clipboard clipboard = null;
				try {
					clipboard = new Clipboard(xmlEditor.getSite().getShell().getDisplay());
					clipboard.setContents(new Object[] { replacement }, new Transfer[] { TextTransfer.getInstance() });
				} finally {
					if (clipboard != null) {
						clipboard.dispose();
					}
				}
				super.apply(document);
			}
		});
	}

	private void addSurroundWithTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int offset,
			int length, String replacement, IDocument document, String tagIndentation, String indentationToken) {
		replacement = "<${element}>" + this.getLineSeparator() + tagIndentation + indentationToken
				+ replacement.replace(this.getLineSeparator(), this.getLineSeparator() + indentationToken)
				+ this.getLineSeparator() + tagIndentation + "</${element}>";

		this.addTemplate(proposals, "Surround With Tag", replacement, PluginImages.IMG_CHANGE, document, offset,
				length, offset, length);
	}

	private void addDeleteSurroundingTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int length) {
		if (!node.isEmptyTag()) {
			ToStringVisitor visitor = new ToStringVisitor();
			node.accept(visitor);
			String replacement = visitor.getString();
			replacement = replacement.substring(replacement.indexOf(">") + 1, replacement.lastIndexOf("</"));
			proposals.add(new CompletionProposal(replacement, node.getOffset(), length, 0, PluginImages
					.get(PluginImages.IMG_DELETE), "Delete Surrounding Tag", null,
					"Deletes this tag leaving its childs."));
		}
	}

	private void addCommentTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int offset, int length,
			String replacement, IDocument document, String tagIndentation, String indentationToken) {
		replacement = "<!--" + this.getLineSeparator() + tagIndentation + replacement + this.getLineSeparator()
				+ tagIndentation + "-->";

		proposals.add(new CompletionProposal(replacement, node.getOffset(), length, 0, PluginImages
				.get(PluginImages.IMG_CHANGE), "Comment Tag", null, null));
	}

	private String getLineSeparator() {
		if (this.lineSeparator == null) {
			this.lineSeparator = this.xmlEditor.getLineSeparator();
		}
		return this.lineSeparator;
	}

	private void addDeleteTagProposal(Collection<ICompletionProposal> proposals, XMLNode node, int length) {
		proposals.add(new CompletionProposal("", node.getOffset(), length, 0,
				PluginImages.get(PluginImages.IMG_DELETE), "Delete Tag", null, null));
	}

	private void addTemplate(Collection<ICompletionProposal> proposals, String name, String pattern, String image,
			IDocument document, int positionOffset, int positionLength, int regionOffset, int regionLength) {
		TemplateContext context = new DocumentTemplateContext(new TemplateContextType(), document, new Position(
				positionOffset - 1, positionLength));
		IRegion region = new Region(regionOffset, regionLength);
		Template template = new Template(name, "", "tag", pattern, false);
		TemplateProposal templateProposal = new TemplateProposal(template, context, region, PluginImages.get(image));
		proposals.add(templateProposal);
	}

	private int getNodeFullLength(XMLNode node) {
		return (node.isEmptyTag()) ? node.getLength() : node.getCorrespondingNode().getOffset() - node.getOffset()
				+ node.getCorrespondingNode().getLength();
	}

	private String getIndentation(IDocument document, int offset) {
		try {
			int line = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(line);
			return Utils.getLeadingWhitespace(document.get(lineOffset, Math.abs(lineOffset - offset)));
		} catch (BadLocationException e) {
			return "";
		}
	}

	public String getErrorMessage() {
		return null;
	}

	public boolean canFix(Annotation annotation) {
		return true;
	}

	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return true;
	}

}