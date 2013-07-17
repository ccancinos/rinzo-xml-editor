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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;

/**
 * Validate the document with an XML parser an inform errors on Problem view.
 * 
 * @author ccancinos
 */
public class XMLFileValidator {
    private ErrorHandler errorHandler;
    
    public XMLFileValidator(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
	 * Validates the file corresponds with the XSD/DTD definition and for well
	 * formness in case no XSD/DTD is defined for the document.
	 * 
	 * @param fileName
	 *            name of the file to validate.
	 * @param schemaFileName
	 *            name of the XSD file used to validate the document. If this
	 *            parameter is <i>null</i>, associated DTD will be used , and if
	 *            no DTD has been defined only well formness is validated.
	 */
    public void validate(String fileName, String schemaFileName) {
    	if(schemaFileName != null) {
    		this.saxSchemaValidate(fileName, schemaFileName);
    	}
    	else {
    		this.saxDTDValidate(fileName);
    	}
    }

    private void saxDTDValidate(String fileName) {
    	try {
         	File file = new File(fileName);
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(true);         
            SAXParser parser = parserFactory.newSAXParser();
            DefaultHandler eventHandler = new DefaultHandler() {
            	public void warning(SAXParseException e) throws SAXException {
            		errorHandler.warning(e);
            	}
            	public void error(SAXParseException e) throws SAXException {
            		errorHandler.error(e);
            	}
            	public void fatalError(SAXParseException e) throws SAXException {
            		errorHandler.fatalError(e);
            	}
            };
            parser.parse(file,eventHandler);
    	} catch (FileNotFoundException fnfE) {
    		try {
				String message = "HastaLosDosPuntosEsParaHackearme:Failed to read DTD document '" + fnfE.getMessage() + "', because 1) could not find the document; 2) the document could not be read";
				this.errorHandler.error(new SAXParseException(message, null, fnfE));
			} catch (SAXException e) { }
        } catch (Exception exception) {
            //No hago nada porque los estoy informando con el errorhandler
        }
    }

	private void saxSchemaValidate(String fileName, String schemaFileName) {
    	try {
    		Validator validator;
            Map schemaValidatorsCache = XMLEditorPlugin.getDefault().getSchemaValidatorsCache();
            
            validator = (Validator) schemaValidatorsCache.get(schemaFileName);
            if(validator == null) {
				validator = createValidator(FileUtils.resolveURI(fileName, schemaFileName).toString());
				schemaValidatorsCache.put(schemaFileName, validator);
            }
            
			validator.reset();
			validator.setErrorHandler(this.errorHandler);
			validator.validate(new StreamSource(fileName));
    	} catch (SAXParseException saxE) {
    		try {
				this.errorHandler.error(saxE);
			} catch (SAXException e) { }
        } catch (Exception exception) {
            //No hago nada porque los estoy informando con el errorhandler
        }
    }

    private Validator createValidator(String uri) throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String url = (uri.contains("://")) ? uri : "file:" + uri;
        Schema schema = sf.newSchema(new URL(url));
        return schema.newValidator();
	}
    
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

}
