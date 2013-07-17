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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.tags.AttributeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.OnlyNameTypeTagDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.TagTypeDefinition;
import ar.com.tadp.xml.rinzo.core.model.tags.XMLTagDefinitionProvider;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;
import ar.com.tadp.xml.rinzo.core.utils.Utils;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttlist;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDOutput;
import com.wutka.dtd.DTDParser;

/**
 * @author ccancinos
 * 
 */
public class DTDTagDefinitionProvider implements XMLTagDefinitionProvider {
	private DocumentStructureDeclaration documentStructureDeclaration;
	private String fileName;
	private Map<String, TagTypeDefinition> tags = new HashMap<String, TagTypeDefinition>();
	private Map<String, Map<String, DTDComment>> attributes = new HashMap<String, Map<String, DTDComment>>();
	private String rootNodeName;
	private String dtdPath;
	private long lastModified;

	public DTDTagDefinitionProvider(String fileName, String rootNodeName,
			DocumentStructureDeclaration structureDeclaration) {
		try {
			this.fileName = fileName;
			this.rootNodeName = rootNodeName;
			this.documentStructureDeclaration = structureDeclaration;
			this.dtdPath = FileUtils.resolveURI(this.fileName, this.documentStructureDeclaration.getSystemId())
					.toString();
			this.updateDefinition();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void setFileName(String fileName) {
		if (!fileName.equals(this.fileName)) {
			this.updateDefinition();
		}
		this.fileName = fileName;
	}

	public void setDocumentDefinition(DocumentStructureDeclaration structureDeclaration) {
		try {
			String definition = FileUtils.resolveURI(this.fileName, this.documentStructureDeclaration.getSystemId()).toString();
			if (!structureDeclaration.equals(this.documentStructureDeclaration) || this.isDefinitionUpdated(definition)) {
				this.updateDefinition();
				this.documentStructureDeclaration = structureDeclaration;
				this.dtdPath = definition;
				this.setLastDefinitionUpdate(definition);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
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

	public TagTypeDefinition getTagDefinition(XMLNode node) {
		String tagName = node.getTagName();
		if (Utils.isEmpty(tagName)) {
			return new TagTypeDefinition() {
				public AttributeDefinition getAttribute(String attributeName) {
					return null;
				}

				public Collection<AttributeDefinition> getAttributes() {
					return null;
				}

				public String getComment() {
					return null;
				}

				public Collection<TagTypeDefinition> getInnerTags() {
					Collection<TagTypeDefinition> c = new ArrayList<TagTypeDefinition>();
					c.add(tags.get(rootNodeName));
					return c;
				}

				public String getName() {
					return null;
				}

				public String getNamespace() {
					return "";
				}
			};
		}
		TagTypeDefinition tagDefinition = this.tags.get(tagName);
		return tagDefinition != null ? tagDefinition : new OnlyNameTypeTagDefinition(tagName);
	}

	/**
	 * Se encarga de mapear las definiciones que se encuentran en el schema
	 */
	private void updateDefinition() {
		String dtdLocation = DocumentCache.getInstance().getLocation(this.documentStructureDeclaration.getPublicId(),
				this.dtdPath);
		this.parseElementsFrom(this.getDTD(dtdLocation));
	}

	private void parseElementsFrom(DTD dtd) {
		DTDComment comment = null;
		for (Iterator<Object> iter = Arrays.asList(dtd.getItems()).iterator(); iter.hasNext();) {
			DTDOutput output = (DTDOutput) iter.next();
			if (output instanceof DTDComment) {
				comment = (DTDComment) output;
			}
			if (output instanceof DTDElement) {
				this.addTagDefinition((DTDElement) output, comment);
				comment = null;
			}
			if (output instanceof DTDAttlist) {
				this.addAttrDefinition((DTDAttlist) output, comment);
				comment = null;
			}
		}
	}

	private DTD getDTD(String dtdLocation) {
		try {
			InputStream inputStream = new FileInputStream(dtdLocation);
			Reader reader = new InputStreamReader(inputStream);
			DTDParser parser = new DTDParser(reader);
			return parser.parse();

		} catch (Exception e) {
			throw new RuntimeException("Trying to read the DTD specification in \"" + dtdLocation + "\"", e);
		}
	}

	private void addTagDefinition(DTDElement element, DTDComment comment) {
		this.tags.put(element.getName(),
				new DTDTagTypeDefinition(element, comment, this.tags, this.attributes.get(element.getName())));
	}

	private void addAttrDefinition(DTDAttlist output, DTDComment comment) {
		if (!this.attributes.containsKey(output.getName())) {
			this.attributes.put(output.getName(), new HashMap<String, DTDComment>());
		}
		Map<String, DTDComment> attr = this.attributes.get(output.getName());
		for (DTDAttribute element : output.getAttribute()) {
			attr.put(element.getName(), comment);
		}
	}

}
