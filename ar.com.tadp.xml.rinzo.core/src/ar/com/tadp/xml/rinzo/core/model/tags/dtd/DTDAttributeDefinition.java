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
package ar.com.tadp.xml.rinzo.core.model.tags.dtd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;

import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDEnumeration;

/**
 * @author ccancinos
 */
public class DTDAttributeDefinition implements AttributeDefinition {
	private final DTDAttribute attribute;
	private final Map<String, DTDComment> attributeComments;

	public DTDAttributeDefinition(DTDAttribute attribute, Map<String, DTDComment> attributeComments) {
		this.attribute = attribute;
		this.attributeComments = attributeComments;
	}

	public String getName() {
		return this.attribute.getName();
	}

	public Collection<String> getAcceptableValues() {
		if (this.attribute.getType() instanceof DTDEnumeration) {
			DTDEnumeration enumeration = (DTDEnumeration) this.attribute.getType();
			return Arrays.asList(enumeration.getItem());
		}
		return Collections.emptyList();
	}

	public String getComment() {
		DTDComment comment = this.attributeComments.get(this.attribute.getName());
		return "<b>Attribute:</b> " + this.attribute.getName() + "<br>"
				+ ((comment != null) ? comment.toString() : "");
	}

	public String getDefaultValue() {
		return "";
	}

}
