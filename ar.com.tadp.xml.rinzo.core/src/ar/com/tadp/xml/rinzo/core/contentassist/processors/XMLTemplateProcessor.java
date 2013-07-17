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
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

import ar.com.tadp.xml.rinzo.XmlEditorUI;
import ar.com.tadp.xml.rinzo.core.PluginImages;
import ar.com.tadp.xml.rinzo.core.contentassist.proposals.ProposalsFactory;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.template.XMLContextType;
import ar.com.tadp.xml.rinzo.core.utils.Utils;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * Retrieves a list of Templates proposals.
 * 
 * @author ccancinos
 */
public class XMLTemplateProcessor extends TemplateCompletionProcessor {
    private static final int TEMPLATE_RELEVANCE = 80;
	private final char[] autoActivationCharacters = new char[]{'<'};

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
				char ch= document.getChar(i - 1);
				if (ch != '<' && !Character.isJavaIdentifierPart(ch)) {
					break;
				}
				i--;
			}
			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return "";
		}
	}

	/**
	 * Cut out angular brackets for relevance sorting, since the template name
	 * does not contain the brackets.
	 * 
	 * @param template the template
	 * @param prefix the prefix
	 * @return the relevance of the <code>template</code> for the given <code>prefix</code>
	 */
	protected int getRelevance(Template template, String prefix) {
		if (prefix.startsWith("<")) {
			prefix= prefix.substring(1);
		}
		if (template.getName().startsWith(prefix)) {
			return TEMPLATE_RELEVANCE;
		}
		return 0;
	}

	/**
	 * Simply return all templates.
	 * 
	 * @param contextTypeId the context type, ignored in this implementation
	 * @return all templates
	 */
	protected Template[] getTemplates(String contextTypeId) {
		return XmlEditorUI.getDefault().getTemplateStore().getTemplates();
	}

	/**
	 * Return the XML context type that is supported by this plug-in.
	 * 
	 * @param viewer the viewer, ignored in this implementation
	 * @param region the region, ignored in this implementation
	 * @return the supported XML context type
	 */
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return XmlEditorUI.getDefault().getContextTypeRegistry().getContextType(XMLContextType.XML_CONTEXT_TYPE);
	}

	/**
	 * Always return the default image.
	 * 
	 * @param template the template, ignored in this implementation
	 * @return the defaul template image
	 */
	protected Image getImage(Template template) {
		return PluginImages.get(PluginImages.IMG_XML_TEMPLATE);
	}
    
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        ICompletionProposal[] templateProposals = this.getTemplates(viewer, offset);
        
        XMLNode currentNode = XMLTreeModelUtilities.getActiveNode(viewer.getDocument(), offset);
    	int currentPosition = offset - currentNode.getOffset();
    	String prefix = "";
    	String content = currentNode.getContent().trim();
    	if (!Utils.isEmpty(content)) {
			int lastSpacePosition = currentNode.getContent().substring(0, currentPosition).lastIndexOf(" ") + 1;
			prefix = currentNode.getContent().substring(lastSpacePosition, currentPosition).trim();
		}
		if (Utils.isEmpty(prefix)) {
            ICompletionProposal[] allProposals = new ICompletionProposal[templateProposals.length + 1];
            System.arraycopy(templateProposals, 0, allProposals, 0, templateProposals.length);
			System.arraycopy(
					new ICompletionProposal[] { ProposalsFactory.createCommentProposal(offset,
							this.extractPrefix(viewer, offset).length()) }, 0, allProposals, templateProposals.length,
					1);
			return allProposals;
		}
        
        return templateProposals;
    }

    /**
     * Return all template proposals that match the prefix written
     * 
     * This implementation is a copy of @see TemplateCompletionProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int) 
     * altered to return only the proposals matching the prefix. The default implementation returns all.
     */
    private ICompletionProposal[] getTemplates(ITextViewer viewer, int offset) {
        ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset) {
			offset= selection.getOffset() + selection.getLength();
		}

        String prefix= extractPrefix(viewer, offset);
        Region region= new Region(offset - prefix.length(), prefix.length());
        TemplateContext context= createContext(viewer, region);
        if (context == null) {
			return new ICompletionProposal[0];
		}

        context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

        Template[] templates= getTemplates(context.getContextType().getId());

        List matches= new ArrayList();
        for (int i= 0; i < templates.length; i++) {
            Template template= templates[i];
            try {
                context.getContextType().validate(template.getPattern());
            } catch (TemplateException e) {
                continue;
            }
            if (template.getName().startsWith(prefix) && template.matches(prefix, context.getContextType().getId())) {
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
			}
        }

        return (ICompletionProposal[]) matches.toArray(new ICompletionProposal[matches.size()]);
    }
    
    public char[] getCompletionProposalAutoActivationCharacters() {
    	return autoActivationCharacters;
    }
    
}
