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
package ar.com.tadp.xml.rinzo.jdt.resources.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ar.com.tadp.xml.rinzo.core.model.XMLAttribute;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.model.visitor.HierarchicalVisitor;
import ar.com.tadp.xml.rinzo.jdt.JDTUtils;
import ar.com.tadp.xml.rinzo.jdt.Utils;

/**
 * Validates for class names on all attributes and tags if their value has the
 * pattern of a dot separated string containing characters allowed in a class
 * name.
 * 
 * @author ccancinos
 */
public class ClassNamesValidatorVisitor implements HierarchicalVisitor {
    private IJavaProject activeJavaProject;
	private IFile inputIFile;
	private int severity;
	private String markerType;

	public boolean visitChild(XMLNode node) {
		if (node.isTextTag()) {
			String content = (String) node.getContent();
			if (content != null && !Utils.isEmpty(content.trim())) {
				XMLAttribute dummy = new XMLAttribute(content, node.offset,
						node.length);
				if (isClassName(dummy)) {
					validateClassName(dummy, node);
				}
			}
		} else {
			node.hasChanged();
			for (Iterator iterator = node.getAttributes().values().iterator(); iterator.hasNext();) {
				XMLAttribute attribute = (XMLAttribute) iterator.next();
				if (isClassName(attribute)) {
					validateClassName(attribute, node);
				}
			}
		}
		return true;
	}

	public boolean visitEnd(XMLNode node) {
		return true;
	}

	public boolean visitStart(XMLNode node) {
		node.hasChanged();
		for (Iterator iterator = node.getAttributes().values().iterator(); iterator.hasNext();) {
			XMLAttribute attribute = (XMLAttribute) iterator.next();
			if (isClassName(attribute)) {
				validateClassName(attribute, node);
			}
		}

		return true;
	}

	private boolean isClassName(XMLAttribute attribute) {
		return JavaConventions.validateJavaTypeName(attribute.getValue()).isOK()
				&& attribute.getValue().contains(".");
	}

	protected void validateClassName(XMLAttribute attribute, XMLNode node) {
		if (this.findType(attribute.getValue()) == null) {
			this.addError(attribute, node);
		}
	}
	
	/**
	 * Retorna el tipo para el nombre de clase candidata dentro del workspace o null si no se encuentra
	 */
	protected IType findType(String classNameCandidate) {
		return JDTUtils.findType(classNameCandidate, this.getActiveJavaProject());
	}
	
    /**
     * Se encarga de devolver siempre el proyecto actual
     */
    protected IJavaProject getActiveJavaProject() {
    	if (activeJavaProject == null) {
   			activeJavaProject = JDTUtils.getActiveJavaProject();
    	}
    	return activeJavaProject;
    }

    protected void addError(XMLAttribute attribute, XMLNode node) {
    	this.addError(attribute, node, attribute.getValue() + " cannot be resolved to a type");
	}

    protected void addError(XMLAttribute attribute, XMLNode node, String message) {
    	this.createMarker(message, 0, attribute.getOffset(), attribute.getLength());
	}

    /**
     * Creates a IMarker to display the message from the XML parser into the <i>Problems</i> View 
     */
	private void createMarker(String message, int lineNumber, int offset, int length) {
        try {
        	Map attributes = new HashMap();
        	attributes.put(IMarker.MESSAGE, message);
        	attributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
        	attributes.put(IMarker.LINE_NUMBER, Integer.valueOf(lineNumber));
        	attributes.put(IMarker.SEVERITY, Integer.valueOf(this.severity));
        	
			attributes.put(IMarker.CHAR_START, Integer.valueOf(offset));
			attributes.put(IMarker.CHAR_END, Integer.valueOf(offset + length));
        	
        	MarkerUtilities.createMarker(this.inputIFile, attributes, this.markerType);
        } catch (Exception coreException) {
            throw new RuntimeException("Error during the creation of the Marker", coreException);
        }
    }

	public void setInputFile(IFile inputIFile) {
		this.inputIFile = inputIFile;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public void setMarkerType(String markerType) {
		this.markerType = markerType;
	}

}
