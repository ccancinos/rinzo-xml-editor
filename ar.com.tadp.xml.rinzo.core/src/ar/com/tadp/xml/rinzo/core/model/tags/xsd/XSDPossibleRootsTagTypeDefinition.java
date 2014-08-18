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

import java.util.Collection;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;

/**
 * 
 * @author ccancinos
 */
public class XSDPossibleRootsTagTypeDefinition implements TagTypeDefinition {
	private Collection<TagTypeDefinition> possibleRoots;

	public XSDPossibleRootsTagTypeDefinition(Collection<TagTypeDefinition> possibleRoots2) {
		this.possibleRoots = possibleRoots2;
	}

	public String getNamespace() {
		return null;
	}

	public String getName() {
		return null;
	}

	public Collection<TagTypeDefinition> getInnerTags() {
		return this.possibleRoots;
	}

	public String getComment() {
		return null;
	}

	public Collection<AttributeDefinition> getAttributes() {
		return null;
	}

	public AttributeDefinition getAttribute(String attributeName) {
		return null;
	}
}