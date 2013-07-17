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

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceImpl;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.OnlyNameTypeTagDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.XMLTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Retrieves the relationship between tags and attributes in a tag defined in an
 * XSD.
 * 
 * @author ccancinos
 */
public class XSDTagDefinitionProvider implements XMLTagDefinitionProvider {
	private String schemaPath;
	private String fileName;
	private Map<String, TagTypeDefinition> tags = new HashMap<String, TagTypeDefinition>();
	private Collection<TagTypeDefinition> possibleRoots = new ArrayList<TagTypeDefinition>();
	private DocumentStructureDeclaration documentStructureDeclaration;
	private long lastModified;

	public XSDTagDefinitionProvider(String fileName, DocumentStructureDeclaration structureDeclaration) {
		try {
			this.fileName = fileName;
			this.documentStructureDeclaration = structureDeclaration;
			this.schemaPath = FileUtils.resolveURI(fileName, structureDeclaration.getSystemId()).toString();
			this.updateDefinition();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public TagTypeDefinition getTagDefinition(XMLNode node) {
		if (node == null) {
			return new TagTypeDefinition() {
				public String getNamespace() {
					return null;
				}

				public String getName() {
					return null;
				}

				public Collection<TagTypeDefinition> getInnerTags() {
					return possibleRoots;
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
			};
		}
		String tagName = node.getTagName();
		TagTypeDefinition tagTypeDefinition = this.tags.get(tagName);
		return tagTypeDefinition != null ? tagTypeDefinition : new OnlyNameTypeTagDefinition(tagName);
	}

	/**
	 * Se encarga de mapear las definiciones que se encuentran en el schema
	 */
	private void updateDefinition() {
		this.parseElementsFrom(this.getSchema(this.documentStructureDeclaration.getPublicId(), this.schemaPath));
	}

	// TODO THIS THREE METHODS ARE REPEATED IN XSDTAG!!!!
	private void parseElementsFrom(XSDSchema schema) {
		for (Iterator it = schema.getElementDeclarations().iterator(); it.hasNext();) {
			XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) it.next();
			if (!elementDeclaration.isAbstract()) {
				this.possibleRoots.add(new XSDTagTypeDefinition(elementDeclaration, this.documentStructureDeclaration
						.getNamespace(), this.tags));
			}
			this.handleLeaf(elementDeclaration);
			this.handleElementDeclaration(elementDeclaration);
		}
	}

	private void handleElementDeclaration(XSDElementDeclaration tagDeclaration) {
		XSDTypeDefinition type = tagDeclaration.getTypeDefinition();
		if (type instanceof XSDComplexTypeDefinition) {
			XSDComplexTypeDefinition xsdComplexTypeDefinition = (XSDComplexTypeDefinition) type;
			int contentType = xsdComplexTypeDefinition.getContentTypeCategory().getValue();
			if (contentType == XSDContentTypeCategory.ELEMENT_ONLY || contentType == XSDContentTypeCategory.MIXED) {
				XSDParticle xsdParticle = (XSDParticle) xsdComplexTypeDefinition.getContentType();
				if (xsdParticle != null) {
					XSDTerm xsdTerm = xsdParticle.getTerm();
					if (xsdTerm instanceof XSDModelGroup) {
						XSDModelGroup xsdModelGroup = (XSDModelGroup) xsdTerm;
						this.handleContainer(xsdModelGroup);
					} else if (xsdTerm instanceof XSDElementDeclaration) {
						XSDElementDeclaration eldeclaration = (XSDElementDeclaration) xsdTerm;
						this.handleLeaf(eldeclaration);
					}
				}
			}
		}
	}

	private void handleLeaf(XSDElementDeclaration tagDeclaration) {
		this.tags.put(tagDeclaration.getName(), new XSDTagTypeDefinition(tagDeclaration,
				this.documentStructureDeclaration.getNamespace(), this.tags));
	}

	private void handleContainer(XSDModelGroup xsdModelGroup) {
		for (Iterator i = xsdModelGroup.getParticles().iterator(); i.hasNext();) {
			XSDParticle childXSDParticle = (XSDParticle) i.next();
			XSDTerm childXSDTerm = childXSDParticle.getTerm();

			if (childXSDTerm instanceof XSDElementDeclaration) {
				XSDElementDeclaration eldeclaration = (XSDElementDeclaration) childXSDTerm;
				// TODO I don't want this if here. But it is here because
				// otherwise a stack overflow is thrown
				if (!this.tags.containsKey(eldeclaration.getName())) {
					this.handleLeaf(eldeclaration);
					this.handleElementDeclaration(eldeclaration);
				}
			} else if (childXSDTerm instanceof XSDModelGroup) {
				this.handleContainer((XSDModelGroup) childXSDTerm);
			}
		}
	}

	public void setFileName(String fileName) {
		if (!fileName.equals(this.fileName)) {
			this.updateDefinition();
		}
		this.fileName = fileName;
	}

	public void setSchema(java.net.URI schema) {
		if (!schema.toString().equals(this.schemaPath) || this.isDefinitionUpdated(schema.toString())) {
			this.updateDefinition();
			this.schemaPath = schema.toString();
			this.setLastDefinitionUpdate(schema.toString());
		}
	}

	protected boolean isDefinitionUpdated(String definitionPath) {
		try {
			return this.lastModified < new File(definitionPath).lastModified();
		} catch (Exception e) {
			return false;
		}
	}

	protected void setLastDefinitionUpdate(String definitionPath) {
		try {
			this.lastModified = new File(definitionPath).lastModified();
		} catch (Exception e) {
		}
	}

	private XSDSchema getSchema(String publicName, String schemaURIString) {
		XSDSchema xsdSchema = null;
		try {
			String schemaLocation = DocumentCache.getInstance().getLocation(publicName, schemaURIString);
			schemaLocation = FileUtils.addProtocol(schemaLocation);
			ResourceSet resourceSet = new ResourceSetImpl();

			XSDResourceFactoryImpl resourceFactoryImpl = new XSDResourceFactoryImpl();
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", resourceFactoryImpl);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(null, resourceFactoryImpl);

			resourceSet.getAdapterFactories().add(new XSDSchemaLocatorAdapterFactory());

			URI uri = createURI(schemaURIString);

			InputStream inputStream = resourceSet.getURIConverter().createInputStream(URI.createURI(schemaLocation));
			XSDResourceImpl resource = (XSDResourceImpl) resourceSet.createResource(URI.createURI("*.xsd"));
			resource.setURI(uri);
			resource.load(inputStream, null);
			xsdSchema = resource.getSchema();
		} catch (Exception e) {
			XMLEditorPlugin.logErrorMessage("Error retrieving Schema for publicName: " + publicName + " schemaURIString: " + schemaURIString, e);
		}

		return xsdSchema;
	}

	public static URI createURI(String uriString) {
		return hasProtocol(uriString) ? URI.createURI(uriString) : URI.createFileURI(uriString);
	}

	private static boolean hasProtocol(String uri) {
		boolean result = false;
		if (uri != null) {
			int index = uri.indexOf(":");
			// assume protocol with be length 3 so that the'C' in 'C:/' is not
			// interpreted as a protocol
			if (index != -1 && index > 2) {
				result = true;
			}
		}
		return result;
	}

}
