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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;

import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDContainer;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDName;

/**
 * An xml tag element as defined by a DTD definition
 * 
 * @author ccancinos
 */
public class DTDTagTypeDefinition implements TagTypeDefinition {
	private final DTDElement tagDeclaration;
	private final DTDComment tagComment;
	private Collection<TagTypeDefinition> innerTags = new ArrayList<TagTypeDefinition>();
	private Collection<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
	private final Map<String, TagTypeDefinition> tagsInDocument;
	private String comment = null;
	private final Map<String, DTDComment> attributeComments;

	public DTDTagTypeDefinition(DTDElement tagDeclaration, DTDComment comment,
			Map<String, TagTypeDefinition> tagsInDocument, Map<String, DTDComment> attributeComments) {
		this.tagDeclaration = tagDeclaration;
		this.tagComment = comment;
		this.tagsInDocument = tagsInDocument;
		this.attributeComments = (Map<String, DTDComment>) ((attributeComments != null) ? attributeComments : Collections.emptyMap());
	}

	public String getName() {
		return this.tagDeclaration.getName();
	}

	public String getNamespace() {
		return "";
	}

	public Collection<AttributeDefinition> getAttributes() {
		if (this.attributes.isEmpty()) {
			for (Iterator iterator = this.tagDeclaration.attributes.values().iterator(); iterator.hasNext();) {
				DTDAttribute attribute = (DTDAttribute) iterator.next();
				this.attributes.add(new DTDAttributeDefinition(attribute, this.attributeComments));
			}
		}
		return this.attributes;
	}

	public AttributeDefinition getAttribute(String attributeName) {
		for (Iterator<AttributeDefinition> iterator = this.getAttributes().iterator(); iterator.hasNext();) {
			AttributeDefinition currentAttribute = iterator.next();
			if (attributeName.startsWith(currentAttribute.getName())) {
				return currentAttribute;
			}
		}
		return null;
	}

	public String getComment() {
		if (this.comment == null) {
			this.comment = "<b>Element:</b> " + this.getName();
			if (this.tagComment != null && !StringUtils.isEmpty(this.tagComment.getText())) {
				this.comment += "<br>" + this.tagComment;
			}
		}
		return this.comment;
	}

	public Collection<TagTypeDefinition> getInnerTags() {
		if (this.innerTags.isEmpty() && (this.tagDeclaration.getContent() instanceof DTDContainer)) {
			this.collectInnerTagsOff((DTDContainer) this.tagDeclaration.getContent(), this.innerTags);
		}
		return this.innerTags;
	}

	private void collectInnerTagsOff(DTDContainer tagsContained, Collection<TagTypeDefinition> result) {
		for (Iterator iterator = tagsContained.getItemsVec().iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			if (next instanceof DTDContainer) {
				this.collectInnerTagsOff((DTDContainer) next, result);
			}
			if (next instanceof DTDName) {
				String tagName = ((DTDName) next).getValue();
				result.add(this.tagsInDocument.get(tagName));
			}
		}
	}

}
