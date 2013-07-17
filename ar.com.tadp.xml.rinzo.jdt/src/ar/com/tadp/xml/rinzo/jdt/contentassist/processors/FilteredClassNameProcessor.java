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
package ar.com.tadp.xml.rinzo.jdt.contentassist.processors;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jface.text.ITextViewer;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.Utils;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.preferences.ClassAttribute;
import ar.com.tadp.xml.rinzo.jdt.preferences.ClassElement;
import ar.com.tadp.xml.rinzo.jdt.preferences.Extending;

/**
 * Generates content assist class names proposals for Attributes and Tags according to
 * configured ones.
 * 
 * @author ccancinos
 */
@SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
public class FilteredClassNameProcessor extends ClassNameProcessor {

	private Extending extending;

	@Override
	public void addAttributeValuesProposals(XMLNode currentNode, String attributeName, String prefix,
			ITextViewer viewer, int offset, Collection resultList) {
		List<ClassAttribute> list = ClassAttribute.loadFromPreference(false);
		for (ClassAttribute classAttribute : list) {
			if (classAttribute.equals(currentNode.getTagName(), attributeName)) {
				this.extending = classAttribute;
				resultList.addAll(this.getProposals(prefix, offset));
				break;
			}
		}
	}

	@Override
	public void addBodyProposals(XMLNode currentNode, String prefix, ITextViewer viewer, int offset,
			Collection resultList) {
		List<ClassElement> list = ClassElement.loadFromPreference(false);
		XMLNode node = (currentNode.isTextTag()) ? currentNode.getParent() : currentNode;
		for (ClassElement classElement : list) {
			if (classElement.getDisplayName().equals(node.getTagName())) {
				this.extending = classElement;
				resultList.addAll(this.getProposals(prefix, offset));
				break;
			}
		}
	}

	@Override
	protected void addProposal(AbstractJavaCompletionProposal proposal, List result) {
		if (proposal instanceof LazyJavaTypeCompletionProposal) {
			LazyJavaTypeCompletionProposal classProposal = (LazyJavaTypeCompletionProposal) proposal;
			if (this.isAccepted(classProposal, extending)) {
				result.add(proposal);
			}
		} else {
			result.add(proposal);
		}
	}

	private boolean isAccepted(LazyJavaTypeCompletionProposal proposal, Extending extending) {
		return proposal.getQualifiedTypeName().equals(extending.getExtending()) || extending.getExtending().equals("*")
				|| Utils.isEmpty(extending.getExtending())
				|| JDTUtils.isSuperType(proposal.getQualifiedTypeName(), extending.getExtending());
	}

}
