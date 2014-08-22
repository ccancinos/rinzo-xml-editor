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

import java.util.Collection;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ar.com.tadp.xml.rinzo.core.contentassist.proposals.ProposalsFactory;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;

/**
 * It generates tags, attributes and attribute values proposals for xml
 * elements.
 * 
 * @author ccancinos
 */
public class XMLContentAssistProcessor implements IXMLContentAssistProcessor {

	/**
	 * TODO When adding an attribute with a default value defined in XSD/DTD add
	 * it.
	 */
	public void addAttributeProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection<ICompletionProposal> resultList) {
		Collection<String> nodeAttributes = currentNode.getAttributes().keySet();
		TagTypeDefinition typeDefinition = currentNode.getTypeDefinition();
		for (AttributeDefinition attrDef : typeDefinition.getAttributes()) {
			if (attrDef.getName().startsWith(prefix) && !nodeAttributes.contains(attrDef.getName())) {
				resultList.add(ProposalsFactory.createAttributeProposal(attrDef, offset, prefix.length()));
			}
		}
	}

	public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix,
			ITextViewer viewer, int offset, Collection<ICompletionProposal> resultList) {
		AttributeDefinition attributeDefinition = currentNode.getTypeDefinition().getAttribute(prefix);
		Collection<String> acceptableValues = null;
		int startAttrPrefix = prefix.indexOf("\"");
		startAttrPrefix = (startAttrPrefix != -1) ? startAttrPrefix : prefix.indexOf("\'");
		String attrPrefix = prefix.substring(startAttrPrefix + 1);
		
		if (attributeDefinition != null) {
			acceptableValues = attributeDefinition.getAcceptableValues();
			if (!acceptableValues.isEmpty()) {
				for (String currentValue : acceptableValues) {
					if (currentValue.startsWith(attrPrefix)) {
						resultList.add(ProposalsFactory.createAttributeValueProposal(currentValue, offset, attrPrefix.length()));
					}
				}
			}
		}
	}

	public void addCloseTagProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection<ICompletionProposal> resultList) {
		if (currentNode.isTextTag()) {
			resultList.add(ProposalsFactory.createEndTagProposal(currentNode.getParent().getTypeDefinition(), offset,
					(currentNode.isIncompleteTag() ? currentNode.getContent().trim().length() : 0)));
		}
		if (currentNode.isIncompleteTag()) {
			resultList.add(ProposalsFactory.createIncompleteEndTagProposal(currentNode.getTypeDefinition(), offset,
					(currentNode.isIncompleteTag() ? currentNode.getContent().trim().length() : 0)));
			resultList.add(ProposalsFactory.createIncompleteClosingEndTagProposal(currentNode.getTypeDefinition(),
					offset, (currentNode.isIncompleteTag() ? currentNode.getContent().trim().length() : 0)));
		}
	}

	public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection<ICompletionProposal> resultList) {
		Collection<TagTypeDefinition> validTags;
		String tagPrefix = "";
		String tagNamespace = "";
		int inc = 0;
		
		if(prefix.startsWith("<")) {
			prefix = prefix.substring(1);
			inc++;
		}
		if (prefix.contains(":")) {
			String[] split = prefix.split(":");
			tagNamespace = split[0];
			tagPrefix = (split.length == 2) ? split[1] : "";
			inc++;
		} else {
			tagPrefix = prefix;
		}
		
		validTags = this.getValidTagsInOffset(currentNode, offset);
		
		for (TagTypeDefinition tagDefinition : validTags) {
			if (tagDefinition != null) {
				String name = tagDefinition.getName();
				String namespace = tagDefinition.getNamespace();
				if (tagNamespace.isEmpty()) {
					if (name.startsWith(tagPrefix) || namespace.startsWith(tagPrefix)) {
						resultList.add(ProposalsFactory.createTagProposal(tagDefinition, offset, tagPrefix.length()
								+ (currentNode.isIncompleteTag() ? 1 : 0)));
					}
				} else {
					if (namespace.startsWith(tagNamespace) && name.startsWith(tagPrefix)) {
						resultList.add(ProposalsFactory.createTagProposal(tagDefinition, offset, tagPrefix.length()
								+ tagNamespace.length() + (currentNode.isIncompleteTag() ? 1 : 0) + inc));
					}
				}
			}
		}
	}

	private Collection<TagTypeDefinition> getValidTagsInOffset(
			XMLNode currentNode, int offset) {
		Collection<TagTypeDefinition> reference;
		if (currentNode.getParent() == null) {
			reference = currentNode.getCorrespondingNode().getTypeDefinition().getInnerTags();
		} else {
			if (currentNode.getOffset() + currentNode.getLength() == offset) {
				reference = currentNode.getTypeDefinition().getInnerTags();
			} else {
				reference = currentNode.getParent().getTypeDefinition().getInnerTags();
			}
		}
		return reference;
	}

}