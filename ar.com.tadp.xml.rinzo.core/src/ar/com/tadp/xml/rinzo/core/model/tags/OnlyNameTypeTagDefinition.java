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
package ar.com.tadp.xml.rinzo.core.model.tags;

import java.util.Collection;
import java.util.Collections;

/**
 * Null Object xml tag definition which only knows the name of a tag
 * 
 * @author ccancinos
 */
public class OnlyNameTypeTagDefinition implements TagTypeDefinition {
	private String tagName;

	public OnlyNameTypeTagDefinition(String tagName) {
		this.tagName = tagName;
	}

	public AttributeDefinition getAttribute(final String attributeName) {
		return new AttributeDefinition() {

			public Collection<String> getAcceptableValues() {
				return Collections.emptyList();
			}

			public String getName() {
				return attributeName;
			}

			public String getComment() {
				return "<b>Attribute:</b> " + attributeName;
			}

			public String getDefaultValue() {
				return "";
			}

		};
	}

	public Collection<AttributeDefinition> getAttributes() {
		return Collections.emptyList();
	}

	public String getComment() {
		return this.tagName;
	}

	public Collection<TagTypeDefinition> getInnerTags() {
		return Collections.emptyList();
	}

	public String getName() {
		return this.tagName;
	}

	public String getNamespace() {
		return "";
	}

	public String getDefaultValue() {
		return "";
	}

}
