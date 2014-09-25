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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.After;
import org.junit.Before;

import ar.com.tadp.xml.rinzo.core.model.XMLTreeModel;

/**
 * @author ccancinos
 * 
 */
public class AbstractRinzoTest {
	protected static final String PROJECT = "RinzoTestProject";
	protected IProject project;
	protected IFile file;
	protected RinzoXMLEditor editor;
	protected XMLTreeModel xmlTreeModel;

	@Before
	public void createProject() throws CoreException {
		this.project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(PROJECT);

		if (this.project.exists()) {
			this.project.delete(true, null);
		}
		IProjectDescription desc = ResourcesPlugin.getWorkspace()
				.newProjectDescription(PROJECT);
		this.project.create(desc, null);
		this.project.open(null);
	}

	@After
	public void deleteProject() throws CoreException {
		if (this.editor != null) {
			this.editor.close(false);
		}
		this.project.delete(true, true, null);
	}

	protected void useFile(String path) throws FileNotFoundException,
			CoreException {
		String fileName = this.copyFile(path);

		this.editor = this.openEditor(PROJECT + "/" + fileName);
		this.xmlTreeModel = this.editor.getModel();
	}

	protected String copyFile(String path) throws FileNotFoundException, CoreException {
		String fileName = path.substring(path.lastIndexOf("/") + 1);
		this.file = this.project.getFile(fileName);
		InputStream source = new FileInputStream(new File("").getAbsolutePath()
				+ "/" + path);
		this.file.create(source, true, null);
		return fileName;
	}

	/**
	 * Compares the content of a file in the test project's directory with the
	 * content of the file being used for the test
	 * 
	 * @param path
	 *            the path to the test project's file with the expected result
	 * @return
	 * @throws IOException
	 * @throws CoreException
	 */
	protected boolean equalFileContents(String path) throws IOException,
			CoreException {
		String currentFileContent = IOUtils.toString(this.file.getContents());
		String expectedFileContent = this.getExpectedFileContent(path);
		return currentFileContent.equals(expectedFileContent);
	}

	protected String getExpectedFileContent(String expectedPath)
			throws IOException {
		return FileUtils.readFileToString(new File(new File("")
				.getAbsolutePath() + "/" + expectedPath));
	}

	protected RinzoXMLEditor openEditor(String fullPath)
			throws PartInitException {
		IPath path = new Path(fullPath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		IWorkbenchPage activePage = getActivePage();
		if (activePage != null) {
			IDE.openEditor(activePage, file);
			return (RinzoXMLEditor) activePage.getActiveEditor();
		} else {
			return null;
		}
	}

	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return null;
		}
		return activePage;
	}

}
