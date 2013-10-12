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
package ar.com.tadp.xml.rinzo.jdt.wizards;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonStatusDialogField;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.wizards.NewContainerWizardPage;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * Abstract wizard page for pages requiring to locate a package and proyect's
 * source folder
 * 
 * @author ccancinos
 */
public abstract class NewPackageContainerWizardPage extends NewContainerWizardPage {
	private JavaPackageCompletionProcessor packageCompletionProcessor;
	private StringButtonStatusDialogField packageDialogField;

	public NewPackageContainerWizardPage(String name, IStructuredSelection selection) {
		super(name);
		TypeFieldsAdapter adapter = new TypeFieldsAdapter();

		packageDialogField = new StringButtonStatusDialogField(adapter);
		packageDialogField.setDialogFieldListener(adapter);
		packageDialogField.setLabelText(NewWizardMessages.NewTypeWizardPage_package_label);
		packageDialogField.setButtonLabel(NewWizardMessages.NewTypeWizardPage_package_button);
		packageDialogField.setStatusWidthHint(NewWizardMessages.NewTypeWizardPage_default);

		packageCompletionProcessor = new JavaPackageCompletionProcessor();

		this.initContainerPage(this.getInitialJavaElement(selection));
	}

	public void createControl(Composite parentRoot) {
		initializeDialogUnits(parentRoot);

		Composite parent = new Composite(parentRoot, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));

		Composite groupParent = new Composite(parent, SWT.NONE);
		groupParent.setLayout(new GridLayout());
		Composite sourceAndPackageContainer = new Composite(groupParent, SWT.NONE);
		sourceAndPackageContainer.setFont(groupParent.getFont());
		int nColumns = 4;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		sourceAndPackageContainer.setLayout(layout);

		// pick & choose the wanted UI components
		createContainerControls(sourceAndPackageContainer, nColumns);
		createPackageControls(sourceAndPackageContainer, nColumns);

		Label separator = new Label(groupParent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		packageDialogField.setFocus();

		this.createAdditionalControls(parent);

		this.setControl(parent);
	}

	protected abstract void createAdditionalControls(Composite parent);

	protected void createPackageControls(Composite composite, int nColumns) {
		packageDialogField.doFillIntoGrid(composite, nColumns);
		Text text = packageDialogField.getTextControl(null);
		BidiUtils.applyBidiProcessing(text, "java");
		LayoutUtil.setWidthHint(text, getMaxFieldWidth());
		LayoutUtil.setHorizontalGrabbing(text);
		ControlContentAssistHelper.createTextContentAssistant(text, packageCompletionProcessor);
		TextFieldNavigationHandler.install(text);
	}

	protected IPackageFragment choosePackage() {
		IPackageFragmentRoot froot = getPackageFragmentRoot();
		IJavaElement[] packages = null;
		try {
			if (froot != null && froot.exists()) {
				packages = froot.getChildren();
			}
		} catch (JavaModelException e) {
			XMLEditorPlugin.log(e);
		}
		if (packages == null) {
			packages = new IJavaElement[0];
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog.setTitle(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_title);
		dialog.setMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_description);
		dialog.setEmptyListMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_empty);
		dialog.setElements(packages);
		dialog.setHelpAvailable(false);

		// IPackageFragment pack = getPackageFragment();
		// if (pack != null) {
		// dialog.setInitialSelections(new Object[] { pack });
		// }

		if (dialog.open() == Window.OK) {
			return (IPackageFragment) dialog.getFirstResult();
		}
		return null;
	}

	@Override
	protected IStatus containerChanged() {
		IStatus status = super.containerChanged();
		packageCompletionProcessor.setPackageFragmentRoot(this.getPackageFragmentRoot());

		return status;
	}

	private void typePageChangeControlPressed(DialogField field) {
		if (field == packageDialogField) {
			IPackageFragment pack = choosePackage();
			if (pack != null) {
				packageDialogField.setText(pack.getElementName());
			}
		}
	}

	public String getSourceDirectoryAbsolutePath() {
		if (this.getPackageFragmentRootText().isEmpty()) {
			return "";
		} else {
			IWorkspaceRoot fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			return fWorkspaceRoot.getFolder(new Path(this.getPackageFragmentRootText())).getLocation().toOSString();
		}
	}

	public String getPackage() {
		return this.packageDialogField.getText();
	}

	private class TypeFieldsAdapter implements IStringButtonAdapter, IDialogFieldListener,
			IListAdapter<InterfaceWrapper>, SelectionListener {

		// -------- IStringButtonAdapter
		public void changeControlPressed(DialogField field) {
			typePageChangeControlPressed(field);
		}

		// -------- IListAdapter
		public void customButtonPressed(ListDialogField<InterfaceWrapper> field, int index) {
			// typePageCustomButtonPressed(field, index);
		}

		public void selectionChanged(ListDialogField<InterfaceWrapper> field) {
		}

		// -------- IDialogFieldListener
		public void dialogFieldChanged(DialogField field) {
			// typePageDialogFieldChanged(field);
		}

		public void doubleClicked(ListDialogField<InterfaceWrapper> field) {
		}

		public void widgetSelected(SelectionEvent e) {
			// typePageLinkActivated();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// typePageLinkActivated();
		}
	}

	protected static class InterfaceWrapper {
		public String interfaceName;

		public InterfaceWrapper(String interfaceName) {
			this.interfaceName = interfaceName;
		}

		@Override
		public int hashCode() {
			return interfaceName.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj != null && getClass().equals(obj.getClass())
					&& ((InterfaceWrapper) obj).interfaceName.equals(interfaceName);
		}
	}

}
