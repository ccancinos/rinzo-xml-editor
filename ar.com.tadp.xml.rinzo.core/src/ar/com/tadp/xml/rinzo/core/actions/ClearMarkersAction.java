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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.EditorPluginAction;
import org.eclipse.ui.internal.PluginAction;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;

/**
 * Deletes all the problem markers (warning and errors) in the current editor
 * 
 * @author ccancinos
 */
public class ClearMarkersAction implements IEditorActionDelegate {
	private RinzoXMLEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = (RinzoXMLEditor) targetEditor;
	}

	public void run(IAction action) {
		if (action instanceof EditorPluginAction) {
			this.deleteMarkers(this.editor.getEditorInputIFile());
		}
		if (action instanceof PluginAction) {
			PluginAction objectAction = (PluginAction) action;
			if (objectAction.getSelection() instanceof TreeSelection) {
				TreeSelection selection = (TreeSelection) objectAction.getSelection();
				for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
					IFile file = (IFile) iterator.next();
					this.deleteMarkers(file);
				}
			}
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
