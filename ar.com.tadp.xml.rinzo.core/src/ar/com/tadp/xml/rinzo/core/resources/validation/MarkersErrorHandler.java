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
package ar.com.tadp.xml.rinzo.core.resources.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * This class is in charge of handle the messages from the XML parser to show them into the <i>Problems</i> view
 * 
 * @author ccancinos
 */
public class MarkersErrorHandler implements ErrorHandler {
    private static final String MARKER_TYPE = IMarker.PROBLEM;
    private IFile file;
	private String severity;
	private RinzoXMLEditor editor;

    public MarkersErrorHandler(RinzoXMLEditor editor) {
		this.editor = editor;
		this.severity = XMLEditorPlugin.getCompilationSeverity();
    }

    public void error(SAXParseException exception) throws SAXException {
        this.createMarker(exception);
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        this.createMarker(exception);
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        this.createMarker(exception);
    }

    /**
     * Creates a IMarker to display the message from the XML parser into the <i>Problems</i> View 
     */
    private void createMarker(SAXParseException exception) {
        try {
            String message = exception.getMessage();
            message = message.substring(message.indexOf(":")+1).trim();
            //REVISARME para ver si hay otra forma de hacer esto
            if(!"no grammar found.".equals(message) &&
               !message.endsWith("must match DOCTYPE root \"null\".")) {
            	
            	Map<String, Comparable> attributes = new HashMap<String, Comparable>();
            	attributes.put(IMarker.MESSAGE, message);
            	attributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
            	attributes.put(IMarker.LINE_NUMBER, Integer.valueOf(exception.getLineNumber()));
            	attributes.put(IMarker.SEVERITY, this.getSeverity());
            	
        		int lineStartChar = getCharStart(exception.getLineNumber(), exception.getColumnNumber());
    			XMLNode activeNode = XMLTreeModelUtilities.getActiveNode(editor.getSourceViewerEditor().getDocument(), lineStartChar);
    			attributes.put(IMarker.CHAR_START, Integer.valueOf(activeNode.getOffset()));
    			attributes.put(IMarker.CHAR_END, Integer.valueOf(activeNode.getOffset() + activeNode.getLength()));

            	MarkerUtilities.createMarker(file, attributes, MARKER_TYPE);
            }
        } catch (Exception coreException) {
            throw new RuntimeException("Error during the creation of the Marker", coreException);
        }
    }
    
    public void setFile(IFile file) {
        this.file = file;
    }

	private int getCharStart(int lineNumber, int columnNumber) {
		try {
			IDocument document = editor.getSourceViewerEditor().getDocument();

			int lineStartChar = document.getLineOffset(lineNumber - 1);
			Integer charEnd = getCharEnd(lineNumber, columnNumber);
			if (charEnd != null) {
				ITypedRegion typedRegion = document.getPartition(charEnd.intValue() - 2);
				int partitionStartChar = typedRegion.getOffset();
				return partitionStartChar;
			} else
				return lineStartChar;
		} catch (BadLocationException e) {
			return 0;
		}
	}

	private Integer getCharEnd(int lineNumber, int columnNumber) {
		try {
			IDocument document = editor.getSourceViewerEditor().getDocument();
			return Integer.valueOf(document.getLineOffset(lineNumber - 1) + columnNumber);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}
    
    private int getSeverity() {
    	if(this.severity.equals("Error")) {
    		return IMarker.SEVERITY_ERROR;
    	}
    	if(this.severity.equals("Warning")) {
    		return IMarker.SEVERITY_WARNING;
    	}
		return IMarker.SEVERITY_ERROR;
    }
	
}
