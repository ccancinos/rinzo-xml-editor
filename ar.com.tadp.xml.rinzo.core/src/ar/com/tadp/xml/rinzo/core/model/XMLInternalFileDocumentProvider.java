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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLDocumentPartitioner;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLPartitionScanner;

/**
 * Used to provide the document's input when the file is inside the Eclipse's
 * Workspace
 * 
 * @author ccancinos
 */
public class XMLInternalFileDocumentProvider extends FileDocumentProvider implements TreeModelContainer {
	private RinzoXMLEditor editor;
	private XMLTreeModel treeModel;

	public XMLInternalFileDocumentProvider(RinzoXMLEditor editor) {
		this.editor = editor;
	}

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new XMLDocumentPartitioner(new XMLPartitionScanner(),
					XMLPartitionScanner.CONTENT_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			treeModel = new XMLTreeModel(editor);
			treeModel.createTree(document);
			document.addDocumentListener(treeModel);
		}
		return document;
	}

	public XMLTreeModel getTreeModel() {
		return treeModel;
	}
	
	@Override
	protected IAnnotationModel createAnnotationModel(Object file) throws CoreException {
		IAnnotationModel model = super.createAnnotationModel(file);
		return (model != null) ? model : new AnnotationModel();
	}

}
