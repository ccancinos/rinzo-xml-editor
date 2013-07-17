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

import java.util.ArrayList;
import java.util.Collection;
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
	private Collection<TagTypeDefinition> innerTags = new ArrayList<TagTypeDefinition>();
	private Collection<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
	private final Map<String, Collection<String>> tagsNamesInDocument;
	private final Map<String, Collection<String>> tagsAttributeNamesInDocument;
	private Map<String, TagTypeDefinition> tagsInDocument;

	public NoDefTagTypeDefinition(String tagName, Map<String, TagTypeDefinition> tagsInDocument,
			Map<String, Collection<String>> tagsNamesInDocument, Map<String, Collection<String>> tagsAttributeNamesInDocument) {
		this.tagName = tagName;
		this.tagsInDocument = tagsInDocument;
		this.tagsNamesInDocument = tagsNamesInDocument;
		this.tagsAttributeNamesInDocument = tagsAttributeNamesInDocument;
	}

	public Collection<AttributeDefinition> getAttributes() {
		if(this.attributes.isEmpty()) {
			Collection<String> attributeNames = this.tagsAttributeNamesInDocument.get(this.getName());
			//TODO SEE when attributes are being added because there is a bad smell here
			if (attributeNames != null) {
				for (String currentAttributeName : attributeNames) {
					this.attributes.add(new NoDefAttributeDefinition(currentAttributeName));
				}
			}
		}
		return this.attributes;
	}
	
	public AttributeDefinition getAttribute(String attributeName) {
		for (AttributeDefinition currentAttribute : this.getAttributes()) {
			if(attributeName.startsWith(currentAttribute.getName())) {
				return currentAttribute;
			}
		}
		return null;
	}

	public String getComment() {
		return "<b>Element:</b> " + this.getName();
	}

	public Collection<TagTypeDefinition> getInnerTags() {
		if(this.innerTags.isEmpty()) {
			Collection<String> tagsForThis = this.tagsNamesInDocument.get(this.tagName);
			if(tagsForThis != null) {
				for (String currentTagName : tagsForThis) {
					TagTypeDefinition definition = this.tagsInDocument.get(currentTagName);
					if(definition == null) {
						definition = new NoDefTagTypeDefinition(currentTagName, this.tagsInDocument,
								this.tagsNamesInDocument, this.tagsAttributeNamesInDocument);
					}
					this.innerTags.add(definition);
				}
			}
		}
		return this.innerTags;
	}

	public String getName() {
		return this.tagName;
	}

	public String getNamespace() {
		return "";
	}

}
