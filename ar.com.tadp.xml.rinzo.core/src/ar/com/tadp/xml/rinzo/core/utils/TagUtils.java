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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Point;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;

/**
 * 
 * @author ccancinos
 */
public class TagUtils {
    
    public static String getTagName(ITextViewer viewer, int offset) {
        try {
            IDocument idocument = viewer.getDocument();
            int posStartTag = idocument.search(offset, "<", false, false, false) + 1;
            StringBuffer str = new StringBuffer();
            while (idocument.getChar(posStartTag) != ' ' && idocument.getChar(posStartTag) != '>'
                && idocument.getChar(posStartTag) != '/'&& posStartTag != offset) {
                str.append(idocument.getChar(posStartTag));
                posStartTag++;
            }
            String tagName = str.toString();
            return (tagName.startsWith("/") || "".equals(tagName))?null:tagName;
        } catch (BadLocationException badlocationexception) {
        	XMLEditorPlugin.log(badlocationexception);
        }
        return null;
    }
    
    private static String getTagName(ITextViewer viewer) {
        Point point = viewer.getSelectedRange();
        return getTagName(viewer, point.x);
    }
    
    public static String getEndTagName(String tag) {
        return tag.substring(tag.indexOf("/")+1, tag.indexOf(">")).trim();
    }

    public static boolean isEndTag(ITextViewer viewer, int offset) {
        try {
            IDocument idocument = viewer.getDocument();
            int posStartTag = idocument.search(offset - 1, "<", false, false, false);
            String tag = idocument.get(posStartTag, offset - posStartTag);
            return tag.startsWith("</") || tag.endsWith("/>");
        } catch (BadLocationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
