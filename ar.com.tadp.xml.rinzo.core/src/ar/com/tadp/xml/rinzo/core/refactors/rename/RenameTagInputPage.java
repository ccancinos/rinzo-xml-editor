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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * The input page for the Rename Property refactoring, where users can control
 * the effects of the refactoring; to be shown in the wizard.
 * 
 * We let the user enter the new name for the property, and we let her decide
 * whether other property files in the bundle should be affected, and whether
 * the operation is supposed to span the entire workspace or only the current
 * project.
 * 
 * @author ccancinos
 */
public class RenameTagInputPage extends UserInputWizardPage {
	private static final String DS_KEY = RenameTagInputPage.class.getName();
	private static final String DS_CURRENT_TAG = "CURRENT_TAG";
	private static final String DS_ALL_PARENT = "ALL_PARENT";
	private static final String DS_ALL_PROJECTS = "ALL_FILE";

	private final RenameTagInfo info;

	private IDialogSettings dialogSettings;
	private Text newNameTxt;
	private Button updateCurrentButton;
	private Button updateAllInParentButton;
	private Button updateAllInFileButton;

	public RenameTagInputPage(final RenameTagInfo info) {
		super(RenameTagInputPage.class.getName());
		this.info = info;
		initDialogSettings();
	}

	// interface methods of UserInputWizardPage
	// /////////////////////////////////////////

	public void createControl(final Composite parent) {
		Composite composite = createRootComposite(parent);
		setControl(composite);

		createNewNameLabel(composite);
		createNewNameTxt(composite);
		createUpdateCurrent(composite);
		createUpdateAllInParent(composite);
		createUpdateAllInFile(composite);

		validate();
	}

	// UI creation methods
	// ////////////////////

	private Composite createRootComposite(final Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		result.setLayout(gridLayout);
		initializeDialogUnits(result);
		Dialog.applyDialogFont(result);
		return result;
	}

	private void createNewNameLabel(final Composite composite) {
		Label lblNewName = new Label(composite, SWT.NONE);
		lblNewName.setText("&New Name");
	}

	private void createNewNameTxt(Composite composite) {
		newNameTxt = new Text(composite, SWT.BORDER);
		newNameTxt.setText(info.getOldName());
		newNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newNameTxt.selectAll();
		newNameTxt.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent e) {
				info.setNewName(newNameTxt.getText());
				validate();
			}
		});
		this.newNameTxt.setFocus();
	}
	
	private void createUpdateCurrent(final Composite composite) {
		updateCurrentButton = createCheckbox(composite, "Update &current tag");
		updateCurrentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = updateCurrentButton.getSelection();
				dialogSettings.put(DS_CURRENT_TAG, selected);
				info.setCurrentTag(selected);
			}
		});
		initUpdateCurrentOption();
	}

	private void createUpdateAllInParent(final Composite composite) {
		updateAllInParentButton = createCheckbox(composite, "Update &all in parent tag");
		updateAllInParentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = updateAllInParentButton.getSelection();
				dialogSettings.put(DS_ALL_PARENT, selected);
				info.setAllInParent(selected);
			}
		});
		initUpdateAllInParentOption();
	}

	private void createUpdateAllInFile(final Composite composite) {
		updateAllInFileButton = createCheckbox(composite, "Update all in &file");
		updateAllInFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = updateAllInFileButton.getSelection();
				dialogSettings.put(DS_ALL_PROJECTS, selected);
				info.setAllInFile(selected);
				// for demonstration purposes, we enforce the preview for
				// refactorings
				// that span the entire workspace
				getRefactoringWizard().setForcePreviewReview(selected);
			}
		});
		initAllInFileOption();
	}

	private Button createCheckbox(final Composite composite, final String text) {
		Button result = new Button(composite, SWT.RADIO);
		result.setText(text);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		result.setLayoutData(gridData);

		return result;
	}

	// helping methods
	// ////////////////

	private void initDialogSettings() {
		IDialogSettings ds = XMLEditorPlugin.getDefault().getDialogSettings();
		dialogSettings = ds.getSection(DS_KEY);
		if (dialogSettings == null) {
			dialogSettings = ds.addNewSection(DS_KEY);
			dialogSettings.put(DS_CURRENT_TAG, true);
			dialogSettings.put(DS_ALL_PARENT, false);
			dialogSettings.put(DS_ALL_PROJECTS, false);
		}
	}

	private void validate() {
		String txt = newNameTxt.getText();
		setPageComplete(txt.length() > 0 && !txt.equals(info.getOldName()));
	}

	private void initUpdateCurrentOption() {
		boolean updateRefs = dialogSettings.getBoolean(DS_CURRENT_TAG);
		updateCurrentButton.setSelection(updateRefs);
		info.setCurrentTag(updateRefs);
	}

	private void initUpdateAllInParentOption() {
		boolean updateRefs = dialogSettings.getBoolean(DS_ALL_PARENT);
		updateAllInParentButton.setSelection(updateRefs);
		info.setAllInParent(updateRefs);
	}

	private void initAllInFileOption() {
		boolean allProjects = dialogSettings.getBoolean(DS_ALL_PROJECTS);
		updateAllInFileButton.setSelection(allProjects);
		info.setAllInFile(allProjects);
	}

}
