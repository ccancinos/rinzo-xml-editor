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
package ar.com.tadp.xml.rinzo.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLDocumentPartitioner;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLPartitionScanner;

/**
 * Used to provide the document's input when the file is outside the Eclipse's
 * Workspace such as another directory in the file system or a file from an
 * SVN/CVS repository.
 * 
 * @author ccancinos
 */
public class XMLExternalFileDocumentProvider extends TextFileDocumentProvider implements TreeModelContainer {
	private RinzoXMLEditor editor;
	private XMLTreeModel treeModel;

	public XMLExternalFileDocumentProvider(RinzoXMLEditor editor) {
		this.editor = editor;
	}

	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info = super.createFileInfo(element);
		if (info == null) {
			info = this.createEmptyFileInfo();
		}
		IDocument document = info.fTextFileBuffer.getDocument();
		if (document != null) {
			IDocumentPartitioner partitioner = new XMLDocumentPartitioner(new XMLPartitionScanner(),
					XMLPartitionScanner.CONTENT_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			treeModel = new XMLTreeModel(editor);
			treeModel.createTree(document);
			document.addDocumentListener(treeModel);
		}
		return info;
	}

	@Override
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new AnnotationModel();
	}

	public XMLTreeModel getTreeModel() {
		return this.treeModel;
	}

}
