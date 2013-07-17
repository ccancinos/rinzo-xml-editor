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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;

import ar.com.tadp.xml.rinzo.core.ThreadExecutorService;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;

/**
 * Abstract class for actions over a candidate class name selected in the
 * editor.
 * 
 * @author ccancinos
 */
public abstract class ClassNameSelectedAction extends SelectionAction {

	public boolean isEnabled() {
		IStatus status = JavaConventions.validateJavaTypeName(this.getSelection());
		if (status.getCode() == IStatus.OK || status.getSeverity() == IStatus.WARNING) {
			IType type = JDTUtils.findType(this.getSelection());
			return this.getEnableValue(type);
		}
		return false;
	}

	/**
	 * Es la condici�n para determinar si se habilita o no la acci�n
	 */
	protected abstract boolean getEnableValue(IType type);

	public void initialize() {
		ThreadExecutorService.getInstance().execute(new Runnable() {
			public void run() {
				JDTUtils.findType("java.lang.Object");
			}
		});
	}

}
