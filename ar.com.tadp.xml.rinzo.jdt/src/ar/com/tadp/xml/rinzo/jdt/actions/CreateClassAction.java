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

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.ResourceUtil;

import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;
import ar.com.tadp.xml.rinzo.jdt.eclipse.copies.PixelConverter;

/**
 * Action to create a class from the class name selected in the editor.
 * 
 * @author ccancinos
 */
public class CreateClassAction extends ClassNameSelectedAction {
	public static final String ID = "CreateClassAction.";

	protected boolean getEnableValue(IType type) {
		return type == null && ResourceUtil.getFile(this.getTextEditor().getEditorInput()) != null;
	}

	public void run(IAction action) {
		IWorkbench workbench = RinzoJDTPlugin.getDefault().getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		NewClassCreationWizard wizard = new NewClassCreationWizard();
		String className = this.getSelection().substring(this.getSelection().lastIndexOf(".") + 1);
		String packageName = this.getSelection().substring(0, this.getSelection().lastIndexOf("."));

		wizard.init(workbench, new StructuredSelection(className));
		WizardDialog dialog = new WizardDialog(shell, wizard);
		PixelConverter converter = new PixelConverter(shell);

		dialog.setMinimumPageSize(
				converter.convertWidthInCharsToPixels(70), 
				converter.convertHeightInCharsToPixels(20));
		dialog.create();

		NewClassWizardPage page = (NewClassWizardPage) wizard.getPage("NewClassWizardPage");
		page.setTypeName(className, true);
		page.setAddComments(true, true);

		IPackageFragmentRoot sourceFolder = this.getSourceFolder();
		page.setPackageFragmentRoot(sourceFolder, true);
		page.setPackageFragment(sourceFolder.getPackageFragment(packageName), true);
		
		dialog.open();
	}

	private IPackageFragmentRoot getSourceFolder() {
		try {
			IPackageFragmentRoot[] roots = this.getActiveJavaProject().getPackageFragmentRoots();
			//If the xml file is inside a source folder I look for it
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getPath().isPrefixOf(this.getAsociatedResource().getFullPath())) {
					return roots[i];
				}
			}

			//If it's not I look for the first source folder available
			for (int j = 0; j < roots.length; j++) {
				if (roots[j] instanceof PackageFragmentRoot) {
					return roots[j];
				}
			}
			
			//Otherwise I return the project root folder... :\
			return this.getActiveJavaProject().getPackageFragmentRoot(this.getActiveJavaProject().getResource());
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}
}
