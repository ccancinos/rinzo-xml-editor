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

import java.lang.reflect.Method;
import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Used to order proposal types in the listing displayed to the user.
 * 
 * @author ccancinos
 */
public class XMLCompletionProposalComparator implements Comparator<ICompletionProposal> {

	private static XMLCompletionProposalComparator instance = new XMLCompletionProposalComparator();
	private boolean orderAlphabetically;

	public static XMLCompletionProposalComparator getInstance() {
		return instance;
	}

	private XMLCompletionProposalComparator() {
		orderAlphabetically = false;
	}

	public int compare(ICompletionProposal proposal1, ICompletionProposal proposal2) {
		if (!orderAlphabetically) {
			int r1 = getRelevance(proposal1);
			int r2 = getRelevance(proposal2);
			int relevanceDif = r2 - r1;
			if (relevanceDif != 0)
				return relevanceDif;
		}
		return proposal1.getDisplayString().compareToIgnoreCase(proposal2.getDisplayString());
	}

	private int getRelevance(ICompletionProposal obj) {
		if (obj instanceof IXMLCompletionProposal) {
			IXMLCompletionProposal xcp = (IXMLCompletionProposal) obj;
			return xcp.getRelevance();
		}
		// Maybe I should force to implement IXMLCompletionProposal, but as for
		// now...
		try {
			Method method = obj.getClass().getMethod("getRelevance", new Class[] {});
			Number relevance = (Number) method.invoke(obj, new Object[] {});
			return relevance.intValue();
		} catch (Exception e) {
		}
		return 0;
	}

	public void setOrderAlphabetically(boolean orderAlphabetically) {
		this.orderAlphabetically = orderAlphabetically;
	}

}
