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

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.jdt.wizards.NewParserWizard;

/**
 * 
 * @author ccancinos
 */
public class GenerateParserAction extends SelectionAction {

	public void run(IAction arg0) {
		RinzoXMLEditor editor = (RinzoXMLEditor) this.getTextEditor();
		StructuredSelection selection = new StructuredSelection(editor.getEditorInputIFile());
		Collection<DocumentStructureDeclaration> definitions = editor.getModel().getSchemaDefinitions();
		Map<String, String> fileLocations = DocumentCache.getInstance().getAllLocations(definitions, editor.getFileName());

		NewParserWizard wizard = new NewParserWizard(fileLocations.values());
		wizard.init(PlatformUI.getWorkbench(), selection);
		WizardDialog dialog = new WizardDialog(editor.getSourceViewerEditor().getTextWidget().getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("Generate Parser");
		dialog.open();
	}

}
