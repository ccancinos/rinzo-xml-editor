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

import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.RinzoXMLEditor;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentCache;
import ar.com.tadp.xml.rinzo.core.resources.cache.DocumentStructureDeclaration;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Validates if the content of an editor is a well formed XML file.
 * If the file defines a DTD or XSD the file is validated against it.
 *  
 * @author ccancinos
 */
public class XMLStringValidator implements XmlValidator {
    private MarkersErrorHandler errorHandler;
	private ExternalResolver resolver = new ExternalResolver();
    
    public void validate(RinzoXMLEditor editor) {
    	this.errorHandler = new MarkersErrorHandler(editor);
    	this.errorHandler.setFile(editor.getEditorInputIFile());
    	
    	String fileName = editor.getFileName();
		String fileContent = editor.getSourceViewerEditor().getDocument().get();
		if(!StringUtils.isEmpty(fileContent.trim())) {
			Collection<DocumentStructureDeclaration> schemaDefinitions = editor.getModel().getSchemaDefinitions();
			if(schemaDefinitions != null && !schemaDefinitions.isEmpty()) {
	    		this.saxSchemaValidate(fileName, fileContent, schemaDefinitions);
	    	}
	    	else {
	    		DocumentStructureDeclaration dtdDefinition = editor.getModel().getDTDDefinition();
	    		if(dtdDefinition != null) {
	    			this.saxDTDValidate(fileName, fileContent, dtdDefinition);
	    		} else {
	    			this.plainTextValidate(fileName, fileContent);
	    		}
	    	}
		}
    }

    
    private void plainTextValidate(String fileName, String fileContent) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(this.errorHandler);
			builder.parse(new InputSource(new StringReader(fileContent)));
		} catch (Exception e) {
			//Do nothing because the errorHandler informs the error
		}
	}

	private void saxDTDValidate(String fileName, String fileContent, DocumentStructureDeclaration structureDeclaration) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			URI resolverURI = FileUtils.resolveURI(fileName, structureDeclaration.getSystemId());
			if(resolverURI != null) {
				this.resolver.setBaseURL(fileName);
				this.resolver.setSystemId(structureDeclaration.getSystemId());
				builder.setEntityResolver(this.resolver);
			}
			builder.setErrorHandler(this.errorHandler);
			builder.parse(new InputSource(new StringReader(fileContent)));
		} catch (Exception e) {
			//Do nothing because the errorHandler informs the error
		}
	}

	private void saxSchemaValidate(String fileName, String fileContent, Collection<DocumentStructureDeclaration> schemaDefinitions) {
    	try {
    		Validator validator;
            Map<Collection<DocumentStructureDeclaration>, Validator> schemaValidatorsCache = XMLEditorPlugin.getDefault().getSchemaValidatorsCache();
            
            validator = schemaValidatorsCache.get(schemaDefinitions);
            if(validator == null) {
            	StreamSource[] sources = new StreamSource[schemaDefinitions.size()];
            	int pos = 0;
            	Map<String, String> fileLocations = DocumentCache.getInstance().getAllLocations(schemaDefinitions, fileName);
            	for (Map.Entry<String, String> fileLocation : fileLocations.entrySet()) {
            		StreamSource streamSource = new StreamSource(fileLocation.getValue());
            		streamSource.setPublicId(fileLocation.getKey());
					sources[pos++] = streamSource;
				}
				validator = this.createValidator(sources);
				schemaValidatorsCache.put(schemaDefinitions, validator);
            }
            
			validator.reset();
			validator.setErrorHandler(this.errorHandler);
			validator.validate(new StreamSource(new StringReader(fileContent)));
    	} catch (SAXParseException saxE) {
    		try {
				this.errorHandler.error(saxE);
			} catch (SAXException e) { }
        } catch (Exception exception) {
            //Do nothing because the errorHandler informs the error
        }
    }

    private Validator createValidator(StreamSource[] sources) throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature("http://apache.org/xml/features/xinclude", true);
        Schema schema = sf.newSchema(sources);
        return schema.newValidator();
	}
    
    /**
     * @return Returns the errorHandler.
     */
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * @param errorHandler The errorHandler to set.
     */
    public void setErrorHandler(MarkersErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

}
