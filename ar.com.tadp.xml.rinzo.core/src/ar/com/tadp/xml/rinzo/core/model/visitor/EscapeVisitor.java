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
package ar.com.tadp.xml.rinzo.core.model.visitor;

import org.apache.commons.lang.StringEscapeUtils;

import ar.com.tadp.xml.rinzo.core.model.XMLAttribute;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * This visitor creates a {@link StringBuffer} containing a copy of the content
 * of the nodes visited but with their attributes and bodies escaped
 * 
 * @author ccancinos
 */
public class EscapeVisitor implements StringGeneratorVisitor {
	private StringBuffer buffer = new StringBuffer();

	public String getString() {
		return this.buffer.toString();
	}

	public void reset() {
		this.buffer = new StringBuffer();
	}

	/**
	 * The behavior of this visit is:
	 * 
	 * XML_TEXT: Escape text content 
	 * XML_COMMENT: Do nothing 
	 * XML_EMPTY_TAG: Escape attribute values
	 */
	public boolean visitChild(XMLNode node) {
		if (node.isEmptyTag()) {
			this.buffer.append(this.escapeAttributes(node));
		} else if (node.isTextTag()) {
			this.buffer.append(StringEscapeUtils.escapeHtml(node.getContent()));
		} else {
			this.buffer.append(node.getContent());
		}
		return true;
	}

	/**
	 * The behavior of this visit is:
	 * 
	 * XML_TAG: Escape attribute values
	 */
	public boolean visitEnd(XMLNode node) {
		if (node.isTag()) {
			this.buffer.append(node.getCorrespondingNode().getContent());
		}
		return true;
	}

	/**
	 * The behavior of this visit is:
	 * 
	 * XML_TAG: Escape attribute values
	 */
	public boolean visitStart(XMLNode node) {
		if (node.isTag()) {
			this.buffer.append(this.escapeAttributes(node));
		} else {
			this.buffer.append(node.getContent());
		}
		return true;
	}

	private String escapeAttributes(XMLNode node) {
		String content = node.getContent();
		for (XMLAttribute attribute : node.getAttributes().values()) {
			content = content.replaceAll(attribute.getValue(), StringEscapeUtils.escapeHtml(attribute.getValue()));
		}
		
		return content;
	}

}
