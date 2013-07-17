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

import java.io.InputStream;

import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocator;

import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * 
 * @author ccancinos
 */
public class XSDSchemaLocatorImpl extends AdapterImpl implements XSDSchemaLocator {

	/**
	 * @see org.eclipse.xsd.util.XSDSchemaLocator#locateSchema(org.eclipse.xsd.XSDSchema,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI, String rawSchemaLocationURI,
			String resolvedSchemaLocationURI) {
		XSDSchema result = null;
		String baseLocation = xsdSchema.getSchemaLocation();
		String resolvedURI = FileUtils.addProtocol(DocumentCache.getInstance().getLocation(namespaceURI,
				resolvedSchemaLocationURI));
		if (resolvedURI == null) {
			resolvedURI = resolvedSchemaLocationURI;
		}
		try {
			ResourceSet resourceSet = xsdSchema.eResource().getResourceSet();
			URI uri = URI.createURI(resolvedURI);
			Resource r = resourceSet.getResource(uri, false);
			XSDResourceImpl resolvedResource = null;
			if (r instanceof XSDResourceImpl) {
				resolvedResource = (XSDResourceImpl) r;
			} else {
				String physicalLocation = FileUtils.addProtocol(DocumentCache.getInstance().getLocation(namespaceURI,
						baseLocation));
				InputStream inputStream = resourceSet.getURIConverter().createInputStream(
						URI.createURI(physicalLocation));
				resolvedResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI("*.xsd"));
				resolvedResource.setURI(uri);
				resolvedResource.load(inputStream, null);
			}

			result = resolvedResource.getSchema();
		} catch (Exception exception) {
			// It is generally not an error to fail to resolve.
			// If a resource is actually created,
			// which happens only when we can create an input stream,
			// then it's an error if it's not a good schema
		}
		return result;
	}

	public boolean isAdatperForType(Object type) {
		return type == XSDSchemaLocator.class;
	}
}