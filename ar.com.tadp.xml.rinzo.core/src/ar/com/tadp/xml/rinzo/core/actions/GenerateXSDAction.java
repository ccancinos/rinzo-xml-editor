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

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xml.sax.SAXParseException;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.dtd.DtdInputFormat;
import com.thaiopensource.relaxng.input.xml.XmlInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * Action to generate the XSD from the XML
 * 
 * @author ccancinos
 */
public class GenerateXSDAction implements IEditorActionDelegate {
	private RinzoXMLEditor editor;
	private ISourceViewer viewer;
	private File file;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null) {
			this.editor = (RinzoXMLEditor) targetEditor;
			this.viewer = editor.getSourceViewerEditor();
			this.file = editor.getEditorInputFile();
		}
	}

	public void run(IAction action) {
		FileDialog dialog = new FileDialog(this.viewer.getTextWidget().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.xsd" });
		String file = dialog.open();
		if (file != null) {
			try {
				this.generateXSDFromXML(this.file, new File(file));
			} catch (SAXParseException exception) {
				MessageDialog.openInformation(
						null,
						"XSD Generation",
						"Error parsing XML file " + this.file.getAbsolutePath() + "\n\nLine: "
								+ exception.getLineNumber() + "\n\nError message:\n" + exception.getLocalizedMessage());
				exception.printStackTrace();
			} catch (Exception ex) {
				MessageDialog.openInformation(
						null,
						"XSD Generation",
						"Error generating XSD for file " + this.file.getAbsolutePath() + "\n\nError message:\n"
								+ ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Generats XML Schema from a XML file.
	 * 
	 * @param input
	 *            XML file (input)
	 * @param output
	 *            XSD file (output)
	 * @throws Exception
	 */
	private void generateXSDFromXML(File input, File output) throws Exception {
		ErrorHandlerImpl eh = new ErrorHandlerImpl();
		OutputFormat of = new XsdOutputFormat();
		InputFormat inFormat = new XmlInputFormat();
		SchemaCollection sc = inFormat.load(UriOrFile.toUri(input.getAbsolutePath()), new String[0], "xsd", eh);

		OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(), output, "xml", "utf-8", 80, 4);

		of.output(sc, od, new String[0], "xml", eh);
	}

	/**
	 * Generates XSD from a DTD file.
	 * 
	 * @param input
	 *            DTD file (input)
	 * @param output
	 *            XSD file (output)
	 * @throws Exception
	 */
	public static void generateXSDFromDTD(File input, File output) throws Exception {
		ErrorHandlerImpl eh = new ErrorHandlerImpl();
		OutputFormat of = new XsdOutputFormat();
		InputFormat inFormat = new DtdInputFormat();
		SchemaCollection sc = inFormat.load(UriOrFile.toUri(input.getAbsolutePath()), new String[0], "xsd", eh);
		OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(), output, "xml", "utf-8", 80, 4);
		of.output(sc, od, new String[0], "xml", eh);
	}

}
