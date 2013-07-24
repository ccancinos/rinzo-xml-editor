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
package ar.com.tadp.xml.rinzo.core.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.internal.layout.CellLayout;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.xml.XmlInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * 
 * @author ccancinos
 */
public class NewXMLStructureDefinitionWizardPage extends WizardNewFileCreationPage {
	private Map<String, OutputFormat> formats = new LinkedHashMap<String, OutputFormat>();
	private Combo formatCombo;
	private Combo encodingCombo;
	private Text width;
	private List<IFile> input;

	public NewXMLStructureDefinitionWizardPage(IStructuredSelection selection, List<IFile> input2) {
		super("XML Structure Definition", selection);
		this.setTitle("XML Structure Definition");
		this.setDescription("Creates the XML Schema, DTD, RELAX NG (XML syntax) or RELAX NG (compact syntax) for the selected file.");
		this.input = input2;
		
		this.formats.put("XML Schema", new XsdOutputFormat());
		this.formats.put("DTD", new DtdOutputFormat());
		this.formats.put("RELAX NG (XML syntax)", new RngOutputFormat());
		this.formats.put("RELAX NG (compact syntax)", new RncOutputFormat());
	}

	@Override
	public void createControl(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		// Change this layout for a non-internal one. Also try to put this
		// controls at the bottom.
		Layout layout = new CellLayout(1);
		topLevel.setLayout(layout);

		Composite form = new Composite(topLevel, SWT.None);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		form.setLayout(gridLayout);

		Label formatLabel = new Label(form, SWT.NULL);
		formatLabel.setText("&Format:");
		formatCombo = new Combo(form, SWT.READ_ONLY);
		formatCombo.setItems(this.formats.keySet().toArray(new String[] {}));
		formatCombo.setText("Format");
		formatCombo.select(0);

		Label encodingLabel = new Label(form, SWT.NULL);
		encodingLabel.setText("&Encoding:");
		encodingCombo = new Combo(form, SWT.READ_ONLY);
		encodingCombo.setItems(new String[] { "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "US-ASCII", "TIS-620" });
		encodingCombo.setText("Encoding");
		encodingCombo.select(0);

		Label widthLabel = new Label(form, SWT.NULL);
		widthLabel.setText("&Width:");
		width = new Text(form, SWT.BORDER);
		width.setLayoutData(new GridData(40, 20));
		width.setText(XMLEditorPlugin.getMaximumLineWidth() + "");

		super.createControl(topLevel);
	}

	@Override
	protected void createAdvancedControls(Composite parent) {
	}

	protected IStatus validateLinkedResource() {
		return Status.OK_STATUS;
	}

	@Override
	public IFile createNewFile() {
		try {
			ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
			OutputFormat outputFormat = this.formats.get(this.formatCombo.getText());
			XmlInputFormat inputFormat = new XmlInputFormat();
			SchemaCollection schemaCollection = inputFormat.load(this.getFiles(),
					new String[0], "xsd", errorHandler, null);

			IFile fileHandle = createFileHandle(this.getContainerFullPath().append(this.getFileName()));
			OutputDirectory outputDirectory = new LocalOutputDirectory(schemaCollection.getMainUri(), new File(
					fileHandle.getLocation().toString()), "xml", this.encodingCombo.getText(),
					Integer.valueOf(this.width.getText()), 4);

			outputFormat.output(schemaCollection, outputDirectory, new String[0], "xml", errorHandler);
			fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			return fileHandle;
		} catch (Exception e) {
			MessageDialog.openInformation(null, "Definition Generation", "Error generating definition for file "
					+ this.getFiles() + "\n\nError message:\n" + e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
	}

	private String[] getFiles() {
		List<String> files = new ArrayList<String>();
		for (IFile file : this.input) {
			files.add(UriOrFile.toUri(file.getLocation().toString()));
		}
		return files.toArray(new String[]{});
	}

}
