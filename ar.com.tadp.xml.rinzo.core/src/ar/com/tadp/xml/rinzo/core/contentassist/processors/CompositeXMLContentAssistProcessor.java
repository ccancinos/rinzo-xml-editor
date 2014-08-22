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
package ar.com.tadp.xml.rinzo.core.contentassist.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.contentassist.proposals.XMLCompletionProposalComparator;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Delegates method invocations to all the elements of delegated {@link IXMLContentAssistProcessor}
 * 
 * @author ccancinos
 */
public class CompositeXMLContentAssistProcessor implements IContentAssistProcessor, IXMLContentAssistProcessor {
	protected ITextViewer viewer;
	private Collection<IXMLContentAssistProcessor> processors = new ArrayList<IXMLContentAssistProcessor>();
	private RinzoXMLEditor editor;

	public CompositeXMLContentAssistProcessor(RinzoXMLEditor xmlEditor) {
		this.editor = xmlEditor;
	}

	public void addProcessor(IXMLContentAssistProcessor processor) {
		this.processors.add(processor);
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		this.viewer = viewer;
		ICompletionProposal result[] = null;
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		XMLNode currentNode = this.editor.getModel().getTree().getActiveNode(documentOffset);
		
		//TODO MAKE THIS METHOD TO CALL this.computeCompletionProposal SO EACH PROCESSOR COULD GIVE PROPOSALS FOR AN EMPTY FILE LIKE FILE TEMPLATES
		if (currentNode == null) {
			return new ICompletionProposal[0];
		}

		if(currentNode.offset == documentOffset) {
			currentNode = this.editor.getModel().getTree().getPreviousNode(documentOffset);
		}

		this.computeCompletionProposal(resultList, documentOffset, currentNode);
		result = (ICompletionProposal[]) resultList.toArray(new ICompletionProposal[resultList.size()]);

		Arrays.sort(result, XMLCompletionProposalComparator.getInstance());

		return result;
	}

    protected void computeCompletionProposal(Collection<ICompletionProposal> resultList, int offset, XMLNode currentNode) {
		String prefix = this.extractPrefix(this.viewer, offset);

		if (this.shouldProposeInBody(currentNode, prefix, offset)) {
			this.addBodyProposals(currentNode, prefix, this.viewer, offset, resultList);
		}
		if (this.shouldProposeCloseTag(currentNode, prefix, offset)) {
			this.addCloseTagProposals(currentNode, prefix, this.viewer, offset, resultList);
		}
		if (this.shouldProposeAttributeNames(currentNode, prefix, offset)) {
			this.addAttributeProposals(currentNode, prefix, this.viewer, offset, resultList);
		}
		if (this.shouldProposeAttributeValues(currentNode, prefix, offset)) {
			this.addAttributeValuesProposals(currentNode, prefix.substring(0, prefix.indexOf("=")).trim(), prefix,
					this.viewer, offset, resultList);
		}
    }

    /**
	 * We watch for angular brackets since those are often part of XML
	 * templates.
	 * 
	 * @param viewer the viewer
	 * @param offset the offset left of which the prefix is detected
	 * @return the detected prefix
	 */
	protected String extractPrefix(ITextViewer viewer, int offset) {
		IDocument document= viewer.getDocument();
		int i= offset;
		if (i > document.getLength()) {
			return "";
		}
		
		try {
			while (i > 0) {
				i--;
				char ch= document.getChar(i);
				if (ch != '<' && !(Character.isJavaIdentifierPart(ch) || ch == ':' || ch == '='  || ch == '"' || ch == '\'')) {
					i++;
					break;
				}
			}
			return document.get(i, offset - i);
		} catch (Exception e) {
			return "";
		}
	}
    	
    private boolean shouldProposeAttributeValues(XMLNode currentNode, String prefix, int offset) {
		return !currentNode.isTextTag() && isInAttributeValue(prefix);
	}

	private boolean shouldProposeAttributeNames(XMLNode currentNode, String prefix, int offset) {
		return !currentNode.isTextTag() && !currentNode.isEndTag() && !isInAttributeValue(prefix)
				&& !prefix.startsWith("<") && !prefix.trim().endsWith("=");
	}

	private boolean shouldProposeCloseTag(XMLNode currentNode, String prefix, int offset) {
		return (currentNode.isIncompleteTag() && !StringUtils.isEmpty(currentNode.getTagName()) && (StringUtils.isEmpty(prefix) || prefix.startsWith("<"))) 
				|| 
				(currentNode.isTextTag() && currentNode.getParent().getCorrespondingNode() == null);
	}

	private boolean shouldProposeInBody(XMLNode currentNode, String prefix, int offset) {
		return currentNode.isTextTag() || currentNode.getContent().trim().equals(prefix)
				|| (currentNode.getParent() == null && currentNode.isEndTag());
	}

	
	public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection<ICompletionProposal> resultList) {
		for (IXMLContentAssistProcessor processor : this.processors) {
			processor.addBodyProposals(currentNode, prefix, viewer, offset, resultList);
		}
	}
	
	public void addAttributeProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection<ICompletionProposal> resultList) {
		for (IXMLContentAssistProcessor processor : this.processors) {
			processor.addAttributeProposals(currentNode, prefix, viewer, offset, resultList);
		}
	}
	
	public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix, ITextViewer viewer, int offset, Collection<ICompletionProposal> resultList) {
		for (IXMLContentAssistProcessor processor : this.processors) {
			processor.addAttributeValuesProposals(currentNode, attributeName, prefix, viewer, offset, resultList);
		}
	}

	public void addCloseTagProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection<ICompletionProposal> resultList) {
		for (IXMLContentAssistProcessor processor : this.processors) {
			processor.addCloseTagProposals(currentNode, prefix, viewer, offset, resultList);
		}
	}

	/*
	* TODO Here I'm asking if I'm in the part of the declaration of the attribute name or the attribute value.
	* This should be handled using the parser partitions and defining the class completion proposal just in the attribute value part.   
	*/
	private boolean isInAttributeValue(String attributePrefix) {
		int firsIndex = attributePrefix.indexOf("\"");
		firsIndex = (firsIndex != -1) ? firsIndex : attributePrefix.indexOf("\'");
		return firsIndex != -1;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
