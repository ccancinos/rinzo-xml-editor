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
package ar.com.tadp.xml.rinzo.core.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.visitor.StringGeneratorVisitor;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * An abstract class containing the template behavior for all actions who wants
 * to modify the content of the current edited document by using a
 * {@link StringGeneratorVisitor}.
 * 
 * @author ccancinos
 */
public abstract class VisitorAction implements IEditorActionDelegate {
	private RinzoXMLEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = (RinzoXMLEditor) targetEditor;
	}

	public void run(IAction action) {
		try {
			IDocument document = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput());
			RinzoXMLEditor basicXMLEditor = (RinzoXMLEditor) this.editor;
			basicXMLEditor.getModel().createTree(document);
			XMLNode rootNode = this.editor.getModel().getTree().getRootNode();
			int initialOffset = rootNode.getOffset();
			int length = rootNode.getCorrespondingNode().getOffset() + rootNode.getCorrespondingNode().getLength();

			this.getVisitor().reset();
			rootNode.accept(this.getVisitor());

			document.replace(initialOffset, length - initialOffset, this.getVisitor().getString());
			basicXMLEditor.getModel().createTree(document);
		} catch (BadLocationException exception) {
			throw new RuntimeException(exception);
		}
	}

	protected abstract StringGeneratorVisitor getVisitor();

	public void selectionChanged(IAction action, ISelection selection) {
	}

	protected XMLNode getSelectedNode() {
		int offset = ((TextSelection) this.editor.getSelectionProvider().getSelection()).getOffset();
		XMLNode node = XMLTreeModelUtilities.getActiveNode(
				this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput()), offset);
		return node;
	}
}
