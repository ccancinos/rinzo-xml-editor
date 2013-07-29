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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.OnlyNameTypeTagDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.XMLTagDefinitionProvider;

/**
 * @author ccancino
 * 
 */
public class CompositeXMLTagDefinitionProvider implements XMLTagDefinitionProvider {
	private Collection<XMLTagDefinitionProvider> tagDefinitionProviders = new ArrayList<XMLTagDefinitionProvider>();

	public TagTypeDefinition getTagDefinition(XMLNode node) {
		TagTypeDefinition tagDefinition = null;
		if (node.isRoot()) {
			return this.getAllInRoot(node);
		}
		for (XMLTagDefinitionProvider definition : this.tagDefinitionProviders) {
			tagDefinition = definition.getTagDefinition(node);
			if (tagDefinition != null && !(tagDefinition instanceof OnlyNameTypeTagDefinition)) {
				return tagDefinition;
			}
		}
		return tagDefinition;
	}

	private TagTypeDefinition getAllInRoot(XMLNode node) {
		// TODO This implementation is not correct.
		// because it should delegate on the tag's associated provider
		// in order to use the correct behavior.Like displaying tag's
		// documentation
		CollectionTagTypeDefinition definition = new CollectionTagTypeDefinition();
		for (XMLTagDefinitionProvider provider : this.tagDefinitionProviders) {
			definition.addInnerTags(provider.getTagDefinition(node).getInnerTags());
		}
		return definition;
	}

	public void addTagDefinitionProvider(XMLTagDefinitionProvider provider) {
		this.tagDefinitionProviders.add(provider);
	}
	
	public void setDefinition(String fileName, Collection<URI> uris) {
		Iterator<URI> it = uris.iterator();
		for (Iterator<XMLTagDefinitionProvider> iterator = this.tagDefinitionProviders.iterator(); iterator.hasNext() && it.hasNext();) {
			try {
				XSDTagDefinitionProvider provider = (XSDTagDefinitionProvider) iterator.next();
				provider.setDefinition(fileName, it.next());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @deprecated use setDefinition
	 */
	public void setSchemas(Collection<URI> uris) {
		Iterator<URI> it = uris.iterator();
		for (Iterator<XMLTagDefinitionProvider> iterator = this.tagDefinitionProviders.iterator(); iterator.hasNext() && it.hasNext();) {
			try {
				XSDTagDefinitionProvider provider = (XSDTagDefinitionProvider) iterator.next();
				provider.setSchema(it.next());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @deprecated use setDefinition
	 */
	public void setFileName(String fileName) {
		for (Iterator<XMLTagDefinitionProvider> iterator = this.tagDefinitionProviders.iterator(); iterator.hasNext();) {
			XSDTagDefinitionProvider provider = (XSDTagDefinitionProvider) iterator.next();
			provider.setFileName(fileName);
		}
	}

	private static class CollectionTagTypeDefinition implements TagTypeDefinition {
		private Collection<TagTypeDefinition> innerTags = new ArrayList<TagTypeDefinition>();

		public AttributeDefinition getAttribute(String attributeName) {
			return null;
		}

		public Collection<AttributeDefinition> getAttributes() {
			return Collections.emptyList();
		}

		public String getComment() {
			return null;
		}

		public void addInnerTags(Collection<TagTypeDefinition> tags) {
			this.innerTags.addAll(tags);
		}

		public Collection<TagTypeDefinition> getInnerTags() {
			return this.innerTags;
		}

		public String getName() {
			return null;
		}

		public String getNamespace() {
			return null;
		}
	}

}
