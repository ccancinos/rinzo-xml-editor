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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.resources.validation.XmlValidator;
import ar.com.tadp.xml.rinzo.jdt.RinzoJDTPlugin;

/**
 * Validates class name existence on an editor delegating on a validation
 * strategy depending on the preference configurations
 * 
 * @author ccancinos
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClassNamesValidator implements XmlValidator {
    private static final String MARKER_TYPE = IMarker.PROBLEM;
	private IFile inputIFile;
	private String severity;
	
	private ClassNamesValidatorVisitor namesValidatorVisitor = new ClassNamesValidatorVisitor();;
	private FilteredClassNamesValidatorVisitor filteredNamesValidatorVisitor = new FilteredClassNamesValidatorVisitor();;
	
	public void validate(RinzoXMLEditor editor) {
		inputIFile = editor.getEditorInputIFile();
		this.severity = RinzoJDTPlugin.getCompilationSeverity();

		if(!this.severity.equalsIgnoreCase("ignore")) {
			if (!RinzoJDTPlugin.isEnableClassName()) {
				this.namesValidatorVisitor.setInputFile(this.inputIFile);
				this.namesValidatorVisitor.setSeverity(this.getSeverity());
				this.namesValidatorVisitor.setMarkerType(MARKER_TYPE);
				editor.getModel().getTree().accept(this.namesValidatorVisitor);
			} else {
				this.filteredNamesValidatorVisitor.setInputFile(this.inputIFile);
				this.filteredNamesValidatorVisitor.setSeverity(this.getSeverity());
				this.filteredNamesValidatorVisitor.setMarkerType(MARKER_TYPE);
				editor.getModel().getTree().accept(this.filteredNamesValidatorVisitor);
			}
		}
	}
    
    private int getSeverity() {
    	if(this.severity.equals("Error")) {
    		return IMarker.SEVERITY_ERROR;
    	}
    	if(this.severity.equals("Warning")) {
    		return IMarker.SEVERITY_WARNING;
    	} else {
    		return IMarker.SEVERITY_INFO;
    	}
    }

}
