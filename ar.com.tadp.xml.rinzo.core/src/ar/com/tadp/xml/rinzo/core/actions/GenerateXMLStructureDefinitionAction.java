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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import ar.com.tadp.xml.rinzo.core.wizards.NewXMLStructureDefinitionWizard;

/**
 * Action to generate the structure definition from the XML
 * The output generated could be one of XML Schema, DTD, RELAX NG (XML syntax) or RELAX NG (compact syntax)
 * 
 * @author ccancinos
 */
public class GenerateXMLStructureDefinitionAction extends EditorAndTreeAction {

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			super.setActiveEditor(action, targetEditor);
		}
	}

	@Override
	protected void runEditor(IFile editorInputIFile) {
		List<IFile> files = new ArrayList<IFile>();
		files.add(editorInputIFile);
		this.createDefinition(files);
	}
	
	@Override
	protected void runTree(List<IFile> files) {
		this.createDefinition(files);
	}
	
	private void createDefinition(List<IFile> files) {
		StructuredSelection selection = new StructuredSelection(files.get(0));
		NewXMLStructureDefinitionWizard wizard = new NewXMLStructureDefinitionWizard(files);
		// Use this point to mark initial destination folder based on selected file
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(/*this.viewer.getTextWidget().getShell()*/null, wizard);
		dialog.create();
		dialog.getShell().setText("Create XML Definition");
		dialog.open();
	}

}
