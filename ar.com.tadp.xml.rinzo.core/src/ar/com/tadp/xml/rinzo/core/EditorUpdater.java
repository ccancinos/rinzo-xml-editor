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
package ar.com.tadp.xml.rinzo.core;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.source.ISourceViewer;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.resources.validation.XmlValidator;

/**
 * Thread monitoring editor changes in order to execute validators on change.
 * 
 * @author ccancinos
 */
public class EditorUpdater implements Runnable {
	private static final String RINZO_VALIDATORS_EXTENSION_POINT_ID = "ar.com.tadp.xml.rinzo.core.validators";
	private RinzoXMLEditor editor;
	private Collection<XmlValidator> validators = new ArrayList<XmlValidator>();

	private boolean stop;
	private Object object = new Object();

	public EditorUpdater(RinzoXMLEditor editor) {
		this.editor = editor;
		this.loadValidators();
	}

	public void run() {
		editor.getTagContainersRegistry();
		while (!isStop()) {
			sleep(1200);
			if (editor.isChanged()) {
				editor.setChanged(false);
				sleep(100);
				if (!editor.isChanged()) {
					this.update();
				}
			}
		}
	}

	public void update() {
		synchronized(object) {
			try {
				this.deleteProblems();
				ISourceViewer sourceViewerEditor = editor.getSourceViewerEditor();
				if(sourceViewerEditor != null && sourceViewerEditor.getDocument() != null) {
					for (XmlValidator validator : this.validators) {
						validator.validate(this.editor);
					}
				} else {
					this.stop = true;
				}
			} catch (Exception e) {
				XMLEditorPlugin.log(e);
			}
		}
	}

	private void deleteProblems() {
		try {
			IFile iFile = this.editor.getEditorInputIFile();
			if (iFile != null) {
				iFile.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			}
		} catch (Exception coreException) {
			throw new RuntimeException("The Markers in this file cannot be deleted", coreException);
		}
	}
	
	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	public synchronized boolean isStop() {
		return stop;
	}

	public synchronized void setStop() {
		stop = true;
	}
	
	private void loadValidators() {
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				RINZO_VALIDATORS_EXTENSION_POINT_ID);

		if (configurationElements != null) {
			for (IConfigurationElement element : configurationElements) {
				try {
					XmlValidator validator = (XmlValidator) element.createExecutableExtension("class");
					this.validators.add(validator);
				} catch (CoreException e) {
					XMLEditorPlugin.logErrorMessage("cannot create validator: " + element.getName());
					XMLEditorPlugin.log(e);
				}
			}
		}
	}
}
