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
package ar.com.tadp.xml.rinzo.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Collection of utility methods for interaction with JDT.
 * 
 * @author ccancinos
 */
public class JDTUtils {
	private static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature";

	public static IType findType(String classNameCandidate) {
		return findType(classNameCandidate, getActiveJavaProject());
	}

	/**
	 * @return the IType for the candidate class name in the workspace or null if not available.
	 */
	public static IType findType(String classNameCandidate, IJavaProject activeJavaProject) {
		IJavaProject[] projects = getWorkspaceProjects();
		IType type = null;

		// Busco primero en el proyectos actual
		try {
			type = activeJavaProject.findType(classNameCandidate);
		}
		catch (Exception e) {
			// TODO: At this moment I don't care about any explotion here, because it could be caused by trying to 
			// open a type in a file without associated project such as files external to the workspace
		}

		// Si no lo encuentro ah�, entonces busco en el resto de los proyectos
		// del workspace
		if (type == null) {
			for (int i = 0; i < projects.length; i++) {
				IJavaProject project = projects[i];
				if (project != activeJavaProject) {
					try {
						type = project.findType(classNameCandidate);
					}
					catch (JavaModelException exception) {
						// Don't mind about this one.
					}
					if (type != null) {
						break;
					}
				}
			}
		}
		return type;
	}

    /**
     * @return All projects contained in the workspace.
     */
    public static IJavaProject[] getWorkspaceProjects() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IJavaModel javaModel = JavaCore.create(workspace.getRoot());
		IJavaProject[] projects = null;
		try {
			projects = javaModel.getJavaProjects();
		} catch (JavaModelException jme) {
			projects = new IJavaProject[0];
		}
		return projects;
    }
    
    public static IJavaProject getActiveJavaProject() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IJavaProject project = null;
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IFile input = ResourceUtil.getFile(page.getActiveEditor().getEditorInput());
				if (input != null) {
					project = JavaCore.create(input.getProject());
				}
			}
		}
		return project;
	}

    /**
     * Opens the type corresponding to the class name using the associated editor.
     * 
     * @param selection the name of the class to be opened.
     * @return
     */
	public static boolean openType(String selection) {
		if (selection.length() > 0 && JavaConventions.validateJavaTypeName(selection).isOK()) {
			try {
				IType type = JDTUtils.findType(selection);
				if (type != null) {
					JavaUI.openInEditor(type);
					return true;
				}
			} catch (Exception e) { }
		}
		return false;
	}
	
    public static boolean isJavaProject(IJavaProject project) {
		try {
			return project != null && project.getProject().getNature(JAVA_NATURE_ID) != null;
		} catch (CoreException e) {
			return false;
		}
	}

	public static IType[] getAllSuperTypes(String qualifiedTypeName) {
		IType[] superTypes = null; 
		try {
			IType type;
			type = JDTUtils.findType(qualifiedTypeName);
			if (type!=null) {
				ITypeHierarchy typeHierachy = type.newSupertypeHierarchy(null);
				if (typeHierachy!=null) {
					superTypes = typeHierachy.getAllSupertypes(type);
				}
			}
		} catch (JavaModelException e) {
		}
		return (superTypes == null) ?  new IType[0] : superTypes;
	}
	
	public static boolean isSuperType(String className, String superType) {
		IType[] supertypes = JDTUtils.getAllSuperTypes(className);
		for (IType iType : supertypes) {
			if (iType.getFullyQualifiedName().equals(superType)) {
				return true;
			}
		}
		return false;
	}

}
