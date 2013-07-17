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
package ar.com.tadp.xml.rinzo.jdt.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IEditorStatusLine;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.Utils;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;

/**
 * Superclass to all actions executed over an editor's text selection
 * 
 * @author ccancinos
 */
public abstract class SelectionAction implements IEditorActionDelegate {
    private String selection;
    /** Cache del proyecto activo */
    private IJavaProject activeJavaProject;
    /** Cache del recurso asociado */
    private IResource asociatedResource = null;
    private String notEnableMessageID = null;
	private IEditorPart editor;
	private boolean isNewEditor = false;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
		this.isNewEditor = true;
		this.asociatedResource = null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof ITextSelection) {
			this.setSelection(((ITextSelection) selection).getText());
		}
    }

	public TextEditor getTextEditor() {
		return (TextEditor) editor;
	}

    protected void setSelection(String classNameCandidate) {
    	this.selection = classNameCandidate;
    }
    
    protected String getSelection() {
        if(!Utils.isEmpty(this.selection)) {
            return this.selection;
        }
        
        XMLNode selectedNode = this.getSelectedNode();
        if(selectedNode != null) {
        	int offset = ((TextSelection)this.getTextEditor().getSelectionProvider().getSelection()).getOffset();
			return selectedNode.getStringAt(offset);
        } else {
        	return null;
        }
    }
    
    protected XMLNode getSelectedNode() {
        int offset = ((TextSelection)this.getTextEditor().getSelectionProvider().getSelection()).getOffset();
        XMLNode node = XMLTreeModelUtilities.getActiveNode(this.getTextEditor().getDocumentProvider().getDocument(this.getTextEditor().getEditorInput()), offset);
        return node;
    }

    /**
     * Se encarga de devolver siempre el proyecto actual
     */
    protected IJavaProject getActiveJavaProject() {
    	if (activeJavaProject == null) {
   			activeJavaProject = JDTUtils.getActiveJavaProject();
    	}
    	return activeJavaProject;
    }

    /**
     * Devuelve el recurso asociado al editor al que pertenece esta acci�n
     */
    protected IResource getAsociatedResource() {
    	if (this.asociatedResource == null && this.isNewEditor && this.getTextEditor() != null) {
    		IEditorInput input = this.getTextEditor().getEditorInput();
    		this.isNewEditor = false;
    		if (input != null) {
    			Object adapter = input.getAdapter(IResource.class);
    			if (adapter != null && (adapter instanceof IResource)) {
    				this.asociatedResource = (IResource) adapter;
    			}
    		}
    	}
    	return this.asociatedResource;
    }

    public void displayNotEnableMessage() {
        if (this.notEnableMessageID != null) {
//            String message = XMLEditorPlugin.getResourceString(this.notEnableMessageID);
//            this.setStatusLineMessage(true, message, null);
        }
    }

    protected void setStatusLineMessage(boolean error, String message, Image image) {
        IEditorStatusLine statusLine = (IEditorStatusLine) this.getTextEditor().getAdapter(
                IEditorStatusLine.class);
            if (statusLine != null) {
                statusLine.setMessage(error, message, image);
            }
            this.getTextEditor().getSite().getShell().getDisplay().beep();
    }

    public void setNotEnableMessageID(String notEnableMessageID) {
		this.notEnableMessageID = notEnableMessageID;
	}
}
