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
package ar.com.tadp.xml.rinzo.core.utils;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TypedPosition;

import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.partitioner.IXMLPartitions;
import ar.com.tadp.xml.rinzo.core.partitioner.XMLDocumentPartitioner;

/**
 * 
 * @author ccancinos
 */
public class XMLTreeModelUtilities {

    public XMLTreeModelUtilities() {
    }

    public static XMLNode getActiveNode(IDocument document, int offset) {
        XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(document);
        if(xmlPartitioner != null) {
            TypedPosition position = xmlPartitioner.findClosestPosition(offset);
            if (position instanceof XMLNode) {
                XMLNode node = (XMLNode) position;
                return node;
            }        
        }
        return null;
    }

    public static XMLNode getActiveXMLNode(IDocument document, int offset) {
        XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(document);
        if(xmlPartitioner != null) {
            TypedPosition position = xmlPartitioner.findClosestPosition(offset++);
            while(position != null) {
	            if (position instanceof XMLNode && !position.getType().equals(IXMLPartitions.XML_TEXT)) {
	                XMLNode node = (XMLNode) position;
	                return node;
	            }
	            position = xmlPartitioner.findClosestPosition(offset++);
            }
        }
        return null;
    }

    public static XMLNode getParentNode(IDocument document, int offset) {
        XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(document);
        if(xmlPartitioner != null) {
            TypedPosition position = xmlPartitioner.findPreviousNonWhiteSpacePosition(offset);
            if(position instanceof XMLNode) {
                XMLNode node = (XMLNode)position;
                return node;
            }
        }
        return null;
    }

    public static XMLNode getPreviousNode(IDocument document, int offset) {
        XMLDocumentPartitioner xmlPartitioner = getDocumentPartitioner(document);
        if(xmlPartitioner != null) {
            TypedPosition position = xmlPartitioner.findPreviousPosition(offset);
            if(position instanceof XMLNode) {
                XMLNode node = (XMLNode)position;
                return node;
            }
        }
        return null;
    }
    
    private static XMLDocumentPartitioner getDocumentPartitioner(IDocument document) {
        IDocumentPartitioner partitioner = document.getDocumentPartitioner();
        if (partitioner instanceof XMLDocumentPartitioner) {
            return (XMLDocumentPartitioner) partitioner;
        }
        return null;
    }
}
