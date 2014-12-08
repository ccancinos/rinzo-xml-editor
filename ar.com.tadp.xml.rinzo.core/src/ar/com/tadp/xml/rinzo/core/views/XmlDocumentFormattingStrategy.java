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
package ar.com.tadp.xml.rinzo.core.views;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IFormattingStrategy;

/**
 * 
 * @author ccancinos
 */
public class XmlDocumentFormattingStrategy implements IFormattingStrategy {

	private IDocument document;

	public XmlDocumentFormattingStrategy(IDocument document) {
		this.document = document;
	}

	public String format(String content, boolean isLineStart, String indentation, int[] positions) {
		XmlDocumentFormatter formatter = new XmlDocumentFormatter();
		if (content == null) {
			content = "";
		}
		String string = formatter.format(content);
		return string;
	}

	public void formatterStarts(String initialIndentation) {
		// TODO Auto-generated method stub
	}

	public void formatterStops() {
		// TODO Auto-generated method stub
	}

}
