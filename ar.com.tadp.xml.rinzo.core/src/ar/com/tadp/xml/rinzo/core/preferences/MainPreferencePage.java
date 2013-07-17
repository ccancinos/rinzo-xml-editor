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
package ar.com.tadp.xml.rinzo.core.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;

/**
 * 
 * @author ccancino
 */
public class MainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Combo severityCombo;

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		Composite group = this.createGroup(composite);
		Label label = new Label(group, SWT.NULL);
		label.setText("&Clean XSD/DTD Cache:");
		Button button = new Button(group, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText("Clean");

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DocumentCache.getInstance().clear();
			}
		});

		group = this.createGroup(composite);
		label = new Label(group, SWT.NULL);
		label.setText("XML Validation &Severity:");
		severityCombo = new Combo(group, SWT.READ_ONLY);
		severityCombo.setItems(new String[] { "Error", "Warning" });
		severityCombo.setText(XMLEditorPlugin.getCompilationSeverity());

		return composite;
	}

	private Composite createGroup(Composite composite) {
		Composite group = new Composite(composite, SWT.NULL);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(2, false));
		return group;
	}
	
	@Override
	protected void performDefaults() {
		severityCombo.setText(XMLEditorPlugin.getCompilationSeverity());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		XMLEditorPlugin.setCompilationSeverity(this.severityCombo.getText());
		return true;
	}

	public void init(IWorkbench workbench) {
	}

}
