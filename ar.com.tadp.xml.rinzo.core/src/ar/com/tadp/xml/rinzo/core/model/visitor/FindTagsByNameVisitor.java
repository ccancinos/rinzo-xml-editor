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

import java.util.ArrayList;
import java.util.Collection;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Collects the nodes of the xml file with the same name
 * 
 * @author ccancinos
 */
public class FindTagsByNameVisitor implements HierarchicalVisitor {
	private Collection<XMLNode> nodes = new ArrayList<XMLNode>();
	private final String tagName;

	public FindTagsByNameVisitor(String tagName) {
		this.tagName = tagName;
	}

	public boolean visitStart(XMLNode node) {
		if (node.getTagName().equals(this.tagName)) {
			this.nodes.add(node);
		}
		return true;
	}

	public boolean visitEnd(XMLNode node) {
		return true;
	}

	public boolean visitChild(XMLNode node) {
		if (node.getTagName().equals(this.tagName)) {
			this.nodes.add(node);
		}
		return true;
	}

	public Collection<XMLNode> getNodes() {
		return this.nodes;
	}

}
