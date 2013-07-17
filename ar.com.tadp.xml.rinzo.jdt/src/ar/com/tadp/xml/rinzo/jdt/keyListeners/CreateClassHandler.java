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
package ar.com.tadp.xml.rinzo.jdt.keyListeners;

import org.eclipse.ui.IEditorActionDelegate;

import ar.com.tadp.xml.rinzo.core.keyListeners.ActionToHandlerAdapter;
import ar.com.tadp.xml.rinzo.jdt.actions.CreateClassAction;

/**
 * Handler to call the creation of the class whose name has been selected
 * 
 * @author ccancinos
 */
public class CreateClassHandler extends ActionToHandlerAdapter {

	protected IEditorActionDelegate createAction() {
		return new CreateClassAction();
	}

}
