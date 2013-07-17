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
package ar.com.tadp.xml.rinzo.jdt.resources.validation;

import java.util.Iterator;
import java.util.List;

import ar.com.tadp.xml.rinzo.core.model.XMLAttribute;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.preferences.ClassAttribute;
import ar.com.tadp.xml.rinzo.jdt.preferences.ClassElement;

/**
 * Validates for class names only the values of tags and attributes configured
 * in preference page.
 * 
 * @author ccancinos
 */
public class FilteredClassNamesValidatorVisitor extends ClassNamesValidatorVisitor {

	@Override
	public boolean visitStart(XMLNode node) {
		if(node.isTextTag()) {
			String supertype = this.getTagBodySupertype(node.getParent());
			if(supertype != null) {
				this.validateBoddy(node, supertype);
			}
		} else {
			this.validateAttributes(node);
		}
		return true;
	}

	@Override
	public boolean visitEnd(XMLNode node) {
		return true;
	}

	@Override
	public boolean visitChild(XMLNode node) {
		if(node.isTextTag()) {
			String supertype = this.getTagBodySupertype(node.getParent());
			if(supertype != null) {
				this.validateBoddy(node, supertype);
			}
		} else {
			this.validateAttributes(node);
		}
		return true;
	}
	
	private void validateAttributes(XMLNode node) {
		node.hasChanged();
		for (Iterator iterator = node.getAttributes().values().iterator(); iterator.hasNext();) {
			XMLAttribute attribute = (XMLAttribute) iterator.next();
			String attributeSupertype = this.getAttributeSupertype(node, attribute);
			if (attributeSupertype != null) {
				validateAttrClassName(attribute, node, attributeSupertype);
			}
		}
	}

	private void validateBoddy(XMLNode node, String supertype) {
		if(supertype.equals("*") || JDTUtils.isSuperType(node.getContent(), supertype)) {
			XMLAttribute dummy = new XMLAttribute(node.getContent(), node.offset, node.length);
			this.validateClassName(dummy, node);
		} else {
			XMLAttribute dummy = new XMLAttribute(node.getContent(), node.offset, node.length);
			this.addError(dummy, node, dummy.getValue() + " does not extends " + supertype);
		}
	}

	private void validateAttrClassName(XMLAttribute attribute, XMLNode node, String attributeSupertype) {
		if(attributeSupertype.equals("*") || JDTUtils.isSuperType(attribute.getValue(), attributeSupertype)) {
			validateClassName(attribute, node);
		} else {
			this.addError(attribute, node, attribute.getValue() + " does not extends " + attributeSupertype);
		}
	}

	private String getAttributeSupertype(XMLNode node, XMLAttribute attribute) {
		List<ClassAttribute> list = this.getClassAttributesRestrictions();
		for (ClassAttribute classAttribute : list) {
			if (classAttribute.equals(node.getTagName(), attribute.getName())) {
				return classAttribute.getExtending();
			}
		}
		return null;
	}

	private String getTagBodySupertype(XMLNode parent) {
		List<ClassElement> list = this.getTagsRestrictions();
		for (ClassElement classElement : list) {
			if(classElement.getDisplayName().equals(parent.getTagName())) {
				return classElement.getExtending();
			}
		}
		return null;
	}

	private List<ClassElement> getTagsRestrictions() {
		return ClassElement.loadFromPreference(false);
	}

	private List<ClassAttribute> getClassAttributesRestrictions() {
		return ClassAttribute.loadFromPreference(false);
	}

}
