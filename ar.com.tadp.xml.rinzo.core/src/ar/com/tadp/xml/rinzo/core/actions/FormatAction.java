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

import ar.com.tadp.xml.rinzo.core.model.visitor.StringGeneratorVisitor;
import ar.com.tadp.xml.rinzo.core.model.visitor.ToStringVisitor;

/**
 * TODO Hacer que esta acci�n sea una @link
 * ar.com.tadp.xml.rinzo.core.actions.SelectionAction para que se formatee solo
 * lo que se selecciona o todo el documento si no hay nada seleccionado
 * 
 * @author ccancinos
 */
public class FormatAction extends VisitorAction {
	private ToStringVisitor visitor = new ToStringVisitor();

	protected StringGeneratorVisitor getVisitor() {
		return this.visitor;
	}
}
