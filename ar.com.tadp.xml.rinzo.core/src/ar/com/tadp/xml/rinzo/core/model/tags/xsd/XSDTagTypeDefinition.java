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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.w3c.dom.Node;

import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;

import com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl;

/**
 * An xml tag element as defined by a XSD Schema definition
 * 
 * @author ccancinos
 */
public class XSDTagTypeDefinition implements TagTypeDefinition {
	private XSDElementDeclaration tagDeclaration;
	private Collection<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
	private Collection<TagTypeDefinition> innerTags = new ArrayList<TagTypeDefinition>();
	private String comment = null;
	private final Map<String, TagTypeDefinition> tagsInDocument;
	private final String namespaceId;
	private String namespaceURI;

	public XSDTagTypeDefinition(XSDElementDeclaration tagDeclaration, String namespaceId, Map<String, TagTypeDefinition> tagsInDocument) {
		this.tagDeclaration = tagDeclaration;
		this.namespaceId = namespaceId;
		this.tagsInDocument = tagsInDocument;
		this.namespaceURI = tagDeclaration.getTargetNamespace();
	}
	
	public String getName() {
		return this.tagDeclaration.getName();
	}

	public String getNamespace() {
		return this.namespaceId;
	}

	public String getNamespaceURI() {
		return this.namespaceURI;
	}

	public Collection<AttributeDefinition> getAttributes() {
		if(this.attributes.isEmpty()) {
			XSDTypeDefinition typeDefinition = this.tagDeclaration.getTypeDefinition();
			if(typeDefinition instanceof XSDComplexTypeDefinition) {
				Iterator it = ((XSDComplexTypeDefinition)typeDefinition).getAttributeUses().iterator();
				while (it.hasNext()) {
					XSDAttributeDeclaration attributeDeclaration = ((XSDAttributeUse) it.next()).getAttributeDeclaration();
					this.attributes.add(new XSDAttributeDefinition(attributeDeclaration));
				}
			}
		}
		return this.attributes;
	}
	
	public AttributeDefinition getAttribute(String attributeName) {
		for (Iterator<AttributeDefinition> iterator = this.getAttributes().iterator(); iterator.hasNext();) {
			AttributeDefinition currentAttribute = iterator.next();
			if(attributeName.startsWith(currentAttribute.getName())) {
				return currentAttribute;
			}
		}
		return null;
	}
	
	public String getComment() {
		if(this.comment == null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<b>Element:</b> ");
			buffer.append(StringUtils.isEmpty(this.getNamespace()) ? this.getName() : this.getNamespace() + ":" + this.getName());
			buffer.append("<br>");
	        String typeName = this.tagDeclaration.getTypeDefinition().getName();
	        
	        XSDAnnotation annotation = this.tagDeclaration.getAnnotation();
	        annotation = (annotation != null) ? annotation : this.tagDeclaration.getTypeDefinition().getAnnotation();
	        
	        if(annotation != null) {
	        	for (Iterator iterator = annotation.getUserInformation().iterator(); iterator.hasNext();) {
					DeferredElementNSImpl element = (DeferredElementNSImpl) iterator.next();
					Node firstChild = element.getFirstChild();
		            if(firstChild != null) {
		            	buffer.append(firstChild.getNodeValue());
		            }
				}
	        }
	        if(typeName != null) {
	        	buffer.append("<br><b>Data Type:</b> " + typeName);
	        }
	        this.comment = buffer.toString();
		}
        return this.comment;
	}
	
	public Collection<TagTypeDefinition> getInnerTags() {
		if(this.innerTags.isEmpty()) {
            this.collectInnerTags();
		}
		return this.innerTags;
	}

	//TODO  THIS THREE METHODS ARE REPEATED IN SCHEMATAGCONTAINERSREGISTRY!!!!
	private void collectInnerTags() {
        XSDTypeDefinition type = tagDeclaration.getTypeDefinition();
        if (type instanceof XSDComplexTypeDefinition) {
            XSDComplexTypeDefinition xsdComplexTypeDefinition = (XSDComplexTypeDefinition) type;
            int contentType = xsdComplexTypeDefinition.getContentTypeCategory().getValue();
			if (contentType == XSDContentTypeCategory.ELEMENT_ONLY || contentType == XSDContentTypeCategory.MIXED) {
                XSDParticle xsdParticle = (XSDParticle) xsdComplexTypeDefinition.getContentType();
                XSDTerm xsdTerm = xsdParticle.getTerm();
                if (xsdTerm instanceof XSDModelGroup) {
                    this.handleContainer((XSDModelGroup) xsdTerm);
                }
                else {
                	if (xsdTerm instanceof XSDElementDeclaration) {
                		this.handleLeaf((XSDElementDeclaration) xsdTerm);
                	}
                }
            }
        }
	}
	
	private void handleLeaf(XSDElementDeclaration elementDeclaration) {
		this.innerTags.add(this.tagsInDocument.get(this.getFullDeclarationName(elementDeclaration)));
	}

    private void handleContainer(XSDModelGroup xsdModelGroup) {
        for (Iterator i = xsdModelGroup.getParticles().iterator(); i.hasNext();) {
            XSDParticle childXSDParticle = (XSDParticle) i.next();
            XSDTerm childXSDTerm = childXSDParticle.getTerm();

            if (childXSDTerm instanceof XSDModelGroup) {
            	this.handleContainer((XSDModelGroup) childXSDTerm);
            } else {
	            if (childXSDTerm instanceof XSDElementDeclaration) {
	                this.handleLeaf((XSDElementDeclaration) childXSDTerm);
	            }
            }
        }
    }

	private String getFullDeclarationName(XSDElementDeclaration eldeclaration) {
		if (this.namespaceId.isEmpty()) {
			return eldeclaration.getName();
		} else {
			return this.namespaceId + ":" + eldeclaration.getName();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String name = getName();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((namespaceId == null) ? 0 : namespaceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XSDTagTypeDefinition other = (XSDTagTypeDefinition) obj;
		String name = getName();
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		if (namespaceId == null) {
			if (other.namespaceId != null)
				return false;
		} else if (!namespaceId.equals(other.namespaceId))
			return false;
		return true;
	}

}
