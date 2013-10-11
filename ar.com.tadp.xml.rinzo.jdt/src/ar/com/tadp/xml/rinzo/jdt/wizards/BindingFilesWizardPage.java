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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.ui.viewsupport.FilteredElementTreeSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ArchiveFileFilter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.ide.dialogs.ResourceComparator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ar.com.tadp.xml.rinzo.jdt.preferences.TableViewerSupport;

/**
 * 
 * @author ccancinos
 *
 */
public class BindingFilesWizardPage extends WizardPage {
	private ListViewer listViewer;
	private List<String> files = new ArrayList<String>();
	
	public BindingFilesWizardPage(String pageName) {
		super(pageName);
		this.setTitle("Create JAXB Parser");
		this.setDescription("Select JAXB binding files");
	}

	public void createControl(Composite root) {
		Composite parent = new Composite(root, NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		parent.setLayout(gridLayout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.listViewer = new ListViewer(parent);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = true;
		this.listViewer.getControl().setLayoutData(layoutData);
		this.listViewer.setContentProvider(new TableViewerSupport.ListContentProvider());
		this.listViewer.setInput(this.files);
		
		Composite buttonParent = new Composite(parent, SWT.NULL);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.spacing = 2;
		buttonParent.setLayout(fillLayout);
		Button buttonAdd = new Button(buttonParent, SWT.PUSH);
		buttonAdd.setText("Add");
		Button buttonRemove = new Button(buttonParent, SWT.PUSH);
		buttonRemove.setText("Remove");

		buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				ElementTreeSelectionDialog selectionDialog = new ElementTreeSelectionDialog(null, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
//				selectionDialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
//				selectionDialog.open();
				
				FilteredElementTreeSelectionDialog dialog = new FilteredElementTreeSelectionDialog(null, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setInitialFilter("*.xml");
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				ArrayList<IResource> usedJars= new ArrayList<IResource>();
				dialog.addFilter(new ArchiveFileFilter(usedJars, true, true));
				if (dialog.open() == Window.OK) {
					Object[] elements= dialog.getResult();
					IPath[] res= new IPath[elements.length];
					for (int i= 0; i < res.length; i++) {
						IResource elem= (IResource)elements[i];
						files.add(elem.getLocation().toOSString());
					}
				}
				listViewer.refresh(false);
			}
		});
		
		buttonRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
				for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
					String element = (String) iterator.next();
					files.remove(element);
				}
				listViewer.refresh(false);
			}
		});
		
		this.setControl(parent);
	}

	public List<String> getFiles() {
		return files;
	}
	
}
