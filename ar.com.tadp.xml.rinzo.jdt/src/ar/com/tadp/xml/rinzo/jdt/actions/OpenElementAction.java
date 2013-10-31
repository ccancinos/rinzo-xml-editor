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
package ar.com.tadp.xml.rinzo.jdt.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.Utils;

/**
 * It opens an element which name is under selection.
 * 
 * @author ccancinos
 */
public class OpenElementAction extends ClassNameSelectedAction {
	public static final String ID = "OpenClassAction.";
    private IWorkbenchPage activePage = null;

	public boolean isEnabled() {
		return this.getSelection() != null;
	}

	// TODO mmm... this is just because I'm not using the template method of ClassNameSelectedAction.
	protected boolean getEnableValue(IType type) {
		return true;
	}

	public void run(IAction action) {
		String selection = this.getSelection();
		if(!JDTUtils.openType(selection)) {
			this.openResource(selection);
		}
	}
	
	private void openResource(String selection) {
		if(selection.startsWith("http")) {
			this.openURL(selection);
		} else {
			this.openFile(selection);
		}
	}

	// WORK IN PROGRESS!!!
	private void openURL(String fileUrl) {
		try {
			WebBrowserEditor.open(new WebBrowserEditorInput(new URL(fileUrl), BrowserViewer.LOCATION_BAR + BrowserViewer.BUTTON_BAR));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	// ************************************************
	// ** Helpers to open a resource 
	// ************************************************

	private IFile getSelectedFile(String fileName) {
        IFile file = null;
		if (!Utils.isEmpty(fileName)) {
			file = this.getRootRelativeFile(fileName);
            if (!this.existFile(file)) {
            	file = this.getEditorRelativeFile(fileName);
                if (!this.existFile(file)) {
                	file = this.getNoRelativeFile(fileName);
                }
            }
        }
        return file;
    }

	private void openFile(String filePath) {
        try {
			IFile file = this.getSelectedFile(filePath);
			if(file == null) {
				return;
			}
            if (this.activePage == null) {
                IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (activeWorkbenchWindow == null) {
                    return;
                }
                this.activePage = activeWorkbenchWindow.getActivePage();
                if (this.activePage == null) {
                    return;
                }
            }
            IDE.openEditor(this.activePage, file);
            // this.activePage.openEditor(editorInput,
            // this.getDefauiltEditor(fileName).getId());
        } catch (PartInitException exception) {
            throw new RuntimeException(exception);
        }
    }

    private IFile getEditorRelativeFile(String fileName) {
        IPath currentPath = this.getAsociatedResource().getFullPath().removeLastSegments(1).removeFirstSegments(1);
        return this.getActiveJavaProject().getProject().getFile(currentPath.append(fileName));
    }

	private IFile getRootRelativeFile(String fileName) {
		return this.getActiveJavaProject().getProject().getFile(new Path(fileName));
	}

    private IEditorDescriptor getDefauiltEditor(String fileName) {
        return PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileName);
    }

    private IFile getNoRelativeFile(String fileName) {
    	try {
			IResourceVisitorImplementation visitor = new IResourceVisitorImplementation(fileName);
			this.getActiveJavaProject().getProject().accept(visitor);
			return visitor.getIFile();
		} catch (CoreException e) {
			XMLEditorPlugin.log(e);
		}
    	
		return null;
	}

	private boolean existFile(IFile file) {
		return file != null && file.exists();
	}

	/**
	 * 
	 * @author ccancinos
	 */
    private final class IResourceVisitorImplementation implements IResourceVisitor {
		private IFile iFile;
		private String fileName;

		public IResourceVisitorImplementation(String fileName) {
			this.fileName = fileName;
		}

		public boolean visit(IResource resource) throws CoreException {
			String name = resource.getLocation().toString().toLowerCase();
			if (name.endsWith(this.fileName.toLowerCase())) {
				this.iFile = (IFile)resource;
				return false;
			}
			return true;
		}

		public IFile getIFile() {
			return this.iFile;
		}
	}
	
}
