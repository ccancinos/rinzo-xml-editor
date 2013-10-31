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
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TypedPosition;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.partitioner.IXMLPartitions;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLDocumentPartitioner;

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

	public XMLNode getActiveXMLNode(int offset) {
		XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(this.getDocument());
		if (xmlPartitioner != null) {
			TypedPosition position = xmlPartitioner.findClosestPosition(offset++);
			while (position != null) {
				if (position instanceof XMLNode && !position.getType().equals(IXMLPartitions.XML_TEXT)) {
					XMLNode node = (XMLNode) position;
					return node;
				}
				position = xmlPartitioner.findClosestPosition(offset++);
			}
		}
		return null;
	}

	public XMLNode getParentNode(int offset) {
		XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(this.getDocument());
		if (xmlPartitioner != null) {
			TypedPosition position = xmlPartitioner.findPreviousNonWhiteSpacePosition(offset);
			if (position instanceof XMLNode) {
				XMLNode node = (XMLNode) position;
				return node;
			}
		}
		return null;
	}

	public XMLNode getPreviousNode(int offset) {
		XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(this.getDocument());
		if (xmlPartitioner != null) {
			TypedPosition position = xmlPartitioner.findPreviousPosition(offset);
			if (position instanceof XMLNode) {
				XMLNode node = (XMLNode) position;
				return node;
			}
		}
		return null;
	}

	public XMLNode getActiveNode(int offset) {
		XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(this.getDocument());
		if (xmlPartitioner != null) {
			TypedPosition position = xmlPartitioner.findClosestPosition(offset);
			if (position instanceof XMLNode) {
				XMLNode node = (XMLNode) position;
				return node;
			}
		}
		return null;
	}

	private XMLDocumentPartitioner getDocumentPartitioner(IDocument document) {
		IDocumentPartitioner partitioner = document.getDocumentPartitioner();
		if (partitioner instanceof XMLDocumentPartitioner) {
			return (XMLDocumentPartitioner) partitioner;
		}
		return null;
	}

	public XMLNode getActiveNode() {
		int offset = ((TextSelection) this.getEditor().getSelectionProvider().getSelection()).getOffset();
		return this.getActiveNode(offset);
	}

}
