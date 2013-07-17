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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Basic XML proposal
 * 
 * @author ccancinos
 */
public class XMLCompletionProposal implements IXMLCompletionProposal {
	private String displayString;
	private String replacementString;
	private int replacementOffset;
	private int replacementLength;
	private int cursorPosition;
	private Image image;
	private IContextInformation contextInformation;
	private String additionalProposalInfo;
	private int relevance;

	public XMLCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	public XMLCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo) {
		this.replacementString = replacementString;
		this.replacementOffset = replacementOffset;
		this.replacementLength = replacementLength;
		this.cursorPosition = cursorPosition;
		this.image = image;
		this.displayString = displayString;
		this.contextInformation = contextInformation;
		this.additionalProposalInfo = additionalProposalInfo;
	}

	public void apply(IDocument document) {
		try {
			document.replace(replacementOffset, replacementLength, replacementString);
		} catch (BadLocationException _ex) {
		}
	}

	public String getReplacementString() {
		return replacementString;
	}

	public Point getSelection(IDocument document) {
		return new Point(replacementOffset + cursorPosition, 0);
	}

	public IContextInformation getContextInformation() {
		return contextInformation;
	}

	public Image getImage() {
		return image;
	}

	public String getDisplayString() {
		return displayString != null ? displayString : replacementString;
	}

	public String getAdditionalProposalInfo() {
		return additionalProposalInfo;
	}

	public int getRelevance() {
		return relevance;
	}

	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}

	public void setReplacementOffset(int replacementOffset) {
		this.replacementOffset = replacementOffset;
	}

	public void setReplacementLength(int replacementLength) {
		this.replacementLength = replacementLength;
	}
	
}
