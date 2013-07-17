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
package ar.com.tadp.xml.rinzo.core.model.tags.xsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.w3c.dom.Node;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;

import com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl;

/**
 * @author ccancinos
 */
public class XSDAttributeDefinition implements AttributeDefinition {
	private final XSDAttributeDeclaration attribute;
	private String comment;

	public XSDAttributeDefinition(XSDAttributeDeclaration attributeDeclaration) {
		this.attribute = attributeDeclaration;
	}

	public String getName() {
		return this.attribute.getName();
	}

	public boolean isRequired() {
		return ((XSDAttributeUse) this.attribute.getContainer()).isRequired();
	}

	public Collection<String> getAcceptableValues() {
		Collection<String> result = new ArrayList<String>();
		for (Iterator iterator = this.attribute.getTypeDefinition().getFacetContents().iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			if (next instanceof XSDEnumerationFacet) {
				XSDEnumerationFacet enumerationElement = (XSDEnumerationFacet) next;
				result.add(enumerationElement.getLexicalValue());
			}
		}
		return result;
	}

	public String getComment() {
		if (this.comment == null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<b>Attribute:</b> " + this.getName());
			XSDAnnotation annotation = this.attribute.getAnnotation();

			if (annotation == null) {
				annotation = this.attribute.getTypeDefinition().getAnnotation();
			}
			if (annotation != null) {
				Iterator it = annotation.getUserInformation().iterator();
				while (it.hasNext()) {
					DeferredElementNSImpl element = (DeferredElementNSImpl) it.next();
					Node firstChild = element.getFirstChild();
					if (firstChild != null) {
						buffer.append("<br>" + firstChild.getNodeValue());
					}
				}
			}
			buffer.append("<br><b>Data Type:</b> " + this.attribute.getTypeDefinition().getName());
			this.comment = buffer.toString();
		}
		return this.comment;
	}

	public String getDefaultValue() {
		return this.attribute.getElement().getAttribute("default");
	}

}
