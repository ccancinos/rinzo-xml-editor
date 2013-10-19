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
package ar.com.tadp.xml.rinzo.core.model.tags.nodef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;

/**
 * An xml tag element as defined by usages in current xml document
 * 
 * @author ccancinos
 */
public class NoDefTagTypeDefinition implements TagTypeDefinition {
	private String tagName;
	private Map<String, Collection<String>> tagsNamesInDocument;
	private Map<String, Collection<String>> tagsAttributeNamesInDocument;

	public NoDefTagTypeDefinition() {
	}

	public NoDefTagTypeDefinition(String tagName, Map<String, Collection<String>> tagsNamesInDocument,
			Map<String, Collection<String>> tagsAttributeNamesInDocument) {
		this.tagName = tagName;
		this.tagsNamesInDocument = tagsNamesInDocument;
		this.tagsAttributeNamesInDocument = tagsAttributeNamesInDocument;
	}

	public Collection<AttributeDefinition> getAttributes() {
		Collection<AttributeDefinition> attributes = new HashSet<AttributeDefinition>();
		Collection<String> attributeNames = this.tagsAttributeNamesInDocument.get(this.getName());
		if (attributeNames != null) {
			for (String currentAttributeName : attributeNames) {
				attributes.add(new NoDefAttributeDefinition(currentAttributeName));
			}
		}
		return attributes;
	}

	public AttributeDefinition getAttribute(String attributeName) {
		for (AttributeDefinition currentAttribute : this.getAttributes()) {
			if (attributeName.startsWith(currentAttribute.getName())) {
				return currentAttribute;
			}
		}
		return null;
	}

	public String getComment() {
		return "<b>Element:</b> " + this.getName();
	}

	public Collection<TagTypeDefinition> getInnerTags() {
		Collection<TagTypeDefinition> innerTags = new HashSet<TagTypeDefinition>();
		Collection<String> tagsForThis = this.tagsNamesInDocument.get(this.tagName);
		if (tagsForThis != null) {
			for (String currentTagName : tagsForThis) {
				TagTypeDefinition definition = new NoDefTagTypeDefinition(currentTagName, this.tagsNamesInDocument,
						this.tagsAttributeNamesInDocument);
				innerTags.add(definition);
			}
		}
		return innerTags;
	}

	public String getName() {
		return this.tagName;
	}

	public String getNamespace() {
		return "";
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setTagsNamesInDocument(Map<String, Collection<String>> tagsNamesInDocument) {
		this.tagsNamesInDocument = tagsNamesInDocument;
	}

	public void setTagsAttributeNamesInDocument(Map<String, Collection<String>> tagsAttributeNamesInDocument) {
		this.tagsAttributeNamesInDocument = tagsAttributeNamesInDocument;
	}

}
