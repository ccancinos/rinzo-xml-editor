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

import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Interface used to respond to content assist generation for each section of
 * the XML document in a domain specific interface.
 * 
 * @author ccancinos
 */
public interface IXMLContentAssistProcessor {

	public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset, Collection<ICompletionProposal> results);

	public void addCloseTagProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection<ICompletionProposal> results);

	public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix,
			ITextViewer viewer, int offset, Collection<ICompletionProposal> results);

	public void addAttributeProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection<ICompletionProposal> results);

}