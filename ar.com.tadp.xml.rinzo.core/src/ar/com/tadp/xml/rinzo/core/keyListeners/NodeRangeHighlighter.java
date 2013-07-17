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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * In charge of follow caret position and highlight on the left ruler the current node area.
 * 
 * @author ccancinos
 */
public class NodeRangeHighlighter implements ISelectionChangedListener {
	private RinzoXMLEditor editor;
	private int line;

	public NodeRangeHighlighter(RinzoXMLEditor editor) {
		this.editor = editor;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		ITextSelection selection = (ITextSelection) event.getSelection();
		int currentLine = selection.getStartLine();
		
		if (currentLine != this.line) {
			this.line = currentLine;
			IDocument document = this.editor.getSourceViewerEditor().getDocument();
			XMLNode activeNode = XMLTreeModelUtilities.getActiveNode(document, selection.getOffset());
			
			if (activeNode != null) {
				if (activeNode.isTextTag()) {
					activeNode = XMLTreeModelUtilities.getParentNode(document, selection.getOffset());
					if(activeNode == null) {
						return;
					}
				}
				if (activeNode.isEndTag()) {
					activeNode = activeNode.getCorrespondingNode();
					if(activeNode == null) {
						return;
					}
				}

				XMLNode correspondingNode = activeNode.getCorrespondingNode();
				int length = correspondingNode != null ? 
						correspondingNode.getOffset() + correspondingNode.getLength() - activeNode.getOffset() : 
						activeNode.getLength();
				this.editor.setHighlightRange(activeNode.getOffset(), length, false);
			}
		}
	}
}