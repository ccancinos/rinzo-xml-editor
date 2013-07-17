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
package ar.com.tadp.xml.rinzo.core.contentassist.proposals;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;
import ar.com.tadp.xml.rinzo.core.utils.Utils;

/**
 * Factory of completion proposals for content assist
 * 
 * @author ccancinos
 */
public class ProposalsFactory {
	private static final int COMMENT_RELEVANCE = 1;
	private static final int TAG_RELEVANCE = 110;
	private static final int ATTR_RELEVANCE = 110;
    private static final int ATTR_VALUE_RELEVANCE = 100;
	private static final int END_TAG_RELEVANCE = 100;
	
	private static final String COMMENT_INFO = "&lt;!-- --&gt;";
    private static XMLCompletionProposal commentProposal = null;
    private static final String TERMINATE_TAG_INFO = "Terminate the parent element.";

    /**
     * Returns a proposal for creating a comment
     */
    public static XMLCompletionProposal createCommentProposal(int offset, int replacementLength) {
        if (commentProposal == null) {
            String replacement = "<!--  -->";
            
            commentProposal = new XMLCompletionProposal(replacement, offset - replacementLength, replacementLength, 5, PluginImages
                    .get(PluginImages.IMG_XML_COMMENT), "Comment " + replacement, null, COMMENT_INFO);
            commentProposal.setRelevance(COMMENT_RELEVANCE);
        }
        commentProposal.setReplacementOffset(offset - replacementLength);
        commentProposal.setReplacementLength(replacementLength);
        return commentProposal;
    }

    /**
     * Creates a proposal for adding an end tag
     * @param replacementLength 
     */
	public static XMLCompletionProposal createEndTagProposal(TagTypeDefinition tagTypeDefinition, int offset,
			int replacementLength) {
        String tagName = tagTypeDefinition.getName();
        String namespace = tagTypeDefinition.getNamespace();
        tagName = (Utils.isEmpty(namespace)) ? tagName : namespace + ":" + tagName;
        String replacement = "</" + tagName + ">";
        
        XMLCompletionProposal proposal = new XMLCompletionProposal(
        		replacement, 
        		offset - replacementLength, 
        		replacementLength, 
        		replacement.length(),
                PluginImages.get(PluginImages.IMG_XML_TAGDEF), 
                "End with " + replacement, 
                null, 
                TERMINATE_TAG_INFO);
        proposal.setRelevance(END_TAG_RELEVANCE);
        return proposal;
    }
    
	public static XMLCompletionProposal createIncompleteEndTagProposal(TagTypeDefinition tagTypeDefinition, int offset,
			int replacementLength) {
        String replacement = "/>";
        
        XMLCompletionProposal proposal = new XMLCompletionProposal(
        		replacement, 
        		offset, 
        		0, 
        		replacement.length(),
                PluginImages.get(PluginImages.IMG_XML_TAGDEF), 
                "End with " + replacement, 
                null, 
                TERMINATE_TAG_INFO);
        proposal.setRelevance(END_TAG_RELEVANCE);
        return proposal;
    }

	public static XMLCompletionProposal createIncompleteClosingEndTagProposal(TagTypeDefinition tagTypeDefinition,
			int offset, int replacementLength) {
        String tagName = tagTypeDefinition.getName();
        String namespace = tagTypeDefinition.getNamespace();
        tagName = (Utils.isEmpty(namespace)) ? tagName : namespace + ":" + tagName;
        String replacement = "></" + tagName + ">";
        
        XMLCompletionProposal proposal = new XMLCompletionProposal(
        		replacement, 
        		offset, 
        		0, 
        		1,
                PluginImages.get(PluginImages.IMG_XML_TAGDEF), 
                "End with " + replacement.substring(1), 
                null, 
                TERMINATE_TAG_INFO);
        proposal.setRelevance(END_TAG_RELEVANCE);
        return proposal;
    }

    /**
     * Creates a proposal for adding an entire tag
     */
	public static XMLCompletionProposal createTagProposal(TagTypeDefinition tagTypeDefinition, int offset,
			int replacementLength) {
        String tagName = tagTypeDefinition.getName();
        String namespace = tagTypeDefinition.getNamespace();
        tagName = (Utils.isEmpty(namespace)) ? tagName : namespace + ":" + tagName;
		String replacement = "<" + tagName + "></" + tagName + ">";
        
        XMLCompletionProposal proposal = new XMLCompletionProposal(
        		replacement, 
        		offset - replacementLength,
                replacementLength, 
                tagName.length() + 2, 
                PluginImages.get(PluginImages.IMG_XML_TAGDEF), 
                tagName, 
                null, 
                tagTypeDefinition.getComment());
        proposal.setRelevance(TAG_RELEVANCE);
        return proposal;
    }

    /**
     * Creates a proposal for adding an attribute into a tag
     */
	public static ICompletionProposal createAttributeProposal(AttributeDefinition attributeDefinition, int offset,
			int replacementLength) {
    	String attributeName = attributeDefinition.getName();
        String replacement = attributeName + "=\"" + attributeDefinition.getDefaultValue() + "\"";
        
        XMLCompletionProposal proposal = new XMLCompletionProposal(replacement, offset - replacementLength,
                replacementLength, replacement.length() - 1, PluginImages.get(PluginImages.IMG_XML_ATTRIBUTE),
                attributeName, null, attributeDefinition.getComment());
        proposal.setRelevance(ATTR_RELEVANCE);
        return proposal;
    }
    
    public static ICompletionProposal createAttributeValueProposal(String replacement, int offset) {
	    XMLCompletionProposal proposal = new XMLCompletionProposal(replacement, offset, 0, replacement.length(),
	            PluginImages.get(PluginImages.IMG_XML_ATTRIBUTE), replacement, null, "Set value: <b>" + replacement + "</b>");
	    proposal.setRelevance(ATTR_VALUE_RELEVANCE);
	    return proposal;
	}

}
