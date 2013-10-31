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
package ar.com.tadp.xml.rinzo.core.keyListeners;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Controls the document's navigation by moving between sibling nodes and to the
 * parent when last/first sibling is reached.
 * 
 * @author ccancinos
 */
public class NavigateTagsHandler extends KeyAdapter {
	private final ISourceViewer sourceViewer;
	private final RinzoXMLEditor editor;

	public NavigateTagsHandler(RinzoXMLEditor editor) {
		this.editor = editor;
		this.sourceViewer = editor.getSourceViewerEditor();
	}

	public void keyReleased(KeyEvent keyevent) {
		if (keyevent.stateMask == (SWT.CONTROL | SWT.SHIFT)) {
			XMLNode currentNode = this.editor.getModel().getTree()
					.getActiveNode(this.sourceViewer.getSelectedRange().x);
			XMLNode nextNode = null;
			if (keyevent.keyCode == SWT.ARROW_UP) {
				nextNode = this.getUpNode(currentNode);
			}
			if (keyevent.keyCode == SWT.ARROW_DOWN) {
				nextNode = this.getDownNode(currentNode);
			}
			if (nextNode != null) {
				keyevent.doit = false;
				this.editor.selectAndReveal(nextNode.offset + 1, 0);
			}
		}
	}

	private XMLNode getDownNode(XMLNode currentNode) {
		if (currentNode.isTextTag()) {
			return this.editor.getModel().getTree().getActiveXMLNode(this.sourceViewer.getSelectedRange().x);
		}
		XMLNode downNode = (currentNode.isEndTag()) ? currentNode.getCorrespondingNode() : currentNode;
		XMLNode[] children = downNode.getParent().getChildren(false);
		XMLNode previous = downNode;
		for (int i = children.length - 1; i >= 0; i--) {
			if (children[i].equals(downNode)) {
				if (i != (children.length - 1)) {
					return previous;
				} else {
					return downNode.getParent().isRoot() ? downNode : downNode.getParent();
				}
			}
			previous = children[i];
		}
		return downNode;
	}

	private XMLNode getUpNode(XMLNode currentNode) {
		if (currentNode.isTextTag()) {
			currentNode = this.editor.getModel().getTree().getActiveXMLNode(this.sourceViewer.getSelectedRange().x);
		}
		XMLNode upNode = (currentNode.isEndTag()) ? currentNode.getCorrespondingNode() : currentNode;
		XMLNode[] children = upNode.getParent().getChildren(false);
		XMLNode previous = upNode;
		for (int i = 0; i < children.length; i++) {
			if (children[i].equals(upNode)) {
				if (i != 0) {
					return previous;
				} else {
					return upNode.getParent().isRoot() ? upNode : upNode.getParent();
				}
			}
			previous = children[i];
		}
		return upNode;
	}

}
