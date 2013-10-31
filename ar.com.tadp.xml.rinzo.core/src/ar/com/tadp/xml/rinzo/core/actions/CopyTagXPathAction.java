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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;

/**
 * Copies to the clipboard the XPath associated to the tag under the cursor. At
 * the moment it does not have the sufficient granularity to know if the cursor
 * is on an attribute. If the cursor is on the body of a tag, it copies the
 * XPath to the tag containing that body.
 * 
 * @author ccancinos
 */
public class CopyTagXPathAction implements IEditorActionDelegate {
	private RinzoXMLEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = (RinzoXMLEditor) targetEditor;
	}

	public void run(IAction action) {
		XMLNode node = this.editor.getModel().getTree().getActiveNode();
		String selection = node.getXPath();

		IWorkbench workbench = XMLEditorPlugin.getDefault().getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		Clipboard clipboard = new Clipboard(shell.getDisplay());

		try {
			clipboard.setContents(new String[] { selection }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw new RuntimeException(e);
			}
		} finally {
			clipboard.dispose();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
