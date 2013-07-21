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
import com.thaiopensource.relaxng.input.xml.XmlInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * The action to generate DTD from XML.
 * 
 * @author ccancinos
 */
public class GenerateDTDAction implements IEditorActionDelegate {
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
		dialog.setFilterExtensions(new String[] { "*.dtd" });
		String file = dialog.open();
		if (file != null) {
			try {
				this.generateDTDFromXML(this.file, new File(file));
			} catch (SAXParseException exception) {
				MessageDialog.openInformation(
						null,
						"DTD Generation",
						"Error parsing XML file " + this.file.getAbsolutePath() + "\n\nLine: "
								+ exception.getLineNumber() + "\n\nError message:\n" + exception.getLocalizedMessage());
				exception.printStackTrace();
			} catch (Exception ex) {
				MessageDialog.openInformation(
						null,
						"DTD Generation",
						"Error generating DTD for file " + this.file.getAbsolutePath() + "\n\nError message:\n"
								+ ex.getLocalizedMessage());
				ex.printStackTrace();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Generates DTD from a XML file.
	 * 
	 * @param input
	 *            XML file (input)
	 * @param output
	 *            DTD file (output)
	 * @throws Exception
	 */
	private void generateDTDFromXML(File input, File output) throws Exception {
		ErrorHandlerImpl eh = new ErrorHandlerImpl();
		OutputFormat of = new DtdOutputFormat();
		InputFormat inFormat = new XmlInputFormat();
		SchemaCollection sc = inFormat.load(UriOrFile.toUri(input.getAbsolutePath()), new String[0], "dtd", eh, null);

		OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(), output, "xml", "utf-8", 80, 4);

		of.output(sc, od, new String[0], "xml", eh);
	}

}
