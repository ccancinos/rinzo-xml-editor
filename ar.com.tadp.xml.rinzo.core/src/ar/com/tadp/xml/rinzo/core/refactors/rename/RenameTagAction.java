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
package ar.com.tadp.xml.rinzo.core.refactors.rename;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.refactors.BasePageWizard;
import ar.com.tadp.xml.rinzo.core.refactors.BaseRefactoringProcessor;

/**
 * 
 * @author ccancinos
 */
public class RenameTagAction implements IEditorActionDelegate {
	private static final String EXT_XML = "xml";
	private ISelection selection;
	private RinzoXMLEditor targetEditor;
	private boolean onXmlFile;
	private RenameTagInfo info = new RenameTagInfo();

	public void run(IAction action) {
		if (!onXmlFile) {
			refuse();
		} else {
			if (selection != null && selection instanceof ITextSelection) {
				applySelection((ITextSelection) selection);
				openWizard();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = (RinzoXMLEditor) targetEditor;
		onXmlFile = false;
		IFile file = getFile();
		if (file != null && file.getFileExtension().equals(EXT_XML)) {
			onXmlFile = true;
		}
	}

	private void openWizard() {
		RenameTagNameDelegate delegate = new RenameTagNameDelegate(info, this.targetEditor);
		RenameTagInputPage page = new RenameTagInputPage(info);
		RefactoringWizardOpenOperation op = this.getRefactoringWizardOperation(delegate, page);
		try {
			String titleForFailedChecks = "";
			op.run(getShell(), titleForFailedChecks);
			this.targetEditor.getEditorInputIFile().refreshLocal(0, null);
		} catch (Exception irex) {
			// operation was cancelled
		}
	}

	private RefactoringWizardOpenOperation getRefactoringWizardOperation(RenameTagNameDelegate delegate, RenameTagInputPage page) {
		RefactoringProcessor processor = new BaseRefactoringProcessor(info, delegate);
		ProcessorBasedRefactoring ref = new ProcessorBasedRefactoring(processor);
		BasePageWizard wizard = new BasePageWizard(ref, page);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		return op;
	}

	private final IFile getFile() {
		IFile result = null;
		if (targetEditor instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) targetEditor;
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				result = ((IFileEditorInput) input).getFile();
			}
		}
		return result;
	}

	private void applySelection(final ITextSelection textSelection) {
		info.setOldName(textSelection.getText());
		info.setNewName(textSelection.getText());
		info.setOffset(textSelection.getOffset());
		info.setSourceFile(getFile());
	}

	private void refuse() {
		String title = "Refuse Refactor";
		String message = "Cannot apply this refactor in this file";
		MessageDialog.openInformation(getShell(), title, message);
	}

	private Shell getShell() {
		Shell result = null;
		if (targetEditor != null) {
			result = targetEditor.getSite().getShell();
		} else {
			result = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
		}
		return result;
	}

}
