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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import ar.com.tadp.xml.rinzo.core.contentassist.proposals.XMLCompletionProposalComparator;

/**
 * Delegates method invocations to all the elements of delegated {@link IContentAssistProcessor}
 * 
 * @author ccancinos
 */
public class CompositeContentAssistProcessor implements IContentAssistProcessor {
	private Collection<IContentAssistProcessor> processors = new ArrayList<IContentAssistProcessor>();

	public void addProcessor(IContentAssistProcessor processor) {
		this.processors.add(processor);
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		Collection<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		ICompletionProposal result[] = null;
		
		for (IContentAssistProcessor processor : this.processors) {
			resultList.addAll(Arrays.asList(processor.computeCompletionProposals(viewer, offset)));
		}
		
        result = resultList.toArray(new ICompletionProposal[resultList.size()]);
        Arrays.sort(result, XMLCompletionProposalComparator.getInstance());
        return result;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[]{'<'};
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

}
