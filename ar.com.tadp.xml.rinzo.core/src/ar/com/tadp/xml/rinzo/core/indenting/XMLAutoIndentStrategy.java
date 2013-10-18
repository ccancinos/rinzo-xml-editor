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
package ar.com.tadp.xml.rinzo.core.indenting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

import ar.com.tadp.xml.rinzo.XMLEditorPlugin;
import ar.com.tadp.xml.rinzo.core.model.XMLNode;
import ar.com.tadp.xml.rinzo.core.utils.FileUtils;
import ar.com.tadp.xml.rinzo.core.utils.XMLTreeModelUtilities;

/**
 * Possible automatic indentation implementation
 * 
 * @author ccancinos
 */
public class XMLAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	public XMLAutoIndentStrategy() {
	}

	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		if (command.length == 0 && command.text != null && command.offset != -1 && document.getLength() != 0
				&& TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1) {
			try {
				this.smartIndentAfterNewLine(document, command);
			} catch (Exception e) {
				throw new RuntimeException("Error trying to autoindent line.\n" + "text: \"" + command.text + "\""
						+ "caret offset: \"" + command.caretOffset + "\"", e);
			}
		}
	}

	private void smartIndentAfterNewLine(IDocument document, DocumentCommand command) throws BadLocationException {
		StringBuffer buf = new StringBuffer(command.text);
		XMLNode previousNode = XMLTreeModelUtilities.getPreviousNode(document, command.offset);
		XMLNode previousPreviousNode = XMLTreeModelUtilities.getPreviousNode(document, previousNode.getOffset());
		if (previousNode != null) {
			String indentOfPreviousNode = getIndentOfLine(document, previousNode.getOffset());
			buf.append(indentOfPreviousNode);
			String indentationToken = this.getIndentationToken();
			command.shiftsCaret = false;
			command.caretOffset = command.offset + indentOfPreviousNode.length()
					+ FileUtils.LINE_SEPARATOR.length() + indentationToken.length();
			
			if ((previousNode.isEmpty() || previousNode.isTextTag()) && !previousPreviousNode.isEndTag()) {
				buf.append(indentationToken);
			}
			if(previousNode.isEndTag() || (previousPreviousNode != null && previousPreviousNode.isEndTag())) {
				command.caretOffset = command.offset + indentOfPreviousNode.length()
						+ FileUtils.LINE_SEPARATOR.length();
			}
			if (previousNode.isTag() ) {
				if (previousNode.getCorrespondingNode().getOffset() == command.offset) {
					buf.append(indentationToken);
					buf.append(FileUtils.LINE_SEPARATOR);
					buf.append(indentOfPreviousNode);
				}
				if (previousNode.getCorrespondingNode().getOffset() > command.offset) {
					buf.append(indentationToken);
				}
			}
		}
		command.text = buf.toString();
	}

	private String getIndentationToken() {
		return XMLEditorPlugin.getDefault().getIndentToken();
	}

	protected String getIndentOfLine(IDocument document, int offset) throws BadLocationException {
		int line = document.getLineOfOffset(offset);
		if (line <= -1) {
			return "";
		}
		int start = document.getLineOffset(line);
		int end = (start + document.getLineLength(line)) - 1;
		int whiteend = findEndOfWhiteSpace(document, start, end);
		return document.get(start, whiteend - start);
	}

}
