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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author ccancinos
 */
public class NewJAXBParserWizardPage extends NewPackageContainerWizardPage {
	private Combo encodingCombo;
	private Combo targetCombo;
	private Button useFluentApi;
	private Button useValueConstructor;
	private Button useDefaultValue;

	public NewJAXBParserWizardPage(IStructuredSelection selection) {
		super("New JAXB Parser", selection);
		this.setTitle("Create JAXB Parser");
		this.setDescription("Create a JAXB parser in the workspace");
	}

	public void createAdditionalControls(Composite parent) {
		Composite combosContainer = new Composite(parent, SWT.NONE);
		combosContainer.setFont(parent.getFont());
		int nColumns = 6;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		combosContainer.setLayout(layout);
		
		Label encodingLabel = new Label(combosContainer, SWT.NULL);
		encodingLabel.setText("&Encoding:");
		encodingCombo = new Combo(combosContainer, SWT.READ_ONLY);
		encodingCombo.setItems(new String[] { "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "US-ASCII", "ISO-8859-1" });
		encodingCombo.setText("Encoding");
		encodingCombo.select(0);

		Label targetLabel = new Label(combosContainer, SWT.NULL);
		targetLabel.setText("    JAXB &Target:");
		targetCombo = new Combo(combosContainer, SWT.READ_ONLY);
		targetCombo.setItems(new String[] { "2.2", "2.1", "2.0" });
		targetCombo.setText("Target");
		targetCombo.select(0);

		Composite checksContainer = new Composite(parent, SWT.NONE);
		checksContainer.setFont(parent.getFont());
		nColumns = 1;
		layout = new GridLayout();
		layout.numColumns = nColumns;
		layout.marginBottom = 10;
		checksContainer.setLayout(layout);

		this.useFluentApi = this.createCheck(checksContainer, "Add &Fluent setters");
		this.useValueConstructor = this.createCheck(checksContainer, "Add Value &Constructor");
		this.useDefaultValue = this.createCheck(checksContainer, "Add &Default Values");
	}
	
	private Button createCheck(Composite checksContainer, String text) {
		Button check = new Button(checksContainer, SWT.CHECK);
		check.setText(text);
		check.setSelection(true);
		return check;
	}

	public String getEncoding() {
		return this.encodingCombo.getText();
	}

	public String getTargetVersion() {
		return this.targetCombo.getText();
	}

	public boolean isFluentAPI() {
		return this.useFluentApi.getSelection();
	}

	public boolean isValueConstructor() {
		return this.useValueConstructor.getSelection();
	}

	public boolean isDefaultValue() {
		return this.useDefaultValue.getSelection();
	}

}
