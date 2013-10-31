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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.PlatformUI;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * @author ccancinos
 *
 */
public abstract class ActionToHandlerAdapter implements IHandler {
	private IEditorActionDelegate action;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorActionDelegate action = this.getAction();
		action.setActiveEditor(null, XMLEditorPlugin.getDefault().getActiveEditor());
		this.setSelection(action);
		action.run(null);
		return null;
	}

	private void setSelection(IEditorActionDelegate theAction) {
		if (theAction instanceof IActionDelegate) {
			IActionDelegate delegate = (IActionDelegate) theAction;
			delegate.selectionChanged(null, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection());
		}
	}

	protected IEditorActionDelegate getAction() {
		if(this.action == null) {
			this.action = this.createAction();
		}
		return this.action;
	}

	protected abstract IEditorActionDelegate createAction();
	
	public void addHandlerListener(IHandlerListener handlerListener) {
	}
	
	public void dispose() {
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
