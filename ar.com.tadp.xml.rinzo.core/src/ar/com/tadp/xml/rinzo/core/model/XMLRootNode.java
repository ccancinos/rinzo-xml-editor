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
package ar.com.tadp.xml.rinzo.core.model;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;

/**
 * DOCME
 * 
 * @author ccancinos
 */
public class XMLRootNode extends XMLNode {
	private XMLNode root = null;

	public XMLRootNode(int offset, int length, String type, IDocument document, RinzoXMLEditor editor) {
		super(offset, length, type, document);
		this.setEditor(editor);
	}

	public XMLRootNode(ITypedRegion region) {
		super(region);
	}

	public XMLNode getRootNode() {
		if (this.root == null) {
			for (Iterator<XMLNode> iter = this.getChildren().iterator(); iter.hasNext() && this.root == null;) {
				XMLNode node = iter.next();
				if (node.isTag()) {
					this.root = node;
				}
			}
		}
		if (this.root != null) {
			this.root.documentChanged = true;
		}
		return this.root;
	}

}
